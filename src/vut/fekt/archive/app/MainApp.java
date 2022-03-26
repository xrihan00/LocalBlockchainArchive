package vut.fekt.archive.app;

import org.xml.sax.SAXException;
import vut.fekt.archive.Archive;
import vut.fekt.archive.ArchiveDocument;
import vut.fekt.archive.BlockchainValidator;
import vut.fekt.archive.blockchain.Block;
import vut.fekt.archive.blockchain.Blockchain;
import vut.fekt.archive.blockchain.Crypto;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.*;

//hlavní okno aplikace
public class MainApp extends JFrame {
    public JLabel FileLabel;
    public JLabel archiveLabel;
    public Blockchain blockchain = new Blockchain();
    public MainApp frame;
    public ShowDocument sd;
    public Client client;
    private JPanel panel1;
    private JTextPane textPane1;
    private JButton printBlockchainButton;
    private JButton validateBlockchainButton;

    private JTextPane vypis;
    private JTextField urlField;
    private JTextField usernameField;
    private JTextField passwordField;
    private JButton connectButton;
    private JButton authButton;
    private JCheckBox blockCheckBox;

    private KeyPair keyPair;
    private Map<String, ArchiveDocument> documentTimeMap = new HashMap<>();
    private boolean isAuthorized = false;
    private String url=null;

    public MainApp(ShowDocument showdoc) {
        this.sd = showdoc;

        try {
            keyPair = Crypto.generateKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //tlačítko výpis blockchainu
        printBlockchainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textPane1.setText(blockchain.toString());
            }
        });
        //tlačítko validovat blockchain
        validateBlockchainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // FileLabel.setText("Validating blockchain of archive " + archive.getName() + "...");
                BlockchainValidator bv = new BlockchainValidator(blockchain,url);
                try {
                    bv.validateAll();
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
                String result = bv.getResultString();
                FileLabel.setText(result);
                textPane1.setText(bv.getDetailedLog());

            }
        });
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (urlField.getText().isEmpty()) {
                    vypis.setText(vypis.getText() + "Vyplňte URL/IP adresu archivu\n");
                } else {
                    client = new Client();
                    try {
                        client.createConnection(urlField.getText());
                        url = urlField.getText();
                        vypis.setText(vypis.getText() + "Připojení úspěšné\n");
                        newBlockThread();
                    } catch (IOException connecteexc) {
                        vypis.setText(vypis.getText() + "Připojení neúspěšné!\n");
                        connecteexc.printStackTrace();
                    }
                }
            }
        });
        authButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (usernameField.getText().isEmpty()) {
                    vypis.setText(vypis.getText() + "Vyplňte jméno\n");
                }
                if (passwordField.getText().isEmpty()) {
                    vypis.setText(vypis.getText() + "Vyplňte heslo\n");
                } else {
                    try {
                        client.send("auth;" + usernameField.getText() + ":" + passwordField.getText(), "server");
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    //inicializace framu
    public void initMainapp() {

        frame = new MainApp(this.sd);
        frame.setTitle("Blockchain Archiv");
        frame.pack();
        frame.setContentPane(this.panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(100, 100, 500, 500);
        frame.setSize(500, 500);
        frame.addWindowListener(new WindowListener() {
            //při zavírání okna je odeslána zpráva k odhlášení od serveru
            @Override
            public void windowClosing(WindowEvent e) {
                if(client!=null) {
                    try {
                        client.send("End", "server");
                        Thread.sleep(100);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
            }
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {  }
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });
        clientThred();

    }

    public String getVypis() {
        return vypis.getText();
    }

    public void setVypis(String s) {
        vypis.setText(s);
    }

    public void clientThred() {
        Thread vypisThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(100);
                        if (client != null) {
                            isAuthorized = client.isAuthorized;
                            if (client.newVypis) {
                                setVypis(vypis.getText() + "\n" + client.vypis);
                                client.newVypis = false;
                            }
                            if(client.newBlock){
                                validateNewBlock(client.newBlockchain);
                                client.newBlock=false;
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        vypisThread.start();
    }

    public void newBlockThread(){
        Thread blockThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(1000);
                        String folder = url;
                        if (folder != null && isAuthorized == true && blockCheckBox.isSelected()) {
                            URL path = new URL("http://"+folder + "/documents.txt");
                            InputStream in = path.openStream();
                            String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                            if(!text.isEmpty()) {
                                System.out.println(text);
                                in.close();
                                String[] docs = text.split(",");
                                try {
                                    getBlockchainsAndSetMostCommon();
                                } catch (Exception e) {
                                }
                                client.send("files;" + docs[0], "server");
                                Thread.sleep(200);
                                blockchain.addBlock(createBlock(client.newFiles, docs[0]));
                                client.setBlockchain(blockchain);
                                client.send("newblock;" + client.serialize(blockchain), "broadcast");
                            }
                        }
                        Thread.sleep(25000);
                    } catch (Exception eee) {
                        eee.printStackTrace();
                    }
                }
            }
        });
        blockThread.start();
    }

    public Block createBlock(File[] files, String docId) throws Exception {
        String meta = null;
        ArrayList<String> contentNames = new ArrayList<>();

        for (File f : files) {
            String mimeType = Files.probeContentType(f.toPath());
            if (f.getName().contains("metadata.xml")) {
                //meta = "http://"+url+"/"+docId+"/"+f.getName();
                meta = f.getName();
            } else {
                contentNames.add(f.getName());
            }
        }
        String hash = Crypto.getHashOfUrls(contentNames.toArray(new String[0]),url,docId);
        Block block = new Block(contentNames.toArray(new String[0]), meta, blockchain.getBlocks().size(), docId, Crypto.sign(hash.getBytes(StandardCharsets.UTF_8), keyPair.getPrivate()), keyPair.getPublic(), hash);
        return block;
    }

    public void validateNewBlock(Blockchain newBlock) throws Exception {
        BlockchainValidator bv = new BlockchainValidator(blockchain,url);
        if(bv.validateNewBlock(newBlock)){
            blockchain.addBlock(newBlock.getBlocks().getLast());
        }
        else System.out.println("Recieved invalid block");
        //vypis.setText(bv.getDetailedLog());
    }

    public void getBlockchainsAndSetMostCommon() throws Exception {
        client.send("blockrequest;"+"I need blocks","broadcast");
        Thread.sleep(2000);
        ArrayList<Blockchain> chains = client.chains;
        HashMap<Blockchain,Integer> hashes = new HashMap<>();
        for (Blockchain b:chains) {
            BlockchainValidator bv = new BlockchainValidator(blockchain,url);
            if(bv.validateBlockHashes()){
                String hash = b.getLastHash();
                if(hashes.containsKey(hash)){
                    hashes.put(b,hashes.get(hash)+1);
                }
                else hashes.put(b,1);
            }
        }
        Blockchain max = Collections.max(hashes.entrySet(), Map.Entry.comparingByValue()).getKey();
        blockchain.setBlocks(max.getBlocks());
    }
}
