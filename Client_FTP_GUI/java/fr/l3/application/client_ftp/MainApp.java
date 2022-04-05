package fr.l3.application.client_ftp;


import fr.l3.application.client_ftp.controller.MainController;
import fr.l3.application.client_ftp.service.CommunicationService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class MainApp extends Application {

    private Stage mainWindow;



    private Pane rootLayout;

    private static CommunicationService cs = null;

    private static MainController cc;

    private static String username;

    public Pane getRootLayout() {
        return rootLayout;
    }

    public static MainController getMainController(){
        return MainApp.cc;
    }


    public static CommunicationService getCommunicationService(){
        return MainApp.cs;
    }


    @Override
    public void start(Stage mainWindow) throws IOException {

        this.intitMainWindow(mainWindow);
        this.loadMainWindow();
        this.mainWindow.show();
    }

    public void intitMainWindow(Stage mainWindow){
        this.mainWindow = mainWindow;
        this.mainWindow.setTitle("Client FTP - Mathis PLANCHET");
        this.mainWindow.setWidth(1000);
        this.mainWindow.setHeight(600);
        this.mainWindow.setMinHeight(550);
        this.mainWindow.setMinWidth(700);
        this.mainWindow.getIcons().add(new Image(MainApp.class.getResource("icone.png").toString()));
    }


    private void loadMainWindow()  throws IOException{
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainApp.class.getResource("view/mainView.fxml"));
        this.rootLayout = (Pane) loader.load();
        ScrollPane sp = (ScrollPane) this.rootLayout.lookup("#consoleScrollPan");
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        MainApp.cc = loader.getController();
        Scene scene = new Scene(this.rootLayout);
        this.mainWindow.setScene(scene);
    }



    public static void startCommunicationService(String hote, int port){
        if(MainApp.cs == null){
            MainApp.cs = new CommunicationService(hote,port);
            MainApp.cs.start();
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


    public static File[] getFileList(String path){
        return new File(path).listFiles();
    }


    public static void main(String[] args){
        launch(args);
    }

}

