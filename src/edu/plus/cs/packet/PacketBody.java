package edu.plus.cs.packet;

import java.io.Serializable;

public abstract class PacketBody implements Serializable {

    protected byte packetBodyIdentifier;

    public abstract String toString();

    public abstract byte[] serialize();
}
