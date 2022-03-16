package fr.l3.application.client_ftp.service;

import fr.l3.application.client_ftp.*;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

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

    public Thread thEcoute = null;
    public Thread thEnvoi = null;

    private String lastCmd ="";

    public String getLastRep() {
        return lastRep;
    }

    public void setLastRep(String lastRep) {
        this.lastRep = lastRep;
    }

    private volatile String lastRep ="";

    public boolean normalStop = false; // Indique si la demande de deconnexion est voulu ou non

    public volatile Set<Socket> fileTransfertSockets;

    public CommunicationService(String hote, int port) {
        this.hote = hote;
        this.port = port;
        this.lastCmd = "";
        this.lastRep = "";
    }

    public void stopConnexion(){
        if(this.connected){
            try {
                Client.getSocket().close();

            MainApp.getCommunicationService().thEcoute.interrupt();
            MainApp.getCommunicationService().thEnvoi.interrupt();

            }catch (IOException e){
                MainApp.getMainController().addError("Erreur lors de la fermeture de la connexion");
            }
            this.fileTransfertSockets.forEach(i-> {
                try {
                    i.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            this.fileTransfertSockets.clear();
        }
        this.lastRep = "";
        this.lastCmd = "";
        this.connected = false;
        Platform.runLater(this::cancel);

    }

    @Override
    protected Task<Void> createTask() {
        this.fileTransfertSockets = new HashSet<>();
        return new Task<Void>(){

            @Override
            protected Void call() {
                CommunicationService.this.normalStop = false;

                try {
                    System.in.readNBytes(System.in.available());
                } catch (IOException e) {}

                MainApp.getMainController().addText("Tentative de résolution de l'hôte");
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
                    MainApp.getMainController().addError("Hôte introuvable");
                    MainApp.getCommunicationService().stopConnexion();
                    return null;
                }

                if(!CommunicationService.this.addressFound){
                    th.interrupt();
                    MainApp.getMainController().addError("Hôte introuvable");
                    MainApp.getCommunicationService().stopConnexion();
                    return null;
                }

                try {
                    MainApp.getMainController().addText("Adresse ip trouvée : "+CommunicationService.this.hote);
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

                                    if(CommunicationService.this.lastRep==null){
                                        throw new IOException("Fin du stream serveur atteint");
                                    }
                                    if(Thread.currentThread().isInterrupted()){
                                        return;
                                    }
                                    if(CommunicationService.this.lastRep.startsWith("0") || CommunicationService.this.lastRep.startsWith("1")){
                                        MainApp.getMainController().addText(CommunicationService.this.lastRep.substring(2));
                                    }else if(CommunicationService.this.lastRep.startsWith("2")){
                                        MainApp.getMainController().addError(CommunicationService.this.lastRep.substring(2));
                                    }else{
                                        MainApp.getMainController().addText(CommunicationService.this.lastRep);
                                    }
                                    CommunicationService.this.lastCmd="";
                                }catch (IOException e){
                                    if(!CommunicationService.this.normalStop){
                                        MainApp.getMainController().addError("Serveur deconnecté");
                                        MainApp.getCommunicationService().stopConnexion();
                                    }
                                    return;
                                }catch (StringIndexOutOfBoundsException ex){
                                    ex.printStackTrace();
                                }


                            }
                        },"Ecoute");


                        CommunicationService.this.thEnvoi = new Thread(()->{
                        	String temp = "";
                            while(!Thread.currentThread().isInterrupted()){
                                try {

                                    temp = Client.lireClavier();
                                    if(temp != null){

                                    }
                                    
                                    if(Thread.currentThread().isInterrupted()){
                                        return;
                                    }
                                    
                                    if(temp==null) {
                                    	continue;
                                    }else {
                                    	CommunicationService.this.lastCmd = temp;
                                    }
                                    if(!Client.analyseCmdSend(CommunicationService.this.lastCmd)){
                                        Client.envoyerCommande(CommunicationService.this.lastCmd);
                                    }





                                } catch (IOException e) {
                                    MainApp.getCommunicationService().stopConnexion();
                                    MainApp.getMainController().addError("Erreur lecture entrée clavier / envoie commande");
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


                }catch (SocketException e){
                    if(CommunicationService.this.connected) {
                        MainApp.getMainController().addError("Le Serveur s'est déconnecté");
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