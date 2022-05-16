package vut.fekt.archive.app;

import vut.fekt.archive.Connection;
import vut.fekt.archive.blockchain.Blockchain;
import vut.fekt.archive.blockchain.Crypto;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.StringTokenizer;

public class Client {
    public Connection connection;
    public String vypis = " ";
    public boolean isAuthorized = false;
    public boolean newVypis = false;
    public boolean newBlock = false;
    public boolean newKeys = false;
    public boolean newKeyPair = false;
    public boolean confirmation = true;
    public String newDoc;
    public File[] newFiles = null;
    public Blockchain blockchain = null;
    public ArrayList<Blockchain> chains = new ArrayList<>();
    public Blockchain newBlockchain = null;
    public ArrayList<PublicKey> keys = new ArrayList<>();
    public KeyPair keyPair = null;


    public void createConnection(String url) throws IOException, UnrecoverableKeyException, CertificateException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {                 // je vytvořeno spojení se serverem
        connection = new Connection();
        connection.initialize(url);
        Thread receive = new Thread(new Runnable(){
            @Override
            public void run() {
                while (true) {
                    if(connection.getReceivedData()!=null) {            // kontroluje se proměnná recievedData ve třídě Connection
                        try {
                           // pokud jsou přijata data, tak se využije parsování, kdy podle kódu určíme co se bude dělat dále
                            parse(connection.getReceivedData());
                        } catch (IOException | ClassNotFoundException | InterruptedException e) {
                            e.printStackTrace();
                        }
                        connection.setReceivedData(null);
                    }
                    try {
                        Thread.sleep(5);
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
        System.out.println("I recieved this: " +message);
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
                send("keys;Generate keys please","server");
                break;

            case "authFail":
                vypis=msg;
                System.out.println("Neúspěšné přihlášení: " + msg);
                isAuthorized = false;
                newVypis = true;
                break;
            case "pubkey":
                keys = (ArrayList<PublicKey>) Crypto.deserialize(msg);
                newKeys = true;
                break;
            case "keypair":
                keyPair=(KeyPair) Crypto.deserialize(msg);
                newKeyPair = true;
                break;
            case "keys":
                confirmation = Boolean.parseBoolean(msg);
                break;
            case "files":
                vypis+="Recieved new files.";
                System.out.println("Recieved new files");
                String[] desmsg = msg.split(",");
                newFiles = (File[]) deserialize(desmsg[0]);
                newDoc = desmsg[1];
                break;
            case "blockrequest":
                if(blockchain==null){
                    System.out.println("Blockchain is null, not sennding it.");
                    break;
                }
                Thread.sleep((long)Math.random()*1500);
                send("blockresponse;"+serialize(blockchain),source);
                break;
            case "blockresponse":
                Blockchain newchain = (Blockchain) deserialize(msg);
                System.out.println("Got this blockchain: " + newchain.getLastHash());
                chains.add(newchain);
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
