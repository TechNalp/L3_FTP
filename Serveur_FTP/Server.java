import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class Server implements Runnable{

    Socket sck;
    BufferedReader br;
    PrintStream ps;
    static String userDataPath = "./USERS/";
    String CWD =".";
    boolean userOk = false;
    boolean pwOk = false;
    String userConnecting = "";
    final static int  firstAvailablePort = 3500;
    static List<Integer> availablePort = new ArrayList<>();

    public Server(Socket sck){
        this.sck = sck;
        try {
            this.CWD = new File(".").getCanonicalPath();
            this.br = new BufferedReader(new InputStreamReader(this.sck.getInputStream()));
            this.ps = new PrintStream(this.sck.getOutputStream());
        } catch (IOException e) {}
    }

    public void run(){
        try {
            try {
                this.ps.println("1 Bienvenue ! ");
                this.ps.println("1 Serveur FTP Personnel.");
                this.ps.println("0 Authentification : ");

                String commande = "";

                while (!(commande = this.br.readLine()).equals("bye")) {
                    System.out.println("[THREAD] >> " + commande);
                    CommandExecutor.executeCommande(ps, commande, this);
                }

                System.out.println("\n[THREAD] Client déconnecté\n");
                this.sck.close();

            }catch (SocketException e){
                System.out.println("\n[THREAD] Client déconnecté\n");
                this.sck.close();
            }
        }catch (IOException e){}
    }
}

