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

//Třída která obsahuje metody zpracování zpráv
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

    //vytvoří objekt Connection a následně ho sleduje pomocí threadu

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
    // k odeslání dat využívá klient metodu send ve třídě connection
    public void send(String msg, String name) throws InterruptedException {
        connection.send(msg + "#" + name);
    }

    // Přijaté data jsou předány této metodě
    //Ta rozdělí String na jednotlivé části a zpracuje jejich obsah
    public void parse(String message) throws IOException, ClassNotFoundException, InterruptedException {
        System.out.println("I recieved this: " +message);
        StringTokenizer st = new StringTokenizer(message, ";");
        String source = st.nextToken();
        String code = st.nextToken();
        String msg = st.nextToken();
        //switch rozhoduje na základě kodu co dělat se zprávou
        switch(code){
            //autentizace byla úspěšná -> nastavím proměnné na true
            case "authSucc":
                vypis=msg;
                System.out.println("Úspěšné přihlášení");
                isAuthorized = true;
                newVypis = true;
                send("keys;Generate keys please","server"); //žádost o klíče od serveru
                break;
            //autenziace nebyla uspěšná - vypíšu do logu
            case "authFail":
                vypis=msg;
                System.out.println("Neúspěšné přihlášení: " + msg);
                isAuthorized = false;
                newVypis = true;
                break;
            //zpráva obsahuje nový veřejný klíč
            case "pubkey":
                keys = (ArrayList<PublicKey>) Crypto.deserialize(msg);
                newKeys = true;
                break;
            //zpráva obsahuje pár klíčů k podpisům
            case "keypair":
                keyPair=(KeyPair) Crypto.deserialize(msg);
                newKeyPair = true;
                break;
            //informace o tom zda je používáno potvrzování nových dokumentů
            case "keys":
                confirmation = Boolean.parseBoolean(msg);
                break;
            //obsah je seznam nově uploadnutých souborů (resp. jejich názvy)
            case "files":
                vypis="Recieved new files.";
                System.out.println("Recieved new files");
                String[] desmsg = msg.split(",");
                newFiles = (File[]) deserialize(desmsg[0]);
                newDoc = desmsg[1];
                break;
            //uživatel odeslal požadavek na blockchain
            case "blockrequest":
                //pokud je můj prázdný tak ho neposílám
                if(blockchain==null){
                    System.out.println("Blockchain is null, not sennding it.");
                    break;
                }
                Thread.sleep((long)Math.random()*1500);
                send("blockresponse;"+serialize(blockchain),source);
                break;
            //jedná se o odpověd na mou žádost, přidám si ji do seznamu
            case "blockresponse":
                Blockchain newchain = (Blockchain) deserialize(msg);
                System.out.println("Got this blockchain: " + newchain.getLastHash());
                chains.add(newchain);
                break;
            //obsah je nový blok
            case "newblock":
                newBlockchain = (Blockchain) deserialize(msg);
                System.out.println(source + " sent a new block. Validating.");
                newBlock=true;
                break;
            //netuším co to je za zprávu
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

    // serializace objektů do Stringu pro přenos
    public static String serialize(Serializable o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    // u přijatých dat se provede deserializace - dekódování
    public static Object deserialize(String s)   {
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
