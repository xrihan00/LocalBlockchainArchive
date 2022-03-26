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

        ShowDocument sd = new ShowDocument();

        MainApp mainApp = new MainApp(sd);
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

                    //pokud uživatel stisknu novou verzi v okně showdocument
                    //aplikace inicializuje přidávání dokumentů s předvyplněnými hodnotami
                    if(sd.newVersion ==true){
                        sd.newVersion=false;

                    }

                }
            }
        });
        t.start();

        String v = "";


    }

}
