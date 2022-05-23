package vut.fekt.archive.app;


public class App {
    //hlavní metoda validační aplikace
    public static void main(String[] args) {

        ShowDocument sd = new ShowDocument();
        NewUser nu = new NewUser();
        nu.init();
        //nu.getFrame().setVisible(true);

        MainApp mainApp = new MainApp(sd,nu);
        mainApp.initMainapp();
        mainApp.frame.setVisible(true); //hlavní okno je po spuštění jediné zviditelněno
        //Thread který kontroluje jednotlivá okna
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (mainApp.docConfirmation) {
                        sd.frame.setVisible(true);
                        mainApp.docConfirmation = false;
                    }
                    if ((sd.result.equals("confirmed") || sd.result.equals("rejected"))&& !mainApp.client.confirmation) {
                        mainApp.sd.getFrame().setVisible(false);
                        sd.frame.setVisible(false);
                    }
                    if (mainApp.newUserClicked) {
                        mainApp.nu.getFrame().setVisible(true);
                        mainApp.newUserClicked = false;
                    }
                    if (mainApp.nuDone) {
                        nu.getFrame().setVisible(false);
                        mainApp.nuDone = false;
                    }
                }
            }
        });
        t.start();
    }

}
