package fr.l3.application.client_ftp;

import java.io.*;
import java.net.*;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;


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
		rep = Client.ecouterServeur();
		if(rep.startsWith("2")){
			MainApp.getMainController().addError(rep.substring(2));
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

	
	public static boolean analyseCmdSend(String cmd) throws IOException {
		String rep_serv = MainApp.getCommunicationService().getLastRep();
		if(cmd.toLowerCase().startsWith("bye")){
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Client.deconnexionServeur();
			return true;
		}


		if(cmd.toLowerCase().startsWith("get")){
			Client.envoyerCommande(cmd);
			//Client.sck_cmd.getOutputStream().write(255);
			rep_serv = MainApp.getCommunicationService().getLastRep();
			while(!rep_serv.toLowerCase().contains("port transfert fichier")){
				rep_serv = MainApp.getCommunicationService().getLastRep();
				if(rep_serv.startsWith("2")){
					return true;
				}
			}
			Client.receptionFichier(new File(cmd.split(" ")[1]).getName(), Integer.parseInt(rep_serv.split(" ")[1]));
			MainApp.getCommunicationService().setLastRep("");
			
		}else if(cmd.toLowerCase().startsWith("stor")){
			String filePath = "";
			String[] commandArg = cmd.split(" ");
			if(commandArg.length <2 || commandArg[1].equals("")){
				MainApp.getMainController().addError("Veuillez saisir le nom du fichier à envoyer");
				//Client.sck_cmd.getOutputStream().write(0);
				return true;
			}
			if(!new File(commandArg[1]).isFile()){
				try {
					MainApp.getMainController().addError("Le fichier : " + Paths.get(commandArg[1]).getFileName().toString() + " n'existe pas");
				}catch (InvalidPathException e){
					MainApp.getMainController().addError(commandArg[1]+" n'est pas un nom valide");
				}
				//Client.sck_cmd.getOutputStream().write(0);
				return true;
			}
			Client.envoyerCommande(cmd);
			while(!rep_serv.toLowerCase().contains("port transfert fichier")){
				rep_serv = MainApp.getCommunicationService().getLastRep();
				if(rep_serv.startsWith("2")){
					return true;
				}
			}
			Client.envoieFichier(new File(cmd.split(" ")[1]).getCanonicalPath(),Integer.parseInt(rep_serv.split(" ")[1]));
			MainApp.getCommunicationService().setLastRep("");
			return true;
		}
		return false;
	}

	public static void receptionFichier(String fileName,int port) {
		FileTransfert ft = new FileTransfert(fileName,port,'R');
		Thread nt = new Thread(ft,"Reception Fichier : "+ Paths.get(fileName).getFileName());
		nt.start();

	}

	public static void envoieFichier(String fileName,int port){
		FileTransfert ft = new FileTransfert(fileName,port,'E');
		Thread nt = new Thread(ft,"Envoi Fichier : "+ Paths.get(fileName).getFileName());
		nt.start();
	}
	
	public static void envoyerCommande(String cmd) {
		ps.println(cmd);
	}
	
}
