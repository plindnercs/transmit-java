package edu.plus.cs;

import edu.plus.cs.util.OperatingMode;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;

public class Main {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InterruptedException {
        if (args.length > 0) {
            int operatingModeCode = Integer.parseInt(args[0]);
            OperatingMode operatingMode = parseOperatingMode(operatingModeCode);

            if (operatingMode == OperatingMode.NO_ACK && args.length != 6) {
                System.err.println("Usage: <operatingMode> <transmissionId> <ip> <port> <fileName> <packetSize>");
                return;
            } else if (operatingMode == OperatingMode.STOP_WAIT && args.length != 7) {
                System.err.println("Usage: <operatingMode> <transmissionId> <ip> <port> <fileName> " +
                        "<packetSize> <ackPort>");
                return;
            } else if (operatingMode == OperatingMode.SLIDING_WINDOW && args.length != 8) {
                System.err.println("Usage: <operatingMode> <transmissionId> <ip> <port> <fileName> " +
                        "<packetSize> <ackPort> <windowSize>");
                return;
            }

            short transmissionId = Short.parseShort(args[1]);
            String ipAddress = args[2];
            int port = Integer.parseInt(args[3]);
            File file = new File(args[4]);
            int packetSize = Integer.parseInt(args[5]);
            int ackPort = (operatingMode != OperatingMode.NO_ACK) ? Integer.parseInt(args[6]) : -1;
            int windowSize = (operatingMode == OperatingMode.SLIDING_WINDOW) ? Integer.parseInt(args[7]) : -1;

            Sender sender = new Sender(transmissionId, file, InetAddress.getByName(ipAddress), port,
                    packetSize, ackPort, operatingMode, windowSize);

            sender.send();
        }
    }

    private static OperatingMode parseOperatingMode(int operatingModeCode) {
        switch (operatingModeCode) {
            case 0:
                return OperatingMode.NO_ACK;
            case 1:
                return OperatingMode.STOP_WAIT;
            case 2:
                return OperatingMode.SLIDING_WINDOW;
            default:
                System.err.println("No valid operating mode provided, set to NO_ACK as default!");
                return OperatingMode.NO_ACK;
        }
    }
}