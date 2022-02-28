package fr.l3.application.client_ftp;

import fr.l3.application.client_ftp.controller.ConnexionController;
import fr.l3.application.client_ftp.service.CommunicationService;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class Client {

	static private BufferedReader rd = null;
	static private PrintStream ps = null;
	static private Socket sck_cmd = null;
	static String hostname ="";
	static int port = 0;
	
	public static String lireClavier() throws IOException {
		String str = new BufferedReader(new InputStreamReader(System.in)).readLine();
		return str;
	}
	
	public static Socket getSocket() {
		return Client.sck_cmd;
	}
	
	
	public static void connexionServeur(String hostname, int port) throws IOException {
		try {
			Client.sck_cmd = new Socket();
			Client.sck_cmd.connect(new InetSocketAddress(hostname,port),1000);

		}catch (IOException e){
			MainApp.getConsoleController().addError("Impossible de se connecter à l'adresse : "+hostname+":"+port);
			MainApp.getConnexionController().stopConnexion();
		}

		Client.rd = new BufferedReader(new InputStreamReader(Client.sck_cmd.getInputStream()));
		Client.ps = new PrintStream(Client.sck_cmd.getOutputStream());
		Client.hostname = hostname;
		Client.port = port;
	}
	
	public static void deconnexionServeur() throws IOException {
		Client.sck_cmd.close();
		MainApp.getConnexionController().stopConnexion();
		MainApp.getConsoleController().addText("Déconnexion du serveur réussi");
	}
	
	public static String ecouterServeur() throws IOException{
		return Client.rd.readLine();
	}
	
	public static void analyseCmdSend(String cmd, String rep_serv) throws UnknownHostException, IOException {

		if(cmd.toLowerCase().startsWith("bye")){
			Client.deconnexionServeur();
			MainApp.getConsoleController().addText("Déconnexion Réussi");
			System.exit(0);
		}

		if(rep_serv.startsWith("2") || rep_serv.isBlank() || cmd.isBlank()) {
			return;
		}
		if(cmd.toLowerCase().startsWith("get")){
			Client.receptionFichier(new File(cmd.split(" ")[1]).getName(), Integer.parseInt(rep_serv.split(" ")[1]));
			
		}else if(cmd.toLowerCase().startsWith("stor")){

			Client.envoieFichier(new File(cmd.split(" ")[1]).getCanonicalPath(),Integer.parseInt(rep_serv.split(" ")[1]));
		}
	}
	
	public static void receptionFichier(String fileName,int port) throws UnknownHostException, IOException {
		Thread nt = new Thread(new FileTransfert(fileName,port,'R'));
		nt.start();
		
	}

	public static void envoieFichier(String fileName,int port){
		Thread nt = new Thread(new FileTransfert(fileName,port,'E'));
		nt.start();
	}
	
	public static void envoyerCommande(String cmd) {
		ps.println(cmd);
	}
	
}
