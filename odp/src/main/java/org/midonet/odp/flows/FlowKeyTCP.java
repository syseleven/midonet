/*
 * Copyright (c) 2012 Midokura SARL, All Rights Reserved.
 */
package org.midonet.odp.flows;

import org.midonet.netlink.BytesUtil;
import org.midonet.netlink.NetlinkMessage;
import org.midonet.netlink.messages.Builder;
import org.midonet.packets.TCP;
import org.midonet.packets.Unsigned;

public class FlowKeyTCP implements FlowKey {
    /*__be16*/ private int tcp_src;
    /*__be16*/ private int tcp_dst;

    // This is used for deserialization purposes only.
    FlowKeyTCP() { }

    FlowKeyTCP(int source, int destination) {
        TCP.ensurePortInRange(source);
        TCP.ensurePortInRange(destination);
        tcp_src = source;
        tcp_dst = destination;
    }

    @Override
    public void serialize(Builder builder) {
        builder.addValue(BytesUtil.instance.reverseBE((short)tcp_src));
        builder.addValue(BytesUtil.instance.reverseBE((short)tcp_dst));
    }

    @Override
    public boolean deserialize(NetlinkMessage message) {
        try {
            tcp_src = Unsigned.unsign(BytesUtil.instance.reverseBE(message.getShort()));
            tcp_dst = Unsigned.unsign(BytesUtil.instance.reverseBE(message.getShort()));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public NetlinkMessage.AttrKey<FlowKeyTCP> getKey() {
        return FlowKeyAttr.TCP;
    }

    @Override
    public FlowKeyTCP getValue() {
        return this;
    }

    public int getSrc() {
        return tcp_src;
    }

    public int getDst() {
        return tcp_dst;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        @SuppressWarnings("unchecked")
        FlowKeyTCP that = (FlowKeyTCP) o;

        return (tcp_dst == that.tcp_dst) && (tcp_src == that.tcp_src);
    }

    @Override
    public int hashCode() {
        int result = tcp_src;
        result = 31 * result + tcp_dst;
        return result;
    }

    @Override
    public String toString() {
        return "FlowKeyTCP{tcp_src=" + tcp_src + ", tcp_dst=" + tcp_dst + '}';
    }
}
