# Copyright 2014 Midokura SARL
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

from hamcrest import assert_that
from hamcrest import calling
from hamcrest import equal_to
from hamcrest import raises
import logging
from mdts.lib.binding_manager import BindingManager
from mdts.lib.failure.pkt_failure import PktFailure
from mdts.lib.physical_topology_manager import PhysicalTopologyManager
from mdts.lib.virtual_topology_manager import VirtualTopologyManager
from mdts.services import service
from mdts.tests.utils.utils import bindings
from mdts.tests.utils.utils import wait_on_futures
from nose.plugins.attrib import attr
from nose.tools import nottest
from nose.tools import with_setup
import time

LOG = logging.getLogger(__name__)

PTM = PhysicalTopologyManager('../topologies/mmm_physical_test_bgp.yaml')
VTM = VirtualTopologyManager('../topologies/mmm_virtual_test_bgp.yaml')
BM = BindingManager(PTM, VTM)

# routes BGP advertises:
route_direct = [{'nwPrefix': '172.16.0.0', 'prefixLength': 16}]
route_snat = [{'nwPrefix': '100.0.0.0', 'prefixLength': 16}]

# peers available:
uplink1_session1 = {'port': 2, 'peerAddr': '10.1.0.240', 'peerAs': 64511,
                    'peerHostname': 'quagga1', 'peerIface': 'bgp1'}
uplink1_session2 = {'port': 2, 'peerAddr': '10.1.0.241', 'peerAs': 64512,
                    'peerHostname': 'quagga2', 'peerIface': 'bgp2'}
uplink2_session1 = {'port': 3, 'peerAddr': '10.2.0.240', 'peerAs': 64512,
                    'peerHostname': 'quagga2', 'peerIface': 'bgp1'}
uplink2_session2 = {'port': 3, 'peerAddr': '10.2.0.241', 'peerAs': 64511,
                    'peerHostname': 'quagga1', 'peerIface': 'bgp2'}
uplink1_multisession = [uplink1_session1, uplink1_session2]
uplink2_multisession = [uplink2_session1, uplink2_session2]


# NOTE midolman 1 and midolman 2 may honor different versions of quagga
# protocol, so tests should be repeated for both. The setup uses the
# host 3 for non-uplink agent.

binding_unisession1 = {
    'description': 'vm not connected to uplink (mm1 as first link)',
    'bindings': [
        {'binding':
            {'device_name': 'bridge-000-001', 'port_id': 2,
             'host_id': 3, 'interface_id': 1}},
        {'binding':
            {'device_name': 'router-000-001', 'port_id': 2,
             'host_id': 1, 'interface_id': 2}},
        {'binding':
            {'device_name': 'router-000-001', 'port_id': 3,
             'host_id': 2, 'interface_id': 2}},
    ],
    'config': {
        'link1': uplink1_session1,
        'link2': uplink2_session1,
        'pre_filter': 'pre_filter_snat_ip_1'}
}

binding_unisession2 = {
    'description': 'vm not connected to uplink (mm2 as first link)',
    'bindings': [
        {'binding':
            {'device_name': 'bridge-000-001', 'port_id': 2,
             'host_id': 3, 'interface_id': 1}},
        {'binding':
            {'device_name': 'router-000-001', 'port_id': 2,
             'host_id': 1, 'interface_id': 2}},
        {'binding':
            {'device_name': 'router-000-001', 'port_id': 3,
             'host_id': 2, 'interface_id': 2}},
    ],
    'config': {
        'link1': uplink2_session1,
        'link2': uplink1_session1,
        'pre_filter': 'pre_filter_snat_ip_2'}
}

binding_snat1 = {
    'description': 'one connected to uplink #1 and another not connected',
    'bindings': [
        {'binding':
            {'device_name': 'bridge-000-001', 'port_id': 2,
             'host_id': 1, 'interface_id': 1}},
        {'binding':
            {'device_name': 'bridge-000-001', 'port_id': 3,
             'host_id': 2, 'interface_id': 1}},
        {'binding':
            {'device_name': 'router-000-001', 'port_id': 2,
             'host_id': 1, 'interface_id': 2}},
    ],
    'config': {
        'link': uplink1_session1,
        'pre_filter': 'pre_filter_snat_ip_1'}

}

