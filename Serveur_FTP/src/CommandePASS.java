import java.io.*;
import java.nio.file.FileSystems;

public class CommandePASS extends Commande {
	
	public CommandePASS(PrintStream ps, String commandeStr,Server srv) {
		super(ps, commandeStr, srv);
	}

	public void execute() {
		if((!srv.userOk) || srv.userConnecting.isBlank()) {
			ps.println("2 Veuillez d'abord saisir votre nom d'utilisateur avec la commande user");
		}else{
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(Main.userDataPath + srv.userConnecting+ FileSystems.getDefault().getSeparator()+"pw.txt")));
				String verifMdp = br.readLine();
				br.close();

				if(verifMdp.equals(commandeArgs[0])){
					if(Server.userConnected.contains(srv.userConnecting)){
						ps.println("2 Un utilisateur avec cet identifaint est déjà connecté");
						return;
					}
					srv.pwOk = true;
					Server.userConnected.add(srv.userConnecting);
					ps.println("1 Commande pass OK");
					ps.println("0 Vous êtes bien connecté sur notre serveur");

				}else{
					ps.println("2 Mot de passe faux");
				}

			} catch (IOException e) {e.printStackTrace();}
		}
		
	}

}
