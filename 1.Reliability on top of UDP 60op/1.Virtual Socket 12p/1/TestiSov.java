import java.io.*;
import java.net.*;

class TestiSov {
	private static DatagramSocket soketti = null;

	public static void main(String[] args) throws IOException {
		soketti = new DatagramSocket(6666);
		boolean listening = true;
		while (listening) {
			try {
				byte[] rec = new byte[256];
				DatagramPacket paketti = new DatagramPacket(rec, rec.length);
				soketti.receive(paketti);
				System.out.println("a ", new String (rec, 0, paketti.getLength()));
			}
			catch (Exception e) {
				listening = false;
				System.out.println("catch");
				break;
			}
		}
	}
}