package vut.fekt.archive;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import org.apache.commons.io.FileUtils;
import vut.fekt.archive.blockchain.Crypto;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import org.json.*;
import vut.fekt.archive.blockchain.CryptoException;

public class Server extends Thread {
    private JPanel panelserver;
    private JLabel label;
    //kam spadají uploady z webu
    public static String secretsFile = "D:/secrets.txt";
    public static String rootDir = "D:/Archiv/Upload/upload-api/";
    //kam se bude archivovat - kde běží apache
    public static String archDir = "C:/Programy/Xampp/htdocs/archive/";
    static Vector<ClientHandler> ar = new Vector<>();
    static Vector<String> connectedUsers = new Vector<>();
    static Vector<String> availableAdmin = new Vector<>();
    static int i = 0;
    static HashMap<String, String> admins = new HashMap<>();
    static HashMap<String, String> users = new HashMap<>();
    public static ArrayList<PublicKey> legitKeys = new ArrayList<java.security.PublicKey>();

    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame("Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setContentPane(new Server().panelserver);
        frame.setVisible(true);
        frame.setBounds(100, 100, 300, 100);

        loadSecrets();
        try {
            loadKeys();
        }
        catch (IOException e){
            System.out.println("No keys found");
        }
        newDocumentThread();

        ServerSocket serverSocket = new ServerSocket(2021);
        Socket socket;
        while (true) {
            socket = serverSocket.accept();
            System.out.println("New Client " + socket);

            DataInputStream dis = new DataInputStream((socket.getInputStream()));       // vstupní data
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());      // výstupní data
            ClientHandler clientHandler = new ClientHandler(socket, "client" + i, dis, dos, admins, users);
            Thread t = new Thread(clientHandler);
            System.out.println("Adding client" + i);
            ar.add(clientHandler);
            System.out.println(ar);
            t.start();
            i++;
        }
    }

    private static void loadSecrets() throws IOException {
        //String path = System.getProperty("user.dir");
        Path path = Path.of(secretsFile);
        String all = Files.readString(path);
        String[] pairs = all.split(";");
        for (String pair : pairs) {
            System.out.println(pair);
            String[] login = pair.split(":");
            if(login[2].equals("admin")) {
                admins.put(login[0], login[1]);
            }if(login[2].equals("user")) {
                users.put(login[0], login[1]);
            }
        }
    }

    public static void addUser(String username, String password) throws IOException, NoSuchAlgorithmException {
        Path path = Path.of(secretsFile);
        String s = username +":"+Crypto.getStringHash(password)+":user;";
        Files.write(
                path,
                s.getBytes(),
                StandardOpenOption.APPEND);
        loadSecrets();
    }

    //načtení serializovaných klíčů
    public static void loadKeys() throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(System.getProperty("user.dir")+"/keys.txt");
        ObjectInputStream ois = new ObjectInputStream(fis);
        String serialized = (String) ois.readObject();
        legitKeys = (ArrayList<PublicKey>) Crypto.deserialize(serialized);
    }

    private static void newDocumentThread() throws Exception {
        Thread filethread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                        try {
                            Thread.sleep(5000);
                            if (checkIfClientsConnectedAndRemove()) {
                                File uploadDir = new File(rootDir + "uploads");
                                Charset charset = StandardCharsets.UTF_8;
                                File[] newDocs = uploadDir.listFiles();
                                if(newDocs == null){
                                    System.out.println("Can't see the upload folder, something is wrong.");
                                }
                                else if (newDocs.length != 0) {
                                    System.out.println("Document " + newDocs[0].getName());
                                    File dir = new File(newDocs[0].getAbsolutePath());
                                    if(dir.getName().contains(",#,")){
                                        String[] s = dir.getName().split(",#,");
                                        String webUrl = s[1];
                                        System.out.println("Website archive request for " + s[1]);

                                    }
                                    if (dir.listFiles().length != 0) {
                                        System.out.println("I'm moving it here: " +archDir + newDocs[0].getName());
                                        File archiveDir = new File(archDir +  newDocs[0].getName());
                                        FileUtils.moveToDirectory(dir, new File(archDir),true);
                                        String docname = createJsonAndRename(archiveDir);
                                        File[] files = archiveDir.listFiles();
                                        String serializedFiles = Crypto.serialize(files);
                                        pickClientAndSend(serializedFiles, docname);
                                    }
                                }
                            }
                        } catch (InterruptedException | IOException | CryptoException e) {
                            e.printStackTrace();
                        } catch (ConcurrentModificationException e){
                            System.out.println("Concurrent modification exception, moving on.");
                        }
                }
            }
        });
        filethread.start();
    }

    private static String saveSite(String[] s){
        String id = s[0];
        String url = s[1];
        try {
            crawl(archDir+"archive/"+ id,url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;

    }

    public static void crawl(String folder, String url) throws Exception {
        String crawlStorageFolder = folder;
        int numberOfCrawlers = 5;

        CrawlConfig config = new CrawlConfig();
        config.setIncludeHttpsPages(true);
        config.setCrawlStorageFolder(crawlStorageFolder);

        // Instantiate the controller for this crawl.
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        // For each crawl, you need to add some seed urls. These are the first
        // URLs that are fetched and then the crawler starts following links
        // which are found in these pages
        controller.addSeed(url);

        // Start the crawl. This is a blocking operation, meaning that your code
        // will reach the line after this only when crawling is finished.
        controller.start(Crawler.class, numberOfCrawlers);

    }

    private static String createJsonAndRename(File dir) throws IOException, CryptoException {
        int randomNum = ThreadLocalRandom.current().nextInt(100000000, 999999999 + 1);
        String id = String.valueOf(randomNum);
        File[] files = dir.listFiles();
        String s = files[0].getName();
        System.out.println(s);
        String[] str =s.split("\\.");
        //System.out.println(str.length);
        String docname = str[0];
        String username = str[1];
        String password = "";
        Boolean encrypt = Boolean.valueOf(str[2]);
        if(encrypt){
            password = str[3];
        }
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        ArrayList<String> filenames = new ArrayList<>();
        for (File f:files) {
            String[] ss = f.getName().split("\\.");
            if(encrypt) {
                String filename =ss[4] + "." + ss[5];
                filenames.add(filename);
                Crypto.encrypt(password,f, new File(dir.getAbsolutePath() + "/" + filename),id);
                f.delete();
                //Files.move(Path.of(f.getAbsolutePath()),Path.of(dir.getAbsolutePath()+"/"+ss[4]+"."+ss[5]));
            }
            else if(!encrypt){
                filenames.add(ss[3] + "." + ss[4]);
                Files.move(Path.of(f.getAbsolutePath()), Path.of(dir.getAbsolutePath() + "/" + ss[3] + "." + ss[4]));
            }
        }
        JSONObject obj=new JSONObject();
        obj.put("author",username);
        obj.put("added",formatter.format(date));
        obj.put("docName",docname);
        obj.put("id",id);
        obj.put("files",filenames);
        obj.put("encrypted",encrypt);
        System.out.print(obj.toString());
        FileOutputStream fos = new FileOutputStream(dir.getAbsolutePath()+"/metadata.json");
        fos.write(obj.toString().getBytes(StandardCharsets.UTF_8));
        fos.close();
        return docname;
        //json.write(obj.);
    }


    private static void pickClientAndSend(String files,String docId) {
        Random rng = new Random();
        int random = rng.nextInt(ar.size());
        ClientHandler client = ar.get(random);
        client.sendMsg("files;"+files + "," + docId);
        client.isAvailable = false;
        availableAdmin.remove(client.username);
        System.out.println("Sent files ");

    }

    public static void deleteDoc(String docId) throws IOException {
        String dir = archDir+docId;
        FileUtils.deleteDirectory(new File(dir));
    }

    private static boolean checkIfClientsConnectedAndRemove() {
        if (ar.isEmpty()) return false;
        Iterator<ClientHandler> iter = ar.iterator();
        while(iter.hasNext()){
            ClientHandler c = iter.next();
            if(c.disconnected == true){
                ar.remove(c);
                connectedUsers.remove(c.username);
            }
        }
        if (ar.isEmpty()|| availableAdmin.isEmpty()) return false;
        else return true;
    }

}

class ClientHandler implements Runnable {
    final String name;
    final DataInputStream dis;          // vstupní data
    final DataOutputStream dos;         // výstupní data
    Socket socket;
    boolean isLogged;
    boolean isAdmin;
    public boolean isAuthorized;
    public boolean isAvailable = true;
    public String username;
    public boolean disconnected;
    HashMap<String, String> admins;
    HashMap<String, String> users;


    // konstruktor pre triedu clientHandler
    public ClientHandler(Socket socket, String name, DataInputStream dis, DataOutputStream dos, HashMap<String, String> admins, HashMap<String, String>  users) throws IOException {
        this.dos = dos;
        this.dis = dis;
        this.name = name;
        this.socket = socket;
        this.isLogged = true;
        this.isAdmin = false;
        this.isAuthorized = false;
        this.disconnected = false;
        this.admins = admins;
        this.users = users;
    }

    // metoda s cyklom while
    @Override
    public void run() {
        String received;
        while (true) {
            try {
                received = dis.readUTF();
                System.out.println(this.name + " sent this: " + received);
                while (received != null) {

                    StringTokenizer st = new StringTokenizer(received, "#");
                    String MsgToSend = st.nextToken();
                    String recipient = st.nextToken();


                    if (MsgToSend.equals("End")) {
                        this.isLogged = false;
                        this.isAuthorized=false;
                        this.disconnected = true;
                        this.dos.close();
                        this.dis.close();
                        this.socket.close();
                        if(isAdmin){
                            Server.availableAdmin.remove(username);
                        }if(!isAdmin){
                            Server.connectedUsers.remove(username);
                        }
                        System.out.println("Socket closed.");
                        return;
                    }

                    if (recipient.equals("server")) {
                        String[] result = parse(name, MsgToSend);
                        recipient = name;
                        MsgToSend = result[0] + ";" + result[1];
                        System.out.println("My response: " + MsgToSend);
                    }
                    if(isAdmin) {
                        for (ClientHandler mc : Server.ar) {
                            if (mc.name.equals(recipient) && mc.isLogged == true) {
                                mc.dos.writeUTF(this.name + ";" + MsgToSend);
                                received = null;
                                break;
                            }
                            if (recipient.equals("broadcast") && mc.isLogged == true && this.name != mc.name) {     //pokud je příjemce "broadcast" pošle zprávu všem
                                mc.dos.writeUTF(this.name + ";" + MsgToSend);
                            }
                        }
                    }
                    received = null;
                }
            } catch (SocketException f) {
                System.out.println("Unexpected disconnect");
                disconnected = true;
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String[] parse(String recipient, String message) throws Exception {          // z přijatých dat se opět provede parsování a podle kódu se provádí následné akce
        StringTokenizer st = new StringTokenizer(message, ";");
        String[] result = {null, null};
        String code = st.nextToken();
        String msg = st.nextToken();

        switch (code) {
            case "auth":
                String[] pair = msg.split(":");
                result[1] = authenticate(pair[0], pair[1]);
                if (isAuthorized) {
                    result[0] = "authSucc";
                } else result[0] = "authFail";
                break;
            case "files":
                String directory = msg;
                File dir = new File("C:/Programy/Xampp/htdocs/" + directory);
                File[] files = dir.listFiles();
                result[0] = "files";
                result[1] = serialize(files);
                Path path = Path.of("C:/Programy/Xampp/htdocs/documents.txt");
                Charset charset = StandardCharsets.UTF_8;
                String content = new String(Files.readAllBytes(path), charset);
                content = content.replaceAll(directory + ",", "");
                Files.write(path, content.getBytes(charset));
                break;
            case "keys":
                result[0] = "keys";
                if(isAuthorized&&isAdmin){
                    genKeysAndSend(this);
                    result[1] = "Keys generated and sent";
                }
                else result[1] = "Not authorized to recieve keys";
                break;
            case "confirmed":
                if(isAdmin) {
                    isAvailable = true;
                    Server.availableAdmin.add(username);
                    result[0] = "OK";
                    result[1] = "OK";
                }
                else{
                    result[0] = "NOK";
                    result[1] = "Not authorized";
                }
                break;
            case "rejected":
                if(isAdmin) {
                    isAvailable = true;
                    Server.availableAdmin.add(username);
                    Server.deleteDoc(msg);
                    result[1] = "OK";
                    result[0] = "OK";
                }
                else{
                    result[0] = "NOK";
                    result[1] = "Not authorized";
                }
                break;
            case "newuser":
                if(isAdmin) {
                    String[] newuser = msg.split(":");
                    Server.addUser(newuser[0], newuser[1]);
                    result[0] = "OK";
                    result[1] = "Added user";
                }
                else{
                    result[0] = "NOK";
                    result[1] = "Not authorized";
                }
        }
        return result;
    }

    private static void genKeysAndSend(ClientHandler cl) throws Exception {
        KeyPair keys = Crypto.generateKeyPair();
        System.out.println("Generated key pair");
        String serializedPair = Crypto.serialize(keys);
        cl.sendMsg("keypair;"+serializedPair);
        System.out.println("Sent client "+cl.name+" his key pair");
        Server.legitKeys.add(keys.getPublic());
        String serializedKeyArray = Crypto.serialize(Server.legitKeys);
        saveKeys(serializedKeyArray);
        Thread.sleep(50);
        for (ClientHandler c:Server.ar) {
                c.sendMsg("pubkey;"+serializedKeyArray);
                System.out.println("Sent client "+c.name+" legitimate keys");

        }
        Thread.sleep(50);
    }

    private String authenticate(String username, String pass) throws NoSuchAlgorithmException {
        isAuthorized = false;
        System.out.println("Authenticaton - Username: " + username + " , Password: " + pass);
        if (!admins.containsKey(username)&&!users.containsKey(username)) {
            return "Uživatel nenalezen";
        } else if (Server.connectedUsers.contains(username)||Server.availableAdmin.contains(username)) {
            return "Uživatel je již přihlášen";
        } else if(admins.containsKey(username)){
              if (!admins.get(username).equals(Crypto.getStringHash(pass))) {
                return "Špatné heslo";
            } else if (admins.get(username).equals(Crypto.getStringHash(pass))) {
                isAuthorized = true;
                isAdmin = true;
                this.username = username;
                Server.availableAdmin.add(username);
                return "Přihlášení úspěšné - role je admin";
            }
        } else if(users.containsKey(username)){
              if (!users.get(username).equals(Crypto.getStringHash(pass))) {
                return "Špatné heslo";
            } else if (users.get(username).equals(Crypto.getStringHash(pass))) {
                isAuthorized = true;
                isAdmin = false;
                this.username = username;
                Server.connectedUsers.add(username);
                return "Přihlášení úspěšné - role je uživatel";
            }
        }
        return "Autentizace byla neúspěšná";
    }

    private static String serialize(Serializable o) throws IOException {            // serializace objektů do Stringu pro přenos
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public void sendMsg(String msg){
        try {
            this.dos.writeUTF("server" + ";" + msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveKeys(String keys) throws IOException {
        FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir")+"/keys.txt");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(keys);
        oos.close();
        fos.close();
    }

}