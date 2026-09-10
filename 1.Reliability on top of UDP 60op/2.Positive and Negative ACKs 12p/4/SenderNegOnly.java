import java.io.*;
import java.net.*;

class SenderNegOnly {
    public static void main(String[] args) throws IOException {
        DatagramSocket soketti = new DatagramSocket(5555);
        soketti.setSoTimeout(300); // ms — if nothing arrives, assume success
        InetAddress addr = InetAddress.getByName("localhost");
        int receiverPort = 6666;
        int seq = 0;

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = input.readLine()) != null) {
            byte[] frame = Frame.build(seq, line.getBytes());
            boolean done = false;
            while (!done) {
                soketti.send(new DatagramPacket(frame, frame.length, addr, receiverPort));
                System.out.println("[sender] sent seq " + seq);

                try {
                    byte[] respBuf = new byte[16];
                    DatagramPacket respPacket = new DatagramPacket(respBuf, respBuf.length);
                    soketti.receive(respPacket); // blocks up to timeout
                    String resp = new String(respPacket.getData(), 0, respPacket.getLength());

                    if (resp.equals("NAK")) {
                        System.out.println("[sender] got NAK, resending...");
                    } else {
                        System.out.println("[sender] got unexpected reply, resending...");
                    }
                } catch (SocketTimeoutException e) {
                    // no NAK arrived -> assume success
                    System.out.println("[sender] no NAK within timeout, assuming delivered");
                    done = true;
                    seq = 1 - seq;
                }
            }
        }
    }
}