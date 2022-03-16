import java.io.File;
import java.io.PrintStream;

public class CommandeSTOR extends Commande {
	
	public CommandeSTOR(PrintStream ps, String commandeStr,Server srv) {
		super(ps, commandeStr, srv);
	}

	public void execute() {
		if(commandeArgs.length<1){
			System.out.println("Fichier a recevoir n'a pas de nom");
			return;
		}
		Thread nt = new Thread(new FileTransfert(new File(commandeArgs[0]).getName(),'R',srv),"Reception Fichier client : "+srv.clientNumber);
		nt.start();
	}

}
