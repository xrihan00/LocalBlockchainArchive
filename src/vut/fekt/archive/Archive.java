package vut.fekt.archive;

import vut.fekt.archive.blockchain.Block;
import vut.fekt.archive.blockchain.Blockchain;
import vut.fekt.archive.blockchain.Crypto;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

public class Archive implements Serializable {

    String name;
    Blockchain blockchain = new Blockchain();
    String archiveFolder;
    KeyPair keyPair;

    //seznam dokumentů
    List<ArchiveDocument> documents = new ArrayList<>();

    //interní reprezentace celého archivu
    public Archive(String name, String folder) throws IOException {
        this.archiveFolder = folder;
        this.name = name;
        new File(archiveFolder).mkdirs();
    }

    //přidávání dokumentu
    public void addDocument(File content, String author, String docName, String version) throws Exception {
        ArchiveDocument archDoc = new ArchiveDocument(content, archiveFolder, author, docName, version,keyPair.getPrivate());
        documents.add(archDoc);
        Block block = new Block(archDoc.docuFile.getAbsolutePath(),archDoc.metadata.getAbsolutePath(), blockchain.randomId(), archDoc.docName,archDoc.getSignature(),keyPair.getPublic());
        blockchain.addBlock(block);
        saveArchiveBlockchain();
    }

    //ukládání blockchainu pomocí serializace
    public void saveArchiveBlockchain() throws IOException {
        FileOutputStream fos = new FileOutputStream(new File(archiveFolder+"/blockchain.txt"));
        fos.write(blockchain.toString().getBytes(StandardCharsets.UTF_8));
        fos = new FileOutputStream(new File(archiveFolder+"/serializedBlockchain.txt"));
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(blockchain);
        oos.close();
        fos.close();
    }

    //ukládání klíčů pomocí serializace
    public void saveKeyPair() throws IOException {
        FileOutputStream fos = new FileOutputStream(new File(archiveFolder+"/KeyPair.txt"));
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(keyPair);
        oos.close();
        fos.close();
    }

    //načtení serializovaného blockchainu
    public void loadArchiveBlockchain(File chain) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(chain);
        ObjectInputStream ois = new ObjectInputStream(fis);
        blockchain = (Blockchain) ois.readObject();
    }
    //načtení serializovaných klíčů
    public void loadKeyPair(File file) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        keyPair = (KeyPair) ois.readObject();
    }

    //vygeneruje klíče
    public void generateKeys() throws Exception {
        keyPair = Crypto.generateKeyPair();
        saveKeyPair();
    }


    public String getName() {
        return name;
    }
    public String getArchiveFolder() {return archiveFolder;    }
    public void setName(String name) {
        this.name = name;
    }
    public Blockchain getBlockchain() {
        return blockchain;
    }

}

