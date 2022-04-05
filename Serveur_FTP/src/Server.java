import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Server implements Runnable{
    private static int numberOfClient = 0; // Stock le nombre de clients
    public int clientNumber; // Numéro du client courant
    Socket sck; // Socket de communication pour ce client
    BufferedReader br; // Pour la lecture des commande
    PrintStream ps; // Pour l'envoie des réponses
    static String userDataPath = "./USERS/"; // Emplacement des dossier USERS et fichier pwd
    String CWD ="."; // Stock le répertoore courant de ce client
    boolean userOk = false; // Si le client à fait une commande user valide
    boolean pwOk = false; // Si le client a mis le bon mot de passe avec le bon username
    String userConnecting = ""; // Stock le nom d'utilisateur du client courant
    final static int  firstAvailablePort = 3500; // Premier numéro de port disponible pour les transfert de fichier
    static volatile List<Integer> availablePort = new ArrayList<>(); // Stock tous les numéro de port déjà utilisé dans un transfert de fichier et donc indisponible
    static volatile Set<String> userConnected = new HashSet<>(); // Liste des utilisateur connecté (empêche le même user de se connecter 2 fois)

    public Server(Socket sck){
        this.clientNumber = Server.numberOfClient++;
        this.sck = sck;
        try {
            this.CWD = new File(".").getCanonicalPath(); // Tous les client commence avec le même chemin courant
            this.br = new BufferedReader(new InputStreamReader(this.sck.getInputStream()));
            this.ps = new PrintStream(this.sck.getOutputStream());
        } catch (IOException e) {}
    }

    public void run(){
        Thread.currentThread().setName("Serveur client : "+this.clientNumber);
        try {
            try {
                int verifNumber = 103; // Si le client nous renvoie ce nombre incrémenté de 48, on est sûr qu'il s'agit d'un client compatible
                this.sck.getOutputStream().write(verifNumber); // On envoi au serveur un octect pour lui indiquer qu'il est sur le bon serveur
                int recu = this.sck.getInputStream().read(); // On attend une réponse
                if(recu!=(verifNumber+48)){
                    this.sck.close();
                    System.out.println("La connexion n'a pas pu être vérifié");
                    Server.numberOfClient--;
                    return;
                }
                this.ps.println("1 Bienvenue ! ");
                this.ps.println("1 Serveur FTP Personnel.");
                this.ps.println("0 Authentification : ");

                String commande = "";
                try {
                    while (!(commande = this.br.readLine()).equals("bye")) {
                        System.out.println("["+Thread.currentThread().getName()+"]"+" >> " + commande);
                        CommandExecutor.executeCommande(ps, commande, this);
                    }
                }catch (NullPointerException e){
                    System.out.println("\n["+Thread.currentThread().getName()+"]"+"  Client déconnecté\n");
                    Server.userConnected.remove(this.userConnecting);

                    this.sck.close();
                    Server.numberOfClient--;
                    return;
                }
                System.out.println("\n["+Thread.currentThread().getName()+"] Client déconnecté\n");
                Server.userConnected.remove(this.userConnecting);
                Server.numberOfClient--;
                this.sck.close();
                return;

            }catch (SocketException e){
                System.out.println("\n["+Thread.currentThread().getName()+"] Client déconnecté\n");
                Server.userConnected.remove(this.userConnecting);
                this.sck.close();
                Server.numberOfClient--;
                return;
            }
        }catch (IOException e){e.printStackTrace();}
    }
}

