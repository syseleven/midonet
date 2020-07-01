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
package org.midonet.cluster.rest_api.neutron.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.primitives.Ints;

import org.midonet.cluster.data.ZoomEnum;
import org.midonet.cluster.data.ZoomEnumValue;
import org.midonet.cluster.models.Commons;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ZoomEnum(clazz = Commons.Protocol.class)
public enum RuleProtocol {

    @ZoomEnumValue("TCP")
    TCP("tcp", org.midonet.packets.TCP.PROTOCOL_NUMBER),

    @ZoomEnumValue("UDP")
    UDP("udp", org.midonet.packets.UDP.PROTOCOL_NUMBER),

    @ZoomEnumValue("ICMP")
    ICMP("icmp", org.midonet.packets.ICMP.PROTOCOL_NUMBER),

    @ZoomEnumValue("ICMPV6")
    ICMPv6("icmpv6", org.midonet.packets.ICMPv6.PROTOCOL_NUMBER),

    @ZoomEnumValue("HOPOPT")
    HOPOPT("hopopt", (byte)0), // IPv6 Hop-by-Hop Option

    @ZoomEnumValue("IGMP")
    IGMP("igmp", (byte)2), // Internet Group Management

    @ZoomEnumValue("GGP")
    GGP("ggp", (byte)3), // Gateway-to-Gateway

    @ZoomEnumValue("IPIP")
    IPV4("ipip", (byte)4), // IPv4 encapsulation

    @ZoomEnumValue("ST")
    ST("st", (byte)5), // Stream

    @ZoomEnumValue("CBT")
    CBT("cbt", (byte)7), // CBT

    @ZoomEnumValue("EGP")
    EGP("egp", (byte)8), // Exterior Gateway Protocol

    @ZoomEnumValue("IGP")
    IGP("igp", (byte)9), // any private interior gateway (used by Cisco for their IGRP)

    @ZoomEnumValue("BBN_RCC_MON")
    BBN_RCC_MON("bbn-rcc-mon", (byte)10), // BBN RCC Monitoring

    @ZoomEnumValue("NVP_II")
    NVP_II("nvp-ii", (byte)11), // Network Voice Protocol

    @ZoomEnumValue("PUP")
    PUP("pup", (byte)12), // PUP

    @ZoomEnumValue("ARGUS")
    ARGUS("argus", (byte)13), // ARGUS

    @ZoomEnumValue("EMCON")
    EMCON("emcon", (byte)14), // EMCON

    @ZoomEnumValue("XNET")
    XNET("xnet", (byte)15), // Cross Net Debugger

    @ZoomEnumValue("CHAOS")
    CHAOS("chaos", (byte)16), // Chaos

    @ZoomEnumValue("MUX")
    MUX("mux", (byte)18), // Multiplexing

    @ZoomEnumValue("DCN_MEAS")
    DCN_MEAS("dcn-meas", (byte)19), // DCN Measurement Subsystems

    @ZoomEnumValue("HMP")
    HMP("hmp", (byte)20), // Host Monitoring

    @ZoomEnumValue("PRM")
    PRM("prm", (byte)21), // Packet Radio Measurement

    @ZoomEnumValue("XNS_IDP")
    XNS_IDP("xns-idp", (byte)22), // XEROX NS IDP

    @ZoomEnumValue("TRUNK_1")
    TRUNK_1("trunk-1", (byte)23), // Trunk-1

    @ZoomEnumValue("TRUNK_2")
    TRUNK_2("trunk-2", (byte)24), // Trunk-2

    @ZoomEnumValue("LEAF_1")
    LEAF_1("leaf-1", (byte)25), // Leaf-1

    @ZoomEnumValue("LEAF_2")
    LEAF_2("leaf-2", (byte)26), // Leaf-2

    @ZoomEnumValue("RDP")
    RDP("rdp", (byte)27), // Reliable Data Protocol

    @ZoomEnumValue("IRTP")
    IRTP("irtp", (byte)28), // Internet Reliable Transaction

    @ZoomEnumValue("ISO_TP4")
    ISO_TP4("iso-tp4", (byte)29), // ISO Transport Protocol Class 4

