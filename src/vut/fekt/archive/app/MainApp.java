package vut.fekt.archive.app;

import vut.fekt.archive.ArchiveDocument;
import vut.fekt.archive.BlockchainValidator;
import vut.fekt.archive.blockchain.Block;
import vut.fekt.archive.blockchain.Blockchain;
import vut.fekt.archive.blockchain.Crypto;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.*;

//hlavní okno aplikace
public class MainApp extends JFrame {
    public JLabel FileLabel;
    public JLabel archiveLabel;
    public Blockchain blockchain = new Blockchain();
    public MainApp frame;

    public ShowDocument sd;
    public boolean docConfirmation = false;

    public NewUser nu;
    public boolean newUserClicked = false;
    public boolean nuDone = false;

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
    private JButton newUserButton;

    private KeyPair keyPair;
    private ArrayList<PublicKey> publicKeys = new ArrayList<>();
    private Map<String, ArchiveDocument> documentTimeMap = new HashMap<>();
    private boolean isAuthorized = false;
    private String url=null;

    public MainApp(ShowDocument showdoc, NewUser newUser) {
        this.sd = showdoc;
        this.nu = newUser;

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
                BlockchainValidator bv = new BlockchainValidator(blockchain,url,publicKeys);
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
                if(client!=null) {
                    try {
                        client.send("End", "server");
                        client.connection = null;
                        client = null;
                        vypis.setText("Odpojen");
                        Thread.sleep(100);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
                if (urlField.getText().isEmpty()) {
                    vypis.setText("Vyplňte URL/IP adresu archivu");
                } else {
                    client = new Client();
                    try{
                        loadArchiveBlockchain();
                    }
                    catch (IOException ioe){
                        System.out.println("Nenašel jsem serializované verze blockchainu a klíčů");
                    } catch (ClassNotFoundException cnfe) {
                        cnfe.printStackTrace();
                    }
                    try {
                        client.createConnection(urlField.getText());
                        url = urlField.getText();
                        vypis.setText("Připojení úspěšné");
                        getBlockchainsAndSetMostCommon();
                       // newBlockThread();
                    } catch (IOException connecteexc) {
                        vypis.setText("Připojení neúspěšné!");
                        connecteexc.printStackTrace();
                    } catch (Exception exception) {
                        vypis.setText("Problém se získáním blockchainu.");
                    }
                }
            }
        });
        authButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(client==null){
                    vypis.setText("Chybí spojení se serverem");
                }
                if (usernameField.getText().isEmpty()) {
                    vypis.setText("Vyplňte jméno");
                }
                if (passwordField.getText().isEmpty()) {
                    vypis.setText("Vyplňte heslo");
                } else {
                    try {
                        client.send("auth;" + usernameField.getText() + ":" + passwordField.getText(), "server");
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        newUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newUserClicked = true;
                System.out.println("This happened "+newUserClicked);
            }
        });
        /*saveBlockchainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    blockchain =Crypto.loadBlockchain(new File("C:\\Programy\\Xampp\\htdocs\\blockchain.txt"));
                    client.blockchain = blockchain;
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        });*/
        /*       requestBlockchainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    getBlockchainsAndSetMostCommon();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });*/

    }

    //inicializace framu
    public void initMainapp() {

        frame = new MainApp(this.sd, this.nu);
        frame.setTitle("Blockchain Archiv");
        frame.pack();
        frame.setContentPane(this.panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(100, 100, 500, 500);
        frame.setSize(750, 900);


        frame.addWindowListener(new WindowListener() {
            //při zavírání okna je odeslána zpráva k odhlášení od serveru
            @Override
            public void windowClosing(WindowEvent e) {
                if(client!=null) {
                    try {
                        saveArchiveBlockchain();
                        client.send("End", "server");
                        Thread.sleep(100);
                    } catch (InterruptedException | IOException interruptedException) {
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
        clientThread();

    }

    public String getVypis() {
        return vypis.getText();
    }

    public void setVypis(String s) {
        vypis.setText(vypis.getText()+"\n"+s);
    }

    public void clientThread() {
        Thread clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                File[] files =null;
                while (true) {
                    try {
                        Thread.sleep(100);
                        if (client != null) {
                            isAuthorized = client.isAuthorized;
                            if (client.newVypis) {
                                setVypis(client.vypis);
                                client.newVypis = false;
                            }
                            if(client.newFiles!=null){
                                if(client.confirmation==false){
                                    sd.result ="confirmed";
                                    files = client.newFiles;
                                    client.newFiles = null;
                                }
                                else {
                                    sd.init(url, client.newDoc);
                                    files = client.newFiles;
                                    client.newFiles = null;
                                    docConfirmation = true;
                                }
                                setVypis("Nové archiválie přidány");
                            }
                            if(client.newBlock){
                                client.newBlock=false;
                                validateNewBlock(client.newBlockchain);
                                client.setBlockchain(blockchain);
                            }
                            if(client.newKeys){
                                publicKeys = client.keys;
                                client.newKeys = false;
                                setVypis("Seznam legitimních klíčů přidán");
                            }
                            if(client.newKeyPair){
                                keyPair = client.keyPair;
                                client.newKeyPair = false;
                                setVypis("Klíče obdrženy");
                            }
                            if(sd.result.equals("confirmed")){
                                getBlockchainsAndSetMostCommon();
                                blockchain.addBlock(createBlock(files,client.newDoc));
                                client.setBlockchain(blockchain);
                                client.send("confirmed;Looks good!", "server");
                                client.send("newblock;" + client.serialize(blockchain), "broadcast");
                                sd.result = "";
                            }
                            if(sd.result.equals("rejected")){
                                client.send("rejected;"+sd.docName,"server");
                                sd.result = "";
                            }
                            if(nu.ok){
                                client.send("newuser;"+nu.getUsername()+":"+nu.getPassword(), "server");
                                nuDone = true;
                                nu.ok = false;
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
        clientThread.start();
    }



    public Block createBlock(File[] files, String docId) throws Exception {

        String meta = null;
        ArrayList<String> contentNames = new ArrayList<>();

        for (File f : files) {
           // String mimeType = Files.probeContentType(f.toPath());
            if (f.getName().contains("metadata.json")) {
                //meta = "http://"+url+"/"+docId+"/"+f.getName();
                meta = f.getName();
            } else {
                contentNames.add(f.getName());
            }
        }
        String hash = Crypto.getHashOfUrls(contentNames.toArray(new String[0]),url,docId);
        Block block = new Block(contentNames.toArray(new String[0]), meta, blockchain.getBlocks().size(), docId, Crypto.sign(hash.getBytes(StandardCharsets.UTF_8), keyPair.getPrivate()), keyPair.getPublic(), hash);
        setVypis("Nový blok vytvořen a přidán");
        return block;
    }

    public void validateNewBlock(Blockchain newBlock) throws Exception {
        BlockchainValidator bv = new BlockchainValidator(blockchain,url, publicKeys);
        if(bv.containsBlock(newBlock.getBlocks().getLast())){
            System.out.println("Blockchain already contains this block.");
            return;
        }
        else if(bv.validateNewBlock(newBlock)){
            System.out.println("Adding a new block");
            blockchain.addRecievedBlock(newBlock.getBlocks().getLast());
        }
        else System.out.println("Recieved invalid block: \n" + bv.getDetailedLog());
        //vypis.setText(bv.getDetailedLog());
    }

    public void getBlockchainsAndSetMostCommon() throws Exception {
        client.send("blockrequest;"+"I need blocks","broadcast");
        setVypis("Žádost o blockchainy odeslána");
        Thread.sleep(2000);
        ArrayList<Blockchain> chains = client.chains;
        if(chains.isEmpty()){
            System.out.println("Didn't receive any blockchains, using my own.");
            return;
        }
        if(chains.size()==1){
            System.out.println("Only received one blockchain, keeping mine.");
        }
        HashMap<Blockchain,Integer> hashes = new HashMap<>();
        System.out.println("Received " + chains.size() + " blockchains. Picking the most common one.");
        for (Blockchain b:chains) {
            BlockchainValidator bv = new BlockchainValidator(blockchain,url, publicKeys);
            if(bv.validateBlockHashes()){
                String hash = b.getLastHash();
                System.out.println(hash);
                if(hashes.containsKey(hash)){
                    hashes.put(b,hashes.get(hash)+1);
                }
                else hashes.put(b,1);
            }
        }
        if(hashes.isEmpty()){
            System.out.println("No blockchains were valid");
            return;
        }
        Blockchain max = Collections.max(hashes.entrySet(), Map.Entry.comparingByValue()).getKey();
        System.out.println("Last hash of the winning blockchain: " + max.getLastHash());
        setVypis("Vybrán nejčastější blockchain");
        blockchain.setBlocks(max.getBlocks());
        client.blockchain = blockchain;
        client.chains.clear();
    }
    public void saveArchiveBlockchain() throws IOException {
        FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir")+"/blockchain.txt");
        fos.write(blockchain.toString().getBytes(StandardCharsets.UTF_8));
        fos = new FileOutputStream(new File(System.getProperty("user.dir")+"/serializedBlockchain.txt"));
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(blockchain);
        oos.close();
        fos.close();
    }

    //načtení serializovaného blockchainu
    public void loadArchiveBlockchain() throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(System.getProperty("user.dir")+"/serializedBlockchain.txt");
        ObjectInputStream ois = new ObjectInputStream(fis);
        blockchain = (Blockchain) ois.readObject();
        client.blockchain = blockchain;
    }
}
