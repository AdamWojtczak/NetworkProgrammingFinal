import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.Scanner;


public class TCPServer {
	public static void main(String argv[]) throws Exception {
//        boolean global_flag_end = false;
		int global_threads_counter = 0;
		Scanner scanner = new Scanner(System.in);
		System.out.println("Listening to port 7 by standard, do you want to change it? (y/n) ");
		String answer = scanner.nextLine();
		int port = 7;
		if (answer.equals("y") || answer.equals("Y")) {
			System.out.println("Insert port number: ");
			port = scanner.nextInt();
		}
		ServerSocket welcomeSocket = new ServerSocket(port);
		welcomeSocket.setReuseAddress(true);
		while (true) {
			System.out.println("Waiting for Client...");
			global_threads_counter++;
			Socket connectionSocket = welcomeSocket.accept();
			if (global_threads_counter > 3) {
				System.out.println("There are 3 clients connected. No possibility to maintain another connection.");
				DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
				outToClient.write(-1);
				outToClient.close();
				connectionSocket.close();
			} else {
				ServerThread serverThread = new ServerThread(connectionSocket);
				new Thread(serverThread).start();
			}
		}
	}
}

class ServerThread implements Runnable {
	boolean thread_flag_end;
	Socket connectionSocket;

	ServerThread(Socket connectionSocket) {
		this.connectionSocket = connectionSocket;
	}

	@Override
	public void run() {
		thread_flag_end = false;
		try {
			System.out.println("Client " + connectionSocket.toString() + " connected!");
			DataInputStream inFromClient = new DataInputStream(connectionSocket.getInputStream());
			DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

			while (!thread_flag_end) {
				try {
					if (inFromClient.available() != 0) {
						int clientsQuery = inFromClient.read();
						if (!connectionSocket.isConnected()) {
							System.out.println("Connection lost :(");
							thread_flag_end = true;
						} else {
							System.out.println("Received: " + clientsQuery);
							long now = LocalDateTime.now().getLong(ChronoField.NANO_OF_DAY);
							outToClient.writeLong(now);
							System.out.println("Sent: " + now);
						}
						if(clientsQuery < 0) {
							thread_flag_end = true;
							System.out.println("Connection was terminated");
							outToClient.close();
							connectionSocket.close();
							inFromClient.close();
						}
					}
				} catch (Exception e) {
					System.out.println("Connection lost :(");
					thread_flag_end = true;
					outToClient.close();
					connectionSocket.close();
					inFromClient.close();
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			try {
				connectionSocket.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}
