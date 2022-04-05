import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.net.SocketException;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.ServerError;

public class FileTransfert implements Runnable{
	
	String fileName; // Nom du fichier à transferer
	char type; // Indique si on doit envoyer ou recevoir
	Server srv; // Instance du serveur qui gère le client courant
	int port; // Port qui servira au transfert du fichier
	ServerSocket serv_sck = null; // Socket servant au transfert des commande
	Socket sck = null; // Socket servant au transfert du fichier
	Path tempFile = null; // Chemin vers le fichier de sauvegarde
	boolean fileExist = false; // Si le fichier existe déjà
	public FileTransfert(String fileName, char type,Server srv) {
		this.fileName = fileName;
		this.type=type;
		this.srv = srv;
	}
	
	@Override
	public void run() {
		System.out.println("File transfert lancé");
		try{
			try {
				synchronized (this){
					for(this.port=Server.firstAvailablePort;Server.availablePort.contains(this.port);this.port++); // On cherche le premier port disponible
					Server.availablePort.add(this.port);
				}
				this.srv.ps.println("1 "+ this.port + " <- Port transfert fichier"); // On envoie le port choisi au client
				this.serv_sck = new ServerSocket(this.port);
				this.sck = serv_sck.accept();
				if(Thread.currentThread().isInterrupted()){ // Si le thread est interrompu
					Server.availablePort.remove((Object)this.port);
					sck.close();
					serv_sck.close();
					System.out.println("["+Thread.currentThread().getName()+"] Transfert interrompu");
					synchronized (this){
						Server.availablePort.remove((Object)this.port); // Le port est de nouveau disponible on l'enlève de la liste des ports utilisé
					}
					return;
				}

				sck.getInputStream().read(); // Attent que le client envoi un octect pour confirmer que le transfert puisse commencer
				if(this.type=='E'){ // Si on doit envoyer un fichier
					BufferedInputStream is;
					BufferedOutputStream os;
					this.srv.ps.println("1 Fichier à Recevoir : " + this.fileName);
					is = new BufferedInputStream(new FileInputStream(this.fileName));
					os = new BufferedOutputStream(sck.getOutputStream());
					this.srv.ps.println("1 Début du transfert du fichier : "+this.fileName);
					for(int oct = is.read();oct!=-1;oct = is.read()) { // Transfert vers le client
						if(Thread.currentThread().isInterrupted()){
							synchronized (this){Server.availablePort.remove((Object)this.port);}// Le port est de nouveau disponible on l'enlève de la liste des ports utilisé
							sck.close();
							serv_sck.close();
							is.close();
							os.close();
							System.out.println("["+Thread.currentThread().getName()+"] Transfert interrompu pour le fichier : " + this.fileName);
							synchronized (this){
								Server.availablePort.remove((Object)this.port); // Le port est de nouveau disponible on l'enlève de la liste des ports utilisé
							}
							return;
						}
						os.write(oct);
					}

					this.srv.ps.println("0 Fin du transfert du fichier : "+this.fileName);
					is.close();
					os.close();

				}else if(this.type=='R'){ // Si on doit recevoir un fichier
					this.srv.ps.println("1 Fichier à Envoyer : " + this.fileName);
					this.fileExist = new File(srv.CWD+FileSystems.getDefault().getSeparator()+this.fileName).exists(); // On regarde si le fichier existe déjà
					this.tempFile = null;
					if(fileExist){ // Si le fichier existe on crée une sauvegarde qui
						srv.ps.println("0 Création d'un fichier de sauvegarde temporaire pour : "+this.fileName);
						try {
							tempFile = Files.createTempFile(this.fileName.split("\\.")[0],null);
							FileChannel is = new FileInputStream(srv.CWD+FileSystems.getDefault().getSeparator()+this.fileName).getChannel();
							FileChannel os = new FileOutputStream(tempFile.toString()).getChannel();

							os.transferFrom(is,0,is.size());
							is.close();
							os.close();
						}catch (IOException e){
							srv.ps.println("2 Impossible de créer un fichier de sauvegarde temporaire");
							Files.delete(tempFile);
							this.tempFile = null;
						}

					}
					BufferedInputStream is;
					BufferedOutputStream os;
					os = new BufferedOutputStream(new FileOutputStream(this.srv.CWD+ FileSystems.getDefault().getSeparator()+this.fileName));
					is = new BufferedInputStream(sck.getInputStream());
					this.srv.ps.println("1 Début du transfert de fichier");
					for(int oct = is.read();oct!=-1;oct = is.read()){
						if(Thread.currentThread().isInterrupted()){
							synchronized (this){Server.availablePort.remove((Object)this.port);}// Le port est de nouveau disponible on l'enlève de la liste des ports utilisé
							sck.close();
							serv_sck.close();
							is.close();
							os.close();
							System.out.println("["+Thread.currentThread().getName()+"] Transfert interrompu");
							synchronized (this){
								Server.availablePort.remove((Object)this.port);// Le port est de nouveau disponible on l'enlève de la liste des ports utilisé
							}
							return;
						}
						os.write(oct);
					}

					try {
						Thread.sleep(20); // Laisse le temps de changer l'état de connexion
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if(!Server.userConnected.contains(this.srv.userConnecting)){
						throw new SocketException("Transfert interrompu");
					}

					this.srv.ps.println("0 Fin du transfert de fichier");
					is.close();
					os.close();

				}
				synchronized (this){
					Server.availablePort.remove((Object)this.port);
				}
				serv_sck.close();
				sck.close();



			} catch (SocketException e) {
				System.out.println("["+Thread.currentThread().getName()+"] Transfert Interrompu");
				if(this.fileExist){
					try {
						if (this.tempFile != null) {
							System.out.println("Tentative de restauration de : " + this.fileName);
							FileChannel os = new FileOutputStream(srv.CWD + FileSystems.getDefault().getSeparator() + this.fileName).getChannel();
							FileChannel is = new FileInputStream(tempFile.toString()).getChannel();

							os.transferFrom(is, 0, is.size());
							is.close();
							os.close();
							System.out.println("Restauration de : "+this.fileName+" réussi");
						}else{
							System.out.println("Impossible de restaurer : "+ this.fileName);
							System.out.println("Suppression de : "+ this.fileName);
							Files.delete(Paths.get(this.fileName));

						}
					}catch (IOException ex){
							System.out.println("Impossible de restaurer : "+ this.fileName);
							System.out.println("Suppression de : "+ this.fileName);

					}


				}
				synchronized (this){
					Server.availablePort.remove((Object)this.port);
				}
				this.srv.sck.close();
				this.sck.close();
				this.serv_sck.close();
			}
		} catch (IOException e) {
			System.out.println("Erreur lors du transfert de fichier");
		}
		
		
	}

}
