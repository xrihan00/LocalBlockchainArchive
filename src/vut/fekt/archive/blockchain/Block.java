package vut.fekt.archive.blockchain;

import java.io.File;
import java.io.Serializable;
import java.security.PublicKey;
import java.text.SimpleDateFormat;

public class Block implements Serializable {

    private String previousHash;
    private String docName;
    private String[] filepath;
    private String metapath;
    private String filehash;
    private String timeStamp;
    private String signature;
    private PublicKey pubKey;
    private int blockId;



    //reprezentuje jeden blok blockchainu, obsahuje všechny udaje bloku
    public Block(String[] filepath, String metapath, int blockId, String docName, String signature, PublicKey pubKey) throws Exception {
        this.filepath = filepath;
        this.metapath = metapath;
        this.blockId = blockId;
        this.docName = docName;
        this.signature = signature;
        this.pubKey = pubKey;
        this.timeStamp = new SimpleDateFormat("HH:mm:ss dd. MM. yyyy").format(new java.util.Date()); //časové razítko bloku
        this.filehash = Crypto.getFileHash(filepath[0]);
    }

    public Block(String[] filepath, String metapath, int blockId, String docName, String signature, PublicKey pubKey, String filehash) throws Exception {
        this.filepath = filepath;
        this.metapath = metapath;
        this.blockId = blockId;
        this.docName = docName;
        this.signature = signature;
        this.pubKey = pubKey;
        this.timeStamp = new SimpleDateFormat("HH:mm:ss dd. MM. yyyy").format(new java.util.Date()); //časové razítko bloku
        this.filehash = filehash;
    }

    // getter pre predošlý hash
    public String getPreviousHash() {
        return previousHash;
    }
    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

       // getter ID hlasu
    public int getBlockId() {
        return blockId;
    }

    // getter časového razítka
    public String getTimeStamp() {
        return timeStamp;
    }

    public String getFileName(){
        return new File(filepath[0]).getName();
    }
    public String[] getFilepath() {

        return filepath;
    }
    public String getMetapath() {
        return metapath;
    }
    public String getFilehash() {
        return filehash;
    }
    public String getDocName() { return docName;}

    public String getSignature() {
        return signature;
    }

    public PublicKey getPubKey() {
        return pubKey;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nPředchozí hash: " + previousHash);
        sb.append("\nNázev dokument: "+docName);
        sb.append("\nSoubory obsahu: ");
        for (String file:filepath) {
            sb.append("\n"+file);
        }
        sb.append("\nMetadata: " + metapath);
        sb.append("\nHash souboru: " + filehash);
        sb.append("\nDigitální podpis: " +getSignature());
        sb.append("\nVeřejný klíč: " + getPubKey());
        sb.append("\nČasové razítko: " + getTimeStamp());
        sb.append("\nID bloku: " + getBlockId());
        return sb.toString();
    }
}
