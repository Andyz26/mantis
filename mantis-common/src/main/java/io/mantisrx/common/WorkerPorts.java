/*
 * Copyright 2019 Netflix, Inc.
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

package io.mantisrx.common;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


public class WorkerPorts {

    private final int metricsPort;
    private final int debugPort;
    private final int consolePort;
    private final int customPort;
    private int sinkPort;
    private List<Integer> ports;

    public WorkerPorts(final List<Integer> assignedPorts) {
        if (!(assignedPorts.size() >= 4)) {
            throw new IllegalArgumentException("assignedPorts should be >= 4");
        }
        this.metricsPort = assignedPorts.get(0);
        this.debugPort = assignedPorts.get(1);
        this.consolePort = assignedPorts.get(2);
        this.customPort = assignedPorts.get(3);
        this.ports = new ArrayList<>(3);
        for (int idx = 4; idx < assignedPorts.size(); idx++) {
            ports.add(assignedPorts.get(idx));

            sinkPort = assignedPorts.get(idx);
            break;
        }
    }

    public WorkerPorts(int metricsPort, int debugPort, int consolePort, int customPort, int sinkPort) {
        this.ports = new ArrayList<>(5);


        this.sinkPort = sinkPort;
        ports.add(sinkPort);

        this.metricsPort = metricsPort;
        ports.add(metricsPort);

        this.debugPort = debugPort;
        ports.add(debugPort);

        this.consolePort = consolePort;
        ports.add(consolePort);

        this.customPort = customPort;
        ports.add(customPort);


    }

    @JsonCreator
    @JsonIgnoreProperties(ignoreUnknown = true)
    public WorkerPorts(@JsonProperty("metricsPort") int metricsPort,
                       @JsonProperty("debugPort") int debugPort,
                       @JsonProperty("consolePort") int consolePort,
                       @JsonProperty("customPort") int customPort,
                       @JsonProperty("ports") List<Integer> ports) {
        this.metricsPort = metricsPort;
        this.debugPort = debugPort;
        this.consolePort = consolePort;
        this.customPort = customPort;
        this.ports = ports;
    }

    public int getMetricsPort() {
        return metricsPort;
    }

    public int getDebugPort() {
        return debugPort;
    }

    public int getConsolePort() {
        return consolePort;
    }

    public int getCustomPort() {
        return customPort;
    }

    public int getSinkPort() { return sinkPort; }

    @JsonIgnore
    public List<Integer> getAllPorts() {
        final List<Integer> allPorts = new ArrayList<>(ports);
        allPorts.add(metricsPort);
        allPorts.add(debugPort);
        allPorts.add(consolePort);
        allPorts.add(customPort);
        return allPorts;
    }

    public List<Integer> getPorts() {
        return ports;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WorkerPorts that = (WorkerPorts) o;

        if (metricsPort != that.metricsPort) return false;
        if (debugPort != that.debugPort) return false;
        if (consolePort != that.consolePort) return false;
        return ports != null ? ports.equals(that.ports) : that.ports == null;
    }

    @Override
    public int hashCode() {
        int result = metricsPort;
        result = 31 * result + debugPort;
        result = 31 * result + consolePort;
        result = 31 * result + (ports != null ? ports.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "WorkerPorts{" +
                "metricsPort=" + metricsPort +
                ", debugPort=" + debugPort +
                ", consolePort=" + consolePort +
                ", customPort=" + customPort +
                ", ports=" + ports +
                '}';
    }
}


