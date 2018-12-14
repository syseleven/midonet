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

package org.midonet.cluster.services.c3po.translators

import java.util.UUID

import org.junit.runner.RunWith
import org.midonet.cluster.C3POMinionTestBase
import org.midonet.cluster.data.neutron.NeutronResourceType.{HealthMonitorV2 => HealthMonitorV2Type}
import org.midonet.cluster.models.Topology._
import org.midonet.cluster.services.c3po.LbaasV2ITCommon
import org.midonet.cluster.services.rest_api.resources.ServiceContainerGroupResource
import org.midonet.cluster.util.UUIDUtil._
import org.midonet.util.concurrent.toFutureOps
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class HealthMonitorV2IT extends C3POMinionTestBase
                        with LbaasV2ITCommon
                        with LoadBalancerManager {

    private def verifyHealthMonitor(id: UUID,
                                    delay: Int,
                                    timeout: Int,
                                    maxRetries: Int,
                                    poolIds: Set[UUID],
                                    adminStateUp: Boolean): Unit = {
        val hm = storage.get(classOf[HealthMonitor], id).await()
        hm.getId shouldBe toProto(id)
        hm.getDelay shouldBe delay
        hm.getTimeout shouldBe timeout
        hm.getMaxRetries shouldBe maxRetries
        hm.getAdminStateUp shouldBe adminStateUp
        hm.getPoolIdsList should contain theSameElementsAs (poolIds map toProto)
    }

    "HealthMonitorV2Translator" should "add, update, and delete HM" in {
        val (vipPortId, _, vipSubnetId) = createVipV2PortAndNetwork(1)

        val lbId = createLbV2(10, vipPortId, vipSubnetId, "10.0.1.4")

        val pool1Id = createLbV2Pool(20, lbId)
        val pool2Id = createLbV2Pool(30, lbId)
        val pools = Set(pool1Id, pool2Id)

        val hmId = createHealthMonitorV2(40, poolIds = pools)

        verifyHealthMonitor(hmId, delay = 1, timeout = 2, maxRetries = 3,
                            poolIds = pools, adminStateUp = true)

        val pool1 = storage.get(classOf[Pool], pool1Id).await()
        pool1.getHealthMonitorId shouldBe toProto(hmId)

        val pool2 = storage.get(classOf[Pool], pool1Id).await()
        pool2.getHealthMonitorId shouldBe toProto(hmId)

        val lb = storage.get(classOf[LoadBalancer], lbId).await()
        val routerId = fromProto(lb.getRouterId)
        val lbSCId = lbServiceContainerId(routerId)
        val lbSCGId = lbServiceContainerGroupId(routerId)
        val lbSCPId = lbServiceContainerPortId(routerId)

        lb.getServiceContainerId shouldBe toProto(lbSCId)

        val sc = storage.get(classOf[ServiceContainer], lbSCId).await()
        sc.getPortId shouldBe toProto(lbSCPId)
        sc.getConfigurationId shouldBe toProto(lb.getId)
        sc.getServiceGroupId shouldBe toProto(lbSCGId)
        sc.getServiceType shouldBe "HAPROXY"

        val scg = storage.get(classOf[ServiceContainerGroup], lbSCGId).await()
        scg.getServiceContainerIdsList should contain only lbSCId

        val scp = storage.get(classOf[Port], lbSCPId).await()
        scp.getRouterId shouldBe toProto(routerId)
        scp.hasPortMac shouldBe true
        scp.hasPortAddress shouldBe true
        scp.getPortSubnetCount shouldBe 1
        scp.getAdminStateUp shouldBe true

        var router = storage.get(classOf[Router], routerId).await()
        router.getPortIdsCount shouldBe 2
        router.getPortIdsList should contain (toProto(lbSCPId))

        updateHealthMonitorV2(50, hmId, delay = 5, timeout = 6,
                              maxRetries = 7, adminStateUp = true)
        verifyHealthMonitor(hmId, delay = 5, timeout = 6, maxRetries = 7,
                            poolIds = pools, adminStateUp = true)

        insertDeleteTask(60, HealthMonitorV2Type, hmId)
        storage.exists(classOf[HealthMonitor], hmId).await() shouldBe false
        storage.exists(classOf[ServiceContainer], lbSCId).await() shouldBe false
        storage.exists(classOf[ServiceContainerGroup], lbSCGId).await() shouldBe false
        storage.exists(classOf[Port], lbSCPId).await() shouldBe false
        router = storage.get(classOf[Router], routerId).await()
        router.getPortIdsCount shouldBe 1
        router.getPortIdsList should not contain (toProto(lbSCPId))

    }
}
