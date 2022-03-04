import java.io.*;
import java.nio.file.FileSystems;

public class CommandePASS extends Commande {
	
	public CommandePASS(PrintStream ps, String commandeStr,Server srv) {
		super(ps, commandeStr, srv);
	}

	public void execute() {
		// Le mot de passe est : abcd
		if((!srv.userOk) || srv.userConnecting.isBlank()) {
			ps.println("2 Veuillez d'abord saisir votre nom d'utilisateur avec la commande user");
		}else{
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(Main.userDataPath + srv.userConnecting+ FileSystems.getDefault().getSeparator()+"pw.txt")));
				String verifMdp = br.readLine();
				br.close();

				if(verifMdp.equals(commandeArgs[0])){
					srv.pwOk = true;
					ps.println("1 Commande pass OK");
					ps.println("0 Vous êtes bien connecté sur notre serveur");

				}else{
					ps.println("2 Mot de passe faux");
				}

			} catch (FileNotFoundException e) {} catch (IOException e) {}
		}
		
	}

}
