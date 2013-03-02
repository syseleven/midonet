/*
 * Copyright 2012 Midokura Pte. Ltd.
 */

package org.midonet.midolman.topology.builders

import java.util.UUID
import org.midonet.cluster.client.{ArpCache, SourceNatResource, RouterBuilder}
import akka.actor.{ActorRef, ActorContext}
import org.midonet.midolman.layer3.{RoutingTable, Route}
import org.midonet.midolman.topology.RouterManager.{InvalidateFlows, TriggerUpdate}
import collection.immutable
import collection.mutable
import org.midonet.midolman.topology.{RoutingTableWrapper, RouterManager, RouterConfig}
import org.midonet.midolman.FlowController.InvalidateFlowsByTag

class RouterBuilderImpl(val id: UUID, val routerManager: ActorRef)
    extends RouterBuilder {

    private var cfg: RouterConfig = new RouterConfig
    private var arpCache: ArpCache = null
    private val routes = new scala.collection.mutable.HashSet[Route]()
    private val routesToAdd = new scala.collection.mutable.HashSet[Route]()
    private val routesToRemove = new scala.collection.mutable.HashSet[Route]()

    def setArpCache(table: ArpCache) {
        if (arpCache != null)
            throw new RuntimeException("RouterBuilder: attempt to re-set arp cache")
        if (table != null)
            arpCache = table
    }

    def addRoute(rt: Route) {
        routes.add(rt)
        routesToAdd.add(rt)
    }

    def removeRoute(rt: Route) {
        routes.remove(rt)
        routesToRemove.add(rt)
    }

    def setSourceNatResource(resource: SourceNatResource) {}

    def setID(id: UUID) = null

    def setInFilter(filterID: UUID) = {
        cfg = cfg.copy(inboundFilter = filterID)
        this
    }

    def setOutFilter(filterID: UUID) = {
        cfg = cfg.copy(outboundFilter = filterID)
        this
    }

    def build() {
        // we always pass a new copy of the RoutingTable since this is accessed
        // by the RCU Router
        val table = new RoutingTable()
        for (rt <- routes)
            table.addRoute(rt)
        if(routesToAdd.size > 0 || routesToRemove.size > 0){
            val added = routesToAdd.clone()
            val deleted = routesToRemove.clone()
            routerManager ! InvalidateFlows(added, deleted)
        }
        routesToAdd.clear()
        routesToRemove.clear()
        routerManager ! TriggerUpdate(cfg, arpCache, new RoutingTableWrapper(table))
    }

    def start() = null

}
