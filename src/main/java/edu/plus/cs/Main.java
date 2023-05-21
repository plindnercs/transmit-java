package edu.plus.cs;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;

public class Main {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InterruptedException {
        if (args.length != 5) {
            System.err.println("Usage: <transmissionId> <ip> <port> <fileName> <packetSize>");
            return;
        }

        short transmissionId = Short.parseShort(args[0]);
        String ipAddress = args[1];
        int port = Integer.parseInt(args[2]);
        File file = new File(args[3]);
        int packetSize = Integer.parseInt(args[4]);

        Sender sender = new Sender(transmissionId, file, InetAddress.getByName(ipAddress), port,
                packetSize, 0);

        sender.send();
    }
}