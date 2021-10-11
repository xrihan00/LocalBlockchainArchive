package vut.fekt.archive.blockchain;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Wallet {

    private KeyPair keyPair;

    // v tejto triede dochádza k ukladaniu párov kľúčov
    public Wallet(){
    }

    //setter na pár kľúčov
    public void setKeyPair(KeyPair keyPair) {    this.keyPair = keyPair; }

    //getter pre privátny kľúč
    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    //setter pre verejný kľúč
    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }
}
