package fr.l3.application.client_ftp.controller;

import fr.l3.application.client_ftp.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ConnexionController implements Initializable {

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

    public void stopConnexion(){
        MainApp.setCommunicationService(null);
    }

    public void startConnexion(){
        boolean canConnect = false;

        String errorText = "";

        if(hoteField.getText().isBlank()){
            errorText += "- nom d'hôte\n";
            canConnect = false;
        }else{
            canConnect = true;
        }

        if(usernameField.getText().isBlank()){
            errorText += "- nom d'utilisateur\n";
            canConnect = false;
        }else{
            canConnect = true;
        }

        if(passwordField.getText().isBlank()){
            errorText += "- mot de passe\n";
            canConnect = false;
        }else{
            canConnect = true;
        }

        if(portField.getText().isBlank()){
            errorText += "- port du serveur\n";
            canConnect = false;
        }else{
            canConnect = true;
        }

        if(!canConnect){
           Alert alert = new Alert(Alert.AlertType.WARNING);
           MainApp.getConsoleController().addError("Connexion Impossible");
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
                MainApp.getConsoleController().addError("Numéro de port invalide");
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


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        connexionButton.setOnAction((ActionEvent e) -> {this.startConnexion();});
    }
}
