package vut.fekt.archive;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Server extends Thread{
    private JPanel panelserver;
    private JLabel label;
    static Vector<ClientHandler> ar = new Vector<>();
    static int i = 0;
    static HashMap<String, String> secrets = new HashMap<>();

    public static void main(String[]args) throws IOException{               // metóda main na spuštění aplikácie Server
        JFrame frame = new JFrame("Server");                            // GUI pre aplikáciu Server
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setContentPane(new Server().panelserver);
        frame.setVisible(true);
        frame.setBounds(100, 100, 300, 100);

        loadSecrets();

        ServerSocket serverSocket = new ServerSocket(2021);             // vytvorenie Socketu pre Server, potom vytvorenie triedy Client Handler v novom vlákne
        Socket socket;
        while (true){
            socket=serverSocket.accept();
            System.out.println("New Client " + socket);

            DataInputStream dis = new DataInputStream((socket.getInputStream()));       // vstupní data
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());      // výstupní data
            ClientHandler clientHandler = new ClientHandler(socket,"client"+i,dis, dos, secrets);
            Thread t = new Thread(clientHandler);

            System.out.println("Adding client"+i);
            ar.add(clientHandler);
            System.out.println(ar);
            t.start();
            i++;
        }
    }

    private static void loadSecrets() throws IOException {
        //String path = System.getProperty("user.dir");
        Path path  = Path.of("D:/secrets.txt");
        String all = Files.readString(path);
        String[] pairs = all.split(";");
        for (String pair:pairs) {
            System.out.println(pair);
            String[] login = pair.split(":");
            secrets.put(login[0],login[1]);
        }
    }
}

class ClientHandler implements Runnable {
    private String name;
    final DataInputStream dis;          // vstupní data
    final DataOutputStream dos;         // výstupní data
    Socket socket;
    boolean isLogged;
    boolean isAdmin;
    boolean isAuthorized;
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
        this.secrets = secrets;
    }

    // metoda s cyklom while
    @Override
    public void run() {
        String received;
        while (true) {
            try {
                received = dis.readUTF();
                System.out.println(this.name + received + " " + this.socket.toString());
                while (received != null) {

                    StringTokenizer st = new StringTokenizer(received, "#");        // rozdelí vstup na základe "#"
                    String MsgToSend = st.nextToken();                                      // správa
                    String recipient = st.nextToken();                                      // príjemca
                    System.out.println(recipient);

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
                        MsgToSend = result[0] + ";"+result[1];
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
            } catch (SocketException f){
                System.out.println("Unexpected disconnect");
                return;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public String[] parse(String message) throws IOException, ClassNotFoundException {          // z přijatých dat se opět provede parsování a podle kódu se provádí následné akce
        StringTokenizer st = new StringTokenizer(message, ";");
        String[] result= {null,null};
        String code = st.nextToken();
        String msg = st.nextToken();

        switch (code) {
            case "auth":
                String[] pair = msg.split(":");
                result[1] = authenticate(pair[0],pair[1]);
                if(isAuthorized){ result[0] = "authSucc";}
                else result[0] = "authFail";
                break;
            case "files":
                String directory = msg;
                ArrayList<String> fileList;
                File dir = new File("C:/Programy/Xampp/htdocs/"+directory);
                File[] files = dir.listFiles();
                result[0] = "files";
                result[1] = serialize(files);
                Path path = Path.of("C:/Programy/Xampp/htdocs/documents.txt");
                Charset charset = StandardCharsets.UTF_8;
                String content = new String(Files.readAllBytes(path), charset);
                content = content.replaceAll(directory+",", "");
                Files.write(path, content.getBytes(charset));
                break;
        }
        return result;
    }

    private String authenticate(String username, String pass){
        isAuthorized = false;
        System.out.println("Authenticaton - Username: " + username + " , Password: " + pass);
        if(!secrets.containsKey(username)){
            return "Uživatel nenalezen";
        }
        else if(!secrets.get(username).equals(pass)){
            System.out.println("Correct password: " +secrets.get(username));
            return "Špatné heslo";
        }
        else if(secrets.get(username).equals(pass)){
            isAuthorized = true;
            return "Přihlášení úspěšné";
        }
        else return "Autentizace byla neúspěšná";
    }

    private static String serialize(Serializable o) throws IOException {            // serializace objektů do Stringu pro přenos
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

}