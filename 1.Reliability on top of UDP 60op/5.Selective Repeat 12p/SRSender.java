import java.io.*;
import java.net.*;
import java.util.*;

class SRSender {
    static final int WINDOW_SIZE = 4;
    static final int TIMEOUT_MS = 1000;

    static DatagramSocket soketti;
    static InetAddress addr;
    static int receiverPort = 6666;

    static int base = 0;
    static int nextSeqNum = 0;
    static Map<Integer, byte[]> window = new HashMap<>();
    static Map<Integer, Boolean> acked = new HashMap<>();
    static Map<Integer, TimerTask> timers = new HashMap<>();
    static final Object lock = new Object();
    static Timer timer = new Timer(true);

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
                            if (ackNum >= base && window.containsKey(ackNum) && !Boolean.TRUE.equals(acked.get(ackNum))) {
                                System.out.println("[sender] individual ACK " + ackNum);
                                acked.put(ackNum, true);
                                cancelTimer(ackNum);
                                while (Boolean.TRUE.equals(acked.get(base))) {
                                    window.remove(base);
                                    acked.remove(base);
                                    base++;
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
                acked.put(seq, false);
                sendFrame(seq, frame);
                startTimer(seq);
                nextSeqNum++;
            }
        }
    }

    static void sendFrame(int seq, byte[] frame) throws IOException {
        System.out.println("[sender] sending seq " + seq);
        soketti.send(new DatagramPacket(frame, frame.length, addr, receiverPort));
    }

    static void startTimer(int seq) {
        TimerTask task = new TimerTask() {
            public void run() {
                synchronized (lock) {
                    if (!Boolean.TRUE.equals(acked.get(seq)) && window.containsKey(seq)) {
                        System.out.println("[sender] TIMEOUT seq " + seq + " - resending just this packet");
                        try { sendFrame(seq, window.get(seq)); } catch (IOException e) {}
                        startTimer(seq);
                    }
                }
            }
        };
        timers.put(seq, task);
        timer.schedule(task, TIMEOUT_MS);
    }

    static void cancelTimer(int seq) {
        TimerTask t = timers.remove(seq);
        if (t != null) t.cancel();
    }
}