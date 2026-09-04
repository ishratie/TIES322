import java.io.*;
import java.net.*;
import java.util.Random;

class VirtualSocket {
    private DatagramSocket realSocket;
    private double dropProbability;
    private Random rand = new Random();

    public VirtualSocket(int port, double dropProbability) throws SocketException {
        this.realSocket = new DatagramSocket(port);
        this.dropProbability = dropProbability;
    }

    public void receive(DatagramPacket packet) throws IOException {
        while (true) {
            realSocket.receive(packet);
            //t2
            if (rand.nextDouble() < dropProbability) {
                System.out.println("[dropped]");
                continue;
            }
            return; 
        }
    }

    public void send(DatagramPacket packet) throws IOException {
        realSocket.send(packet);
    }
}