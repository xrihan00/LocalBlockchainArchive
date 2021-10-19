package vut.fekt.archive.app;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class App {

    public static void main(String[] args) {

        NewArchive na = new NewArchive();
        na.init();
        MainApp mainApp = new MainApp(na);
        mainApp.initMainapp();
        mainApp.frame.setVisible(true);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(na.ok==true){
                        mainApp.FileLabel.setText(na.getName()); //YES!!!!!!!!!!!!
                    }

                }
            }
        });
        t.start();

      //  na.frame.setVisible(true);

    }


}
