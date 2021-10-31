package vut.fekt.archive.blockchain;

import vut.fekt.archive.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;

public class Block implements Serializable {

    private String previousHash;
    private String docName;
    private String filepath;
    private String metapath;
    private String filehash;
    private String timeStamp;
    private int blockId;



    //jeden block blockchainu s informáciou o predchádzajucom hashi a o transakcii spolu s ID hlasu
    public Block(String filepath, String metapath, int blockId, String docName) throws Exception {
        this.filepath = filepath;
        this.metapath = metapath;
        this.blockId = blockId;
        this.docName = docName;
        this.timeStamp = new SimpleDateFormat("HH:mm:ss dd. MM. yyyy").format(new java.util.Date()); //časové razítko bloku
        this.filehash = FileUtils.getFileHash(filepath);
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
        return new File(filepath).getName();
    }
    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String getMetapath() {
        return metapath;
    }

    public void setMetapath(String metapath) {
        this.metapath = metapath;
    }

    public String getFilehash() {
        return filehash;
    }
    public String getDocName() { return docName;}

    public void setFilehash(String filehash) {
        this.filehash = filehash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nPrevious hash: " + previousHash);
        sb.append("\nNázev souboru: " + getFileName());
        sb.append("\nCesta k souboru: " + filepath);
        sb.append("\nMetadata: " + metapath);
        sb.append("\nHash souboru: " + filehash);
        sb.append("\nČasové razítko: " + getTimeStamp());
        sb.append("\nID bloku: " + getBlockId());
        return sb.toString();
    }
}
