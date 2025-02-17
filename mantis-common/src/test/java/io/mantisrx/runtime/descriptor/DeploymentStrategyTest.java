/*
 * Copyright 2021 Netflix, Inc.
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

package io.mantisrx.runtime.descriptor;

import static org.junit.Assert.*;

import org.junit.Test;

public class DeploymentStrategyTest {
    @Test
    public void shouldRequireInheritInstanceCheck() {
        DeploymentStrategy res = DeploymentStrategy.builder()
            .stage(1, StageDeploymentStrategy.builder().inheritInstanceCount(true).build())
            .build();
        assertTrue(res.requireInheritInstanceCheck());
        assertTrue(res.requireInheritInstanceCheck(1));
        assertFalse(res.requireInheritInstanceCheck(2));

        res = DeploymentStrategy.builder()
                .stage(2, StageDeploymentStrategy.builder().inheritInstanceCount(true).build())
                .stage(3, StageDeploymentStrategy.builder().inheritInstanceCount(false).build())
                .build();
        assertTrue(res.requireInheritInstanceCheck());
        assertTrue(res.requireInheritInstanceCheck(2));
        assertFalse(res.requireInheritInstanceCheck(1));
        assertFalse(res.requireInheritInstanceCheck(3));
    }

    @Test
    public void shouldNotRequireInheritInstanceCheck() {
        DeploymentStrategy res = DeploymentStrategy.builder()
                .stage(1, StageDeploymentStrategy.builder().inheritInstanceCount(false).build())
                .build();
        assertFalse(res.requireInheritInstanceCheck(1));
        assertFalse(res.requireInheritInstanceCheck(2));
        assertFalse(res.requireInheritInstanceCheck());

        // test default setting
        res = DeploymentStrategy.builder().build();
        assertFalse(res.requireInheritInstanceCheck(1));
        assertFalse(res.requireInheritInstanceCheck(2));
        assertFalse(res.requireInheritInstanceCheck());

        // test multiple stages
        res = DeploymentStrategy.builder()
                .stage(1, StageDeploymentStrategy.builder().build())
                .stage(3, StageDeploymentStrategy.builder().inheritInstanceCount(false).build())
                .build();
        assertFalse(res.requireInheritInstanceCheck(1));
        assertFalse(res.requireInheritInstanceCheck(2));
        assertFalse(res.requireInheritInstanceCheck());
    }
}
