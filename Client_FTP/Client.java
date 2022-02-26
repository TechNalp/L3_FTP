import java.io.*;
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
	
	
	public static void connexionServeur(String hostname, int port) throws UnknownHostException, IOException {
		System.out.println("Client FTP");
		Client.sck_cmd = new Socket(hostname,port);
		Client.rd = new BufferedReader(new InputStreamReader(Client.sck_cmd.getInputStream()));
		Client.ps = new PrintStream(Client.sck_cmd.getOutputStream());
		Client.hostname = hostname;
		Client.port = port;
	}
	
	public static void deconnexionServeur() throws IOException {
		Client.sck_cmd.close();
	}
	
	public static String ecouterServeur() throws IOException{
		return Client.rd.readLine();
	}
	
	public static void analyseCmdSend(String cmd, String rep_serv) throws UnknownHostException, IOException {

		if(cmd.toLowerCase().startsWith("bye")){
			Client.deconnexionServeur();
			System.out.println("Déconnexion Réussi");
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
		ps.flush();
	}
	
}