binding_snat2 = {
    'description': 'one connected to uplink #2 and another not connected',
    'bindings': [
        {'binding':
            {'device_name': 'bridge-000-001', 'port_id': 2,
             'host_id': 1, 'interface_id': 1}},
        {'binding':
            {'device_name': 'bridge-000-001', 'port_id': 3,
             'host_id': 2, 'interface_id': 1}},
        {'binding':
            {'device_name': 'router-000-001', 'port_id': 3,
             'host_id': 2, 'interface_id': 2}},
    ],
    'config': {
        'link': uplink2_session1,
        'pre_filter': 'pre_filter_snat_ip_2'}
}

binding_multisession1 = {
    'description': 'two sessions in uplink #1 and one vm not in uplink',
    'bindings': [
        {'binding':
            {'device_name': 'bridge-000-001', 'port_id': 2,
             'host_id': 3, 'interface_id': 1}},
        {'binding':
            {'device_name': 'router-000-001', 'port_id': 2,
             'host_id': 1, 'interface_id': 2}}
    ],
    'config': {
        'session1': uplink1_session1,
        'session2': uplink1_session2,
        'multisession': uplink1_multisession}
}

binding_multisession2 = {
    'description': 'two sessions in uplink #2 and one vm not in uplink',
    'bindings': [
        {'binding':
            {'device_name': 'bridge-000-001', 'port_id': 2,
             'host_id': 3, 'interface_id': 1}},
        {'binding':
            {'device_name': 'router-000-001', 'port_id': 3,
             'host_id': 2, 'interface_id': 2}}
    ],
    'config': {
        'session1': uplink2_session1,
        'session2': uplink2_session2,
        'multisession': uplink2_multisession}
}

# Even though quickly copied from test_nat_router for now, utilities below
# should probably be moved to somewhere shared among tests
def set_filters(router_name, inbound_filter_name, outbound_filter_name):
    """Sets in-/out-bound filters to a router."""
    router = VTM.get_router(router_name)

    inbound_filter = None
    if inbound_filter_name:
        inbound_filter = VTM.get_chain(inbound_filter_name)
    outbound_filter = None
    if outbound_filter_name:
        outbound_filter = VTM.get_chain(outbound_filter_name)

    router.set_inbound_filter(inbound_filter)
    router.set_outbound_filter(outbound_filter)
    # Sleep here to make sure that the settings have been propagated.
    time.sleep(5)


def unset_filters(router_name):
    """Unsets in-/out-bound filters from a router."""
    set_filters(router_name, None, None)


def add_bgp(bgp_peers, networks):
    router = VTM.get_router('router-000-001')
    localAs = 64513

    router.set_asn(localAs)
    peers = []
    for bgp_peer in bgp_peers:
        peer = router.add_bgp_peer(bgp_peer['peerAs'], bgp_peer['peerAddr'])
        peers.append(peer)
    for network in networks:
        router.add_bgp_network(network['nwPrefix'], network['prefixLength'])

    for bgp_peer in bgp_peers:
        await_router_route(bgp_peer, '0.0.0.0', 0, exists=True)
        await_internal_route_exported(localAs, bgp_peer['peerAs'])

    return peers[0] if len(peers) == 1 else peers


def add_bgp_peer(peer):
    router = VTM.get_router('router-000-001')
    return router.add_bgp_peer(peer['peerAs'], peer['peerAddr'])


def clear_bgp():
    router = VTM.get_router('router-000-001')
    router.clear_bgp_networks()
    router.clear_bgp_peers()
    router.clear_asn()


def clear_bgp_peer(peer, wait=0):
    peer.delete()
    if wait > 0:
        time.sleep(wait)


