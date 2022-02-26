import java.io.File;
import java.io.PrintStream;

public class CommandeUSER extends Commande {
	
	public CommandeUSER(PrintStream ps, String commandeStr) {
		super(ps, commandeStr);
	}

	public void execute() {

		if(new File(Main.userDataPath+ commandeArgs[0]).exists()){
			CommandExecutor.userOk = true;
			CommandExecutor.userConnecting = commandeArgs[0];
			ps.println("0 Commande user OK");
		}else{
			CommandExecutor.userOk = false;
			CommandExecutor.userConnecting = "";
			ps.println("2 L'utilisateur "+commandeArgs[0]+" n'existe pas");
		}
		
	}

}