    @ZoomEnumValue("NETBLT")
    NETBLT("netblt", (byte)30), // Bulk Data Transfer Protocol

    @ZoomEnumValue("MFE_NSP")
    MFE_NSP("mfe-nsp", (byte)31), // MFE Network Services Protocol

    @ZoomEnumValue("MERIT_INP")
    MERIT_INP("merit-inp", (byte)32), // MERIT Internodal Protocol

    @ZoomEnumValue("DCCP")
    DCCP("dccp", (byte)33), // Datagram Congestion Control Protocol

    @ZoomEnumValue("THREEPC")
    THREEPC("3pc", (byte)34), // Third Party Connect Protocol

    @ZoomEnumValue("IDPR")
    IDPR("idpr", (byte)35), // Inter-Domain Policy Routing Protocol

    @ZoomEnumValue("XTP")
    XTP("xtp", (byte)36), // XTP

    @ZoomEnumValue("DDP")
    DDP("ddp", (byte)37), // Datagram Delivery Protocol

    @ZoomEnumValue("IDPR_CMTP")
    IDPR_CMTP("idpr-cmtp", (byte)38), // IDPR Control Message Transport Proto

    @ZoomEnumValue("TPPP")
    TPPP("tp++", (byte)39), // TP++ Transport Protocol

    @ZoomEnumValue("IL")
    IL("il", (byte)40), // IL Transport Protocol

    @ZoomEnumValue("IPV6_ENCAP")
    IPV6_ENCAP("ipv6-encap", (byte)41), // IPv6 encapsulation

    @ZoomEnumValue("SDRP")
    SDRP("sdrp", (byte)42), // Source Demand Routing Protocol

    @ZoomEnumValue("IPV6_ROUTE")
    IPV6_ROUTE("ipv6-route", (byte)43), // Routing Header for IPv6

    @ZoomEnumValue("IPV6_FRAG")
    IPV6_FRAG("ipv6-frag", (byte)44), // Fragment Header for IPv6

    @ZoomEnumValue("IDRP")
    IDRP("idrp", (byte)45), // Inter-Domain Routing Protocol

    @ZoomEnumValue("RSVP")
    RSVP("rsvp", (byte)46), // Reservation Protocol

    @ZoomEnumValue("GRE")
    GRE("gre", (byte)47), // Generic Routing Encapsulation

    @ZoomEnumValue("DSR")
    DSR("dsr", (byte)48), // Dynamic Source Routing Protocol

    @ZoomEnumValue("BNA")
    BNA("bna", (byte)49), // BNA

    @ZoomEnumValue("ESP")
    ESP("esp", (byte)50), // Encap Security Payload

    @ZoomEnumValue("AH")
    AH("ah", (byte)51), // Authentication Header

    @ZoomEnumValue("I_NLSP")
    I_NLSP("i-nlsp", (byte)52), // Integrated Net Layer Security  TUBA

    @ZoomEnumValue("SWIPE")
    SWIPE("swipe", (byte)53), // IP with Encryption

    @ZoomEnumValue("NARP")
    NARP("narp", (byte)54), // NBMA Address Resolution Protocol

    @ZoomEnumValue("MOBILE")
    MOBILE("mobile", (byte)55), // IP Mobility

    @ZoomEnumValue("TLSP")
    TLSP("tlsp", (byte)56), // Transport Layer Security Protocol using Kryptonet key management

    @ZoomEnumValue("SKIP")
    SKIP("skip", (byte)57), // SKIP

    @ZoomEnumValue("IPV6_NONXT")
    IPV6_NONXT("ipv6-nonxt", (byte)59), // No Next Header for IPv6

    @ZoomEnumValue("IPV6_OPTS")
    IPV6_OPTS("ipv6-opts", (byte)60), // Destination Options for IPv6

    @ZoomEnumValue("PROTO61")
    PROTO61("61", (byte)61), // any host internal protocol

    @ZoomEnumValue("CFTP")
    CFTP("cftp", (byte)62), // CFTP

    @ZoomEnumValue("PROTO63")
    PROTO63("63", (byte)63), // any local network

    @ZoomEnumValue("SAT_EXPAK")
    SAT_EXPAK("sat-expak", (byte)64), // SATNET and Backroom EXPAK

