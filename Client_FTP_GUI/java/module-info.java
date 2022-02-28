module fr.l3.application.client_ftp_gui {
    requires javafx.controls;
    requires javafx.fxml;


    opens fr.l3.application.client_ftp to javafx.fxml;
    exports fr.l3.application.client_ftp;
    exports fr.l3.application.client_ftp.controller;
    opens fr.l3.application.client_ftp.controller to javafx.fxml;
}