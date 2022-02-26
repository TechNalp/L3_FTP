import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.nio.file.FileSystems;

public class FileTransfert implements Runnable{
	
	String fileName;
	int port;
	char type; // Indique si on doit envoyer ou recevoir

	public FileTransfert(String fileName, int port, char type) {
		this.fileName = fileName;
		this.port = port;
		this.type=type;
	}
	
	@Override
	public void run() {
		
		try {
			ServerSocket serv_sck = new ServerSocket(this.port);
			Socket sck = serv_sck.accept();
			BufferedInputStream is;
			BufferedOutputStream os;
			if(this.type=='E'){ // Si on doit envoyer un fichier
				is = new BufferedInputStream(new FileInputStream(this.fileName));
				os = new BufferedOutputStream(sck.getOutputStream());

				for(int oct = is.read();oct!=-1;oct = is.read()) {
					os.write(oct);
				}

				is.close();
				os.close();

			}else if(this.type=='R'){
				os = new BufferedOutputStream(new FileOutputStream(Commande.CWD+ FileSystems.getDefault().getSeparator()+this.fileName));
				is = new BufferedInputStream(sck.getInputStream());

				for(int oct = is.read();oct!=-1;oct = is.read()){
					os.write(oct);
				}

				is.close();
				os.close();

			}

			sck.close();
			serv_sck.close();

			
		} catch (IOException e) {}
		
		
	}

}
