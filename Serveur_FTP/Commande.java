import java.io.PrintStream;
import java.io.File;

public abstract class Commande {
	
	protected PrintStream ps;
	protected String commandeNom = "";
	protected String [] commandeArgs ;

	protected static String CWD = new File(".").getAbsolutePath();

	public Commande(PrintStream ps, String commandeStr) {
		this.ps = ps ;
		String [] args = commandeStr.split(" ");
		commandeNom = args[0];
		commandeArgs = new String[args.length-1];
		
		for(int i=0; i<commandeArgs.length; i++) {
			commandeArgs[i] = args[i+1];
		}
	}
	
	public abstract void execute();

}
