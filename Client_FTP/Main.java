import java.net.ConnectException;
import java.net.SocketException;

public class Main {
	public static void main(String[] args) throws Exception{


		try {
			Client.connexionServeur("localhost", 2121);

			String cmd = "";

			String rep_serveur = "";

			while (true) {

				do {
					rep_serveur = Client.ecouterServeur();
					Client.analyseCmdSend(cmd, rep_serveur);
					System.out.println(rep_serveur);
					cmd = "";
				}
				while (!(rep_serveur.substring(0, 1).contains("0") || rep_serveur.substring(0, 1).contains("2")));


				cmd = Client.lireClavier();

				Client.envoyerCommande(cmd);


			}
		}catch (SocketException e){
			System.out.println("Le Serveur s'est déconnecté");
			System.out.println("Fermeture du programme");
			return;
		}


	}
}
