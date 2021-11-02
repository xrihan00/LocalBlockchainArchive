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
        ShowDocument sd = new ShowDocument();

        MainApp mainApp = new MainApp(na,nd,sd);
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
                            na.ok = false;
                            archive = new Archive(na.getName(),na.getDirectory());
                            archive.saveArchiveBlockchain();
                            System.out.println("AAAAAA");
                            mainApp.setArchive(archive);
                            mainApp.updateList();
                            mainApp.archiveLabel.setText(archive.getName());

                            //archiveLoaded = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if(nd.ok==true){
                        try {
                            nd.ok = false;
                            archive.addDocument(nd.getNewContent(),nd.getAuthorName(), nd.getDocumentName(),nd.getVersion());
                            archive.saveArchiveBlockchain();
                            mainApp.updateList();

                            //nd= new NewDocument();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }
                    if(sd.newVersion ==true){
                        sd.newVersion=false;
                        nd.initVersion(sd.getDoc().getDocName(),sd.getDoc().getAuthor(),sd.getNewVersionCount());

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
