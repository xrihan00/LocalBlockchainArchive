package vut.fekt.archive.app;

import vut.fekt.archive.Connection;
import vut.fekt.archive.blockchain.Block;
import vut.fekt.archive.blockchain.Blockchain;

import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedList;
import java.util.StringTokenizer;

public class Client {
    public Connection connection;
    public String vypis = " ";
    public boolean isAuthorized = false;
    public boolean newVypis = false;
    public boolean newBlock = false;
    public File[] newFiles = null;
    public Blockchain blockchain = null;
    public ArrayList<Blockchain> chains = null;
    public Blockchain newBlockchain = null;

    public void createConnection(String url) throws IOException {                 // je vytvořeno spojení se serverem
        connection = new Connection();
        connection.initialize(url);
        Thread receive = new Thread(new Runnable(){
            @Override
            public void run() {
                while (true) {
                    if(connection.getReceivedData()!=null) {            // kontroluje se proměnná recievedData ve třídě Connection
                        try {                                           // pokud jsou přijata data, tak se využije parsování, kdy podle kódu určíme co se bude dělat dále
                            parse(connection.getReceivedData());
                        } catch (IOException | ClassNotFoundException | InterruptedException e) {
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

    public void parse(String message) throws IOException, ClassNotFoundException, InterruptedException {          // z přijatých dat se opět provede parsování a podle kódu se provádí následné akce
        StringTokenizer st = new StringTokenizer(message, ";");
        String source = st.nextToken();
        String code = st.nextToken();
        String msg = st.nextToken();
        switch(code){
            case "authSucc":
                vypis=msg;
                System.out.println("Úspěšné přihlášení");
                isAuthorized = true;
                newVypis = true;
                break;

            case "authFail":
                vypis=msg;
                System.out.println("Neúspěšné přihlášení: " + msg);
                isAuthorized = false;
                newVypis = true;
                break;
            case "files":
                vypis+="Recieved new files.";
                newFiles = (File[]) deserialize(msg);
                break;
            case "blockrequest":
                Thread.sleep((long)Math.random()*2000);
                send("blockresponse;"+serialize(blockchain),source);
                break;
            case "blockresponse":
                chains.add((Blockchain) deserialize(msg));
                break;
            case "newblock":
                newBlockchain = (Blockchain) deserialize(msg);
                System.out.println(source + " sent a new block. Validating.");
                newBlock=true;
                break;
            default:
                System.out.println("Unknown message.");
                newVypis = true;
        }
    }

    public void setVypis(String vypis) {
        this.vypis = vypis;
    }

    public void setBlockchain(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    public static String serialize(Serializable o) throws IOException {            // serializace objektů do Stringu pro přenos
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
