package com.midokura.midolman.packets;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import junit.framework.Assert;

import org.junit.Test;

public class TestICMP {

    @Test
    public void testChecksum() {
        // The icmp packet is taken from wireshark, and the checksum bytes
        // have been zeroed out bytes[2] and bytes [3]. Wireshark claims
        // the checksum should be:
        short expCksum = 0x2c1e;
        byte[] icmpBytes = new byte[] {
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0xb2, (byte)0x78, (byte)0x00, (byte)0x01,
                (byte)0xf8, (byte)0x59, (byte)0x98, (byte)0x4e,
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0xc6, (byte)0xec, (byte)0x0b, (byte)0x00,
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x10, (byte)0x11, (byte)0x12, (byte)0x13,
                (byte)0x14, (byte)0x15, (byte)0x16, (byte)0x17,
                (byte)0x18, (byte)0x19, (byte)0x1a, (byte)0x1b,
                (byte)0x1c, (byte)0x1d, (byte)0x1e, (byte)0x1f,
                (byte)0x20, (byte)0x21, (byte)0x22, (byte)0x23,
                (byte)0x24, (byte)0x25, (byte)0x26, (byte)0x27,
                (byte)0x28, (byte)0x29, (byte)0x2a, (byte)0x2b,
                (byte)0x2c, (byte)0x2d, (byte)0x2e, (byte)0x2f,
                (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x33,
                (byte)0x34, (byte)0x35, (byte)0x36, (byte)0x37
        };
        // First, let's just try to compute the checksum using IPv4's method.
        short cksum = IPv4.computeChecksum(icmpBytes, 0, icmpBytes.length, -2);
        Assert.assertEquals(expCksum, cksum);

        // Now let's see if ICMP deserialization/serialization sets it
        // correctly.
        ICMP icmp = new ICMP();
        icmp.deserialize(icmpBytes, 0, icmpBytes.length);
        byte[] bytes = icmp.serialize();
        // Verify that the checksum field has been set properly.
        Assert.assertEquals(0x2c, bytes[2]);
        Assert.assertEquals(0x1e, bytes[3]);
    }

}
