import java.io.*;
import java.net.*;

class SenderPosNeg {
    public static void main(String[] args) throws IOException {
        DatagramSocket soketti = new DatagramSocket(5555);
        InetAddress addr = InetAddress.getByName("localhost");
        int receiverPort = 6666;
        int seq = 0;

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = input.readLine()) != null) {
            byte[] frame = Frame.build(seq, line.getBytes());
            boolean acked = false;
            while (!acked) {
                soketti.send(new DatagramPacket(frame, frame.length, addr, receiverPort));
                System.out.println("[sender] sent seq " + seq);

                byte[] respBuf = new byte[16];
                DatagramPacket respPacket = new DatagramPacket(respBuf, respBuf.length);
                soketti.receive(respPacket);
                String resp = new String(respPacket.getData(), 0, respPacket.getLength());
                System.out.println("[sender] got " + resp);

                if (resp.equals("ACK" + seq)) {
                    acked = true;
                    seq = 1 - seq;
                } else {
                    System.out.println("[sender] resending");
                }
            }
        }
    }
}