    @ZoomEnumValue("KRYPTOLAN")
    KRYPTOLAN("kryptolan", (byte)65), // Kryptolan

    @ZoomEnumValue("RVD")
    RVD("rvd", (byte)66), // MIT Remote Virtual Disk Protocol

    @ZoomEnumValue("IPPC")
    IPPC("ippc", (byte)67), // Internet Pluribus Packet Core

    @ZoomEnumValue("PROTO68")
    PROTO68("68", (byte)68), // any distributed file system

    @ZoomEnumValue("SAT_MON")
    SAT_MON("sat-mon", (byte)69), // SATNET Monitoring

    @ZoomEnumValue("VISA")
    VISA("visa", (byte)70), // VISA Protocol

    @ZoomEnumValue("IPCV")
    IPCV("ipcv", (byte)71), // Internet Packet Core Utility

    @ZoomEnumValue("CPNX")
    CPNX("cpnx", (byte)72), // Computer Protocol Network Executive

    @ZoomEnumValue("CPHB")
    CPHB("cphb", (byte)73), // Computer Protocol Heart Beat

    @ZoomEnumValue("WSN")
    WSN("wsn", (byte)74), // Wang Span Network

    @ZoomEnumValue("PVP")
    PVP("pvp", (byte)75), // Packet Video Protocol

    @ZoomEnumValue("BR_SAT_MON")
    BR_SAT_MON("br-sat-mon", (byte)76), // Backroom SATNET Monitoring

    @ZoomEnumValue("SUN_ND")
    SUN_ND("sun-nd", (byte)77), // SUN ND PROTOCOL-Temporary

    @ZoomEnumValue("WB_MON")
    WB_MON("wb-mon", (byte)78), // WIDEBAND Monitoring

    @ZoomEnumValue("WB_EXPAK")
    WB_EXPAK("wb-expak", (byte)79), // WIDEBAND EXPAK

    @ZoomEnumValue("ISO_IP")
    ISO_IP("iso-ip", (byte)80), // ISO Internet Protocol

    @ZoomEnumValue("VMTP")
    VMTP("vmtp", (byte)81), // VMTP

    @ZoomEnumValue("SECURE_VMTP")
    SECURE_VMTP("secure-vmtp", (byte)82), // SECURE-VMTP

    @ZoomEnumValue("VINES")
    VINES("vines", (byte)83), // VINES

    @ZoomEnumValue("TTP")
    TTP("ttp", (byte)84), // Transaction Transport Protocol; also IPTM = Internet Protocol Traffic Manager

    @ZoomEnumValue("NSFNET_IGP")
    NSFNET_IGP("nsfnet-igp", (byte)85), // NSFNET-IGP

    @ZoomEnumValue("DGP")
    DGP("dgp", (byte)86), // Dissimilar Gateway Protocol

    @ZoomEnumValue("TCF")
    TCF("tcf", (byte)87), // TCF

    @ZoomEnumValue("EIGRP")
    EIGRP("eigrp", (byte)88), // EIGRP

    @ZoomEnumValue("OSPF")
    OSPF("ospf", (byte)89), // Open Shortest Path First

    @ZoomEnumValue("SPRITE_RPC")
    SPRITE_RPC("sprite-rpc", (byte)90), // Sprite RPC Protocol

    @ZoomEnumValue("LARP")
    LARP("larp", (byte)91), // Locus Address Resolution Protocol

    @ZoomEnumValue("MTP")
    MTP("mtp", (byte)92), // Multicast Transport Protocol

    @ZoomEnumValue("AX25")
    AX25("ax25", (byte)93), // AX.25 Frames

    @ZoomEnumValue("IPIP94")
    IPIP("ipip94", (byte)94), // IP-within-IP Encapsulation Protocol

    @ZoomEnumValue("MICP")
    MICP("micp", (byte)95), // Mobile Internetworking Control Pro.

    @ZoomEnumValue("SCC_SP")
    SCC_SP("scc-sp", (byte)96), // Semaphore Communications Sec. Pro.

    @ZoomEnumValue("ETHERIP")
    ETHERIP("etherip", (byte)97), // Ethernet-within-IP Encapsulation

