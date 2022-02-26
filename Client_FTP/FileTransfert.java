import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class FileTransfert implements Runnable{

	String fileName;
	int port;
	char type;

	public FileTransfert(String fileName, int port, char type) {
		this.fileName = fileName;
		this.port = port;
		this.type = type; // Indique si on doit envoyer ou recevoir
	}
	
	@Override
	public void run() {
		try {

			Socket sck_file;
			sck_file = new Socket(Client.hostname, port);
			if(this.type=='E'){

				BufferedInputStream is = new BufferedInputStream(new FileInputStream(this.fileName));
				BufferedOutputStream os = new BufferedOutputStream(sck_file.getOutputStream());

				for (int oct = is.read(); oct != -1; oct = is.read()) {
					os.write(oct);
				}

				is.close();
				os.close();


			}else if(this.type=='R') {
				BufferedInputStream is = new BufferedInputStream(sck_file.getInputStream());
				BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(this.fileName));

				for (int oct = is.read(); oct != -1; oct = is.read()) {
					os.write(oct);
				}

				is.close();
				os.close();

			}
			sck_file.close();
		} catch (UnknownHostException e) {} catch (IOException e) {}
		
		
		
		
		
	}
	

}
