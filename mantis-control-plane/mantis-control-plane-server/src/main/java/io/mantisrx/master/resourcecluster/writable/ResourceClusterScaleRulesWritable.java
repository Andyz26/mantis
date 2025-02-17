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

package io.mantisrx.master.resourcecluster.writable;

import io.mantisrx.master.resourcecluster.proto.ResourceClusterScaleSpec;
import io.mantisrx.shaded.com.fasterxml.jackson.annotation.JsonCreator;
import io.mantisrx.shaded.com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ResourceClusterScaleRulesWritable {
    String clusterId;
    String version;

    @Singular
    Map<String, ResourceClusterScaleSpec> scaleRules;

    @JsonCreator
    public ResourceClusterScaleRulesWritable(
        @JsonProperty("clusterId") final String clusterId,
        @JsonProperty("version") final String version,
        @JsonProperty("rules") final Map<String, ResourceClusterScaleSpec> scaleRules) {
        this.clusterId = clusterId;
        this.version = version;
        this.scaleRules = scaleRules;
    }
}
