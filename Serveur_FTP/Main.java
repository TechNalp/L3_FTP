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


public class Main {

	static String userDataPath = "./USERS/";

	public static void main(String[] args) throws Exception {
		System.out.println("Le Serveur FTP");
		boolean acceptNewClient = true;
		ServerSocket serveurFTP = new ServerSocket(2121);
		while(acceptNewClient) {
			System.out.println("Attente client...");
			Socket socket = serveurFTP.accept();
			System.out.println("Client connecté !");
			try {

				BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintStream ps = new PrintStream(socket.getOutputStream());

				ps.println("1 Bienvenue ! ");
				ps.println("1 Serveur FTP Personnel.");
				ps.println("0 Authentification : ");

				String commande = "";

				// Attente de reception de commandes et leur execution

				while (!(commande = br.readLine()).equals("bye")) {
					System.out.println(">> " + commande);
					CommandExecutor.executeCommande(ps, commande);
				}

				System.out.println("\nClient déconnecté\n");
				Commande.CWD = new File(".").getCanonicalPath();
				CommandExecutor.userOk = false;
				CommandExecutor.pwOk = false;
				socket.close();
				continue;

			} catch (SocketException e) {
				System.out.println("\nClient déconnecté\n");
				Commande.CWD = new File(".").getCanonicalPath();
				CommandExecutor.userOk = false;
				CommandExecutor.pwOk = false;
				socket.close();
				continue;

			}

		}

		serveurFTP.close();

	}

}
