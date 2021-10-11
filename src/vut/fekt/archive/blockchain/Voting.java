package vut.fekt.archive.blockchain;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class Voting {
    // getter pre kandidátov
    public HashMap<String, PublicKey> getCandidates() {
        return candidates;
    }

    // setter pre kandidátov
    public void setCandidates(HashMap<String, PublicKey> candidates) {
        this.candidates = candidates;
    }

    //hashmapa s kandidátmi
    private HashMap<String, PublicKey> candidates;

    // getter pre meno kandidáta na základe kľúča z hashmapy
    public String getCandidateName(PublicKey pk){
        for (Map.Entry e: candidates.entrySet())
            if (e.getValue().equals(pk)) {
                return (String) e.getKey();
            }
        return null;
    }
}
