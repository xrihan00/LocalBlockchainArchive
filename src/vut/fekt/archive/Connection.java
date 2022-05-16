package vut.fekt.archive;

import javax.net.SocketFactory;
import javax.net.ssl.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Objects;
import java.util.Scanner;

public class Connection{
    private int port = 2021;
    private String receivedData;
    private DataOutputStream dos;
    Socket socket;
    DataInputStream dis;

    public void initialize(String url) throws IOException, UnrecoverableKeyException, CertificateException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {                    // inicializace pro vytvoření komunikace - connection
        Scanner sc = new Scanner(System.in);

        InetAddress ip = InetAddress.getByName(url);
        socket = getSSLSocket(ip.getHostAddress());
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
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
        read.start();
    }

    public Socket getSSLSocket(String ip) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {
        Objects.requireNonNull("TLSv1.2", "TLS version is mandatory");
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        InputStream tstore = Connection.class
                .getResourceAsStream("/keystore.p12");
        trustStore.load(tstore, new char[] {'a', 'b', 'c', '1','2', '3'});
        tstore.close();
        TrustManagerFactory tmf = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        InputStream kstore = Server.class
                .getResourceAsStream("/" + "keystore.p12");
        keyStore.load(kstore, new char[] {'a', 'b', 'c', '1','2', '3'});
        KeyManagerFactory kmf = KeyManagerFactory
                .getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, new char[] {'a', 'b', 'c', '1','2', '3'});
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(),
                SecureRandom.getInstanceStrong());
        SocketFactory factory = ctx.getSocketFactory();

         Socket connection = factory.createSocket(ip, port);
            ((SSLSocket) connection).setEnabledProtocols(new String[] {"TLSv1.2"});
            SSLParameters sslParams = new SSLParameters();
            sslParams.setEndpointIdentificationAlgorithm("HTTPS");
            ((SSLSocket) connection).setSSLParameters(sslParams);
            return connection;


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
        } catch (IOException | NullPointerException e) {
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


