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


        //thread checkuje okna newArchive a NewDocument jestli s ními uživatel interagoval
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
                    if(na.ok==true){ //pokud uživatel kliknul ok u newarchive, tak je vytvořen nový archiv a předán MainApp aby s ním šlo dál pracovat
                        try {
                            archive = new Archive(na.getName(),na.getDirectory());
                            archive.saveArchiveBlockchain();
                            System.out.println("AAAAAA");
                            mainApp.setArchive(archive);
                            mainApp.updateList();
                            mainApp.archiveLabel.setText(archive.getName());
                            na.ok = false;
                            //archiveLoaded = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if(nd.ok==true){
                        try {
                            archive.addDocument(nd.getNewContent(),nd.getAuthorName(), nd.getDocumentName(),"1.0");
                            archive.saveArchiveBlockchain();
                            mainApp.updateList();
                            nd.ok = false;
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
