package fr.l3.application.client_ftp.service;

import fr.l3.application.client_ftp.*;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

public class CommunicationService extends Service<Void> {

    private String hote;
    private int port;
    private  boolean addressFound = false;
    public CommunicationService(String hote, int port) {
        this.hote = hote;
        this.port=port;

    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>(){

            @Override
            protected Void call() throws UnknownHostException {
                MainApp.getConsoleController().addText("Tentative de résolution de l'hôte");
                CommunicationService.this.addressFound=false;
                    Thread th = new Thread(){
                        @Override
                        public void run(){
                            try {
                                if(Thread.currentThread().isInterrupted()){
                                    return;
                                }
                                CommunicationService.this.hote = InetAddress.getByName(CommunicationService.this.hote).getHostAddress();
                                CommunicationService.this.addressFound = true;
                            } catch (java.net.UnknownHostException e) {
                                if(Thread.currentThread().isInterrupted()){
                                    return;
                                }
                                CommunicationService.this.addressFound = false;
                                return;
                        }
                    }

                };
                th.start();
                try {
                    th.join(2000);
                } catch (InterruptedException e) {
                    MainApp.getConsoleController().addError("Hôte introuvable");
                    MainApp.getConnexionController().stopConnexion();
                    return null;
                }

                if(!CommunicationService.this.addressFound){
                    th.interrupt();
                    MainApp.getConsoleController().addError("Hôte introuvable");
                    MainApp.getConnexionController().stopConnexion();
                    return null;
                }

                try {
                    MainApp.getConsoleController().addText("Adresse ip trouvée : "+CommunicationService.this.hote);
                    Client.connexionServeur(CommunicationService.this.hote, CommunicationService.this.port);
                    String cmd = "";

                    String rep_serveur = "";

                    while (true) {

                        do {
                            rep_serveur = Client.ecouterServeur();
                            Client.analyseCmdSend(cmd, rep_serveur);
                            if(rep_serveur.startsWith("0") || rep_serveur.startsWith("1")){
                                MainApp.getConsoleController().addText(rep_serveur.substring(2));
                            }else if(rep_serveur.startsWith("2")){
                                MainApp.getConsoleController().addError(rep_serveur.substring(2));
                            }else{
                                MainApp.getConsoleController().addText(rep_serveur);
                            }


                            cmd = "";
                        }
                        while (!(rep_serveur.substring(0, 1).contains("0") || rep_serveur.substring(0, 1).contains("2")));

                        cmd = Client.lireClavier();

                        Client.envoyerCommande(cmd);


                    }
                }catch (SocketException e){
                    MainApp.getConsoleController().addError("Le Serveur s'est déconnecté");
                    MainApp.getConnexionController().stopConnexion();
                    return null;
                } catch (IOException e){}
               return null;
            }
        };

    }
}
