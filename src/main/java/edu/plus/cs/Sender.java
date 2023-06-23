package edu.plus.cs;

import edu.plus.cs.packet.*;
import edu.plus.cs.packet.util.PacketInterpreter;
import edu.plus.cs.util.ChunkedIterator;
import edu.plus.cs.util.OperatingMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class Sender {
    private final short transmissionId;
    private final File fileToTransfer;
    private final InetAddress receiver;
    private final int port;
    private final int chunkSize;
    private final int ackPort;
    private final DatagramSocket socket;
    private int sequenceNumber = 0;
    private OperatingMode operatingMode;
    private int windowSize;
    private Map<Integer, Packet> windowBuffer;

    public Sender(short transmissionId, File fileToTransfer, InetAddress receiver, int port, int chunkSize,
                  int ackPort, OperatingMode operatingMode, int windowSize) throws IOException {
        this.transmissionId = transmissionId;
        this.fileToTransfer = fileToTransfer;
        this.receiver = receiver;
        this.port = port;
        this.chunkSize = chunkSize;
        this.ackPort = ackPort;
        this.socket = (operatingMode != OperatingMode.NO_ACK) ? new DatagramSocket(this.ackPort) : new DatagramSocket();
        this.socket.setSoTimeout(5000);
        this.operatingMode = operatingMode;
        this.windowSize = windowSize;
        this.windowBuffer = new HashMap<>();
    }

    public void send() throws IOException, NoSuchAlgorithmException, InterruptedException {
        // calculate maxSequenceNumber
        int maxSequenceNumber = (int) Math.ceilDiv(fileToTransfer.length(), chunkSize);

        // send first (initialize) packet
        Packet initializePacket = new InitializePacket(transmissionId, sequenceNumber++, maxSequenceNumber,
                fileToTransfer.getName().toCharArray());
        System.out.println("Sent initialize packet at: " + System.currentTimeMillis());
        sendPacket(initializePacket);

        // only check for acknowledgement if the operating mode requires to
        if (operatingMode == OperatingMode.STOP_WAIT && !handleAcknowledgementPacket()) {
            return;
        }

        // check for SLIDING_WINDOW acknowledgement
        if (operatingMode == OperatingMode.SLIDING_WINDOW && windowBuffer.size() == windowSize) {
            while(!handleSlidingWindowAcknowledgement());
        }

        // send data packets while computing the md5 hash
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (FileInputStream input = new FileInputStream(fileToTransfer)) {
            for (ChunkedIterator it = new ChunkedIterator(input, chunkSize); it.hasNext(); ) {
                byte[] chunk = it.next();
                Packet dataPacket = new DataPacket(transmissionId, sequenceNumber++, chunk);
                sendPacket(dataPacket);
                md.update(chunk);

                // only check for acknowledgement if the operating mode requires to
                if (operatingMode == OperatingMode.STOP_WAIT && !handleAcknowledgementPacket()) {
                    return;
                }

                // check for SLIDING_WINDOW acknowledgement
                if (operatingMode == OperatingMode.SLIDING_WINDOW && windowBuffer.size() == windowSize) {
                    while(!handleSlidingWindowAcknowledgement());
                }
            }
        }

        // send last (finalize) packet
        Packet finalizePacket = new FinalizePacket(transmissionId, sequenceNumber++, md.digest());
        System.out.println(("Sent finalize packet at: " + System.currentTimeMillis()));
        sendPacket(finalizePacket);

        // only check for acknowledgement if the operating mode requires to
        if (operatingMode == OperatingMode.STOP_WAIT && !handleAcknowledgementPacket()) {
            return;
        }

        // check for SLIDING_WINDOW acknowledgement
        if (operatingMode == OperatingMode.SLIDING_WINDOW) {
            while(!handleSlidingWindowAcknowledgement());
        }

        this.socket.close();
    }

    private boolean handleSlidingWindowAcknowledgement() throws IOException {
        byte[] buffer = new byte[65535];

        DatagramPacket udpPacket;
        try {
            // receive first acknowledgement
            udpPacket = new DatagramPacket(buffer, buffer.length);
            this.socket.receive(udpPacket);

            if (!checkAcknowledgementPacket(udpPacket)) {
                // duplicate acknowledgement received, resend packet
                udpPacket = new DatagramPacket(buffer, buffer.length);
                this.socket.receive(udpPacket);

                sendPacket(windowBuffer.get(PacketInterpreter.getSequenceNumber(udpPacket.getData())));

                return false;
            } else { // received cumulative acknowledgement
                windowBuffer.clear();

                return true;
            }
        } catch (SocketTimeoutException ex) {
            System.err.println("Did not receive acknowledgement packet in time, abort transmission");
            this.socket.close();
            return true;
        }
    }

    private void sendPacket(Packet packet) throws IOException {
        if (operatingMode == OperatingMode.SLIDING_WINDOW) {
            windowBuffer.put(packet.getSequenceNumber(), packet);
        }

        // convert packet to byte[]
        byte[] bytes = packet.serialize();

        DatagramPacket udpPacket = new DatagramPacket(bytes, bytes.length, receiver, port);
        socket.send(udpPacket);

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
        DatagramPacket udpPacket;
        try {
            udpPacket = new DatagramPacket(buffer, buffer.length);
            this.socket.receive(udpPacket);
        } catch (SocketTimeoutException ex) {
            System.err.println("Did not receive acknowledgement packet in time, abort transmission");
            this.socket.close();
            return false;
        }

        if (!checkAcknowledgementPacket(udpPacket)) {
            System.err.println("Did not receive valid acknowledgement packet, abort transmission");
            this.socket.close();
            return false;
        }

        return true;
    }
}