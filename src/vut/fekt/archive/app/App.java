package vut.fekt.archive.app;


import vut.fekt.archive.Archive;

public class App {
    public static Archive archive;

    //hlavní metoda celé  aplikace
    public static void main(String[] args) {

        ShowDocument sd = new ShowDocument();

        MainApp mainApp = new MainApp(sd);
        mainApp.initMainapp();
        mainApp.frame.setVisible(true); //hlavní okno je po spuštění jediné zviditelněno


    }

}
