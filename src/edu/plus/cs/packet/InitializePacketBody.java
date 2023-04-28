package edu.plus.cs.packet;

import java.io.Serializable;
import java.util.Arrays;

public class InitializePacketBody extends PacketBody implements Serializable {
    int maxSequenceNumber; // 32 bit
    char[] filename; // [0 ... 2048] bit

    public InitializePacketBody(int maxSequenceNumber, char[] filename) {
        this.maxSequenceNumber = maxSequenceNumber;
        this.filename = filename;
    }

    @Override
    public String toString() {
        return "InitializePacketBody{" +
                "maxSequenceNumber=" + maxSequenceNumber +
                ", filename=" + Arrays.toString(filename) +
                '}';
    }
}