    @ZoomEnumValue("ENCAP")
    ENCAP("encap", (byte)98), // Encapsulation Header

    @ZoomEnumValue("PROTO99")
    PROTO99("99", (byte)99), // any private encryption scheme

    @ZoomEnumValue("GMTP")
    GMTP("gmtp", (byte)100), // GMTP

    @ZoomEnumValue("IFMP")
    IFMP("ifmp", (byte)101), // Ipsilon Flow Management Protocol

    @ZoomEnumValue("PNNI")
    PNNI("pnni", (byte)102), // PNNI over IP

    @ZoomEnumValue("PIM")
    PIM("pim", (byte)103), // Protocol Independent Multicast

    @ZoomEnumValue("ARIS")
    ARIS("aris", (byte)104), // ARIS

    @ZoomEnumValue("SCPS")
    SCPS("scps", (byte)105), // SCPS

    @ZoomEnumValue("QNX")
    QNX("qnx", (byte)106), // QNX

    @ZoomEnumValue("AN")
    AN("an", (byte)107), // Active Networks

    @ZoomEnumValue("IPCOMP")
    IPCOMP("ipcomp", (byte)108), // IP Payload Compression Protocol

    @ZoomEnumValue("SNP")
    SNP("snp", (byte)109), // Sitara Networks Protocol

    @ZoomEnumValue("COMPAQ_PEER")
    COMPAQ_PEER("compaq-peer", (byte)110), // Compaq Peer Protocol

    @ZoomEnumValue("IPX_IN_IP")
    IPX_IN_IP("ipx-in-ip", (byte)111), // IPX in IP

    @ZoomEnumValue("VRRP")
    VRRP("vrrp", (byte)112), // Virtual Router Redundancy Protocol

    @ZoomEnumValue("PGM")
    PGM("pgm", (byte)113), // PGM Reliable Transport Protocol

    @ZoomEnumValue("PROTO114")
    PROTO114("114", (byte)114), // any 0-hop protocol

    @ZoomEnumValue("L2TP")
    L2TP("l2tp", (byte)115), // Layer Two Tunneling Protocol

    @ZoomEnumValue("DDX")
    DDX("ddx", (byte)116), // D-II Data Exchange (DDX)

    @ZoomEnumValue("IATP")
    IATP("iatp", (byte)117), // Interactive Agent Transfer Protocol

    @ZoomEnumValue("STP")
    STP("stp", (byte)118), // Schedule Transfer Protocol

    @ZoomEnumValue("SRP")
    SRP("srp", (byte)119), // SpectraLink Radio Protocol

    @ZoomEnumValue("UTI")
    UTI("uti", (byte)120), // UTI

    @ZoomEnumValue("SMP")
    SMP("smp", (byte)121), // Simple Message Protocol

    @ZoomEnumValue("SM")
    SM("sm", (byte)122), // Simple Multicast Protocol

    @ZoomEnumValue("PTP")
    PTP("ptp", (byte)123), // Performance Transparency Protocol

    @ZoomEnumValue("ISIS_OVER_IPV4")
    ISIS_OVER_IPV4("isis-over-ipv4", (byte)124),

    @ZoomEnumValue("FIRE")
    FIRE("fire", (byte)125),

    @ZoomEnumValue("CRTP")
    CRTP("crtp", (byte)126), // Combat Radio Transport Protocol

    @ZoomEnumValue("CRUDP")
    CRUDP("crudp", (byte)127), // Combat Radio User Datagram

    @ZoomEnumValue("SSCOPMCE")
    SSCOPMCE("sscopmce", (byte)128),

    @ZoomEnumValue("IPLT")
    IPLT("iplt", (byte)129),

    @ZoomEnumValue("SPS")
    SPS("sps", (byte)130), // Secure Packet Shield

    @ZoomEnumValue("PIPE")
    PIPE("pipe", (byte)131), // Private IP Encapsulation within IP

    @ZoomEnumValue("SCTP")
    SCTP("sctp", (byte)132), // Stream Control Transmission Protocol

    @ZoomEnumValue("FC")
    FC("fc", (byte)133), // Fibre Channel

