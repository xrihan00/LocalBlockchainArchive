package vut.fekt.archive;

import vut.fekt.archive.blockchain.Block;
import vut.fekt.archive.blockchain.Blockchain;
import vut.fekt.archive.blockchain.Crypto;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Archive implements Serializable {

    String name;
    Blockchain blockchain = new Blockchain();
    String archiveFolder;
    KeyPair keyPair;

    List<ArchiveDocument> documents = new ArrayList<>();

    public Archive(String name, String folder) throws IOException {
        this.archiveFolder = folder;
        this.name = name;
        new File(archiveFolder).mkdirs();
    }

    public void addDocument(File content, String author, String docName, String version) throws Exception {
        ArchiveDocument archDoc = new ArchiveDocument(content, archiveFolder, author, docName, version,keyPair.getPrivate());
        documents.add(archDoc);
        Block block = new Block(archDoc.docuFile.getAbsolutePath(),archDoc.metadata.getAbsolutePath(), blockchain.randomId(), archDoc.docName,archDoc.getSignature(),keyPair.getPublic());
        blockchain.addBlock(block);
        saveArchiveBlockchain();
    }

    public void saveArchiveBlockchain() throws IOException {
        FileOutputStream fos = new FileOutputStream(new File(archiveFolder+"/blockchain.txt"));
        fos.write(blockchain.toString().getBytes(StandardCharsets.UTF_8));
        fos = new FileOutputStream(new File(archiveFolder+"/serializedBlockchain.txt"));
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(blockchain);
        oos.close();
        fos.close();
    }

    public void saveKeyPair() throws IOException {
        FileOutputStream fos = new FileOutputStream(new File(archiveFolder+"/KeyPair.txt"));
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(keyPair);
        oos.close();
        fos.close();
    }

    public void loadArchiveBlockchain(File chain) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(chain);
        ObjectInputStream ois = new ObjectInputStream(fis);
        blockchain = (Blockchain) ois.readObject();
    }

    public void loadKeyPair(File file) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        keyPair = (KeyPair) ois.readObject();
    }

    public void generateKeys() throws Exception {
        keyPair = Crypto.generateKeyPair();
        saveKeyPair();
    }

    public int validateBlockchain() throws IOException, NoSuchAlgorithmException {
        int result = 0;
        LinkedList<Block> blocks = blockchain.getBlocks();
        Crypto crypto = new Crypto();
        String blockhash = "FIRST BLOCK";
        for (Block block:blocks) {
           if(!FileUtils.getFileHash(block.getFilepath()).equals(block.getFilehash())){
                System.out.println("Hash souboru " + block.getFileName() + " není validní! Integrita porušena!");
                result = 1;
            }
            else{
                System.out.println("Hash souboru " + block.getFileName() + " je validní.");
            }
            if(!block.getPreviousHash().equals(blockhash)){
                System.out.println("Blockchain not valid!");
                result = 2;
            }
            blockhash = crypto.blockHash(block);

        }
        return result;
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

