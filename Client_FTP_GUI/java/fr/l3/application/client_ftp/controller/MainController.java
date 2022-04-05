package fr.l3.application.client_ftp.controller;

import fr.l3.application.client_ftp.Client;
import fr.l3.application.client_ftp.MainApp;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ResourceBundle;



public class MainController implements Initializable{

    private static int lineNumber = 0; // Nombre actuelle de ligne de la console

    //********** Header ***********//

    private String clientCWD = ".";

    @FXML
    public Button connexionButton;

    @FXML
    TextField hoteField;

    @FXML
    TextField usernameField;

    @FXML
    TextField portField;

    @FXML
    PasswordField passwordField;

    //********** Header ***********//

    //********** Console ***********//

    @FXML
    TextFlow textFlow;

    @FXML
    ScrollPane consoleScrollPan;


    //********** Console ***********//

    //********** arbreFichier Serveur ***********//

    @FXML
    TreeView treeServer;

    @FXML
    Label   treeServerLabel;

    //********** arbreFichier Serveur ***********//

    //********** arbreFichier Client ***********//
        @FXML
        TreeView treeClient;

    //********** arbreFichier Client ***********//



    //------------ CONSOLE CONTROL --------//
    @FXML
    public void addError(String error){
        this.addText(error);
        Platform.runLater(()->{
            ((Text)textFlow.getChildren().get(MainController.lineNumber-1)).setFill(Color.RED);});
    }
    @FXML
    public void addInfo(String info){
        this.addText(info);
        Platform.runLater(()->{
            ((Text)textFlow.getChildren().get(MainController.lineNumber-1)).setFill(Color.BLUEVIOLET);});
    }


    @FXML
    public synchronized void addText(String text){
        Platform.runLater(()->{
            textFlow.getChildren().add(new Text(text+"\n"));
            System.out.println(text);
            MainController.lineNumber++;
            consoleScrollPan.setVvalue(1.0);
            consoleScrollPan.layout();
        });

    }


    //------------ CONSOLE CONTROL --------//

