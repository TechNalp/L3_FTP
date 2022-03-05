package fr.l3.application.client_ftp.object;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Fichier {

    String name;

    String path;

    long size;

    Icon icone;

    String type;

    long derniereModification;

    public void toFichier(String ligne){
        if(ligne.startsWith("d")){
            this.type = "Dossier";
        }else{
            this.type = "Fichier";
        }
    }

}
