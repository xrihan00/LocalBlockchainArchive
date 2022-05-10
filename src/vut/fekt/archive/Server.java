package vut.fekt.archive;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import vut.fekt.archive.blockchain.Crypto;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import org.json.*;
import org.netpreserve.jwarc.*;

public class Server extends Thread {
    private JPanel panelserver;
    private JLabel label;
    //kam spadají uploady z webu
    public static String rootDir = "D:/Archiv/Upload/upload-api/";
    //kam se bude archivovat - kde běží apache
    public static String archDir = "C:/Programy/Xampp/htdocs/";
    static Vector<ClientHandler> ar = new Vector<>();
    static int i = 0;
    static HashMap<String, String> secrets = new HashMap<>();

    public static void main(String[] args) throws Exception {               // metóda main na spuštění aplikácie Server
        JFrame frame = new JFrame("Server");                            // GUI pre aplikáciu Server
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setContentPane(new Server().panelserver);
        frame.setVisible(true);
        frame.setBounds(100, 100, 300, 100);

        loadSecrets();
        newDocumentThread();

        ServerSocket serverSocket = new ServerSocket(2021);             // vytvorenie Socketu pre Server, potom vytvorenie triedy Client Handler v novom vlákne
        Socket socket;
        while (true) {
            socket = serverSocket.accept();
            System.out.println("New Client " + socket);

            DataInputStream dis = new DataInputStream((socket.getInputStream()));       // vstupní data
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());      // výstupní data
            ClientHandler clientHandler = new ClientHandler(socket, "client" + i, dis, dos, secrets);
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
        Path path = Path.of("D:/secrets.txt");
        String all = Files.readString(path);
        String[] pairs = all.split(";");
        for (String pair : pairs) {
            System.out.println(pair);
            String[] login = pair.split(":");
            secrets.put(login[0], login[1]);
        }
    }

    private static void newDocumentThread() throws Exception {
        Thread filethread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                        try {
                            Thread.sleep(5000);
                            if (checkIfClientsConnectedAndAuthorized()) {
                                File uploadDir = new File(rootDir + "uploads");
                                Charset charset = StandardCharsets.UTF_8;
                                File[] newDocs = uploadDir.listFiles();
                                if (newDocs.length != 0) {
                                    System.out.println("Document " + newDocs[0].getName());
                                    File dir = new File(newDocs[0].getAbsolutePath());
                                    if(dir.getName().contains(",#,")){
                                        String[] s = dir.getName().split(",#,");
                                        String webUrl = s[1];
                                        System.out.println("Website archive request for " + s[1]);

                                    }
                                    if (dir.listFiles().length != 0) {
                                        System.out.println("I'm moving it here: " +archDir + "archive/" + newDocs[0].getName());
                                        File archiveDir = new File(archDir + "archive/" + newDocs[0].getName());
                                        Files.move(dir.toPath(), archiveDir.toPath());
                                        String id = createJsonAndRename(archiveDir);
                                        File[] files = archiveDir.listFiles();
                                        String serializedFiles = Crypto.serialize(files);
                                        pickClientAndSend(serializedFiles, id);
                                    }
                                }
                            }
                        } catch (InterruptedException | MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
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

    private static String createJsonAndRename(File dir) throws IOException {
        String id = dir.getName();
        File[] files = dir.listFiles();
        String s = files[0].getName();
        System.out.println(s);
        String[] str =s.split("\\.");
        //System.out.println(str.length);
        String docname = str[0];
        String username = str[1];
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        ArrayList<String> filenames = new ArrayList<>();
        for (File f:files) {
            String[] ss = f.getName().split("\\.");
            filenames.add(ss[2]+"."+ss[3]);
            Files.move(Path.of(f.getAbsolutePath()),Path.of(dir.getAbsolutePath()+"/"+ss[2]+"."+ss[3]));
        }
        JSONObject obj=new JSONObject();
        obj.put("author",username);
        obj.put("added",formatter.format(date));
        obj.put("docName",docname);
        obj.put("id",id);
        obj.put("files",filenames);
        System.out.print(obj.toString());
        FileOutputStream fos = new FileOutputStream(dir.getAbsolutePath()+"/metadata.json");
        fos.write(obj.toString().getBytes(StandardCharsets.UTF_8));
        fos.close();
        return id;
        //json.write(obj.);
    }


    private static void pickClientAndSend(String files,String docId) {
        Random rng = new Random();
        int random = rng.nextInt(ar.size());
        ClientHandler client = ar.get(random);
        client.sendMsg("files;"+files + "," + docId);
        System.out.println("Sent files ");

    }

    private static boolean checkIfClientsConnectedAndAuthorized() {
        if (ar.isEmpty()) return false;
        for (ClientHandler c : ar) {
            if (c.disconnected == false || c.isAuthorized==true) return true;
        }
        return false;
    }

}

class ClientHandler implements Runnable {
    private final String name;
    final DataInputStream dis;          // vstupní data
    final DataOutputStream dos;         // výstupní data
    Socket socket;
    boolean isLogged;
    boolean isAdmin;
    public boolean isAuthorized;
    public boolean disconnected;
    HashMap<String, String> secrets;


    // konstruktor pre triedu clientHandler
    public ClientHandler(Socket socket, String name, DataInputStream dis, DataOutputStream dos, HashMap<String, String> secrets) throws IOException {
        this.dos = dos;
        this.dis = dis;
        this.name = name;
        this.socket = socket;
        this.isLogged = true;
        this.isAdmin = false;
        this.isAuthorized = false;
        this.disconnected = false;
        this.secrets = secrets;
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

                    StringTokenizer st = new StringTokenizer(received, "#");        // rozdelí vstup na základe "#"
                    String MsgToSend = st.nextToken();                                      // správa
                    String recipient = st.nextToken();                                      // príjemca


                    if (MsgToSend.equals("End")) {
                        this.isLogged = false;
                        this.dos.close();
                        this.dis.close();
                        this.socket.close();
                        System.out.println("Socket closed.");
                        return;
                    }

                    if (recipient.equals("server")) {
                        String[] result = parse(MsgToSend);
                        recipient = name;
                        MsgToSend = result[0] + ";" + result[1];
                        System.out.println("My response: " + MsgToSend);
                    }


                    for (ClientHandler mc : Server.ar) {                                    // preposielanie správ
                        if (mc.name.equals(recipient) && mc.isLogged == true) {             //pošle zprávu zadanému příjemci
                            mc.dos.writeUTF(this.name + ";" + MsgToSend);
                            received = null;
                            break;
                        }
                        if (recipient.equals("broadcast") && mc.isLogged == true && this.name != mc.name) {     //pokud je příjemce "broadcast" pošle zprávu všem
                            mc.dos.writeUTF(this.name + ";" + MsgToSend);
                        }
                    }
                    received = null;
                }
            } catch (SocketException f) {
                System.out.println("Unexpected disconnect");
                disconnected = true;
                return;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public String[] parse(String message) throws IOException, ClassNotFoundException {          // z přijatých dat se opět provede parsování a podle kódu se provádí následné akce
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
        }
        return result;
    }

    private String authenticate(String username, String pass) {
        isAuthorized = false;
        System.out.println("Authenticaton - Username: " + username + " , Password: " + pass);
        if (!secrets.containsKey(username)) {
            return "Uživatel nenalezen";
        } else if (!secrets.get(username).equals(pass)) {
            System.out.println("Correct password: " + secrets.get(username));
            return "Špatné heslo";
        } else if (secrets.get(username).equals(pass)) {
            isAuthorized = true;
            return "Přihlášení úspěšné";
        } else return "Autentizace byla neúspěšná";
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

}