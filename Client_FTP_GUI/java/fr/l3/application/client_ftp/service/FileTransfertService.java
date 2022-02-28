package fr.l3.application.client_ftp.service;

import fr.l3.application.client_ftp.Client;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class FileTransfertService extends Service<Void> {

    String fileName;
    int port;
    char type;

    public FileTransfertService(String fileName,int port, char type){
        this.fileName = fileName;
        this.port = port;
        this.type = type;
    }

    @Override
    public Task<Void> createTask(){
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {

                    Socket sck_file;
                    sck_file = new Socket(Client.hostname, port);
                    if(FileTransfertService.this.type=='E'){

                        BufferedInputStream is = new BufferedInputStream(new FileInputStream(FileTransfertService.this.fileName));
                        BufferedOutputStream os = new BufferedOutputStream(sck_file.getOutputStream());

                        for (int oct = is.read(); oct != -1; oct = is.read()) {
                            os.write(oct);
                        }

                        is.close();
                        os.close();


                    }else if(FileTransfertService.this.type=='R') {
                        BufferedInputStream is = new BufferedInputStream(sck_file.getInputStream());
                        BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(FileTransfertService.this.fileName));

                        for (int oct = is.read(); oct != -1; oct = is.read()) {
                            os.write(oct);
                        }

                        is.close();
                        os.close();

                    }
                    sck_file.close();
                } catch (UnknownHostException e) {} catch (IOException e) {}

                return null;
            }
        };
    }
}
