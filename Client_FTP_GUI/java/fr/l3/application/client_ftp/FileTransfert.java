package fr.l3.application.client_ftp;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileTransfert implements Runnable{

	String fileName;
	int port;
	char type;
	private Path tempFile = null; //Contiendra l'adresse d'un fichier temporaire créer si le fichier à recevoir existe déjà et contenant un copie du fichier déjà présent afin de le restaurer en cas de transfert interrompu

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
				if(new File(this.fileName).exists()){ //Gestion transfert de fichier interrompu
					MainApp.getMainController().addInfo(this.fileName+" existe déjà, création d'une sauvegarde temporaire");

					this.tempFile=Files.createTempFile(Paths.get(this.fileName).getFileName().toString().split("\\.")[0],null);

					BufferedInputStream is = new BufferedInputStream(new FileInputStream(this.fileName));
					BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(this.tempFile.toString()));

					for (int oct = is.read(); oct != -1; oct = is.read()) {
						os.write(oct);
					}

					is.close();
					os.close();

				}
				BufferedInputStream is = new BufferedInputStream(sck_file.getInputStream());
				BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(this.fileName));

				for (int oct = is.read(); oct != -1; oct = is.read()) {
					os.write(oct);
				}

				is.close();
				os.close();

			}
			sck_file.close();
			if(this.tempFile!=null) {
				Files.delete(this.tempFile);
			}
		} catch (UnknownHostException e) {} catch (IOException e) {
			MainApp.getMainController().addError("Erreur lors du transfert du fichier : "+ this.fileName);
			if(this.tempFile!=null){
				MainApp.getMainController().addText("Tentative de restauration du fichier : "+ this.fileName);
				try {
				BufferedInputStream is = new BufferedInputStream(new FileInputStream(this.tempFile.toString()));
				BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(this.fileName));

					for (int oct = is.read(); oct != -1; oct = is.read()) {
						os.write(oct);
					}
					is.close();
					os.close();
					MainApp.getMainController().addInfo("Restauration réussi pour : "+this.fileName);
					Files.delete(this.tempFile);
				} catch (IOException ex) {
					MainApp.getMainController().addError("Impossible de restaurer le fichier :"+ this.fileName);
					try {
						Files.delete(this.tempFile);
					} catch (IOException exc) {
						exc.printStackTrace();
					}
				}


			}

		}


	}
	

}
