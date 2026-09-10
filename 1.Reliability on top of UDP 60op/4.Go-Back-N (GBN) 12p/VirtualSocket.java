import java.io.*;
import java.net.*;
import java.util.Random;

class VirtualSocket {
    private DatagramSocket realSocket;
    private double dropProbability;
    private double delayProbability;   
    private int maxDelayMs;
    private double bitErrorProbability;

    private Random rand = new Random();

    public VirtualSocket(int port, double dropProbability, double delayProbability, int maxDelayMs, double bitErrorProbability) throws SocketException {
        this.realSocket = new DatagramSocket(port);
        this.dropProbability = dropProbability;
        this.delayProbability = delayProbability;
        this.maxDelayMs = maxDelayMs;
        this.bitErrorProbability = bitErrorProbability;
    }

    public void receive(DatagramPacket packet) throws IOException {
        while (true) {
            realSocket.receive(packet);
            if (rand.nextDouble() < dropProbability) {
                System.out.println("[dropped]");
                continue;
            }
            //t3
            if (rand.nextDouble() < delayProbability) {
                int delay = rand.nextInt(maxDelayMs);
                System.out.println("[delayed]" + delay + "ms");
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e){}
            }
            //t4 
            if (rand.nextDouble() < bitErrorProbability) {
                int len = packet.getLength();
                if (len > 0) {
                    byte[] data = packet.getData();
                    int byteIndex = rand.nextInt(len);
                    int bitIndex = rand.nextInt(8);
                    int mask = 1 << bitIndex;
                    data[byteIndex] = (byte) (data[byteIndex] ^ mask);
                    System.out.println("[bit error] byte " + byteIndex + " bit " + bitIndex);
                }
            }
            return; 
        }
    }

    public void send(DatagramPacket packet) throws IOException {
        realSocket.send(packet);
    }
}