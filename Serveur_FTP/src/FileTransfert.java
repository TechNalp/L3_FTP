import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.net.SocketException;
import java.nio.file.FileSystems;
import java.rmi.ServerError;

public class FileTransfert implements Runnable{
	
	String fileName;
	char type; // Indique si on doit envoyer ou recevoir
	Server srv;
	int port;

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
				ServerSocket serv_sck = new ServerSocket(this.port);
				Socket sck = serv_sck.accept();
				if(Thread.currentThread().isInterrupted()){
					Server.availablePort.remove((Object)this.port);
					sck.close();
					serv_sck.close();
					System.out.println("[THREAD] Transfert interrompu");
					return;
				}
				BufferedInputStream is;
				BufferedOutputStream os;

				if(this.type=='E'){ // Si on doit envoyer un fichier
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

				}else if(this.type=='R'){ // Si on doit recevoior un fichier
					this.srv.ps.println("1 Fichier à Envoyer : " + this.fileName);
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
				sck.close();
				serv_sck.close();


			} catch (SocketException e) {
				System.out.println("[THREAD] Client déconnecté");
				synchronized (this){
					Server.availablePort.remove((Object)this.port);
				}
				this.srv.sck.close();
			}
		} catch (IOException e) {
			this.srv.ps.println("2 Erreur lors du transfert de fichier");
		}
		
		
	}

}
