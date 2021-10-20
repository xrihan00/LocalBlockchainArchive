package vut.fekt.archive.app;


import vut.fekt.archive.Archive;

import java.io.File;
import java.io.IOException;

public class App {
    public static  Archive archive;

    public static void main(String[] args) {

        NewArchive na = new NewArchive();
        na.init();
        NewDocument nd = new NewDocument();

        MainApp mainApp = new MainApp(na,nd);
        mainApp.initMainapp();
        mainApp.frame.setVisible(true);

        Thread t = new Thread(new Runnable() {
            boolean archiveLoaded =false;
            boolean newDoc = false;
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
                            archive.loadArchiveBlockchain(new File(na.getDirectory()+"/serializedBlockchain.txt"));
                            System.out.println("AAAAAA");
                            mainApp.setArchive(archive);
                            archiveLoaded = true;
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    if(nd.ok==true&&newDoc==false){
                        try {
                            archive.addDocument(nd.getNewContent(),nd.getAuthorName(), nd.getDocumentName(),"1.0");
                            archive.saveArchiveBlockchain();
                            newDoc= true;
                            //nd= new NewDocument();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }
                    if(archive==null&&mainApp.getArchive()!=null){
                        archive = mainApp.getArchive();
                    }

                }
            }
        });
        t.start();

      //  na.frame.setVisible(true);

    }


}
