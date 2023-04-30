package edu.plus.cs.packet;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class FinalizePacketBody extends PacketBody implements Serializable {
    byte[] md5; // 128 bit

    public FinalizePacketBody(byte[] md5) {
        this.md5 = md5;
        this.packetBodyIdentifier = (byte) 0x02;
    }

    @Override
    public String toString() {
        return "FinalizePacketBody{" +
                "md5=" + Arrays.toString(md5) +
                '}';
    }

    @Override
    public byte[] serialize() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1 + 16);
        byteBuffer.put(packetBodyIdentifier);
        byteBuffer.put(md5);
        return byteBuffer.array();
    }
}
