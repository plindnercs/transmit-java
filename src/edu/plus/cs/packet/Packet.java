package edu.plus.cs.packet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Packet implements Serializable {
    short transmissionId; // 16 bit
    int sequenceNumber; // 32 bit
    PacketBody packetBody;

    public Packet(short transmissionId, int sequenceNumber, PacketBody packetBody) {
        this.transmissionId = transmissionId;
        this.sequenceNumber = sequenceNumber;
        this.packetBody = packetBody;
    }

    public PacketBody getPacketBody() {
        return packetBody;
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeShort(transmissionId);
        oos.writeInt(sequenceNumber);
        oos.writeObject(packetBody);
        oos.flush();
        return bos.toByteArray();
    }

    @Override
    public String toString() {
        return "Packet{" +
                "transmissionId=" + transmissionId +
                ", sequenceNumber=" + sequenceNumber +
                ", packetBody=" + packetBody +
                '}';
    }
}
