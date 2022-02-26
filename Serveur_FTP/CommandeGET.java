import java.io.PrintStream;
import java.nio.file.FileSystems;
import java.io.File;
import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;

public class CommandeGET extends Commande {
	
	public CommandeGET(PrintStream ps, String commandeStr) {
		super(ps, commandeStr);
	}

	public void execute() {
			String filePath = "";
			if(commandeArgs.length == 0) {
				ps.println("2 Veuillez choisir le fichier à recevoir");
			}else {
				File temp = new File(Commande.CWD+FileSystems.getDefault().getSeparator()+commandeArgs[0]);
				try {
					if(temp.exists() && temp.isFile()) {
							filePath = temp.getCanonicalPath();
					}else {
						temp = new File(commandeArgs[0]);
						if(temp.exists() && temp.isFile()) {
							filePath = temp.getCanonicalPath();
						}else {
							ps.println("2 Aucun fichier avec ce nom");
							return;
						}
					}
				} catch (IOException e) {}
				
				
			}
		
			int portEnvoieFichier = 3050;
			ps.println("1 "+ portEnvoieFichier + " <- Port transfert fichier");
			ps.println("1 Fichier à transferer : " + filePath);
			Thread nt = new Thread(new FileTransfert(filePath,portEnvoieFichier,'E'));
			nt.start();
			ps.println("0 Fin du transfert du fichier");
			
			
			
		
	}

}
