import java.io.File;
import java.io.PrintStream;

public class CommandeUSER extends Commande {
	
	public CommandeUSER(PrintStream ps, String commandeStr,Server srv) {
		super(ps, commandeStr, srv);
	}

	public void execute() {

		if(new File(this.srv.userDataPath+commandeArgs[0]).exists()){
			srv.userOk = true;
			srv.userConnecting = commandeArgs[0];
			ps.println("0 Commande user OK");
		}else{
			srv.userOk = false;
			srv.userConnecting = "";
			ps.println("2 L'utilisateur "+commandeArgs[0]+" n'existe pas");
		}
		
	}

}
