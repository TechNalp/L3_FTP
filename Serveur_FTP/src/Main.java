/*
 * TP JAVA RIP
 * Min Serveur FTP
 * */

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.TreeMap;


public class Main {

	static String userDataPath = "./USERS/";

	public static void main(String[] args) throws Exception {
		System.out.println("Le Serveur FTP");
		boolean acceptNewClient = true;
		ServerSocket serveurFTP = new ServerSocket(2121);
		while(acceptNewClient) {
			System.out.println("[MAIN] Attente client...");
			Socket socket = serveurFTP.accept();
			System.out.println("[MAIN] Client connecté !");

			Thread serverThread = new Thread(new Server(socket));
			serverThread.start();

		}

		serveurFTP.close();

	}

}
