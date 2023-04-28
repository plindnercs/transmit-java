package edu.plus.cs;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;

public class Main {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InterruptedException {
        File file = new File("test.txt");
        Sender sender = new Sender(file, InetAddress.getLocalHost(), 1337, 128, 1);

        sender.send();
    }
}