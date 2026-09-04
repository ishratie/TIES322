import java.io.*;
import java.net.*;
import java.util.Random;

class TestiServer {
    public static void main(String[] args) throws IOException {
        VirtualSocket soketti = new VirtualSocket(6666, 0.3, 0.5, 500, 0.5);
        byte[] rec = new byte[256];
        while (true) {
            DatagramPacket paketti = new DatagramPacket(rec, rec.length);
            soketti.receive(paketti);
            System.out.println(new String(rec, 0, paketti.getLength()));
        }
    }
}