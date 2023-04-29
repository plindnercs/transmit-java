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
        ByteBuffer byteBuffer = ByteBuffer.allocate(4 + new String(fileName).getBytes().length);
        byteBuffer.putInt(maxSequenceNumber);
        byteBuffer.put(new String(fileName).getBytes(StandardCharsets.UTF_8));
        return byteBuffer.array();
    }
}