def await_router_route(session, prefix, prefix_len, exists=True):
    router = VTM.get_router('router-000-001')
    port_id = router.get_port(session['port'])._mn_resource.get_id()
    router_id = router._mn_resource.get_id()
    timeout = 60
    while timeout > 0:
        routes = BM._api.get_router_routes(router_id)
        existing = False
        for r in routes:
            if r.get_next_hop_port() == port_id and \
                    r.get_dst_network_length() == prefix_len and \
                    r.get_dst_network_addr() == prefix and \
                    r.get_next_hop_gateway() == session['peerAddr']:
                existing = True
        if existing == exists:
            return
        else:
            time.sleep(1)
            timeout -= 1
    raise Exception("Timed out while waiting for BGP route to {0} "
                    "on router {1} port {2} to destination {3}/{4} "
                    "through next hop {5}.".format(
                        "appear" if exists else "dissapear",
                        router_id,
                        port_id,
                        prefix, prefix_len,
                        session['peerAddr']))


def await_internal_route_exported(localAs, peerAs):
    quagga = service.get_container_by_hostname('quagga0')
    timeout = 60
    while timeout > 0:
        output_stream, exec_id = quagga.exec_command(
            "sh -c \"vtysh -c 'show ip bgp' | grep %s | grep %s\"" % (
                localAs, peerAs), stream=True)
        exit_code = quagga.check_exit_status(exec_id, output_stream)
        if exit_code == 0:  # The route is learnt from the corresponding uplink
            return
        time.sleep(2)
        timeout -= 2
    raise Exception("Timed out while waiting for quagga0 to learn the internal "
                    "network through AS %s " % (peerAs))


# 1.1.1.1 is assigned to lo in ns000 emulating a public IP address
def ping_to_inet(count=5, interval=1, port=2, retries=3):
    try:
        sender = BM.get_iface_for_port('bridge-000-001', port)
        f1 = sender.ping_ipv4_addr('1.1.1.1',
                                   interval=interval,
                                   count=count)
        wait_on_futures([f1])
        output_stream, exec_id = f1.result()
        exit_status = sender.compute_host.check_exit_status(exec_id,
                                                            output_stream,
                                                            timeout=20)

        assert_that(exit_status, equal_to(0), "Ping did not return any data")
    except:
        if retries == 0:
            raise RuntimeError("Ping did not return any data and returned -1")
        LOG.debug("BGP: failed ping to inet... (%d retries left)" % retries)
        ping_to_inet(count, interval, port, retries - 1)


@attr(version="v1.2.0")
@bindings(binding_unisession1, binding_unisession2)
@with_setup(None, clear_bgp)
def test_icmp_multi_add_uplink():
    """
    Title: configure BGP to establish multiple uplinks

    Scenario 1:
    Given: one uplink
    When: enable one BGP on it
    Then: ICMP echo RR should work to a psudo public IP address

    Scenario 2:
    Given: another uplink
    When: enable another BGP on it
    Then: ICMP echo RR should work to a psudo public IP address

    """
    upl1 = BM.get_binding_data()['config']['link1']
    upl2 = BM.get_binding_data()['config']['link2']

    add_bgp([upl1], route_direct)
    ping_to_inet()  # BGP #1 is working

    add_bgp([upl2], route_direct)
    ping_to_inet()  # BGP #1 and #2 working


@attr(version="v1.2.0")
@bindings(binding_unisession1, binding_unisession2)
@with_setup(None, clear_bgp)
def test_icmp_remove_uplink():
    """
    Title: Remove one BGP from multiple BGP links

    Scenario 1:
    Given: multiple uplinks with BGP enabled
    When: disable the BGP uplinks
    Then: ICMP echo RR should work to a pseudo public IP address

    """
    upl1 = BM.get_binding_data()['config']['link1']
    upl2 = BM.get_binding_data()['config']['link2']

    p1 = add_bgp([upl1], route_direct)
    add_bgp([upl2], route_direct)
    ping_to_inet()  # BGP #1 and #2 are working

    clear_bgp_peer(p1, 5)
    ping_to_inet()  # only BGP #2 is working


