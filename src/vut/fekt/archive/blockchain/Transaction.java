package vut.fekt.archive.blockchain;

import java.io.Serializable;
import java.security.PublicKey;

public class Transaction implements Serializable {

    private PublicKey sourcePublicKey;
    private PublicKey destinationPublicKey;
    private int sum;

    // kontruktor transakce
    public Transaction(PublicKey sourceName, PublicKey destinationName, int sum) {
        this.sourcePublicKey = sourceName;
        this.destinationPublicKey = destinationName;
        this.sum = sum;
    }

    // kolik bylo předáno "hlasů"
    public int getSum() {
        return sum;
    }

    //zdrojový verejný kľúč
    public PublicKey getSourcePublicKey() {
        return sourcePublicKey;
    }

    //cieľový verejný kľúč
    public PublicKey getDestinationPublicKey() {
        return destinationPublicKey;
    }

}
