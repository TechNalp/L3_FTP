import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class Server implements Runnable{
    private static int numberOfClient = 0;
    public int clientNumber;
    Socket sck;
    BufferedReader br;
    PrintStream ps;
    static String userDataPath = "./USERS/";
    String CWD =".";
    boolean userOk = false;
    boolean pwOk = false;
    String userConnecting = "";
    final static int  firstAvailablePort = 3500;
    static volatile List<Integer> availablePort = new ArrayList<>();

    public Server(Socket sck){
        this.clientNumber = Server.numberOfClient++;
        this.sck = sck;
        try {
            this.CWD = new File(".").getCanonicalPath();
            this.br = new BufferedReader(new InputStreamReader(this.sck.getInputStream()));
            this.ps = new PrintStream(this.sck.getOutputStream());
        } catch (IOException e) {}
    }

    public void run(){
        Thread.currentThread().setName("Serveur client : "+this.clientNumber);
        try {
            try {
                int verifNumber = 103; // Si le client nous renvoie ce nombre incrémenté de 48, on est sûr qu'il s'agit d'un client compatible
                this.sck.getOutputStream().write(verifNumber); // Permet d'indiquer au client que l'on est le bon serveur
                int recu = this.sck.getInputStream().read();
                if(recu!=(verifNumber+48)){
                    this.sck.close();
                    System.out.println("La connexion n'a pas pu être vérifié");
                    return;
                }
                this.ps.println("1 Bienvenue ! ");
                this.ps.println("1 Serveur FTP Personnel.");
                this.ps.println("0 Authentification : ");

                String commande = "";
                try {
                    while (!(commande = this.br.readLine()).equals("bye")) {
                        System.out.println("[THREAD] >> " + commande);
                        CommandExecutor.executeCommande(ps, commande, this);
                    }
                }catch (NullPointerException e){
                    System.out.println("\n[THREAD] 1 Client déconnecté\n");

                    this.sck.close();
                    return;
                }
                System.out.println("\n[THREAD] 2 Client déconnecté\n");

                this.sck.close();

            }catch (SocketException e){
                e.printStackTrace();
                System.out.println("\n[THREAD] 3 Client déconnecté\n");
                this.sck.close();
            }
        }catch (IOException e){e.printStackTrace();}
    }
}

