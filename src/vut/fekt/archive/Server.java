package vut.fekt.archive;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.StringTokenizer;
import java.util.Vector;

public class Server extends Thread{
    private JPanel panelserver;
    private JLabel label;
    static Vector<ClientHandler> ar = new Vector<>();
    static int i = 0;

    public static void main(String[]args) throws IOException{               // metóda main na spuštění aplikácie Server
        JFrame frame = new JFrame("Server");                            // GUI pre aplikáciu Server
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setContentPane(new Server().panelserver);
        frame.setVisible(true);
        frame.setBounds(100, 100, 300, 100);

        ServerSocket serverSocket = new ServerSocket(2021);             // vytvorenie Socketu pre Server, potom vytvorenie triedy Client Handler v novom vlákne
        Socket socket;
        while (true){
            socket=serverSocket.accept();
            System.out.println("New Client " + socket);

            DataInputStream dis = new DataInputStream((socket.getInputStream()));       // vstupní data
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());      // výstupní data
            ClientHandler clientHandler = new ClientHandler(socket,"client"+i,dis, dos);
            Thread t = new Thread(clientHandler);

            System.out.println("Adding client"+i);
            ar.add(clientHandler);
            System.out.println(ar);
            t.start();
            i++;
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

    // konstruktor pre triedu clientHandler
    public ClientHandler(Socket socket, String name, DataInputStream dis, DataOutputStream dos) {
        this.dos = dos;
        this.dis = dis;
        this.name = name;
        this.socket = socket;
        this.isLogged = true;
        this.isAdmin = false;
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}