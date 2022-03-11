package fr.l3.application.client_ftp;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class FileTransfert implements Runnable{

	String fileName;
	int port;
	char type;
	Path tempFile = null;
	boolean fileExist = false;
	public FileTransfert(String fileName, int port, char type) {
		this.fileName = fileName;
		this.port = port;
		this.type = type; // Indique si on doit envoyer ou recevoir
	}


	public boolean createTempSave() {
		try {
			this.tempFile = Files.createTempFile(this.fileName.replace('.','-'),null);

			FileChannel is = new FileInputStream(this.fileName).getChannel();
			FileChannel os = new FileOutputStream(this.tempFile.toString()).getChannel();
			os.transferFrom(is,0,is.size());
			is.close();
			os.close();
			Thread.sleep(20);
			return MainApp.getCommunicationService().isConnected();

		} catch (IOException | InterruptedException e) {
			return false;
		}

	}

	public boolean restoreFile(){
		try {
			FileChannel is = new FileInputStream(this.tempFile.toString()).getChannel();
			FileChannel os = new FileOutputStream(this.fileName).getChannel();
			os.transferFrom(is,0,is.size());
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public void run() {
		try {
			Socket sck_file;
			sck_file = new Socket(Client.hostname, port);
			MainApp.getCommunicationService().fileTransfertSockets.add(sck_file);
			if(this.type=='E'){
				BufferedInputStream is = new BufferedInputStream(new FileInputStream(this.fileName));
				BufferedOutputStream os = new BufferedOutputStream(sck_file.getOutputStream());

				for (int oct = is.read(); oct != -1; oct = is.read()) {
					os.write(oct);
					if(Thread.currentThread().isInterrupted()){
						return;
					}
				}

				is.close();
				os.close();

			}else if(this.type=='R') {
				if(new File(this.fileName).exists()){
					this.fileExist = true;
					MainApp.getMainController().addInfo(this.fileName+ " existe déjà");
					MainApp.getMainController().addInfo("Création d'une sauvegarde temporaire de : "+this.fileName);
					if(!this.createTempSave()){
						if(!MainApp.getCommunicationService().isConnected()){
							if(this.tempFile!=null){
								Files.delete(this.tempFile);
								this.tempFile = null;
								sck_file.close();
								MainApp.getCommunicationService().fileTransfertSockets.remove(sck_file);
								return;
							}
						}
						MainApp.getMainController().addError("Impossible de créer une sauvegarde temporaire pour : "+this.fileName+", en cas d'erreur de transfert, le fichier ne sera pas rétablie");
						if(this.tempFile != null){
							Files.delete(this.tempFile);
							this.tempFile = null;
						}
					}else{
						MainApp.getMainController().addInfo("Sauvegarde temporaire de : "+this.fileName+" créée avec succès");
					}
				}
				sck_file.getOutputStream().write(1);// Pour indiquer au serveur que l'on peut commencer le transfert
				BufferedInputStream is = new BufferedInputStream(sck_file.getInputStream());
				BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(this.fileName));
				try {
					for (int oct = is.read(); oct != -1; oct = is.read()) {
						os.write(oct);
					}
					os.flush();
					try {
						Thread.sleep(20); // Laisse le temps de changer l'état de connexion à l'autre thread
					} catch (InterruptedException e) {
						e.printStackTrace();
					}



					if (!MainApp.getCommunicationService().isConnected()) {
						MainApp.getMainController().addError("Transfert de :  " + this.fileName + " interrompu");

						if (this.fileExist) {
							MainApp.getMainController().addInfo("Tentative de restauration de : " + this.fileName);
							if (this.tempFile == null) {
								MainApp.getMainController().addError("Impossible de restaurer le fichier : " + this.fileName);
								MainApp.getMainController().addInfo("Suppresion de : " + this.fileName);
								Files.delete(Paths.get(this.fileName));
								sck_file.close();
								MainApp.getCommunicationService().fileTransfertSockets.remove(sck_file);
								return;
							}
							if (this.restoreFile()){
								MainApp.getMainController().addInfo("Restauration de : " + this.fileName + " OK");
							} else {
								MainApp.getMainController().addError("Restauration de : " + this.fileName + " impossible");
								MainApp.getMainController().addInfo("Suppresion de : " + this.fileName);
								Files.delete(Paths.get(this.fileName));
							}

							Files.delete(this.tempFile);
							this.tempFile = null;

						} else {
							MainApp.getMainController().addInfo("Suppresion de:  " + this.fileName);
							Files.delete(Paths.get(this.fileName));
						}
					}
					if(this.tempFile!=null){
						Files.delete(this.tempFile);
						this.tempFile = null;
					}
				}catch (SocketException e){
					MainApp.getMainController().addError("Transfert de : " + this.fileName + " interrompu");
					is.close();
					os.close();
					MainApp.getCommunicationService().fileTransfertSockets.remove(sck_file);
					sck_file.close();

					if (this.fileExist) {
						MainApp.getMainController().addInfo("Tentative de restauration de : " + this.fileName);
						if (this.tempFile == null) {
							MainApp.getMainController().addError("Impossible de restaurer le fichier : " + this.fileName);
							MainApp.getMainController().addInfo("Suppresion de : " + this.fileName);
							Files.delete(Paths.get(this.fileName));
							return;
						}
						if (this.restoreFile()) {
							MainApp.getMainController().addInfo("Restauration de : " + this.fileName + " OK");
						} else {
							MainApp.getMainController().addError("Restauration de : " + this.fileName + " impossible");
							MainApp.getMainController().addInfo("Suppresion de : " + this.fileName);
							Files.delete(Paths.get(this.fileName));
						}
						Files.delete(this.tempFile);
						this.tempFile = null;

					} else {
						MainApp.getMainController().addInfo("Suppresion de:  " + this.fileName);
						Files.delete(Paths.get(this.fileName));
					}
				}
				if(this.tempFile!=null){
					Files.delete(this.tempFile);
					this.tempFile = null;
				}
			}



		} catch (IOException e) {
			MainApp.getMainController().addError("Erreur lors du transfert du fichier : "+ this.fileName);

			return;

		}


	}
	

}
