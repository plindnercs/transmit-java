package edu.plus.cs.packet;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class FinalizePacketBody extends PacketBody implements Serializable {
    char[] md5; // 128 bit

    public FinalizePacketBody(char[] md5) {
        this.md5 = md5;
    }

    @Override
    public String toString() {
        return "FinalizePacketBody{" +
                "md5=" + Arrays.toString(md5) +
                '}';
    }

    @Override
    public byte[] serialize() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(16);
        byteBuffer.put(new String(md5).getBytes(StandardCharsets.UTF_8));
        return byteBuffer.array();
    }
}
