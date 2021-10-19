package vut.fekt.archive.app;


import vut.fekt.archive.Archive;

import java.io.IOException;

public class App {
    public static  Archive archive;

    public static void main(String[] args) {

        NewArchive na = new NewArchive();
        na.init();
        MainApp mainApp = new MainApp(na);
        mainApp.initMainapp();
        mainApp.frame.setVisible(true);

        Thread t = new Thread(new Runnable() {
            boolean archiveLoaded =false;
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(na.ok==true&&archiveLoaded==false){
                        try {
                            archive = new Archive(na.getName(),na.getDirectory());
                            System.out.println("AAAAAA");
                            mainApp.setArchive(archive);
                            archiveLoaded = true;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        });
        t.start();

      //  na.frame.setVisible(true);

    }


}
