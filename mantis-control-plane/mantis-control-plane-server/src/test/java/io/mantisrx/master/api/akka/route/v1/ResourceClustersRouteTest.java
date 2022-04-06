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

package io.mantisrx.master.api.akka.route.v1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import com.netflix.mantis.master.scheduler.TestHelpers;
import io.mantisrx.control.plane.resource.cluster.ResourceClustersManagerActor;
import io.mantisrx.control.plane.resource.cluster.proto.ProvisionResourceClusterRequest;
import io.mantisrx.control.plane.resource.cluster.proto.ResourceClusterProvisionSubmissiomResponse;
import io.mantisrx.control.plane.resource.cluster.proto.ScaleResourceRequest;
import io.mantisrx.control.plane.resource.cluster.proto.ScaleResourceResponse;
import io.mantisrx.control.plane.resource.cluster.resourceprovider.IResourceClusterProvider;
import io.mantisrx.control.plane.resource.cluster.resourceprovider.IResourceClusterResponseHandler;
import io.mantisrx.master.api.akka.payloads.ResourceClustersPayloads;
import io.mantisrx.master.api.akka.route.handlers.ResourceClusterRouteHandler;
import io.mantisrx.master.api.akka.route.handlers.ResourceClusterRouteHandlerAkkaImpl;
import io.mantisrx.shaded.com.fasterxml.jackson.databind.JsonNode;
import io.mantisrx.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Slf4j
public class ResourceClustersRouteTest extends RouteTestBase {
    private final static String TEST_CLUSTER_NAME = "RouteTestCluster1";

    private static Thread t;
    private static final int SERVER_PORT = 8200;
    private static CompletionStage<ServerBinding> binding;

    private static final UnitTestResourceProviderAdapter resourceProviderAdapter =
            new UnitTestResourceProviderAdapter();

    ResourceClustersRouteTest() {
        super("ResourceClustersRouteTest", SERVER_PORT);
    }

    @BeforeClass
    public void setup() throws Exception {
        TestHelpers.setupMasterConfig();
        final CountDownLatch latch = new CountDownLatch(1);

        t = new Thread(() -> {
            try {
                // boot up server using the route as defined below
                final Http http = Http.get(system);
                final Materializer materializer = Materializer.createMaterializer(system);

                ActorRef resourceClustersManagerActor = system.actorOf(
                        ResourceClustersManagerActor.props(resourceProviderAdapter),
                        "jobClustersManager");

                final ResourceClusterRouteHandler resourceClusterRouteHandler = new ResourceClusterRouteHandlerAkkaImpl(
                        resourceClustersManagerActor);

                final ResourceClustersRoute app = new ResourceClustersRoute(resourceClusterRouteHandler, system);
                final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow =
                        app.createRoute(Function.identity())
                                .flow(system, materializer);
                log.info("starting test server on port {}", SERVER_PORT);
                latch.countDown();
                binding = http
                        .newServerAt("localhost", SERVER_PORT)
                        .bind(app.createRoute(Function.identity()));
            } catch (Exception e) {
                log.info("caught exception", e);
                latch.countDown();
                e.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();
        latch.await();
    }

    @AfterClass
    public void teardown() {
        log.info("ResourceClusterRouteTest teardown");
        binding.thenCompose(ServerBinding::unbind) // trigger unbinding from the port
                .thenAccept(unbound -> system.terminate()); // and shutdown when done
        t.interrupt();
    }

    @Test
    public void getClustersList() throws InterruptedException {
        testGet(
                getResourceClusterEndpoint(),
                StatusCodes.OK,
                resp -> compareClustersPayload(resp, node -> assertEquals(0, node.size())));
    }

    @Test(dependsOnMethods = {"getClustersList"})
    public void testClusterCreate() throws InterruptedException {
        testPost(
                getResourceClusterEndpoint(),
                HttpEntities.create(
                        ContentTypes.APPLICATION_JSON,
                        ResourceClustersPayloads.RESOURCE_CLUSTER_CREATE),
                StatusCodes.ACCEPTED,
                resp -> System.out.println(resp));

        // assert this.isClusterExist(TEST_CLUSTER_NAME);
    }

    final String getResourceClusterEndpoint() {
        return String.format(
                "http://127.0.0.1:%d/api/v1/resourceClusters",
                SERVER_PORT);
    }

    private void compareClustersPayload(String clusterListResponse, Consumer<JsonNode> valFunc) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode responseObj = mapper.readTree(clusterListResponse);
            final String clusterListKey = "registeredResourceClusters";

            assertNotNull(responseObj.get(clusterListKey));
            valFunc.accept(responseObj.get(clusterListKey));

        } catch (IOException ex) {
            fail(ex.getMessage());
        }
    }

    private static class UnitTestResourceProviderAdapter implements IResourceClusterProvider {

        private IResourceClusterProvider injectedProvider;

        public void setInjectedProvider(IResourceClusterProvider injectedProvider) {
            this.injectedProvider = injectedProvider;
        }

        public void resetInjectedProvider() {
            this.injectedProvider = null;
        }

        @Override
        public CompletionStage<ResourceClusterProvisionSubmissiomResponse> provisionClusterIfNotPresent(
                ProvisionResourceClusterRequest clusterSpec) {
            if (this.injectedProvider != null) return this.injectedProvider.provisionClusterIfNotPresent(clusterSpec);
            return null;
        }

        @Override
        public CompletionStage<ScaleResourceResponse> scaleResource(ScaleResourceRequest scaleRequest) {
            if (this.injectedProvider != null) return this.injectedProvider.scaleResource(scaleRequest);
            return null;
        }

        @Override
        public IResourceClusterResponseHandler getResponseHandler() {
            if (this.injectedProvider != null) return this.injectedProvider.getResponseHandler();
            return null;
        }
    }
}
