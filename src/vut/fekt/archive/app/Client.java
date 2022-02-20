package vut.fekt.archive.app;

import vut.fekt.archive.Connection;
import vut.fekt.archive.blockchain.Block;

import java.io.*;
import java.util.Base64;
import java.util.LinkedList;
import java.util.StringTokenizer;

public class Client {
    public Connection connection;
    public String vypis = " ";

    public void createConnection() throws IOException {                 // je vytvořeno spojení se serverem
        connection = new Connection();
        connection.initialize();
        Thread receive = new Thread(new Runnable(){
            @Override
            public void run() {
                while (true) {
                    if(connection.getReceivedData()!=null) {            // kontroluje se proměnná recievedData ve třídě Connection
                        try {                                           // pokud jsou přijata data, tak se využije parsování, kdy podle kódu určíme co se bude dělat dále
                            parse(connection.getReceivedData());
                        } catch (IOException |ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        connection.setReceivedData(null);
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        receive.start();
    }

    public void send(String msg, String name) throws InterruptedException {                 // k odeslání dat využívá klient metodu send ve třídě connection
        connection.send(msg + "#" + name);
    }

    public void parse(String message) throws IOException, ClassNotFoundException {          // z přijatých dat se opět provede parsování a podle kódu se provádí následné akce
        StringTokenizer st = new StringTokenizer(message, ";");
        String source = st.nextToken();
        String code = st.nextToken();
        String msg = st.nextToken();
        switch(code){


            case "23":
                vypis+="Špatné údaje!\n";                   // další kódy, které obsahují různé chybové hlášky
                System.out.println("Špatné údaje!");
                break;


            case "27":
                setVypis("Jméno není v seznamu voličů!\n");
                System.out.println("Jméno není v seznamu voličů!");
                break;
            case "28":
                setVypis("Už máte klíče!\n");
                System.out.println("Už máte klíče!!");
                break;
            case "31":                                              // při přijetí kódu 31 se provede validaci blockchainu
                vypis+="Blockchain přijat!\n";
                System.out.println("Blockchain received");
                LinkedList<Block> blocks = (LinkedList<Block>) deserialize(msg);


            case "32":
                System.out.println("Transaction received: " + msg);
                break;
            default:
                System.out.println("Unknown message.");
        }
    }

    public void setVypis(String vypis) {
        this.vypis = vypis;
    }

    private static String serialize(Serializable o) throws IOException {            // serializace objektů do Stringu pro přenos
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public static Object deserialize(String s)   {                                  // u přijatých dat se provede deserializace - dekódování
        Object o = null;
        try {
            byte[] data = Base64.getDecoder().decode(s);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            o = ois.readObject();
            ois.close();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return o;
    }
}
