import java.io.*;
import java.net.*;

class ReceiverPosOnly {
    public static void main(String[] args) throws IOException {
        VirtualSocket vsoketti = new VirtualSocket(6666, 0.0, 0.0, 100, 0.3); // bit-error 30%
        int expectedSeq = 0;
        byte[] buf = new byte[256];

        while (true) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            vsoketti.receive(packet);

            InetAddress senderAddr = packet.getAddress();
            int senderPort = packet.getPort();
            int len = packet.getLength();

            if (!Frame.isValid(packet.getData(), len)) {
                System.out.println("[receiver] corrupt -> silently discard (no NAK)");
                continue; // positive-ACK-only: stay silent, no NAK
            }

            int seq = Frame.getSeq(packet.getData());
            if (seq == expectedSeq) {
                byte[] data = Frame.getData(packet.getData(), len);
                System.out.println(">>> " + new String(data));
                sendControl(vsoketti, "ACK" + seq, senderAddr, senderPort);
                expectedSeq = 1 - expectedSeq;
            } else {
                System.out.println("[receiver] duplicate seq " + seq + " -> re-ACK");
                sendControl(vsoketti, "ACK" + seq, senderAddr, senderPort);
            }
        }
    }

    static void sendControl(VirtualSocket vsoketti, String msg, InetAddress addr, int port) throws IOException {
        byte[] data = msg.getBytes();
        vsoketti.send(new DatagramPacket(data, data.length, addr, port));
    }
}