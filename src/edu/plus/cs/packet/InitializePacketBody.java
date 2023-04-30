package edu.plus.cs.packet;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class InitializePacketBody extends PacketBody implements Serializable {
    int maxSequenceNumber; // 32 bit
    char[] fileName; // [0 ... 2048] bit

    public InitializePacketBody(int maxSequenceNumber, char[] fileName) {
        this.maxSequenceNumber = maxSequenceNumber;
        this.fileName = fileName;
        this.packetBodyIdentifier = (byte) 0x00;
    }

    public int getMaxSequenceNumber() {
        return maxSequenceNumber;
    }

    public char[] getFileName() {
        return fileName;
    }

    @Override
    public String toString() {
        return "InitializePacketBody{" +
                "maxSequenceNumber=" + maxSequenceNumber +
                ", filename=" + Arrays.toString(fileName) +
                '}';
    }

    @Override
    public byte[] serialize() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1 + 4 + new String(fileName).getBytes().length);
        byteBuffer.put(packetBodyIdentifier);
        byteBuffer.putInt(maxSequenceNumber);
        byteBuffer.put(new String(fileName).getBytes(StandardCharsets.UTF_8));
        return byteBuffer.array();
    }
}
