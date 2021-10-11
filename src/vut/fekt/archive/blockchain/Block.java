package vut.fekt.archive.blockchain;

import java.io.Serializable;
import java.text.SimpleDateFormat;

public class Block implements Serializable {


    private String previousHash;
    private Transaction transaction;
    private int voteId;
    private String timeStamp;

    //jeden block blockchainu s informáciou o predchádzajucom hashi a o transakcii spolu s ID hlasu
    public Block(String previousHash, Transaction transaction, int voteId) {
        this.previousHash = previousHash;
        this.transaction = transaction;
        this.voteId = voteId;
        this.timeStamp = new SimpleDateFormat("HH:mm:ss dd. MM. yyyy").format(new java.util.Date()); //časové razítko hlasu
    }

    // getter pre predošlý hash
    public String getPreviousHash() {
        return previousHash;
    }
    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    // getter transakcie
    public Transaction getTransaction() {
        return transaction;
    }

    // getter ID hlasu
    public int getVoteId() {
        return voteId;
    }

    // getter časového razítka
    public String getTimeStamp() {
        return timeStamp;
    }

    // metóda toString na prevod pre výpis textu do informačného okna
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nPrevious hash: " + previousHash);
        sb.append("\n\nVeřejný klíč zdroje: \n" + transaction.getSourcePublicKey());
        sb.append("\n\nVeřejný klíč cíle: \n" + transaction.getDestinationPublicKey());
        sb.append("\n\nSuma transakce: " + transaction.getSum());
        sb.append("\nČasové razítko: " + getTimeStamp());
        sb.append("\nID hlasu: " + getVoteId());
        return sb.toString();
    }
}
