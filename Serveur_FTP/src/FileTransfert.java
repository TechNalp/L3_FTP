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
	
	String fileName;
	char type; // Indique si on doit envoyer ou recevoir
	Server srv;
	int port;
	ServerSocket serv_sck = null;
	Socket sck = null;
	Path tempFile = null;
	boolean fileExist = false;
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
					for(this.port=Server.firstAvailablePort;Server.availablePort.contains(this.port);this.port++);
					Server.availablePort.add(this.port);
					System.out.println(Server.availablePort);
				}
				this.srv.ps.println("1 "+ this.port + " <- Port transfert fichier");
				this.serv_sck = new ServerSocket(this.port);
				this.sck = serv_sck.accept();
				if(Thread.currentThread().isInterrupted()){
					Server.availablePort.remove((Object)this.port);
					sck.close();
					serv_sck.close();
					System.out.println("[THREAD] Transfert interrompu");
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
					for(int oct = is.read();oct!=-1;oct = is.read()) {
						if(Thread.currentThread().isInterrupted()){
							synchronized (this){Server.availablePort.remove((Object)this.port);}
							sck.close();
							serv_sck.close();
							is.close();
							os.close();
							System.out.println("[THREAD] Transfert interrompu pour le fichier : " + this.fileName);
							return;
						}
						os.write(oct);
					}
					this.srv.ps.println("0 Fin du transfert du fichier : "+this.fileName);
					is.close();
					os.close();

				}else if(this.type=='R'){ // Si on doit recevoir un fichier
					this.srv.ps.println("1 Fichier à Envoyer : " + this.fileName);
					this.fileExist = new File(srv.CWD+FileSystems.getDefault().getSeparator()+this.fileName).exists();
					this.tempFile = null;
					if(fileExist){ // Création d'une sauvegarde temporaire
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
							synchronized (this){Server.availablePort.remove((Object)this.port);}
							sck.close();
							serv_sck.close();
							is.close();
							os.close();
							System.out.println("[THREAD] Transfert interrompu");
							return;
						}
						os.write(oct);
					}
					this.srv.ps.println("0 Fin du transfert de fichier");
					is.close();
					os.close();

				}
				synchronized (this){
					Server.availablePort.remove((Object)this.port);
					System.out.println(Server.availablePort);
				}
				serv_sck.close();
				sck.close();



			} catch (SocketException e) {
				System.out.println("[THREAD] Transfert Interrompu");
				if(this.fileExist){
					try {
						if (this.tempFile != null) {
							this.srv.ps.println("0 tentative de restauration de : " + this.fileName);
							FileChannel os = new FileOutputStream(srv.CWD + FileSystems.getDefault().getSeparator() + this.fileName).getChannel();
							FileChannel is = new FileInputStream(tempFile.toString()).getChannel();

							os.transferFrom(is, 0, is.size());
							is.close();
							os.close();
						}else{
							this.srv.ps.println("2 impossible de restaurer : "+ this.fileName);
							this.srv.ps.println("2 suppression de : "+ this.fileName);
							Files.delete(Paths.get(this.fileName));

						}
					}catch (IOException ex){
							this.srv.ps.println("2 impossible de restaurer : "+ this.fileName);
							this.srv.ps.println("2 suppression de : "+ this.fileName);

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
			this.srv.ps.println("2 Erreur lors du transfert de fichier");
		}
		
		
	}

}