    //------------ CONNEXION_BUTTONS CONTROL --------//
    @FXML
    public String[] getConnectionIds(){
        String[] ids = {usernameField.getText().substring(0,usernameField.getText().length()),passwordField.getText().substring(0,passwordField.getText().length())};
        return ids;
    }
    @FXML
    public void startConnexion(){
        int canConnect = 0;

        String errorText = "";

        if(hoteField.getText().isBlank()){
            errorText += "- nom d'hôte\n";
            canConnect++;
        }

        if(usernameField.getText().isBlank()){
            errorText += "- nom d'utilisateur\n";
            canConnect++;
        }

        if(passwordField.getText().isBlank()){
            errorText += "- mot de passe\n";
            canConnect++;
        }

        if(portField.getText().isBlank()){
            errorText += "- port du serveur\n";
            canConnect++;
        }

        if(canConnect>0){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            MainApp.getMainController().addError("Connexion Impossible");
            alert.setTitle("Connexion Impossible");
            alert.setHeaderText("Informations manquantes");
            alert.setContentText(errorText);
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(MainApp.class.getResource("icone.png").toString()));
            alert.showAndWait();
        }else{
            String hote = hoteField.getText();
            int port = -1 ;
            try {
                port = Integer.parseInt(portField.getText());
            }catch (NumberFormatException e){
                Alert alert = new Alert((Alert.AlertType.WARNING));
                MainApp.getMainController().addError("Numéro de port invalide");
                alert.setTitle("Information erronée");
                alert.setHeaderText("Numéro de port invalide");
                alert.setContentText("Veuillez saisir un numéro de port valide");
                ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(MainApp.class.getResource("icone.png").toString()));
                alert.showAndWait();
            }
            if(port!=-1) {
                MainApp.startCommunicationService(hote, port);
            }
        }

    }

    //------------ CONNEXION_BUTTONS CONTROL --------//


    //------------ ARBRE_SERVEUR CONTROL --------//
        @FXML
        public void drawServerTree(){


           try {
               Client.analyseCmdSend("ls");
           } catch (IOException e) {
               e.printStackTrace();
           }
           while (!Client.canDisplayTree());
           Platform.runLater(()->{
               Path path = Paths.get(Client.cwd).getRoot();
               TreeItem<String> rootItem = new TreeItem<String>(path.toString(),new ImageView(new Image(MainApp.class.getResource("folder_logo.png").toString(),16,16,true,false)));
               treeServer.setRoot(rootItem);
               Client.setDisplayTree(false);
               String currentPath = Paths.get(Client.cwd).getRoot().toString();

               TreeItem<String> current = treeServer.getRoot();

               for (int i = 0; i < Paths.get(Client.cwd).getNameCount(); i++) {

                   try {
                       Client.analyseCmdSend("ls "+currentPath);
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
                   //Creer TreeItem Temp

                   //ajouter au parent

                  current.getChildren().add(new TreeItem<String>(Paths.get(Client.cwd).getName(i).toString(),new ImageView(new Image(MainApp.class.getResource("folder_logo.png").toString(),16,16,true,false))));
                   currentPath = currentPath+File.separator+Paths.get(Client.cwd).getName(i);

                   current = current.getChildren().get(0);



                   //Paths.get(Client.cwd).getName(i);

               }



           });

        }

    //------------ ARBRE_SERVEUR CONTROL --------//


    //------------ ARBRE_CLIENT CONTROL --------//

        @FXML
        public void drawClientTree(){
            try {
                TreeItem<File> rootItem = new TreeItem<File>(new File(new File(this.clientCWD).getCanonicalPath()),new ImageView(new Image(MainApp.class.getResource("folder_logo.png").toString(),16,16,true,false)));
                rootItem.setExpanded(true);
            treeClient.setRoot(rootItem);


            if(new File(new File(this.clientCWD).getCanonicalPath()).getParent() != null){
                rootItem.getChildren().add(new TreeItem<>(new File(".."),new ImageView(new Image(MainApp.class.getResource("folder_logo.png").toString(),16,16,true,false))));
            }

            new Thread(()-> {
                File[] fileList = new File(this.clientCWD).listFiles();
                ;
                for (File f : fileList) {
                    f = new File(f.getName());
                    try {
                        System.out.println(f.getCanonicalPath() + " " + Files.isDirectory(f.toPath()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (f.isDirectory()) {
                        if (f.isHidden()) {
                            rootItem.getChildren().add(new TreeItem<>(f, new ImageView(new Image(MainApp.class.getResource("hide_folder_logo.png").toString(), 16, 16, true, false))));
                        } else {
                            rootItem.getChildren().add(new TreeItem<>(f, new ImageView(new Image(MainApp.class.getResource("folder_logo.png").toString(), 16, 16, true, false))));
                        }

                    } else {
                        rootItem.getChildren().add(new TreeItem<>(f, new ImageView(new Image(MainApp.class.getResource("file_logo.png").toString(), 16, 16, true, false))));
                    }

                }
            }).start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    //------------ ARBRE_CLIENT CONTROL --------//




    @FXML
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        drawClientTree();
        connexionButton.setOnAction((ActionEvent e) -> {this.startConnexion();});
        treeClient.setOnDragDetected(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (MainApp.getCommunicationService() != null && MainApp.getCommunicationService().isConnected()) {
                    if(((TreeItem<File>)treeClient.getSelectionModel().getSelectedItem()).getValue().getName().equals("..") || treeClient.getSelectionModel().getSelectedItem() == treeClient.getRoot()){
                        return;
                    }
                    TreeItem<File> ti = (TreeItem<File>) treeClient.getSelectionModel().getSelectedItem();
                    Dragboard db = treeClient.startDragAndDrop(TransferMode.ANY);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(ti.getValue().getName());
                    db.setContent(content);
                    //System.out.println(ti.getValue().getName());
                    mouseEvent.consume();


                } else {
                   MainController.this.addError("Vous devez être connecté au serveur pour lancer un transfert");
                }
            }
        });

        treeServer.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent dragEvent) {
                if(dragEvent.getGestureSource() != treeServer && dragEvent.getDragboard().hasString()){
                    dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
                dragEvent.consume();
            }
        });

        treeServer.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent dragEvent) {

                if(dragEvent.getGestureSource() != treeServer && dragEvent.getDragboard().hasString()){
                    try {
                        Client.analyseCmdSend("stor "+dragEvent.getDragboard().getString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                dragEvent.consume();
                treeServer.setCursor(Cursor.DEFAULT);
            }
        });

        treeClient.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                    if(mouseEvent.getClickCount() == 2){
                        if(((TreeItem<File>)treeClient.getSelectionModel().getSelectedItem()).getValue().getName().equals("..")){


                                MainController.this.clientCWD = MainController.this.clientCWD+File.separator+"..";


                            System.err.println("22 "+MainController.this.clientCWD);
                                drawClientTree();

                        }else{
                            if(((TreeItem<File>)treeClient.getSelectionModel().getSelectedItem()).getValue().isDirectory()){
                                /*try {
                                   // drawClientTree(((TreeItem<File>)treeClient.getSelectionModel().getSelectedItem()).getValue().getCanonicalPath());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }*/
                            }
                        }
                    }
                }

                mouseEvent.consume();
            }
        });
    }
}