# FIXME: see issue MI-593
@attr(version="v1.2.0")
@bindings(binding_unisession1)
@with_setup(None, clear_bgp)
def test_icmp_failback():
    """
    Title: BGP failover/failback

    Scenario 1:
    Given: multiple uplinks
    When: enable BGP on both of them
    Then: ICMP echo RR should work to a pseudo public IP address

    Scenario 2:
    Given:
    When: inject failure into one of BGP uplinks (failover)
    Then: ICMP echo RR should work to a pseudo public IP address

    Scenario 3:
    Given:
    When: eject failure (failback)
    Then: ICMP echo RR should work to a psudo public IP address

    Scenario 4:
    Given:
    When: inject failure into another of BGP uplinks (failover)
    Then: ICMP echo RR should work to a pseudo public IP address

    Scenario 5:
    Given:
    When: eject failure (failback)
    Then: ICMP echo RR should work to a pseudo public IP address

    """
    add_bgp([uplink1_session1], route_direct)
    add_bgp([uplink2_session1], route_direct)

    await_router_route(uplink1_session1, '1.1.1.1', 32, exists=True)
    await_router_route(uplink1_session1, '0.0.0.0', 0,  exists=True)
    await_router_route(uplink2_session1, '1.1.1.1', 32, exists=True)
    await_router_route(uplink2_session1, '0.0.0.0', 0,  exists=True)

    ping_to_inet()  # BGP #1 and #2 are working

    failure1 = PktFailure(uplink1_session1['peerHostname'],
                          uplink1_session1['peerIface'], 5)

    failure1.inject()
    try:
        await_router_route(uplink1_session1, '1.1.1.1', 32, exists=False)
        await_router_route(uplink1_session1, '0.0.0.0', 0,  exists=False)
        await_router_route(uplink2_session1, '1.1.1.1', 32, exists=True)
        await_router_route(uplink2_session1, '0.0.0.0', 0,  exists=True)
        ping_to_inet()  # BGP #1 is lost but continues to work
    finally:
        failure1.eject()

    await_internal_route_exported(64513, 64511)
    ping_to_inet()  # BGP #1 is back

    failure2 = PktFailure(uplink2_session1['peerHostname'],
                          uplink2_session1['peerIface'], 5)
    failure2.inject()
    try:
        await_router_route(uplink1_session1, '1.1.1.1', 32, exists=True)
        await_router_route(uplink1_session1, '0.0.0.0', 0,  exists=True)
        await_router_route(uplink2_session1, '1.1.1.1', 32, exists=False)
        await_router_route(uplink2_session1, '0.0.0.0', 0,  exists=False)
        ping_to_inet()  # BGP #2 is lost but continues to work
    finally:
        failure2.eject()

    await_internal_route_exported(64513, 64512)
    ping_to_inet()  # BGP #2 is back

    failure1.inject()
    failure2.inject()
    try:
        await_router_route(uplink1_session1, '1.1.1.1', 32, exists=False)
        await_router_route(uplink1_session1, '0.0.0.0', 0,  exists=False)
        await_router_route(uplink2_session1, '1.1.1.1', 32, exists=False)
        await_router_route(uplink2_session1, '0.0.0.0', 0,  exists=False)
    finally:
        failure1.eject()
        failure2.eject()


@attr(version="v1.2.0")
@bindings(binding_unisession1, binding_unisession2)
@with_setup(None, clear_bgp)
def test_snat():
    """
    Title: SNAT test with one uplink

    Scenario 1:
    Given: one uplink
    When: adding snat chains
    Then: ICMP echo RR should work

    """
    upl1 = BM.get_binding_data()['config']['link1']
    pre_filter = BM.get_binding_data()['config']['pre_filter']

    add_bgp([upl1], route_snat)

    set_filters('router-000-001', pre_filter, 'post_filter_snat_ip')

    try:
        # requires all 10 trials work and each of which has one pair of
        # echo/reply work at least in 5 trials (i.e. about 10 second)
        # failover possibly takes about 5-6 seconds at most
        for i in range(0, 10):
            # BGP #1 is working
            ping_to_inet()
    finally:
        unset_filters('router-000-001')


