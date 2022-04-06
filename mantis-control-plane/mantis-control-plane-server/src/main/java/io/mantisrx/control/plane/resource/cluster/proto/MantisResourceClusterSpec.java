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

import io.mantisrx.shaded.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

// @Jacksonized TODO fix Jacksonized
@Builder
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class MantisResourceClusterSpec {

    @NonNull
    String name;

    /**
     * ID fields maps to cluster name or spinnaker app name.
     */
    @NonNull
    String id;

    @NonNull
    String ownerName;

    @NonNull
    String ownerEmail;

    MantisResourceClusterEnvType envType;

    @Singular
    Set<SkuTypeSpec> skuSpecs;

    @Singular
    Map<String, String> clusterMetadataFields;

    // @Jacksonized TODO fix Jacksonized
    @Builder
    @Value
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class SkuTypeSpec {
        @NonNull
        @EqualsAndHashCode.Include
        String skuId;

        @NonNull
        SkuCapacity capacity;

        @NonNull
        String imageId;

        int cpuCoreCount;

        int memorySizeInBytes;

        int networkMbps;

        int diskSizeInBytes;

        @Singular
        Map<String, String> skuMetadataFields;
    }

    // @Jacksonized TODO fix Jacksonized
    @Builder
    @Value
    public static class SkuCapacity {
        @NonNull
        String skuId;

        int minSize;

        int maxSize;

        int desireSize;
    }
}
