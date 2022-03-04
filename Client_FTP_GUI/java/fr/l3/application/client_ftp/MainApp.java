package fr.l3.application.client_ftp;

import fr.l3.application.client_ftp.controller.ConnexionController;
import fr.l3.application.client_ftp.controller.ConsoleController;
import fr.l3.application.client_ftp.service.CommunicationService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.Console;
import java.io.IOException;

public class MainApp extends Application {

    private Stage mainWindow;
    private Pane rootLayout;

    private static CommunicationService cs = null;

    private static ConsoleController cc;
    private static ConnexionController coc;

    private static String username;

    public static String getUsername(){
        return MainApp.username;
    }

    public static void setUsername(String username){
        MainApp.username = username;
    }

    public static ConsoleController getConsoleController(){
        return MainApp.cc;
    }

    public static ConnexionController getConnexionController(){
        return MainApp.coc;
    }

    public static CommunicationService getCommunicationService(){
        return MainApp.cs;
    }

    public static void setCommunicationService(CommunicationService cs){
        MainApp.cs = cs;
    }

    @Override
    public void start(Stage mainWindow) throws IOException {
        this.intitMainWindow(mainWindow);
        this.loadMainWindow();
        this.loadConnexionButtons();
        this.loadConsole();
        this.mainWindow.show();
    }

    public void intitMainWindow(Stage mainWindow){
        this.mainWindow = mainWindow;
        this.mainWindow.setTitle("Client FTP - Mathis PLANCHET");
        this.mainWindow.setWidth(1000);
        this.mainWindow.setHeight(600);
        this.mainWindow.setMinHeight(550);
        this.mainWindow.setMinWidth(700);
        this.mainWindow.getIcons().add(new Image("file:icone.png"));
    }


    private void loadMainWindow()  throws IOException{
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainApp.class.getResource("view/mainView.fxml"));
        this.rootLayout = (Pane) loader.load();
        //((MenuBar)((AnchorPane)this.rootLayout.getChildren().get(0)).getChildren().get(0)).getMenus().get(0).getItems().get(0).setOnAction((ActionEvent t) -> {new CommunicationService().start();});
        Scene scene = new Scene(this.rootLayout);
        this.mainWindow.setScene(scene);
    }

    //public void

    private void loadConsole() throws IOException{
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainApp.class.getResource("view/console.fxml"));
        AnchorPane ap = (AnchorPane) loader.load();
        this.rootLayout.getChildren().add(ap);
        ScrollPane sp = (ScrollPane)ap.getChildren().get(0);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        ConsoleController.setScrollpan(sp);
        MainApp.cc = loader.getController();
        MainApp.cc.addText("Console Initialisé");
    }

    private void loadConnexionButtons() throws IOException{
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainApp.class.getResource("view/connexionButtons.fxml"));
        AnchorPane ap = (AnchorPane) loader.load();
        ((AnchorPane)this.rootLayout.getChildren().get(0)).getChildren().add(ap);
        MainApp.coc = loader.getController();
    }

    public static void startCommunicationService(String hote, int port){
        if(MainApp.cs == null){
            MainApp.cs = new CommunicationService(hote,port);
            MainApp.cs.start();
            //
        }else {
            if (MainApp.cs.isConnected() || MainApp.cs.isRunning()){
                MainApp.cc.addError("Une connexion est déjà en cours");
            }else{
                MainApp.cs.setHote(hote);
                MainApp.cs.setPort(port);
                MainApp.cs.restart();
            }

        }
    }

    public static void main(String[] args){
        launch(args);
    }

}

