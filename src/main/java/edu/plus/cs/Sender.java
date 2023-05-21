package edu.plus.cs;

import edu.plus.cs.packet.*;
import edu.plus.cs.packet.util.PacketInterpreter;
import edu.plus.cs.util.ChunkedIterator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sender {
    private final short transmissionId;
    private final File fileToTransfer;
    private final InetAddress receiver;
    private final int port;
    private final int chunkSize;
    private final int ackPort;
    private final long packetDelayUs;
    private final DatagramSocket socket;
    private int sequenceNumber = 0;

    public Sender(short transmissionId, File fileToTransfer, InetAddress receiver, int port, int chunkSize,
                  int ackPort, long packetDelayUs) throws IOException {
        this.transmissionId = transmissionId;
        this.fileToTransfer = fileToTransfer;
        this.receiver = receiver;
        this.port = port;
        this.chunkSize = chunkSize;
        this.ackPort = ackPort;
        this.packetDelayUs = packetDelayUs;
        this.socket = new DatagramSocket(this.ackPort);
    }

    public void send() throws IOException, NoSuchAlgorithmException, InterruptedException {
        // calculate maxSequenceNumber
        int maxSequenceNumber = (int) Math.ceilDiv(fileToTransfer.length(), chunkSize);

        // send first (initialize) packet
        Packet initializePacket = new InitializePacket(transmissionId, sequenceNumber++, maxSequenceNumber, fileToTransfer.getName().toCharArray());
        System.out.println("Sent initialize packet at: " + System.currentTimeMillis());
        sendPacket(initializePacket);

        // wait for ack
        if (!handleAcknowledgementPacket()) {
            return;
        }

        // send data packets while computing the md5 hash
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (FileInputStream input = new FileInputStream(fileToTransfer)) {
            for (ChunkedIterator it = new ChunkedIterator(input, chunkSize); it.hasNext(); ) {
                byte[] chunk = it.next();
                Packet dataPacket = new DataPacket(transmissionId, sequenceNumber++, chunk);
                sendPacket(dataPacket);
                md.update(chunk);

                // wait for ack
                if (!handleAcknowledgementPacket()) {
                    return;
                }
            }
        }

        // send last (finalize) packet
        Packet finalizePacket = new FinalizePacket(transmissionId, sequenceNumber++, md.digest());
        System.out.println(("Sent finalize packet at: " + System.currentTimeMillis()));
        sendPacket(finalizePacket);

        // wait for ack
        if (!handleAcknowledgementPacket()) {
            return;
        }

        this.socket.close();
    }

    private void sendPacket(Packet packet) throws IOException, InterruptedException {
        // convert packet to byte[]
        byte[] bytes = packet.serialize();

        // System.out.println(new String(bytes, StandardCharsets.UTF_8));

        DatagramPacket udpPacket = new DatagramPacket(bytes, bytes.length, receiver, port);
        socket.send(udpPacket);

        Thread.sleep(packetDelayUs / 1000, (int) (packetDelayUs % 1000) * 1000);

        // System.out.println("Sent packet: ");
        // System.out.println(packet.getSequenceNumber());
    }

    private boolean checkAcknowledgementPacket(DatagramPacket packet) {
        return packet.getLength() == 6
                && PacketInterpreter.getTransmissionId(packet.getData()) == this.transmissionId
                && PacketInterpreter.getSequenceNumber(packet.getData()) == (this.sequenceNumber - 1);
    }

    private boolean handleAcknowledgementPacket() throws IOException {
        byte[] buffer = new byte[65535];

        // wait for ack
        DatagramPacket udpPacket = new DatagramPacket(buffer, buffer.length);
        this.socket.receive(udpPacket);

        if (!checkAcknowledgementPacket(udpPacket)) {
            System.err.println("Did not receive valid acknowledgement packet, abort transmission");
            this.socket.close();
            return false;
        }

        return true;
    }
}