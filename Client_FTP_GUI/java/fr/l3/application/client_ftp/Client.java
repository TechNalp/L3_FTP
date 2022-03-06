package fr.l3.application.client_ftp;

import java.io.*;
import java.net.*;
import java.util.Locale;


public class Client {

	static private BufferedReader rd = null;
	static private PrintStream ps = null;
	static private Socket sck_cmd = null;
	public static String hostname ="";
	static int port = 0;
	static BufferedReader keyboardInput = new BufferedReader(new InputStreamReader(System.in));
	
	public static String lireClavier() throws IOException {
		long startTime = System.currentTimeMillis();
		while(System.currentTimeMillis()-startTime<10 && !Client.keyboardInput.ready());
		if(Client.keyboardInput.ready()) {
			String str = Client.keyboardInput.readLine();
			return str;
		}else {
			return null;
		}

		
	}
	
	public static Socket getSocket() {
		return Client.sck_cmd;
	}
	

	private static boolean verifConnexion() throws IOException {
		MainApp.getMainController().addText("Verification de la connexion");
		Client.sck_cmd.setSoTimeout(2000);
		int connexionVerif;
		connexionVerif = Client.sck_cmd.getInputStream().read();
		Client.sck_cmd.setSoTimeout(0);
		Client.sck_cmd.getOutputStream().write(connexionVerif+48); // Permet de confirmer au serveur que l'on est un client compatible
		return true;
	}

	public static boolean connexionServeur(String hostname, int port) throws IOException {
		try {
			Client.sck_cmd = new Socket();
			Client.sck_cmd.connect(new InetSocketAddress(hostname,port),1000);
			if(!Client.verifConnexion()){
				return false;
			}
		} catch (IOException e){
			MainApp.getMainController().addError("Impossible de se connecter à l'adresse : "+hostname+":"+port);
			MainApp.getCommunicationService().stopConnexion();
			return false;
		}

		Client.rd = new BufferedReader(new InputStreamReader(Client.sck_cmd.getInputStream()));
		Client.ps = new PrintStream(Client.sck_cmd.getOutputStream());
		Client.hostname = hostname;
		Client.port = port;

		// Envoie des identifiants de connexion

		String[] ids = MainApp.getMainController().getConnectionIds();
		String rep;
		do {
			rep=Client.ecouterServeur();
			if(rep.startsWith("0") || rep.startsWith("1")){
				MainApp.getMainController().addText(rep.substring(2));
			}else if(rep.startsWith("2")){
				MainApp.getMainController().addError(rep.substring(2));
				MainApp.getCommunicationService().stopConnexion();
				return false;
			}else{
				MainApp.getMainController().addText(rep);
			}
		}while(!rep.startsWith("0"));

		Client.envoyerCommande("user "+ids[0]);

		rep = Client.ecouterServeur();
		if(rep.startsWith("2")){
			MainApp.getMainController().addError("Nom d'utilisateur incorrecte");
			MainApp.getMainController().addError("Arrêt de la connexion");
			MainApp.getCommunicationService().stopConnexion();
			return false;
		}
			MainApp.getMainController().addInfo("Nom d'utilisateur OK");

			Client.envoyerCommande("pass "+ids[1]);

		if(Client.ecouterServeur().startsWith("2")){
			MainApp.getMainController().addError("Mot de passe erronée");
			MainApp.getMainController().addError("Arrêt de la connexion");
			MainApp.getCommunicationService().stopConnexion();
			return false;
		}

		do {
			rep=Client.ecouterServeur();
			if(rep.startsWith("0") || rep.startsWith("1")){
				MainApp.getMainController().addText(rep.substring(2));
			}else if(rep.startsWith("2")){
				MainApp.getMainController().addError(rep.substring(2));
				MainApp.getCommunicationService().stopConnexion();
				return false;
			}else{
				MainApp.getMainController().addText(rep);
			}
		}while(!rep.startsWith("0"));


		return true;
	}
	
	public static void deconnexionServeur(){

		MainApp.getCommunicationService().normalStop = true;
		MainApp.getCommunicationService().stopConnexion();
		MainApp.getMainController().addInfo("Déconnexion du serveur réussi");
	}
	
	public static String ecouterServeur() throws IOException{
		return Client.rd.readLine();
	}

	
	public static void analyseCmdSend(String cmd, String rep_serv) throws IOException {

		if(cmd.toLowerCase().startsWith("bye")){
			Client.deconnexionServeur();
		}

		if(rep_serv.startsWith("2")) {
			return;
		}
		if(cmd.toLowerCase().startsWith("get")){
			while(!rep_serv.toLowerCase().contains("port transfert fichier")){
				if(rep_serv.startsWith("2")){
					return;
				}
				rep_serv = MainApp.getCommunicationService().getLastRep();
			}
			Client.receptionFichier(new File(cmd.split(" ")[1]).getName(), Integer.parseInt(rep_serv.split(" ")[1]));
			
		}else if(cmd.toLowerCase().startsWith("stor")){
			while(rep_serv.toLowerCase().contains("port transfert fichier"));
			Client.envoieFichier(new File(cmd.split(" ")[1]).getCanonicalPath(),Integer.parseInt(rep_serv.split(" ")[1]));
		}
		MainApp.getCommunicationService().setLastRep("");

	}

	public static void receptionFichier(String fileName,int port) {
		Thread nt = new Thread(new FileTransfert(fileName,port,'R'),"Reception Fichier");
		nt.start();
		
	}

	public static void envoieFichier(String fileName,int port){
		Thread nt = new Thread(new FileTransfert(fileName,port,'E'),"Envoi Fichier");
		nt.start();
	}
	
	public static void envoyerCommande(String cmd) {
		ps.println(cmd);
	}
	
}
