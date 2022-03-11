import java.io.PrintStream;
import java.nio.file.FileSystems;
import java.io.File;
import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

public class CommandeGET extends Commande {
	
	public CommandeGET(PrintStream ps, String commandeStr,Server srv) {
		super(ps, commandeStr, srv);
	}

	public void execute() {
			String filePath = "";
			System.out.println("Ok");
			if(commandeArgs.length == 0) {
				ps.println("2 Veuillez choisir le fichier à recevoir");
				return;
			}else {
				File temp = new File(this.srv.CWD+FileSystems.getDefault().getSeparator()+commandeArgs[0]);
				try {
					if(temp.exists() && temp.isFile()) {
							filePath = temp.getCanonicalPath();
					}else {
						temp = new File(commandeArgs[0]);
						if(temp.exists() && temp.isFile()) {
							filePath = temp.getCanonicalPath();
						}else {
							ps.println("2 Aucun fichier avec ce nom");
							System.out.println("2 Aucun fichier avec ce nom");
							return;
						}
					}
				} catch (IOException e) {e.printStackTrace();}

			}
				Thread nt = new Thread(new FileTransfert(filePath, 'E', srv),"Envoi Fichier client : "+srv.clientNumber);
				nt.start();
	}

}