    @ZoomEnumValue("RSVP_E2E_IGNORE")
    RSVP_E2E_IGNORE("rsvp-e2e-ignore", (byte)134),

    @ZoomEnumValue("MOBILITY_HEADER")
    MOBILITY_HEADER("mobility-header", (byte)135),

    @ZoomEnumValue("UDPLITE")
    UDPLITE("udplite", (byte)136),

    @ZoomEnumValue("MPLS_IN_IP")
    MPLS_IN_IP("mpls-in-ip", (byte)137),

    @ZoomEnumValue("MANET")
    MANET("manet", (byte)138), // MANET Protocols

    @ZoomEnumValue("HIP")
    HIP("hip", (byte)139), // Host Identity Protocol

    @ZoomEnumValue("SHIM6")
    SHIM6("shim6", (byte)140), // Shim6 Protocol

    @ZoomEnumValue("WESP")
    WESP("wesp", (byte)141), // Wrapped Encapsulating Security Payload

    @ZoomEnumValue("ROHC")
    ROHC("rohc", (byte)142), // Robust Header Compression

    @ZoomEnumValue("ETHERNET")
    ETHERNET("ethernet", (byte)143), // Ethernet (TEMPORARY - registered 2020-01-31, expires 2021-01-31)

    @ZoomEnumValue("PROTO144")
    PROTO144("144", (byte)144),

    @ZoomEnumValue("PROTO145")
    PROTO145("145", (byte)145),

    @ZoomEnumValue("PROTO146")
    PROTO146("146", (byte)146),

    @ZoomEnumValue("PROTO147")
    PROTO147("147", (byte)147),

    @ZoomEnumValue("PROTO148")
    PROTO148("148", (byte)148),

    @ZoomEnumValue("PROTO149")
    PROTO149("149", (byte)149),

    @ZoomEnumValue("PROTO150")
    PROTO150("150", (byte)150),

    @ZoomEnumValue("PROTO151")
    PROTO151("151", (byte)151),

    @ZoomEnumValue("PROTO152")
    PROTO152("152", (byte)152),

    @ZoomEnumValue("PROTO153")
    PROTO153("153", (byte)153),

    @ZoomEnumValue("PROTO154")
    PROTO154("154", (byte)154),

    @ZoomEnumValue("PROTO155")
    PROTO155("155", (byte)155),

    @ZoomEnumValue("PROTO156")
    PROTO156("156", (byte)156),

    @ZoomEnumValue("PROTO157")
    PROTO157("157", (byte)157),

    @ZoomEnumValue("PROTO158")
    PROTO158("158", (byte)158),

    @ZoomEnumValue("PROTO159")
    PROTO159("159", (byte)159),

    @ZoomEnumValue("PROTO160")
    PROTO160("160", (byte)160),

    @ZoomEnumValue("PROTO161")
    PROTO161("161", (byte)161),

    @ZoomEnumValue("PROTO162")
    PROTO162("162", (byte)162),

    @ZoomEnumValue("PROTO163")
    PROTO163("163", (byte)163),

    @ZoomEnumValue("PROTO164")
    PROTO164("164", (byte)164),

    @ZoomEnumValue("PROTO165")
    PROTO165("165", (byte)165),

    @ZoomEnumValue("PROTO166")
    PROTO166("166", (byte)166),

    @ZoomEnumValue("PROTO167")
    PROTO167("167", (byte)167),

    @ZoomEnumValue("PROTO168")
    PROTO168("168", (byte)168),

    @ZoomEnumValue("PROTO169")
    PROTO169("169", (byte)169),

    @ZoomEnumValue("PROTO170")
    PROTO170("170", (byte)170),

    @ZoomEnumValue("PROTO171")
    PROTO171("171", (byte)171),

    @ZoomEnumValue("PROTO172")
    PROTO172("172", (byte)172),

    @ZoomEnumValue("PROTO173")
    PROTO173("173", (byte)173),

    @ZoomEnumValue("PROTO174")
    PROTO174("174", (byte)174),

    @ZoomEnumValue("PROTO175")
    PROTO175("175", (byte)175),

    @ZoomEnumValue("PROTO176")
    PROTO176("176", (byte)176),

    @ZoomEnumValue("PROTO177")
    PROTO177("177", (byte)177),

