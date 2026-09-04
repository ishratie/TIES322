import java.io.*;
import java.net.*;

class TestiServer {
    public static void main(String[] args) throws IOException {
        DatagramSocket soketti = new DatagramSocket(6666);
        byte[] rec = new byte[256];
        while (true) {
            DatagramPacket paketti = new DatagramPacket(rec, rec.length);
            soketti.receive(paketti);
            System.out.println(new String(rec, 0, paketti.getLength()));
        }
    }
}