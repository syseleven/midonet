/*
 * Copyright 2014 Midokura SARL
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

package org.midonet.midolman.state.l4lb;

import org.midonet.cluster.data.ZoomEnum;
import org.midonet.cluster.data.ZoomEnumValue;
import org.midonet.cluster.models.Topology;

@ZoomEnum(clazz = Topology.HealthMonitor.HealthMonitorType.class)
public enum HealthMonitorType {
    @ZoomEnumValue(value = "TCP") TCP,
    @ZoomEnumValue(value = "HTTP") HTTP,
    @ZoomEnumValue(value = "HTTPS") HTTPS;

    public static HealthMonitorType
    fromProto(Topology.HealthMonitor.HealthMonitorType proto) {
        return HealthMonitorType.valueOf(proto.toString());
    }
}
