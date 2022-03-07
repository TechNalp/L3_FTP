import java.io.PrintStream;

public class CommandExecutor {

	public static void executeCommande(PrintStream ps, String commande, Server srv) {
		if(srv.userOk && srv.pwOk) {
			// Changer de repertoire. Un (..) permet de revenir au repertoire superieur
			if(commande.split(" ")[0].equals("cd")) (new CommandeCD(srv.ps, commande,srv)).execute();
	
			// Telecharger un fichier
			if(commande.split(" ")[0].equals("get")) (new CommandeGET(srv.ps, commande,srv)).execute();
			
			// Afficher la liste des fichiers et des dossiers du repertoire courant
			if(commande.split(" ")[0].equals("ls")) (new CommandeLS(srv.ps, commande,srv)).execute();
		
			// Afficher le repertoire courant
			if(commande.split(" ")[0].equals("pwd")) (new CommandePWD(srv.ps, commande,srv)).execute();
			
			// Envoyer (uploader) un fichier
			if(commande.split(" ")[0].equals("stor")) (new CommandeSTOR(srv.ps, commande,srv)).execute();

		}else {
			if(commande.split(" ")[0].equals("pass") || commande.split(" ")[0].equals("user")) {
				// Le mot de passe pour l'authentification
				if(commande.split(" ")[0].equals("pass")) (new CommandePASS(srv.ps, commande,srv)).execute();
	
				// Le login pour l'authentification
				if(commande.split(" ")[0].equals("user")) (new CommandeUSER(srv.ps, commande,srv)).execute();
			}
			else
				ps.println("2 Vous n'êtes pas connecté !");
		}
	}

}
