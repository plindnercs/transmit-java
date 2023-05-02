package edu.plus.cs;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;

public class Main {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InterruptedException {
        if (args.length != 3) {
            System.err.println("Usage: <transmissionId> <fileName> <port>");
            return;
        }

        short transmissionId = Short.parseShort(args[0]);
        File file = new File(args[1]);
        int port = Integer.parseInt(args[2]);

        Sender sender = new Sender(transmissionId, file, InetAddress.getLocalHost(), port, 1394, 0);

        sender.send();
    }
}