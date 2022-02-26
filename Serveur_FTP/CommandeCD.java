import java.io.IOException;
import java.io.PrintStream;
import java.io.File;
import java.nio.file.FileSystems;


public class CommandeCD extends Commande {
	
	public CommandeCD(PrintStream ps, String commandeStr) {
		super(ps, commandeStr);
	}

	public void execute() {
		if(commandeArgs.length == 0 ){
			Commande.CWD = System.getProperty("user.dir")+FileSystems.getDefault().getSeparator()+".";
		}else if(commandeArgs[0].equalsIgnoreCase(".")) {
			ps.println("2 Vous êtes déjà dans ce répertoire");
			return;
		}else {
			try {
				if(new File(Commande.CWD+FileSystems.getDefault().getSeparator()+commandeArgs[0]).exists()) {
					Commande.CWD = new File(Commande.CWD+FileSystems.getDefault().getSeparator()+commandeArgs[0]).getCanonicalPath();
				}else {
					File temp = new File(commandeArgs[0]);
					if(temp.exists()) {
						Commande.CWD = temp.getCanonicalPath();
					}else {
						ps.println("2 Ce répertoire n'existe pas");
						return;
					}
				}
			} catch (IOException e) {
			}
		}
		ps.println("0 Entré dans : "+  Commande.CWD);
	}

}