    @ZoomEnumValue("PROTO178")
    PROTO178("178", (byte)178),

    @ZoomEnumValue("PROTO179")
    PROTO179("179", (byte)179),

    @ZoomEnumValue("PROTO180")
    PROTO180("180", (byte)180),

    @ZoomEnumValue("PROTO181")
    PROTO181("181", (byte)181),

    @ZoomEnumValue("PROTO182")
    PROTO182("182", (byte)182),

    @ZoomEnumValue("PROTO183")
    PROTO183("183", (byte)183),

    @ZoomEnumValue("PROTO184")
    PROTO184("184", (byte)184),

    @ZoomEnumValue("PROTO185")
    PROTO185("185", (byte)185),

    @ZoomEnumValue("PROTO186")
    PROTO186("186", (byte)186),

    @ZoomEnumValue("PROTO187")
    PROTO187("187", (byte)187),

    @ZoomEnumValue("PROTO188")
    PROTO188("188", (byte)188),

    @ZoomEnumValue("PROTO189")
    PROTO189("189", (byte)189),

    @ZoomEnumValue("PROTO190")
    PROTO190("190", (byte)190),

    @ZoomEnumValue("PROTO191")
    PROTO191("191", (byte)191),

    @ZoomEnumValue("PROTO192")
    PROTO192("192", (byte)192),

    @ZoomEnumValue("PROTO193")
    PROTO193("193", (byte)193),

    @ZoomEnumValue("PROTO194")
    PROTO194("194", (byte)194),

    @ZoomEnumValue("PROTO195")
    PROTO195("195", (byte)195),

    @ZoomEnumValue("PROTO196")
    PROTO196("196", (byte)196),

    @ZoomEnumValue("PROTO197")
    PROTO197("197", (byte)197),

    @ZoomEnumValue("PROTO198")
    PROTO198("198", (byte)198),

    @ZoomEnumValue("PROTO199")
    PROTO199("199", (byte)199),

    @ZoomEnumValue("PROTO200")
    PROTO200("200", (byte)200),

    @ZoomEnumValue("PROTO201")
    PROTO201("201", (byte)201),

    @ZoomEnumValue("PROTO202")
    PROTO202("202", (byte)202),

    @ZoomEnumValue("PROTO203")
    PROTO203("203", (byte)203),

    @ZoomEnumValue("PROTO204")
    PROTO204("204", (byte)204),

    @ZoomEnumValue("PROTO205")
    PROTO205("205", (byte)205),

    @ZoomEnumValue("PROTO206")
    PROTO206("206", (byte)206),

    @ZoomEnumValue("PROTO207")
    PROTO207("207", (byte)207),

    @ZoomEnumValue("PROTO208")
    PROTO208("208", (byte)208),

    @ZoomEnumValue("PROTO209")
    PROTO209("209", (byte)209),

    @ZoomEnumValue("PROTO210")
    PROTO210("210", (byte)210),

    @ZoomEnumValue("PROTO211")
    PROTO211("211", (byte)211),

    @ZoomEnumValue("PROTO212")
    PROTO212("212", (byte)212),

    @ZoomEnumValue("PROTO213")
    PROTO213("213", (byte)213),

    @ZoomEnumValue("PROTO214")
    PROTO214("214", (byte)214),

    @ZoomEnumValue("PROTO215")
    PROTO215("215", (byte)215),

    @ZoomEnumValue("PROTO216")
    PROTO216("216", (byte)216),

    @ZoomEnumValue("PROTO217")
    PROTO217("217", (byte)217),

    @ZoomEnumValue("PROTO218")
    PROTO218("218", (byte)218),

    @ZoomEnumValue("PROTO219")
    PROTO219("219", (byte)219),

    @ZoomEnumValue("PROTO220")
    PROTO220("220", (byte)220),

    @ZoomEnumValue("PROTO221")
    PROTO221("221", (byte)221),

    @ZoomEnumValue("PROTO222")
    PROTO222("222", (byte)222),

    @ZoomEnumValue("PROTO223")
    PROTO223("223", (byte)223),

    @ZoomEnumValue("PROTO224")
    PROTO224("224", (byte)224),

