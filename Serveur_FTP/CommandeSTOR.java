import java.io.File;
import java.io.PrintStream;

public class CommandeSTOR extends Commande {
	
	public CommandeSTOR(PrintStream ps, String commandeStr) {
		super(ps, commandeStr);
	}

	public void execute() {
		int portReceptionFichier = 3051;
		String filePath = commandeArgs[0];
		ps.println("1 "+ portReceptionFichier + " <- Port transfert fichier");

		ps.println("1 Fichier à Envoyer : " + filePath);
		Thread nt = new Thread(new FileTransfert(new File(commandeArgs[0]).getName(),portReceptionFichier,'R'));
		nt.start();
		ps.println("0 Fin du transfert du fichier");
	}

}
