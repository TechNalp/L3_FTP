import java.io.IOException;
import java.io.PrintStream;
import java.io.File;
import java.nio.file.FileSystems;


public class CommandeCD extends Commande {
	
	public CommandeCD(PrintStream ps, String commandeStr,Server srv) {
		super(ps, commandeStr,srv);
	}

	public void execute() {
		if(commandeArgs.length == 0 ){
			this.srv.CWD = System.getProperty("user.dir")+FileSystems.getDefault().getSeparator()+".";
		}else if(commandeArgs[0].equalsIgnoreCase(".")) {
			ps.println("2 Vous êtes déjà dans ce répertoire");
			return;
		}else {
			try {
				if(new File(this.srv.CWD+FileSystems.getDefault().getSeparator()+commandeArgs[0]).exists()) {
					this.srv.CWD = new File(this.srv.CWD+FileSystems.getDefault().getSeparator()+commandeArgs[0]).getCanonicalPath();
				}else {
					File temp = new File(commandeArgs[0]);
					if(temp.exists()) {
						this.srv.CWD = temp.getCanonicalPath();
					}else {
						ps.println("2 Ce répertoire n'existe pas");
						return;
					}
				}
			} catch (IOException e) {
			}
		}
		ps.println("0 Entré dans : "+  this.srv.CWD);
	}

}
