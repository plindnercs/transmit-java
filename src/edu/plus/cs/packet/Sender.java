package edu.plus.cs.packet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
        byte uid = (byte) ThreadLocalRandom.current().nextInt();
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

    private class ChunkedIterator implements java.util.Iterator<byte[]> {
        private final FileInputStream input;
        private final byte[] buffer;
        private boolean closed = false;

        public ChunkedIterator(FileInputStream input, int chunkSize) {
            this.input = input;
            this.buffer = new byte[chunkSize];
        }

        @Override
        public boolean hasNext() {
            if (closed) {
                return false;
            }
            try {
                int read = input.read(buffer);
                if (read == -1) {
                    input.close();
                    closed = true;
                    return false;
                }
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        @Override
        public byte[] next() {
            return buffer;
        }
    }

    private void sendPacket(Packet packet) throws IOException, InterruptedException {
        if (packet.getPacketBody() instanceof InitializePacketBody) {
            log("snd inf at " + System.currentTimeMillis());
        } else if (packet.getPacketBody() instanceof FinalizePacketBody) {
            log("snd fin at " + System.currentTimeMillis());
        }

        // System.out.println("Sending Packet " + packet);
        byte[] bytes = packet.serialize();
        DatagramPacket udpPacket = new DatagramPacket(bytes, bytes.length, receiver, port);
        socket.send(udpPacket);

        Thread.sleep(packetDelayUs / 1000, (int) (packetDelayUs % 1000) * 1000);

        System.out.println("Sent packet: ");
        System.out.println(packet.toString());
    }

    private void log(String message) {
        System.out.println(message);
    }
}