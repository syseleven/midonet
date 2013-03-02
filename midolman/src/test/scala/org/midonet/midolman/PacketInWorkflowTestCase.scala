/*
* Copyright 2012 Midokura Europe SARL
*/
package org.midonet.midolman

import org.apache.commons.configuration.HierarchicalConfiguration
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import org.midonet.midolman.DatapathController.PacketIn
import org.midonet.midolman.topology.LocalPortActive
import org.midonet.cluster.data.{Bridge => ClusterBridge, Ports}
import org.midonet.cluster.data.host.Host
import org.midonet.odp.{FlowMatch, Packet}
import org.midonet.odp.flows.FlowKeys
import org.midonet.packets.{IntIPv4, MAC, Packets}
import util.TestHelpers


@RunWith(classOf[JUnitRunner])
class PacketInWorkflowTestCase extends MidolmanTestCase {

    override protected def fillConfig(config: HierarchicalConfiguration) = {
        config.setProperty("datapath.max_flow_count", "10")
        super.fillConfig(config)
    }

    def testDatapathPacketIn() {

        val host = new Host(hostId()).setName("myself")
        clusterDataClient().hostsCreate(hostId(), host)

        val bridge = new ClusterBridge().setName("test")
        bridge.setId(clusterDataClient().bridgesCreate(bridge))

        val vifPort = Ports.materializedBridgePort(bridge)
        vifPort.setId(clusterDataClient().portsCreate(vifPort))

        materializePort(vifPort, host, "port")

        val portEventsProbe = newProbe()
        actors().eventStream.subscribe(portEventsProbe.ref, classOf[LocalPortActive])

        initializeDatapath() should not be (null)

        requestOfType[DatapathController.DatapathReady](flowProbe()).datapath should not be (null)
        portEventsProbe.expectMsgClass(classOf[LocalPortActive])

        val portNo = dpController().underlyingActor.ifaceNameToDpPort("port").getPortNo
        triggerPacketIn("port", TestHelpers.createUdpPacket(
                "10:10:10:10:10:10", "192.168.100.1",
                "10:10:10:10:10:11", "192.168.200.1"))

        val packetIn = fishForRequestOfType[PacketIn](dpProbe())

        packetIn should not be null
        packetIn.cookie should not be None
        packetIn.wMatch should not be null

        val packetInMsg = requestOfType[PacketIn](simProbe())

        packetInMsg.wMatch should not be null
        // We cannot check that the input port ID has been set correctly
        // because of a race condition: when the simulation finishes, the
        // DatapathController sets the input port ID back to null before
        // passing the wMatch to the FlowController in a InstallWildcardFlow
        // message.
        packetInMsg.wMatch.getInputPortNumber should be (getPortNumber("port"))
    }
}
