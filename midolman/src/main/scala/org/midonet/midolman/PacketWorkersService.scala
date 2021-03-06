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

package org.midonet.midolman

import java.util.concurrent.CountDownLatch

import scala.collection.IndexedSeq

import akka.actor.ActorSystem

import com.google.common.util.concurrent.AbstractService
import com.codahale.metrics.MetricRegistry

import org.slf4j.LoggerFactory

import org.midonet.cluster.services.MidonetBackend
import org.midonet.insights.Insights
import org.midonet.midolman.config.MidolmanConfig
import org.midonet.midolman.datapath.{DatapathChannel, FlowProcessor}
import org.midonet.midolman.logging.MidolmanLogging
import org.midonet.midolman.monitoring.{FlowRecorder, FlowSenderWorker}
import org.midonet.midolman.monitoring.metrics.PacketPipelineMetrics
import org.midonet.midolman.services.HostIdProvider
import org.midonet.midolman.state.ConnTrackState.{ConnTrackKey, ConnTrackValue}
import org.midonet.midolman.state.NatState.NatKey
import org.midonet.midolman.state.TraceState.{TraceContext, TraceKey}
import org.midonet.midolman.state.{NatBlockAllocator, NatLeaser, PeerResolver}
import org.midonet.midolman.topology.VirtualTopology
import org.midonet.packets.NatState.NatBinding
import org.midonet.sdn.state.OnHeapShardedFlowStateTable
import org.midonet.util.StatisticalCounter
import org.midonet.util.concurrent.NanoClock
import org.midonet.util.logging.Logger

object PacketWorkersService {
    def numWorkers(config: MidolmanConfig) = {
        val n = config.simulationThreads
        if (n <= 0)
            1
        else if (n > 16)
            16
        else
            n
    }
}

abstract class PacketWorkersService extends AbstractService {
    def workers: IndexedSeq[PacketWorker]
}

class PacketWorkersServiceImpl(config: MidolmanConfig,
                               hostIdProvider: HostIdProvider,
                               dpChannel: DatapathChannel,
                               dpState: DatapathState,
                               flowProcessor: FlowProcessor,
                               natBlockAllocator: NatBlockAllocator,
                               peerResolver: PeerResolver,
                               backChannel: ShardedSimulationBackChannel,
                               vt: VirtualTopology,
                               clock: NanoClock,
                               backend: MidonetBackend,
                               metricsRegistry: MetricRegistry,
                               insights: Insights,
                               counter: StatisticalCounter,
                               actorSystem: ActorSystem,
                               flowTablePreallocation: FlowTablePreallocation)
        extends PacketWorkersService with Runnable with MidolmanLogging {

    override def logSource = "org.midonet.packet-worker.packet-worker-supervisor"

    val numWorkers = PacketWorkersService.numWorkers(config)

    val connTrackStateTable = new OnHeapShardedFlowStateTable[ConnTrackKey, ConnTrackValue](clock)
    val natStateTable = new OnHeapShardedFlowStateTable[NatKey, NatBinding](clock)
    val natLeaser: NatLeaser = new NatLeaser {
        val log: Logger = Logger(LoggerFactory.getLogger(classOf[NatLeaser]))
        val allocator = natBlockAllocator
        val clock = PacketWorkersServiceImpl.this.clock
    }
    val traceStateTable = new OnHeapShardedFlowStateTable[TraceKey, TraceContext](clock)

    val supervisorThread = new Thread(this, "packet-worker-supervisor")
    supervisorThread.setDaemon(true)
    val shutdownLatch = new CountDownLatch(1)

    private val flowSenderWorker = FlowSenderWorker(config, backend)

    val workers: IndexedSeq[DisruptorPacketWorker] =
        0 until numWorkers map createWorker

    override def doStart(): Unit = {
        flowSenderWorker.startAsync().awaitRunning()
        supervisorThread.start()
    }

    override def doStop(): Unit = {
        shutdownLatch.countDown()
    }

    override def run(): Unit = {
        workers foreach { w => w.start() }

        notifyStarted()

        shutdownLatch.await()

        workers foreach { w => w.shutdown() }

        var shutdownGracePeriod = 5000
        while (workers.exists({ w => w.isRunning }) && shutdownGracePeriod >= 0) {
            Thread.sleep(100)
            shutdownGracePeriod -= 100
        }
        workers filter { w => w.isRunning } foreach {
            w => {
                log.error(s"Worker $w didn't shutdown gracefully, killing")
                w.shutdownNow()
            }
        }

        flowSenderWorker.stopAsync().awaitTerminated()

        notifyStopped()
    }

    private def shardLogger(t: AnyRef) =
        Logger(LoggerFactory.getLogger("org.midonet.state.table"))

    protected def createWorker(index: Int): DisruptorPacketWorker = {
        val cookieGen = new CookieGenerator(index, numWorkers)
        val connTrackShard = connTrackStateTable.addShard(
            log = shardLogger(connTrackStateTable))
        val natShard = natStateTable.addShard(
            log = shardLogger(natStateTable))
        val traceShard = traceStateTable.addShard(
            log = shardLogger(traceStateTable))


        val backChannelProcessor = backChannel.registerProcessor()

        val metrics = new PacketPipelineMetrics(metricsRegistry, index)
        val flowRecorder = FlowRecorder(config, hostIdProvider.hostId,
                                        flowSenderWorker)
        val workflow = new PacketWorkflow(
            numWorkers, index,
            config, hostIdProvider.hostId, dpState,
            cookieGen, clock, dpChannel,
            backChannelProcessor, flowProcessor,
            connTrackShard, natShard, traceShard,
            peerResolver, natLeaser,
            metrics, flowRecorder,
            vt, counter.addAndGet(index, _: Int),
            flowTablePreallocation, insights)

        new DisruptorPacketWorker(workflow, metrics, index)
    }
}
