package fr.l3.application.client_ftp.service;

import fr.l3.application.client_ftp.*;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

public class CommunicationService extends Service<Void> {


    private String hote;

    public String getHote() {
        return hote;
    }

    public void setHote(String hote) {
        this.hote = hote;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private int port;
    private  boolean addressFound = false;

    public boolean isConnected() {
        return connected;
    }

    private boolean connected = false; //Indique si on est actuellent connecté à un serveur

    private Thread thEcoute = null;
    private Thread thEnvoi = null;

    private String lastCmd ="";
    private String lastRep ="";

    public boolean normalStop = false; // Indique si la demande de deconnexion est voulu ou non

    public CommunicationService(String hote, int port) {
        this.hote = hote;
        this.port = port;
        this.lastCmd = "";
        this.lastRep = "";
    }

    public void stopConnexion(){
        if(this.connected){
            MainApp.getCommunicationService().thEcoute.interrupt();
            System.out.print('\n');
            MainApp.getCommunicationService().thEnvoi.interrupt();
            try {
                Client.getSocket().close();
            }catch (IOException e){
                MainApp.getConsoleController().addError("Erreur lors de la fermeture de la connexion");
            }
        }
        this.connected = false;
        Platform.runLater(this::cancel);

    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>(){

            @Override
            protected Void call() {
                CommunicationService.this.normalStop = false;
              
                MainApp.getConsoleController().addText("Tentative de résolution de l'hôte");
                CommunicationService.this.addressFound=false;
                    Thread th = new Thread("Verification Hote"){
                        @Override
                        public void run(){
                            try {
                                if(Thread.currentThread().isInterrupted()){
                                    CommunicationService.this.stopConnexion();
                                    return;
                                }
                                CommunicationService.this.hote = InetAddress.getByName(CommunicationService.this.hote).getHostAddress();
                                CommunicationService.this.addressFound = true;
                            } catch (UnknownHostException e) {
                                if(Thread.currentThread().isInterrupted()){
                                    CommunicationService.this.stopConnexion();
                                    return;
                                }
                                CommunicationService.this.addressFound = false;
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
                    try {
                        if(!Client.connexionServeur(CommunicationService.this.hote, CommunicationService.this.port)){
                            CommunicationService.this.connected = false;
                        }else{
                            CommunicationService.this.connected = true;
                        }
                    }catch (SocketException e){
                        throw e;
                    }

                    if(CommunicationService.this.connected){

                        CommunicationService.this.lastCmd = "";
                        CommunicationService.this.lastRep = "";
                        CommunicationService.this.thEcoute = new Thread(() -> {
                            while(!Thread.currentThread().isInterrupted()) {
                                try {
                                    CommunicationService.this.lastRep = Client.ecouterServeur();
                                    if(Thread.currentThread().isInterrupted()){
                                        return;
                                    }
                                }catch (IOException e){

                                    if(!CommunicationService.this.normalStop){
                                        MainApp.getConsoleController().addError("Serveur deconnecté");
                                        MainApp.getCommunicationService().stopConnexion();
                                    }
                                    return;
                                }

                                if(CommunicationService.this.lastRep.startsWith("0") || CommunicationService.this.lastRep.startsWith("1")){
                                    MainApp.getConsoleController().addText(CommunicationService.this.lastRep.substring(2));
                                }else if(CommunicationService.this.lastRep.startsWith("2")){
                                    MainApp.getConsoleController().addError(CommunicationService.this.lastRep.substring(2));
                                }else{
                                    MainApp.getConsoleController().addText(CommunicationService.this.lastRep);
                                }
                            }
                        },"Ecoute");


                        CommunicationService.this.thEnvoi = new Thread(()->{
                        	String temp = "";
                            while(!Thread.currentThread().isInterrupted()){
                                try {
                                	
                                    temp = Client.lireClavier();
                                    
                                    if(Thread.currentThread().isInterrupted()){
                                        return;
                                    }
                                    
                                    if(temp==null) {
                                    	continue;
                                    }else {
                                    	CommunicationService.this.lastCmd = temp;
                                    }
                                    Client.envoyerCommande( CommunicationService.this.lastCmd);

                                    Client.analyseCmdSend(CommunicationService.this.lastCmd, CommunicationService.this.lastRep);

                                } catch (IOException e) {
                                    MainApp.getCommunicationService().stopConnexion();
                                    MainApp.getConsoleController().addError("Erreur lecture entrée clavier / envoie commande");
                                    return;
                                }
                            }
                        },"Envoi");


                        MainApp.getCommunicationService().thEcoute.start();
                        MainApp.getCommunicationService().thEnvoi.start();
                    }else{
                        MainApp.getCommunicationService().stopConnexion();
                    }

                    String cmd = "";

                    String rep_serveur = "";


                    /*while (true) {

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

                    }*/


                }catch (SocketException e){
                    if(CommunicationService.this.connected) {
                        MainApp.getConsoleController().addError("Le Serveur s'est déconnecté");
                        CommunicationService.this.connected = false;
                    }
                    MainApp.getCommunicationService().stopConnexion();
                    return null;
                } catch (IOException e){
                }
               return null;
            }
        };

    }
}