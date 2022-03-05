package fr.l3.application.client_ftp.controller;

import fr.l3.application.client_ftp.MainApp;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;



public class MainController implements Initializable{

    private static int lineNumber = 0; // Nombre actuelle de ligne de la console

    //********** Header ***********//

    @FXML
    Button connexionButton;

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


    //------------ ARRBRE_SERVEUR CONTROL --------//

    //------------ ARRBRE_SERVEUR CONTROL --------//

    @FXML
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        connexionButton.setOnAction((ActionEvent e) -> {this.startConnexion();});
    }
}
