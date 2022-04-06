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

package io.mantisrx.master.api.akka.route.handlers;

import io.mantisrx.control.plane.resource.cluster.proto.GetResourceClusterSpecRequest;
import io.mantisrx.control.plane.resource.cluster.proto.ListResourceClusterRequest;
import io.mantisrx.control.plane.resource.cluster.proto.ProvisionResourceClusterRequest;
import io.mantisrx.master.jobcluster.proto.JobClusterManagerProto;
import io.mantisrx.control.plane.resource.cluster.proto.ResourceClusterAPIProto.GetResourceClusterResponse;
import io.mantisrx.control.plane.resource.cluster.proto.ResourceClusterAPIProto.ListResourceClustersResponse;
import java.util.concurrent.CompletionStage;

public interface ResourceClusterRouteHandler {
    CompletionStage<ListResourceClustersResponse> get(final ListResourceClusterRequest request);

    CompletionStage<GetResourceClusterResponse> create(final ProvisionResourceClusterRequest request);

    CompletionStage<GetResourceClusterResponse> get(final GetResourceClusterSpecRequest request);

    //CompletionStage<>

    // CompletionStage<DeleteJobClusterResponse> delete(final JobClusterManagerProto.DeleteJobClusterRequest request);
    //
    CompletionStage<JobClusterManagerProto.DisableJobClusterResponse> disable(final JobClusterManagerProto.DisableJobClusterRequest request);
    //
    // CompletionStage<JobClusterManagerProto.EnableJobClusterResponse> enable(final JobClusterManagerProto.EnableJobClusterRequest request);
    //
    // CompletionStage<JobClusterManagerProto.UpdateJobClusterArtifactResponse> updateArtifact(final JobClusterManagerProto.UpdateJobClusterArtifactRequest request);
    //
    // CompletionStage<JobClusterManagerProto.UpdateJobClusterSLAResponse> updateSLA(final JobClusterManagerProto.UpdateJobClusterSLARequest request);
    //
    // CompletionStage<JobClusterManagerProto.UpdateJobClusterWorkerMigrationStrategyResponse> updateWorkerMigrateStrategy(final JobClusterManagerProto.UpdateJobClusterWorkerMigrationStrategyRequest request);

}
