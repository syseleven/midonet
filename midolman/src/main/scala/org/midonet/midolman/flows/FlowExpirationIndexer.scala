/*
 * Copyright 2015 Midokura SARL
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

package org.midonet.midolman.flows

import java.util.ArrayDeque

import scala.concurrent.duration._
import org.midonet.midolman.logging.MidolmanLogging
import org.midonet.packets.{FlowStateStore => FlowState}
import org.midonet.midolman.FlowTablePreallocation
import org.midonet.midolman.config.MidolmanConfig

object FlowExpirationIndexer {
    sealed abstract class Expiration {
        val typeId: Int
    }
    object ERROR_CONDITION_EXPIRATION extends Expiration {
        val typeId = 0
    }
    object FLOW_EXPIRATION extends Expiration {
        val typeId = 1
    }
    object STATEFUL_FLOW_EXPIRATION extends Expiration {
        val typeId = 2
    }
    object TUNNEL_FLOW_EXPIRATION extends Expiration {
        val typeId = 3
    }

    private final val maxType = 4

    class ExpirationQueue(size: Int) {
        private val expiries = new ArrayDeque[Long](size)
        private val ids = new ArrayDeque[Long](size)

        def size(): Int = expiries.size()
        def empty(): Boolean = expiries.size() == 0
        def peekExpiry(): Long = expiries.peek()

        def removeId(): ManagedFlow.FlowId = {
            expiries.removeFirst()
            ids.removeFirst()
        }
        def add(id: ManagedFlow.FlowId,
                expiry: Long): Unit = {
            expiries.add(expiry)
            ids.add(id)
        }
    }
}

/**
 * This trait deals with flow expiration. It registers all new flows and removes
 * them when the specified expiration time has elapsed. Note that a flow may
 * be removed from the kernel via another mechanism (such as flow invalidation),
 * but it is still kept in these data structures until it expires. This is to
 * avoid linear remove operations or smarter, more expensive data structures.
 */
class FlowExpirationIndexer(config: MidolmanConfig, preallocation: FlowTablePreallocation)
        extends MidolmanLogging {
    import FlowExpirationIndexer._

    val flowExpirationDuration = config.flowExpirationTime seconds

    def expirationInterval(expiration: Expiration) : Long = expiration match {
        case ERROR_CONDITION_EXPIRATION => (5 seconds).toNanos
        case FLOW_EXPIRATION => flowExpirationDuration.toNanos
        case STATEFUL_FLOW_EXPIRATION => flowExpirationDuration.toNanos / 2
        case TUNNEL_FLOW_EXPIRATION => flowExpirationDuration.toNanos * 5
    }

    private val expirationQueues = new Array[ExpirationQueue](maxType)

    {
        expirationQueues(ERROR_CONDITION_EXPIRATION.typeId) =
            preallocation.takeErrorExpirationQueue()
        expirationQueues(FLOW_EXPIRATION.typeId) =
            preallocation.takeFlowExpirationQueue()
        expirationQueues(STATEFUL_FLOW_EXPIRATION.typeId) =
            preallocation.takeStatefulFlowExpirationQueue()
        expirationQueues(TUNNEL_FLOW_EXPIRATION.typeId) =
            preallocation.takeTunnelFlowExpirationQueue()
    }

    def enqueueFlowExpiration(flowId: ManagedFlow.FlowId,
                              now: Long,
                              expiration: Expiration): Unit = {
        expirationQueues(expiration.typeId).add(flowId, now + expirationInterval(expiration))
    }

    def pollForExpired(now: Long): ManagedFlow.FlowId = {
        var i = 0
        while (i < maxType &&
                   (expirationQueues(i).empty() ||
                        now < expirationQueues(i).peekExpiry)) {
            i += 1
        }
        if (i < maxType) {
            val flow = expirationQueues(i).removeId()
            log.debug(s"Removing flow $flow for hard expiration")
            flow
        } else {
            maybeEvictExcessFlow()
        }
    }

    private def maybeEvictExcessFlow(): ManagedFlow.FlowId = {
        var excessFlows = 0
        var i = 0
        while (i < maxType) {
            excessFlows += expirationQueues(i).size()
            i += 1
        }
        excessFlows -= preallocation.maxFlows
        if (excessFlows > 0) {
            log.debug(s"$excessFlows excess flows, evicting one")
            removeOldestDpFlow()
        } else {
            ManagedFlow.NoFlow
        }
    }

    private def removeOldestDpFlow(): ManagedFlow.FlowId = {
        var i = 0
        while (i < maxType &&
                   expirationQueues(i).empty) {
            i += 1
        }
        if (i < maxType) {
            expirationQueues(i).removeId()
        } else {
            ManagedFlow.NoFlow
        }
    }
}
