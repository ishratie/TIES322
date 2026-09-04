import java.io.*;
import java.net.*;

class TestiClient {
    public static void main(String[] args) throws IOException {
        DatagramSocket soketti = new DatagramSocket();
        InetAddress addr = InetAddress.getByName("localhost");
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = input.readLine()) != null) {
            byte[] data = line.getBytes();
            DatagramPacket paketti = new DatagramPacket(data, data.length, addr, 6666);
            soketti.send(paketti);
        }
    }
}