@attr(version="v1.2.0")
@bindings(binding_snat1, binding_snat2)
@with_setup(None, clear_bgp)
def test_mn_1172():
    """
    Title: Simultaneous ICMP SNAT

    Scenario 1:
    Given: one uplink
    When: enable one BGP on it
    Then: ICMP echo RR should work from multiple VMs

    """
    uplink = BM.get_binding_data()['config']['link']
    pre_filter = BM.get_binding_data()['config']['pre_filter']

    add_bgp([uplink], route_snat)

    set_filters('router-000-001', pre_filter, 'post_filter_snat_ip')

    try:
        ping_to_inet(port=2)
        ping_to_inet(port=3)
        ping_to_inet(port=2)
    finally:
        unset_filters('router-000-001')


@bindings(binding_multisession1, binding_multisession2)
@with_setup(None, clear_bgp)
def test_multisession_icmp_add_session():
    """
    Title: BGP adding one session to existing port

    Scenario 1:
    Given: a single session to a single uplink quagga
    When: assing a second session to the same port
    Then: ICMP echo should work to a pseudo public IP address
    """
    session1 = BM.get_binding_data()['config']['session1']
    session2 = BM.get_binding_data()['config']['session2']

    add_bgp([session1], route_direct)

    ping_to_inet()  # BGP session #1 is working

    add_bgp_peer(session2)  # peerAS 2

    ping_to_inet()  # BGP session #1 is still working and nothing breaks


@bindings(binding_multisession1, binding_multisession2)
@with_setup(None, clear_bgp)
def test_multisession_icmp_remove_session():
    """
    Title: BGP removing one session to existing port

    Scenario 1:
    Given: two sessions active on a vport connected to two uplink quaggas
    When: removing one of the sessions
    Then: ICMP echo should work to a pseudo public IP address through the 2n
          session
    """
    multisession = BM.get_binding_data()['config']['multisession']
    session1 = BM.get_binding_data()['config']['session1']

    peers = add_bgp(multisession, route_direct)

    ping_to_inet()  # BGP session #1 is working

    clear_bgp_peer(peers[0], 5)

    ping_to_inet()  # BGP session #2 start forwarding packets

    add_bgp_peer(session1)
    await_internal_route_exported(64513, 64511)

    clear_bgp_peer(peers[1], 5)

    ping_to_inet()


