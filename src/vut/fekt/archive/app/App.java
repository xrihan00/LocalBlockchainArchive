package vut.fekt.archive.app;


import vut.fekt.archive.Archive;
import vut.fekt.archive.Connection;

import javax.swing.*;
import java.io.IOException;
import java.util.List;

public class App {
    public static  Archive archive;

    //hlavní metoda celé  aplikace
    public static void main(String[] args) {

        Client client = new Client();
        try {
            client.createConnection();
            System.out.println("Conenction succesful");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //inicializuje jednotlivá okna aplikace
        NewArchive na = new NewArchive();
        na.init();
        NewDocument nd = new NewDocument();
        ShowDocument sd = new ShowDocument();

        MainApp mainApp = new MainApp(na,nd,sd);
        mainApp.initMainapp();
        mainApp.frame.setVisible(true); //hlavní okno je po spuštění jediné zviditelněno



        //thread kontroluje okna newArchive a NewDocument jestli s ními uživatel interagoval
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(5); //krátký sleep aby thread nebyl příliš aktivní
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //pokud uživatel kliknul ok u newarchive, tak je vytvořen nový archiv a předán MainApp aby s ním šlo dál pracovat
                    //hodnota "ok" je změněna na true, což zachytí thread
                    if(na.ok==true){
                        try {
                            na.ok = false; //změní ok zpět na false aby prošel podmínku jen jednou
                            archive = new Archive(na.getName(),na.getDirectory()); //vytvoří nový archiv na základě zadaných hodnot
                            archive.generateKeys(); //vygeneruje klíče pro daný archiv
                            mainApp.setArchive(archive); //předá hlavnímu oknu archiv
                            mainApp.updateList();
                            mainApp.archiveLabel.setText(archive.getName());
                            client.setVypis("Test");

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    //pokud uživatel kliknu ok u přidávání dokumentů, spustí se tato podmínka a do archivu je přidán dokument
                    if(nd.ok==true){
                        try {
                            nd.ok = false;
                            //přidání nového dokumentu na základě zadaných hodnot
                            archive.addDocument(nd.getNewContent(),nd.getAuthorName(), nd.getDocumentName(),nd.getVersion());
                            archive.saveArchiveBlockchain();
                            mainApp.updateList();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }
                    //pokud uživatel stisknu novou verzi v okně showdocument
                    //aplikace inicializuje přidávání dokumentů s předvyplněnými hodnotami
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

        String v = "";
        Thread vypis = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(5);
                        if(!mainApp.getVypis().equals(client.vypis)){
                            System.out.println("We got here!");
                            mainApp.setVypis(client.vypis);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        vypis.start();

    }

    // výsledný výpis pomocí objektu SwingWorker

}
