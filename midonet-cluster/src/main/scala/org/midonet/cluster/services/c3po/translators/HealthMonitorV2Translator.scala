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

import scala.collection.JavaConverters._
import org.midonet.cluster.data.storage.Transaction
import org.midonet.cluster.models.Commons.UUID
import org.midonet.cluster.models.Neutron.NeutronHealthMonitorV2
import org.midonet.cluster.models.Topology._
import org.midonet.cluster.services.c3po.NeutronTranslatorManager.Operation
import org.midonet.cluster.util.IPAddressUtil._
import org.midonet.cluster.util.IPSubnetUtil._
import org.midonet.cluster.util.{IPAddressUtil, IPSubnetUtil}
import org.midonet.containers
import org.midonet.packets.{IPv4Subnet, MAC}

/** Provides a Neutron model translator for NeutronHealthMonitorV2. */
class HealthMonitorV2Translator
        extends Translator[NeutronHealthMonitorV2]
        with LoadBalancerManager {

    override protected def retainHighLevelModel(tx: Transaction,
                                                op: Operation[NeutronHealthMonitorV2])
    : List[Operation[NeutronHealthMonitorV2]] = List()

    override protected def translateCreate(tx: Transaction,
                                           nHm: NeutronHealthMonitorV2)
    : Unit = {
        val hm = convertHm(nHm)
        tx.create(hm)

        var lbs: Set[UUID] = Set()
        for (poolId <- nHm.getPoolsList.asScala.map(_.getId) if tx.exists(classOf[Pool], poolId)) {
            val pool = tx.get(classOf[Pool], poolId)
            lbs += pool.getLoadBalancerId
        }
        for (loadBalancerId <- lbs if tx.exists(classOf[LoadBalancer], loadBalancerId)) {
            val routerId = lbV2RouterId(loadBalancerId)
            val serviceContainerId = lbServiceContainerId(routerId)

            if (!tx.exists(classOf[ServiceContainer], serviceContainerId)) {
                val subnet = containers.findLocalSubnet()
                val routerAddress = containers.routerPortAddress(subnet)
                val containerAddress = containers.containerPortAddress(subnet)
                val routerSubnet = new IPv4Subnet(routerAddress, subnet.getPrefixLen)

                val lb = tx.get(classOf[LoadBalancer], loadBalancerId).toBuilder
                  .setServiceContainerId(serviceContainerId)
                  .build()

                val serviceContainerPort = Port.newBuilder
                  .setId(lbServiceContainerPortId(routerId))
                  .setRouterId(routerId)
                  .addPortSubnet(routerSubnet.asProto)
                  .setPortAddress(routerAddress.asProto)
                  .setPortMac(MAC.random().toString)
                  .build()

                val serviceContainerGroup = ServiceContainerGroup.newBuilder
                  .setId(lbServiceContainerGroupId(routerId))
                  .build()

                val serviceContainer = ServiceContainer.newBuilder
                  .setId(lbServiceContainerId(routerId))
                  .setServiceGroupId(serviceContainerGroup.getId)
                  .setPortId(serviceContainerPort.getId)
                  .setServiceType("HAPROXY")
                  .setConfigurationId(loadBalancerId)
                  .build()

                val route = newNextHopPortRoute(
                    serviceContainerPort.getId,
                    dstSubnet = serviceContainerPort.getPortSubnet(0))

                tx.create(serviceContainerPort)
                tx.create(serviceContainerGroup)
                tx.create(serviceContainer)
                tx.create(route)
                tx.update(lb)
            }
        }
    }

    private def lbHasOtherHealthMonitor(tx: Transaction, lb: LoadBalancer, healthMonitorId: UUID): Boolean = {
        for (poolId <- lb.getPoolIdsList.asScala if tx.exists(classOf[Pool], poolId)) {
            val pool = tx.get(classOf[Pool], poolId)
            if (pool.hasHealthMonitorId && pool.getHealthMonitorId != healthMonitorId) {
                return true
            }
        }
        false
    }

    override protected def translateDelete(tx: Transaction, id: UUID): Unit = {
        if (tx.exists(classOf[HealthMonitor], id)) {
            val hm = tx.get(classOf[HealthMonitor], id)
            for (poolId <- hm.getPoolIdsList.asScala if tx.exists(classOf[Pool], poolId)) {
                val pool = tx.get(classOf[Pool], poolId)
                val loadBalancerId = pool.getLoadBalancerId

                if (tx.exists(classOf[LoadBalancer], loadBalancerId)) {
                    val lb = tx.get(classOf[LoadBalancer], loadBalancerId)

                    if (!lbHasOtherHealthMonitor(tx, lb, id)) {
                        val routerId = lbV2RouterId(loadBalancerId)

                        tx.delete(classOf[ServiceContainerGroup], lbServiceContainerGroupId(routerId), ignoresNeo = true)
                        tx.delete(classOf[Port], lbServiceContainerPortId(routerId), ignoresNeo = true)
                        val lbUpdated = lb.toBuilder.clearServiceContainerId().build()
                        tx.update(lbUpdated)
                    }
                }
            }
        }
        tx.delete(classOf[HealthMonitor], id, ignoresNeo = true)
    }

    override protected def translateUpdate(tx: Transaction,
                                           nHm: NeutronHealthMonitorV2)
    : Unit = {
        val hm = tx.get(classOf[HealthMonitor], nHm.getId)
        val hmUpdated = hm.toBuilder.setAdminStateUp(nHm.getAdminStateUp)
                                    .setDelay(nHm.getDelay)
                                    .setTimeout(nHm.getTimeout)
                                    .setMaxRetries(nHm.getMaxRetries)
                                    .build()
        tx.update(hmUpdated)
    }

    private def convertHm(nHm: NeutronHealthMonitorV2): HealthMonitor = {
        val hm = HealthMonitor.newBuilder
            .setAdminStateUp(nHm.getAdminStateUp)
            .setDelay(nHm.getDelay)
            .setMaxRetries(nHm.getMaxRetries)
            .setTimeout(nHm.getTimeout)
            .addAllPoolIds(nHm.getPoolsList.asScala.map(_.getId).asJava)
            .setId(nHm.getId)

        hm.build
    }
}
