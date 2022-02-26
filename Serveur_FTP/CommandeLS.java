import java.io.IOException;
import java.io.PrintStream;
import java.io.File;
import java.nio.file.*;
import java.util.Calendar;

public class CommandeLS extends Commande {

    public CommandeLS(PrintStream ps, String commandeStr, Server srv) {
        super(ps, commandeStr, srv);
    }

    public void execute() {
        File file;
        if (commandeArgs.length == 0) {
            file = new File(this.srv.CWD);
        } else {
            file = new File(this.srv.CWD + FileSystems.getDefault().getSeparator() + commandeArgs[0]);
        }
        try {
            if(file.exists()){
                if(!file.isDirectory()){

                        this.ps.println("2 "+file.getCanonicalFile()+" n'est pas un répertoire");
                        return;

                }
            }else{
                file = new File(commandeArgs[0]);
                if(file.exists()){
                    if(!file.isDirectory()){
                        this.ps.println("2 "+file.getCanonicalFile()+" n'est pas un répertoire");
                        return;
                    }
                }else{
                    this.ps.println("2 "+file.getCanonicalFile()+" n'existe pas");
                    return;
                }
            }
        } catch (IOException e) {}

        String[] month = new String[]{"jan.", "fev.", "mars", "avr.", "mai ", "juin", "jui.", "août", "sep.", "oct.", "nov.", "dec."};

        int maxNameLength = 0;
        int maxSizeLength = 0;
        for (File f : file.listFiles()) {
            if (f.getName().length() > maxNameLength) {
                maxNameLength = f.getName().length();
            }

            try {
                int currentSizeLength = Long.toString(Files.size(Paths.get(f.getAbsolutePath()))).length();
                if (currentSizeLength > maxSizeLength) {
                    maxSizeLength = currentSizeLength;
                }
            } catch (IOException e) {
            }


        }
        String entete = null;
        try {
            entete = "1 ------------------------Listing de : \"" + file.getCanonicalPath() + "\" -------------------------------";
        } catch (IOException e) {
            e.printStackTrace();
        }
        ps.println("1 " + entete);
        Calendar c = Calendar.getInstance();
        for (File f : file.listFiles()) {
            c.setTimeInMillis(f.lastModified());

            if (f.isDirectory()) {
                ps.print("d");
            } else {
                ps.print("-");
            }

            if (f.canRead()) {
                ps.print("r");
            } else {
                ps.print("-");
            }

            if (f.canWrite()) {
                ps.print("w");
            } else {
                ps.print("-");
            }

            if (f.canExecute()) {
                ps.print("x");
            } else {
                ps.print("-");
            }
            ps.print("  " + f.getName());

            for (int i = f.getName().length(); i < maxNameLength + 1; i++) {
                ps.print(" ");
            }


            try {
                ps.print(" " + Files.size(Paths.get(f.getAbsolutePath())));
                for (long i = Long.toString(Files.size(Paths.get(f.getAbsolutePath()))).length(); i < maxSizeLength + 1; i++) {
                    ps.print(" ");
                }

            } catch (IOException e) {
            }

            ps.print("  ");
            if (c.get(Calendar.DAY_OF_MONTH) < 10) {
                ps.print(" ");
            }
            ps.print(c.get(Calendar.DAY_OF_MONTH));

            ps.print(" ");

            ps.print(month[c.get(Calendar.MONTH)]);

            if (c.get(Calendar.YEAR) != (Calendar.getInstance().get(Calendar.YEAR))) {
                ps.print(" " + c.get(Calendar.YEAR));
            } else {
                ps.print(" ");
                if (c.get(Calendar.HOUR_OF_DAY) < 10) {
                    ps.print("0");
                }
                ps.print(c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE));
            }

            ps.println(" ");


        }

        ps.print("0 ");
        for (int i = 0; i < entete.length(); i++) {
            ps.print("-");
        }
        ps.println("");
    }
}

