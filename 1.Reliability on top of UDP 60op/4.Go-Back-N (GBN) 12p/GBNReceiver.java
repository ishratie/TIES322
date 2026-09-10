import java.io.*;
import java.net.*;

class GBNReceiver {
    public static void main(String[] args) throws IOException {
        VirtualSocket vsoketti = new VirtualSocket(6666, 0.2, 0.3, 400, 0.2);
        int expectedSeqNum = 0;
        byte[] buf = new byte[256];

        while (true) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            vsoketti.receive(packet);

            InetAddress senderAddr = packet.getAddress();
            int senderPort = packet.getPort();
            int len = packet.getLength();

            if (!Frame.isValid(packet.getData(), len)) {
                System.out.println("[receiver] corrupt -> re-ACK " + (expectedSeqNum - 1));
                sendAck(vsoketti, expectedSeqNum - 1, senderAddr, senderPort);
                continue;
            }

            int seq = Frame.getSeq(packet.getData());
            if (seq == (expectedSeqNum % 256)) {
                byte[] data = Frame.getData(packet.getData(), len);
                System.out.println(">>> [" + seq + "] " + new String(data));
                sendAck(vsoketti, seq, senderAddr, senderPort);
                expectedSeqNum++;
            } else {
                System.out.println("[receiver] out-of-order seq " + seq + " expected " + (expectedSeqNum % 256) + " -> re-ACK " + (expectedSeqNum - 1));
                sendAck(vsoketti, expectedSeqNum - 1, senderAddr, senderPort);
            }
        }
    }

    static void sendAck(VirtualSocket vsoketti, int ackNum, InetAddress addr, int port) throws IOException {
        if (ackNum < 0) return;
        String msg = "ACK" + ackNum;
        byte[] data = msg.getBytes();
        vsoketti.send(new DatagramPacket(data, data.length, addr, port));
    }
}