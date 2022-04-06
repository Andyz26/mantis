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

package io.mantisrx.control.plane.resource.cluster.proto;

import io.mantisrx.shaded.com.google.common.base.Joiner;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

// @Jacksonized TODO fix Jacksonized
@Builder
@Value
public class ScaleResourceRequest {
    @NonNull
    String clusterId;

    @NonNull
    String skuId;

    @NonNull
    String region;

    @NonNull
    MantisResourceClusterEnvType envType;

    int desireSize;

    public String getScaleRequestId() {
        return Joiner.on('-').join(this.clusterId, this.region, this.envType.name(), this.skuId, this.desireSize);
    }

}
