package edu.plus.cs;

import edu.plus.cs.packet.DataPacketBody;
import edu.plus.cs.packet.FinalizePacketBody;
import edu.plus.cs.packet.InitializePacketBody;
import edu.plus.cs.packet.Packet;
import edu.plus.cs.util.ChunkedIterator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Sender {
    private final File fileToTransfer;
    private final InetAddress receiver;
    private final int port;
    private final int chunkSize;
    private final long packetDelayUs;
    private final DatagramSocket socket;
    private short sequenceNumber = 0;

    public Sender(File fileToTransfer, InetAddress receiver, int port, int chunkSize, long packetDelayUs) throws IOException {
        this.fileToTransfer = fileToTransfer;
        this.receiver = receiver;
        this.port = port;
        this.chunkSize = chunkSize;
        this.packetDelayUs = packetDelayUs;
        this.socket = new DatagramSocket();
    }

    public void send() throws IOException, InterruptedException, NoSuchAlgorithmException {
        short uid = (short) new Random().nextInt(1, Short.MAX_VALUE + 1);
        Packet infoPacket = new Packet(
                uid,
                sequenceNumber++,
                new InitializePacketBody((int) fileToTransfer.length(), fileToTransfer.getName().toCharArray())
        );
        sendPacket(infoPacket);

        try (FileInputStream input = new FileInputStream(fileToTransfer)) {
            for (byte[] chunk : chunkedSequence(input, chunkSize)) {
                Packet dataPacket = new Packet(
                        uid,
                        sequenceNumber++,
                        new DataPacketBody(chunk)
                );
                sendPacket(dataPacket);
            }
        }

        // TODO: Calculate MD5 sum
        MessageDigest md = MessageDigest.getInstance("MD5");
        Packet finalizePacket = new Packet(
                uid,
                sequenceNumber++,
                new FinalizePacketBody(new char[1])
        );
        sendPacket(finalizePacket);
    }

    private Iterable<byte[]> chunkedSequence(FileInputStream input, int chunkSize) {
        return () -> new ChunkedIterator(input, chunkSize);
    }

    private void sendPacket(Packet packet) throws IOException, InterruptedException {
        if (packet.getPacketBody() instanceof InitializePacketBody) {
            log("snd inf at " + System.currentTimeMillis());
        } else if (packet.getPacketBody() instanceof FinalizePacketBody) {
            log("snd fin at " + System.currentTimeMillis());
        }

        // System.out.println("Sending Packet " + packet);
        byte[] bytes = packet.serialize();
        System.out.println(new String(bytes, StandardCharsets.UTF_8));
        DatagramPacket udpPacket = new DatagramPacket(bytes, bytes.length, receiver, port);
        socket.send(udpPacket);

        Thread.sleep(packetDelayUs / 1000, (int) (packetDelayUs % 1000) * 1000);

        System.out.println("Sent packet: ");
        System.out.println(packet);
    }

    private void log(String message) {
        System.out.println(message);
    }
}