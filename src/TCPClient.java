import java.io.*;
import java.net.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.Scanner;

import static java.lang.Thread.sleep;

class TCPClient {
	static Socket clientSocket = null;
	static DataOutputStream outToServer = null;
	static BufferedReader inFromUser = null;
	static DataInputStream inFromServer = null;
	static boolean global_flag_end = true;
	static Scanner scanner = null;
	private String answer;
	static int frequency = 1000;

	public static void main(String argv[]) throws Exception {
		startConnection("localhost");
		int dateLengthInBytes = String.valueOf(now()).length();
		while (global_flag_end) {
			long timeBefore = getNanos(now());
			askForTime();

			long timeFromServer = getMessage(dateLengthInBytes);
			LocalDateTime timeAfter = now();
			long timeAfterInNanos = getNanos(timeAfter);
			long delta  = timeFromServer + (timeAfterInNanos - timeBefore)/2 - timeAfterInNanos;
			if (delta >= 0) {
				DecimalFormat df = new DecimalFormat("##0.000000");
				System.out.println("Delta: " + df.format(delta/Double.valueOf(1000000)) + "ms");
				System.out.println("Delta in long: " + delta + "ms");

				LocalDateTime finalTime = timeAfter.plusNanos(delta);

				System.out.println(isoFormatDateTime(finalTime));
			}

			waitFor(frequency);
		}
	}


	private static String isoFormatDateTime(LocalDateTime time) {
		SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		return isoDateFormat.format(new Date(time.get(ChronoField.MILLI_OF_DAY) + time.getLong(ChronoField.EPOCH_DAY) * 86400000));
	}

	private static void startConnection(String ip) {
		int port = 7;
		try {
			scanner = new Scanner(System.in);
			port = getPort();
			frequency = getFrequency();

		} catch (Exception e) {
			System.out.println("Error caught while getting port");
		}
		try {
			clientSocket = new Socket(ip, port);
		} catch (Exception e) {
			System.out.println("Error caught while creating socket");
			stopConnection();
		}
		try {
			inFromUser = new BufferedReader(new InputStreamReader(System.in));
			outToServer = new DataOutputStream(clientSocket.getOutputStream());
			inFromServer = new DataInputStream(clientSocket.getInputStream());
		} catch (Exception e) {
			System.out.println("Error caught while creating streams");
		}
	}

	private static int getPort() {
		System.out.println("Listening to port 7 by standard, do you want to change it?: (y/n) ");
		String answer = scanner.nextLine();
		int port = 7;
		if (answer.equals("y") || answer.equals("Y")) {
			System.out.println("Insert port number: ");
			port = scanner.nextInt();
		}
		return port;
	}

	private static int getFrequency() {
		int frequency;
		System.out.println("Insert frequency number (10 - 1000)ms: ");
		frequency = scanner.nextInt();
		if (frequency > 1000 || frequency < 10) {
			System.out.println("Invalid frequency value, frequency wll be set to: 1000");
			frequency = 1000;
		}
		return frequency;
	}

	private static void waitFor( int frequency) {
		try {
			sleep(frequency);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static LocalDateTime now() {
		LocalDateTime now = LocalDateTime.now();
		return now;
	}

	private static long getNanos(LocalDateTime time) {
		long now = time.getLong(ChronoField.NANO_OF_DAY);
		return now;
	}

	public static void askForTime() throws IOException {
//		System.out.println("Pytam o godzine serwera ");
		System.out.println("+-----------------------------------+");
		outToServer.write(1);
	}

	public static long getMessage(int bytesSent) throws IOException {
		try {
			long serverTime = inFromServer.readLong();
			if (serverTime < 0) {
				stopConnection();
			}
			return serverTime;
		} catch(Exception e) {
			System.out.println("Server did not allow the connection");
			stopConnection();
		}
		return 0;
	}

	public static void stopConnection() {
		global_flag_end = false;
		try {
			if (inFromUser != null)
				inFromUser.close();
			if (inFromServer != null)
				inFromServer.close();
			if (outToServer != null) {
				outToServer.write(-1);
				outToServer.close();
			}
			if (clientSocket != null)
				clientSocket.close();
		} catch (IOException e) {
			System.out.println("Error caught while closing streams and socket");
		}

	}

}
