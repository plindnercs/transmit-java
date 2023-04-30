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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

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
        // get random transmissionId
        short transmissionId = (short) new Random().nextInt(1, Short.MAX_VALUE + 1);

        // send first (initialize) packet
        Packet infoPacket = new Packet(transmissionId,sequenceNumber++,
                new InitializePacketBody((int) fileToTransfer.length(), fileToTransfer.getName().toCharArray()));
        System.out.println("snd inf at " + System.currentTimeMillis());
        sendPacket(infoPacket);

        // send data packets while computing the md5 hash
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (FileInputStream input = new FileInputStream(fileToTransfer)) {
            for (ChunkedIterator it = new ChunkedIterator(input, chunkSize); it.hasNext(); ) {
                byte[] chunk = it.next();
                Packet dataPacket = new Packet(transmissionId, sequenceNumber++, new DataPacketBody(chunk));
                sendPacket(dataPacket);
                md.update(chunk);
            }
        }

        // send last (finalize) packet
        Packet finalizePacket = new Packet(transmissionId,sequenceNumber++,new FinalizePacketBody(md.digest()));
        System.out.println(("snd fin at " + System.currentTimeMillis()));
        sendPacket(finalizePacket);
    }

    private void sendPacket(Packet packet) throws IOException, InterruptedException {
        // convert packet to byte[]
        byte[] bytes = packet.serialize();

        // System.out.println(new String(bytes, StandardCharsets.UTF_8));

        DatagramPacket udpPacket = new DatagramPacket(bytes, bytes.length, receiver, port);
        socket.send(udpPacket);

        Thread.sleep(packetDelayUs / 1000, (int) (packetDelayUs % 1000) * 1000);

        System.out.println("Sent packet: ");
        System.out.println(packet);
    }
}