# FIXME: see issue MI-685
@attr(version="v1.2.0")
@bindings(binding_multisession1, binding_multisession2)
@with_setup(None, clear_bgp)
def test_multisession_icmp_failback():
    """
    Title: BGP session failover/failback

    Scenario 1:
    Given: single uplink with multiple sessions
    When: enable two bgp sessions on the same port
    Then: ICMP echo RR should work to a pseudo public IP address

    Scenario 2:
    Given:
    When: inject failure into one of BGP session (failover)
    Then: ICMP echo RR should work to a pseudo public IP address

    Scenario 3:
    Given:
    When: eject failure (failback)
    Then: ICMP echo RR should work to a psudo public IP address

    Scenario 4:
    Given:
    When: inject failure into the other BGP session (failover)
    Then: ICMP echo RR should work to a pseudo public IP address

    Scenario 5:
    Given:
    When: eject failure (failback)
    Then: ICMP echo RR should work to a pseudo public IP address

    """
    data = BM.get_binding_data()['config']
    session1 = data['session1']
    session2 = data['session2']
    multisession = BM.get_binding_data()['config']['multisession']

    add_bgp(multisession, route_direct)

    ping_to_inet()  # BGP #1 and #2 are working

    failure1 = PktFailure(session1['peerHostname'], session1['peerIface'], 5)
    failure1.inject()
    try:
        ping_to_inet()  # BGP session #1 is lost but continues to work
    finally:
        failure1.eject()

    ping_to_inet()  # BGP #1 is back

    failure2 = PktFailure(session2['peerHostname'], session2['peerIface'], 5)
    failure2.inject()
    try:
        ping_to_inet()  # BGP session #2 is lost but continues to work
    finally:
        failure2.eject()

    ping_to_inet()  # BGP #2 is back

    # Check routes
    await_router_route(session1, '1.1.1.1', 32, exists=True)
    await_router_route(session1, '0.0.0.0', 0,  exists=True)
    await_router_route(session2, '1.1.1.1', 32, exists=True)
    await_router_route(session2, '0.0.0.0', 0,  exists=True)

    # Inject failures again and check routes
    failure1.inject()
    try:
        await_router_route(session1, '1.1.1.1', 32, exists=False)
        await_router_route(session1, '0.0.0.0', 0,  exists=False)
        await_router_route(session2, '1.1.1.1', 32, exists=True)
        await_router_route(session2, '0.0.0.0', 0,  exists=True)
    except Exception:
        failure1.eject()
        raise

    failure2.inject()
    try:
        await_router_route(session1, '1.1.1.1', 32, exists=False)
        await_router_route(session1, '0.0.0.0', 0,  exists=False)
        await_router_route(session2, '1.1.1.1', 32, exists=False)
        await_router_route(session2, '0.0.0.0', 0,  exists=False)
    except Exception:
        failure2.eject()
        raise

    failure1.eject()
    failure2.eject()


@bindings(binding_unisession1)
@with_setup(None, clear_bgp)
def test_multisession_icmp_with_redundancy():
    """
    Title: BGP adding double session redundancy to two uplinks

    Scenario:
    Given: two uplinks with two sessions each
    When: disabling one by one
    Then: ICMP echo should work from different vms to a pseudo public IP address
    """

    def await_routes(session, exists=True):
        await_router_route(session, '1.1.1.1', 32, exists)
        await_router_route(session, '0.0.0.0', 0,  exists)

    add_bgp(uplink1_multisession, route_direct)
    await_routes(uplink1_session1, exists=True)
    await_routes(uplink1_session2, exists=True)

    add_bgp(uplink2_multisession, route_direct)
    await_routes(uplink2_session1, exists=True)
    await_routes(uplink2_session2, exists=True)

    failures = []

    ping_to_inet()  # Midolman 2 with 2 sessions on uplink & uplink2

    # Start failing sessions one by one
    failure1 = PktFailure(uplink1_session1['peerHostname'],
                          uplink1_session1['peerIface'], 5)
    failures.append(failure1)
    failure1.inject()
    try:
        await_routes(uplink1_session1, exists=False)
        ping_to_inet()
    except:
        for failure in failures:
            failure.eject()
        raise

    failure2 = PktFailure(uplink2_session1['peerHostname'],
                          uplink2_session1['peerIface'], 5)
    failures.append(failure2)
    failure2.inject()
    try:
        await_routes(uplink2_session1, exists=False)
        ping_to_inet()
    except:
        for failure in failures:
            failure.eject()
        raise

    failure3 = PktFailure(uplink1_session2['peerHostname'],
                          uplink1_session2['peerIface'], 5)
    failures.append(failure3)
    failure3.inject()
    try:
        await_routes(uplink1_session2, exists=False)
        ping_to_inet()
    except:
        for failure in failures:
            failure.eject()
        raise

    failure4 = PktFailure(uplink2_session2['peerHostname'],
                          uplink2_session2['peerIface'], 5)
    failures.append(failure4)
    failure4.inject()
    try:
        await_routes(uplink2_session2, exists=False)
    except Exception:
        for failure in failures:
            failure.eject()
        raise

    try:
        assert_that(calling(ping_to_inet),
                    raises(RuntimeError))
    finally:
        for failure in failures:
            failure.eject()
