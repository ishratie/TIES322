import java.io.*;
import java.net.*;

class ReceiverPosNeg {
    public static void main(String[] args) throws IOException {
        VirtualSocket vsoketti = new VirtualSocket(6666, 0.0, 0.0, 100, 0.3); // 30% bit-error
        int expectedSeq = 0;
        byte[] buf = new byte[256];

        while (true) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            vsoketti.receive(packet);

            InetAddress senderAddr = packet.getAddress();
            int senderPort = packet.getPort();
            int len = packet.getLength();

            if (!Frame.isValid(packet.getData(), len)) {
                System.out.println("[receiver] corrupt -> NAK");
                sendControl(vsoketti, "NAK", senderAddr, senderPort);
                continue;
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