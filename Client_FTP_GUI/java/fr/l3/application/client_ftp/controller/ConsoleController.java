package fr.l3.application.client_ftp.controller;


import fr.l3.application.client_ftp.MainApp;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.util.ResourceBundle;

public class ConsoleController implements Initializable {

    private static int lineNumber = 0;
    private static ScrollPane sp;

    public static void setScrollpan(ScrollPane sp){
        ConsoleController.sp = sp;
    }

    @FXML
    private TextFlow textFlow;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void addError(String error){
        this.addText(error);
        Platform.runLater(()->{
        ((Text)this.textFlow.getChildren().get(ConsoleController.lineNumber-1)).setFill(Color.RED);});
    }

    public void addInfo(String info){
        this.addText(info);
        Platform.runLater(()->{
            ((Text)this.textFlow.getChildren().get(ConsoleController.lineNumber-1)).setFill(Color.BLUEVIOLET);});
    }



    public synchronized void addText(String text){
        Platform.runLater(()->{
            textFlow.getChildren().add(new Text(text+"\n"));
            System.out.println(text);
            ConsoleController.lineNumber++;
            sp.setVvalue(1.0);
            sp.layout();
            });

    }

    public synchronized void appendText(String text){
        Platform.runLater(()->{
            textFlow.getChildren().add(new Text(text));
            sp.setVvalue(1.0);
            sp.layout();
        });
    }

}
