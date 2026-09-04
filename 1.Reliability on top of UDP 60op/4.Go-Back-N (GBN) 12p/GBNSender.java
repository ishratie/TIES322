import java.io.*;
import java.net.*;
import java.util.*;

class GBNSender {
    static final int WINDOW_SIZE = 4;
    static final int TIMEOUT_MS = 1000;

    static DatagramSocket soketti;
    static InetAddress addr;
    static int receiverPort = 6666;

    static int base = 0;
    static int nextSeqNum = 0;
    static Map<Integer, byte[]> window = new HashMap<>();
    static final Object lock = new Object();
    static Timer timer = new Timer(true);
    static TimerTask currentTask = null;

    public static void main(String[] args) throws IOException {
        soketti = new DatagramSocket(5555);
        addr = InetAddress.getByName("localhost");

        Thread ackListener = new Thread(() -> {
            byte[] buf = new byte[16];
            while (true) {
                try {
                    DatagramPacket p = new DatagramPacket(buf, buf.length);
                    soketti.receive(p);
                    String msg = new String(p.getData(), 0, p.getLength());
                    if (msg.startsWith("ACK")) {
                        int ackNum = Integer.parseInt(msg.substring(3));
                        synchronized (lock) {
                            if (ackNum >= base) {
                                System.out.println("[sender] cumulative ACK " + ackNum);
                                for (int s = base; s <= ackNum; s++) window.remove(s);
                                base = ackNum + 1;
                                if (base == nextSeqNum) {
                                    stopTimer();
                                } else {
                                    restartTimer();
                                }
                            }
                        }
                    }
                } catch (IOException e) {}
            }
        });
        ackListener.setDaemon(true);
        ackListener.start();

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = input.readLine()) != null) {
            while (true) {
                synchronized (lock) {
                    if (nextSeqNum - base < WINDOW_SIZE) break;
                }
                try { Thread.sleep(20); } catch (InterruptedException e) {}
            }

            synchronized (lock) {
                int seq = nextSeqNum;
                byte[] frame = Frame.build(seq % 256, line.getBytes());
                window.put(seq, frame);
                sendFrame(seq, frame);
                if (base == seq) startTimer();
                nextSeqNum++;
            }
        }
    }

    static void sendFrame(int seq, byte[] frame) throws IOException {
        System.out.println("[sender] sending seq " + seq);
        soketti.send(new DatagramPacket(frame, frame.length, addr, receiverPort));
    }

    static void startTimer() {
        currentTask = new TimerTask() {
            public void run() {
                synchronized (lock) {
                    System.out.println("[sender] TIMEOUT - resending window " + base + ".." + (nextSeqNum - 1));
                    for (int s = base; s < nextSeqNum; s++) {
                        try { sendFrame(s, window.get(s)); } catch (IOException e) {}
                    }
                }
                restartTimer();
            }
        };
        timer.schedule(currentTask, TIMEOUT_MS);
    }

    static void stopTimer() {
        if (currentTask != null) currentTask.cancel();
        currentTask = null;
    }

    static void restartTimer() {
        stopTimer();
        startTimer();
    }
}