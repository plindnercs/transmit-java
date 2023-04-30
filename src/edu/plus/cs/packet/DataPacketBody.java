package edu.plus.cs.packet;


import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class DataPacketBody extends PacketBody implements Serializable {
    byte[] data;

    public DataPacketBody(byte[] data) {
        this.data = data;
        this.packetBodyIdentifier = (byte) 0x01;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "DataPacketBody{" +
                "data=" + Arrays.toString(data) +
                '}';
    }

    @Override
    public byte[] serialize() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1 + data.length);
        byteBuffer.put(packetBodyIdentifier);
        byteBuffer.put(data);
        return byteBuffer.array();
    }
}
