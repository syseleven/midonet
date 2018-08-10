/*
 * Copyright 2016 Midokura SARL
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
package org.midonet.midolman.l4lb

import java.util.UUID

import scala.collection.immutable.Set
import org.midonet.midolman.state.l4lb.HealthMonitorType

case class ListenerV2Config(id: UUID, adminStateUp: Boolean, port: Int,
                            poolId: UUID)

case class MemberV2Config(id: UUID, adminStateUp: Boolean, address: String,
                          port: Int)

case class HealthMonitorV2Config(id: UUID,
                                 healthMonitorType: HealthMonitorType,
                                 adminStateUp: Boolean, delay: Int,
                                 timeout: Int, maxRetries: Int,
                                 expectedCodes: String, httpMethod: String,
                                 urlPath: String)

case class PoolV2Config(id: UUID, members: Set[MemberV2Config],
                        healthMonitor: HealthMonitorV2Config)

case class LoadBalancerV2Config(id: UUID,
                                vips: Set[ListenerV2Config],
                                pools: Set[PoolV2Config],
                                adminStateUp: Boolean) {

    def generateConfigFile(sockFile: String): String = {
        val conf = new StringBuilder()
        conf append
            s"""
                |global
                |    daemon
                |    user nobody
                |    group daemon
                |    log /dev/log local0
                |    log /dev/log local1 notice
                |    stats socket $sockFile mode 0666 level user
                |defaults
                |    log global
                |    retries 3
                |    timeout connect 5000
                |    timeout client 5000
                |    timeout server 5000
                |""".stripMargin

        vips filter (_.adminStateUp) foreach { v =>
            conf append
                s"""
                    |frontend ${v.id}
                    |    option tcplog
                    |    bind *:${v.port}
                    |    mode tcp
                    |    default_backend ${v.poolId}
                    |""".stripMargin
        }
        pools foreach { p =>
            conf append
                s"""
                    |backend ${p.id}
                    |    timeout check ${p.healthMonitor.timeout}s
                    |""".stripMargin

            val hm = p.healthMonitor

            if (hm.healthMonitorType == HealthMonitorType.HTTP) {
                conf append s" option httpchk ${hm.httpMethod} ${hm.urlPath}\n"

                if (hm.expectedCodes.nonEmpty) {
                    conf append
                      s"    http-check expect rstatus ${hm.expectedCodes.replace(',', '|')}\n"
                }
            }
            p.members filter (_.adminStateUp) foreach { m =>
                conf append
                    s"""
                       |    server ${m.id} ${m.address}:${m.port} check inter ${hm.delay}s fall ${hm.maxRetries}
                       |""".stripMargin
            }
        }
        conf.toString()
    }
}
