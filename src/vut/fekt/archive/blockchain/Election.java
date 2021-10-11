package vut.fekt.archive.blockchain;


import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class Election {
    //vytvoreni electionu - seznamy volicu, kandidatu, scitani hlasu
    private HashMap<String,String> voters;
    private HashMap<String, PublicKey> candidates;
    private HashMap<String,String> voted;
    private ArrayList<KeyPair> keyPairs;
    private Crypto crypto = new Crypto();
    private boolean active = true;

    public Election(){
        this.voters= new HashMap<>();
        this.voted= new HashMap<>();
        this.candidates = new HashMap<>();
        this.keyPairs = new ArrayList<>();
        // kandidáti (prezidenti)
        try {
            this.candidates.put("Karel Karlovitý",crypto.generateKeyPair().getPublic());
            this.candidates.put("Pavel Pavlič",crypto.generateKeyPair().getPublic());
            this.candidates.put("Vlad Vladař",crypto.generateKeyPair().getPublic());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // mená a ID voličov
        this.voters.put("John","1");
        this.voters.put("Jack","2");
        this.voters.put("Bob","3");
        this.voters.put("Tom","4");
        this.voters.put("Karen","5");
        this.voters.put("Chad","6");
        this.voters.put("Steven","7");
        this.voters.put("David","8");
        this.voters.put("Adolf","9");
        this.voters.put("Přemysl","10");
        this.voters.put("Spytihněv","11");
        this.voters.put("Bašta","12");
        generateKeys(); //geneorvání klíču
    }

    //overenie či v Hashmap existuje meno a či suhlasí meno s ID
    public int checkUdaje(String name, String id){
        if(!voters.containsKey(name)){
            return 1;
        }
        if(!voters.get(name).equals(id)){
            return 2;
        }
        return 0;
    }

    //přiřazení voliče mezi ty kteří již volili (HashMapa voted)
    public void setVoted(String name, String id){
        voted.put(name,id);
    }

    // kontorla aby volič nehlasoval dvakrát
    public boolean checkIfVoted(String name){
        if(voted.containsKey(name)){
            return true;
        }
        return false;
    }

    //získanie kandidátv
    public HashMap<String, PublicKey> getCandidates(){
        return candidates;
    }

    //počet voličov
    public int amountOfVoters(){
        return voters.size();
    }

    //generování kliču - vygeneruje tolik párů kolik je voličů
    public void generateKeys(){
        for (int i = 0; i < voters.size(); i++) {
            try {
                keyPairs.add(crypto.generateKeyPair());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //získanie paru klučov - vybere se náhodný pár z listu KeyPairs, který je následně odstraněn
    public KeyPair getKeyPair(){
        int rnd = new Random().nextInt(keyPairs.size());
        KeyPair kp = keyPairs.get(rnd);
        keyPairs.remove(rnd);
        return kp;
    }

    // získanie verejného kľúču ze seznamu keypair-u
    public ArrayList<PublicKey> getPublicKeys(){
        ArrayList<PublicKey> ar = new ArrayList<>();
        for (KeyPair kp: keyPairs) {
            ar.add(kp.getPublic());
        }
        return ar;
    }

    //kontrola zda jsou volby aktivní
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
