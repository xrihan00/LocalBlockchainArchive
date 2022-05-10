package vut.fekt.archive;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Connection{
    private int port = 2021;
    private String receivedData;
    private DataOutputStream dos;
    Socket socket;
    DataInputStream dis;

    public void initialize(String url) throws IOException{                    // inicializace pro vytvoření komunikace - connection
        Scanner sc = new Scanner(System.in);

        InetAddress ip = InetAddress.getByName(url);
        socket = new Socket(ip,port);
        dis = new DataInputStream((socket.getInputStream()));           // vytvoření dataInputStream pro příchozí komunikaci
        this.dos = new DataOutputStream(socket.getOutputStream());

        Thread read = new Thread(new Runnable() {                                       // vytvoření nového vlákna, které kontroluje zda jsou nějaké nové příchozí zprávy
            @Override
            public void run() {
                while (true) {
                    try {
                        String msg = dis.readUTF();
                        receivedData = msg;                                             // když je obdržena nová zpráva, tak se předá do promenné receiveData
                    } catch (EOFException ee){
                        return;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        read.start();
    }
    public String getReceivedData() {
        return receivedData;
    }

    public void setReceivedData(String receivedData) {
        this.receivedData = receivedData;
    }

    public void send(String msg){                                                                    // metoda pro odesílání dat
        try {
            dos.writeUTF(msg);                                                                       // do dataOutputStream se předá požadovaná zpráva k odeslání
            System.out.println("I sent this: "+msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void end(){
        try {
            socket.close();
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