    @ZoomEnumValue("PROTO225")
    PROTO225("225", (byte)225),

    @ZoomEnumValue("PROTO226")
    PROTO226("226", (byte)226),

    @ZoomEnumValue("PROTO227")
    PROTO227("227", (byte)227),

    @ZoomEnumValue("PROTO228")
    PROTO228("228", (byte)228),

    @ZoomEnumValue("PROTO229")
    PROTO229("229", (byte)229),

    @ZoomEnumValue("PROTO230")
    PROTO230("230", (byte)230),

    @ZoomEnumValue("PROTO231")
    PROTO231("231", (byte)231),

    @ZoomEnumValue("PROTO232")
    PROTO232("232", (byte)232),

    @ZoomEnumValue("PROTO233")
    PROTO233("233", (byte)233),

    @ZoomEnumValue("PROTO234")
    PROTO234("234", (byte)234),

    @ZoomEnumValue("PROTO235")
    PROTO235("235", (byte)235),

    @ZoomEnumValue("PROTO236")
    PROTO236("236", (byte)236),

    @ZoomEnumValue("PROTO237")
    PROTO237("237", (byte)237),

    @ZoomEnumValue("PROTO238")
    PROTO238("238", (byte)238),

    @ZoomEnumValue("PROTO239")
    PROTO239("239", (byte)239),

    @ZoomEnumValue("PROTO240")
    PROTO240("240", (byte)240),

    @ZoomEnumValue("PROTO241")
    PROTO241("241", (byte)241),

    @ZoomEnumValue("PROTO242")
    PROTO242("242", (byte)242),

    @ZoomEnumValue("PROTO243")
    PROTO243("243", (byte)243),

    @ZoomEnumValue("PROTO244")
    PROTO244("244", (byte)244),

    @ZoomEnumValue("PROTO245")
    PROTO245("245", (byte)245),

    @ZoomEnumValue("PROTO246")
    PROTO246("246", (byte)246),

    @ZoomEnumValue("PROTO247")
    PROTO247("247", (byte)247),

    @ZoomEnumValue("PROTO248")
    PROTO248("248", (byte)248),

    @ZoomEnumValue("PROTO249")
    PROTO249("249", (byte)249),

    @ZoomEnumValue("PROTO250")
    PROTO250("250", (byte)250),

    @ZoomEnumValue("PROTO251")
    PROTO251("251", (byte)251),

    @ZoomEnumValue("PROTO252")
    PROTO252("252", (byte)252),

    @ZoomEnumValue("PROTO253")
    PROTO253("253", (byte)253),

    @ZoomEnumValue("PROTO254")
    PROTO254("254", (byte)254),

    @ZoomEnumValue("PROTO255")
    PROTO255("255", (byte)255);

    private final String value;
    private final byte number;

    private static final Map<String, RuleProtocol> fromStrMap;
    static {
        HashMap<String, RuleProtocol> map = new HashMap<String, RuleProtocol>();
        for (RuleProtocol protocol : RuleProtocol.values()) {
            map.put(protocol.value, protocol);
        }
        map.put("ipv6-icmp", ICMPv6);
        fromStrMap = Collections.unmodifiableMap(map);
    }

    private static final Map<Byte, RuleProtocol> fromNumberMap;
    static {
        HashMap<Byte, RuleProtocol> map = new HashMap<Byte, RuleProtocol>();
        for (RuleProtocol protocol : RuleProtocol.values()) {
            map.put(protocol.number, protocol);
        }
        fromNumberMap = Collections.unmodifiableMap(map);
    }


    RuleProtocol(final String value, final byte number) {
        this.value = value;
        this.number = number;
    }

    @JsonValue
    public String value() {
        return value;
    }

    public byte number() {
        return number;
    }

    private static RuleProtocol forNumValue(byte num) {
        return fromNumberMap.get(num);
    }

    private static RuleProtocol forStrValue(String s) {
        return fromStrMap.get(s.toLowerCase());
    }

    @JsonCreator
    public static RuleProtocol forValue(String v) {
        if (v == null) return null;
        Integer num = Ints.tryParse(v);
        return (num != null) ? forNumValue(num.byteValue()) : forStrValue(v);
    }
}
