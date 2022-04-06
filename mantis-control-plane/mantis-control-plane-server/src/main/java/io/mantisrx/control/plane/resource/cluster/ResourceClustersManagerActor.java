/*
 * Copyright 2022 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mantisrx.control.plane.resource.cluster;

import static akka.pattern.Patterns.pipe;

import akka.actor.AbstractActorWithTimers;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import io.mantisrx.control.plane.resource.cluster.proto.GetResourceClusterSpecRequest;
import io.mantisrx.control.plane.resource.cluster.proto.ListResourceClusterRequest;
import io.mantisrx.control.plane.resource.cluster.proto.ProvisionResourceClusterRequest;
import io.mantisrx.control.plane.resource.cluster.proto.ResourceClusterProvisionSubmissiomResponse;
import io.mantisrx.control.plane.resource.cluster.proto.ScaleResourceRequest;
import io.mantisrx.control.plane.resource.cluster.resourceprovider.IResourceClusterProvider;
import io.mantisrx.control.plane.resource.cluster.resourceprovider.IResourceStorageProvider;
import io.mantisrx.control.plane.resource.cluster.resourceprovider.InMemoryOnlyResourceStorageProvider;
import io.mantisrx.control.plane.resource.cluster.writable.ResourceClusterSpecWritable;
import io.mantisrx.master.jobcluster.proto.BaseResponse.ResponseCode;
import io.mantisrx.control.plane.resource.cluster.proto.ResourceClusterAPIProto.GetResourceClusterResponse;
import io.mantisrx.control.plane.resource.cluster.proto.ResourceClusterAPIProto.ListResourceClustersResponse;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResourceClustersManagerActor extends AbstractActorWithTimers {

    public static Props props(
            final IResourceClusterProvider resourceClusterProvider) {
        return Props.create(
                ResourceClustersManagerActor.class,
                resourceClusterProvider,
                new InMemoryOnlyResourceStorageProvider());
    }

    public static Props props(
            final IResourceClusterProvider resourceClusterProvider,
            final IResourceStorageProvider resourceStorageProvider) {
        // TODO(andyz): investigate atlas metered-mailbox.
        return Props.create(ResourceClustersManagerActor.class, resourceClusterProvider, resourceStorageProvider);
    }

    private final IResourceClusterProvider resourceClusterProvider;
    private final IResourceStorageProvider resourceStorageProvider;

    public ResourceClustersManagerActor(
            final IResourceClusterProvider resourceClusterProvider,
            final IResourceStorageProvider resourceStorageProvider) {
        this.resourceClusterProvider = resourceClusterProvider;
        this.resourceStorageProvider = resourceStorageProvider;
    }

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(ProvisionResourceClusterRequest.class, this::onProvisionResourceClusterRequest)
                .match(ScaleResourceRequest.class, this::onScaleResourceClusterRequest)
                .match(ListResourceClusterRequest.class, this::onListResourceClusterRequest)
                .match(GetResourceClusterSpecRequest.class, this::onGetResourceClusterSpecRequest)
                .match(ResourceClusterProvisionSubmissiomResponse.class, this::onResourceClusterProvisionResponse)
                .match(String.class, this::onStringResponse)
                .build();
    }

    private void onStringResponse(String res) {
        log.info("ResourceCluster received: " + res);
    }

    private void onResourceClusterProvisionResponse(ResourceClusterProvisionSubmissiomResponse resp) {
        this.resourceClusterProvider.getResponseHandler().handleProvisionResponse(resp);
    }


    private void onListResourceClusterRequest(ListResourceClusterRequest req) {
        pipe(this.resourceStorageProvider.getRegisteredResourceClustersWritable()
                .thenApply(clustersW ->
                        ListResourceClustersResponse.builder()
                                .responseCode(ResponseCode.SUCCESS)
                                .registeredResourceClusters(clustersW.getClusters().entrySet().stream().map(
                                        kv -> ListResourceClustersResponse.RegisteredResourceCluster.builder()
                                                .id(kv.getValue().getClusterId())
                                                .version(kv.getValue().getVersion())
                                                .build())
                                        .collect(Collectors.toList()))
                                .build()
                ).exceptionally(err ->
                                ListResourceClustersResponse.builder()
                                        .message(err.getMessage())
                                        .responseCode(ResponseCode.SERVER_ERROR).build()),
                getContext().dispatcher())
                .to(getSender());
    }

    private void onGetResourceClusterSpecRequest(GetResourceClusterSpecRequest req) {
        pipe(this.resourceStorageProvider.getResourceClusterSpecWritable(req.getId())
                        .thenApply(specW -> {
                            if (specW == null) {
                                return GetResourceClusterResponse.builder()
                                        .responseCode(ResponseCode.CLIENT_ERROR_NOT_FOUND)
                                        .build();
                            }
                            return GetResourceClusterResponse.builder()
                                    .responseCode(ResponseCode.SUCCESS)
                                    .clusterSpec(specW.getClusterSpec())
                                    .build();
                        })
                        .exceptionally(err ->
                                GetResourceClusterResponse.builder()
                                        .responseCode(ResponseCode.SERVER_ERROR)
                                        .message(err.getMessage())
                                        .build()),
                getContext().dispatcher())
                .to(getSender());
    }

    private void onProvisionResourceClusterRequest(ProvisionResourceClusterRequest req) {
        /*
        For a provision request, the following steps will be taken:
        1. Persist the cluster request with spec to the resource storage provider.
        2. Once persisted, reply to sender (e.g. http server route) to confirm the accepted request.
        3. Queue the long-running provision task via resource cluster provider and register callback to self.
        4. Handle provision callback and error handling.
            (only logging for now as agent registration will happen directly inside agent).
         */
        log.trace("Entering onProvisionResourceClusterRequest: " + req);

        ResourceClusterSpecWritable specWritable = ResourceClusterSpecWritable.builder()
                .clusterSpec(req.getClusterSpec())
                .version("")
                .id(req.getClusterId())
                .build();

        // Cluster spec is returned for API request.
        CompletionStage<GetResourceClusterResponse> updateSpecToStoreFut =
                this.resourceStorageProvider.registerAndUpdateClusterSpec(specWritable)
                        .thenApply(specW -> GetResourceClusterResponse.builder()
                                        .responseCode(ResponseCode.SUCCESS)
                                        .clusterSpec(specW.getClusterSpec())
                                        .build())
                        .exceptionally(err ->
                                GetResourceClusterResponse.builder()
                                .responseCode(ResponseCode.SERVER_ERROR)
                                .message(err.getMessage())
                                .build());
        pipe(updateSpecToStoreFut, getContext().dispatcher())
                .to(getSender());

        log.trace("[Pipe finish] storing cluster spec.");

        // Provision response is directed back to this actor to handle its submission result.
        CompletionStage<ResourceClusterProvisionSubmissiomResponse> provisionFut =
                updateSpecToStoreFut
                        .thenCompose(spec -> this.resourceClusterProvider.provisionClusterIfNotPresent(req))
                        .exceptionally(err -> ResourceClusterProvisionSubmissiomResponse.builder().error(err).build());
        pipe(provisionFut, getContext().dispatcher()).to(getSelf());

        //        pipe(this.resourceClusterProvider.provisionClusterIfNotPresent(req)
        //                        .exceptionally(err -> ResourceClusterProvisionResponse.builder().response(err.toString()).build()),
        //                getContext().dispatcher())
        //                .to(getSelf());
        log.trace("[Pipe finish 2]: returned provision fut.");
    }

    private void onScaleResourceClusterRequest(ScaleResourceRequest req) {
        log.info("Entering onScaleResourceClusterRequest: " + req);
        pipe(this.resourceClusterProvider.scaleResource(req)
                        .thenApply(res ->
                        {
                            log.info("onScaleResourceClusterRequest res: " + res);
                            return res;
                        }),
                getContext().dispatcher())
                .to(getSender());
    }
}
