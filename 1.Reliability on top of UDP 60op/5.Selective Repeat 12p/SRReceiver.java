import java.io.*;
import java.net.*;
import java.util.*;

class SRReceiver {
    static final int WINDOW_SIZE = 4;

    public static void main(String[] args) throws IOException {
        VirtualSocket vsoketti = new VirtualSocket(6666, 0.2, 0.3, 400, 0.2);
        int rcvBase = 0;
        Map<Integer, byte[]> buffer = new HashMap<>();
        byte[] buf = new byte[256];

        while (true) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            vsoketti.receive(packet);

            InetAddress senderAddr = packet.getAddress();
            int senderPort = packet.getPort();
            int len = packet.getLength();

            if (!Frame.isValid(packet.getData(), len)) {
                System.out.println("[receiver] corrupt -> ignore, no ACK");
                continue;
            }

            int seq = Frame.getSeq(packet.getData());

            if (seq >= rcvBase && seq < rcvBase + WINDOW_SIZE) {
                byte[] data = Frame.getData(packet.getData(), len);
                if (!buffer.containsKey(seq)) {
                    buffer.put(seq, data);
                    System.out.println("[receiver] buffered seq " + seq);
                }
                sendAck(vsoketti, seq, senderAddr, senderPort);

                while (buffer.containsKey(rcvBase)) {
                    byte[] d = buffer.remove(rcvBase);
                    System.out.println(">>> [" + rcvBase + "] " + new String(d));
                    rcvBase++;
                }
            } else if (seq < rcvBase && seq >= rcvBase - WINDOW_SIZE) {
                System.out.println("[receiver] duplicate seq " + seq + " -> re-ACK");
                sendAck(vsoketti, seq, senderAddr, senderPort);
            } else {
                System.out.println("[receiver] seq " + seq + " outside window -> ignore");
            }
        }
    }

    static void sendAck(VirtualSocket vsoketti, int ackNum, InetAddress addr, int port) throws IOException {
        String msg = "ACK" + ackNum;
        byte[] data = msg.getBytes();
        vsoketti.send(new DatagramPacket(data, data.length, addr, port));
    }
}