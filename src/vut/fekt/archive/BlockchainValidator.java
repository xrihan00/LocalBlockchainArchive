package vut.fekt.archive;

import vut.fekt.archive.blockchain.Block;
import vut.fekt.archive.blockchain.Blockchain;
import vut.fekt.archive.blockchain.Crypto;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;

public class BlockchainValidator {
    private String resultString ="<html>";
    private String detailedLog="";
    private boolean integrity=true;
    private ArrayList<Block> invalidBlocks = new ArrayList();
    private Blockchain blockchain;

    public BlockchainValidator(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    public boolean validate() throws IOException, NoSuchAlgorithmException {
        boolean result = true;
        LinkedList<Block> blocks = blockchain.getBlocks();
        Crypto crypto = new Crypto();
        String blockhash = "FIRST BLOCK";
        detailedLog = "VYPIS VALIDACE BLOCKCHAINU\n--------------------";
        for (Block block : blocks) {
            detailedLog+="\nDokument " + block.getDocName() + "";
            String filehash = FileUtils.getFileHash(block.getFilepath());
            if (!filehash.equals(block.getFilehash())) {
                resultString += "Hash souboru " + block.getFileName() + " není validní! Integrita porušena!<br>";
                detailedLog += "\nHodnota \"Hash souboru\" = " + block.getFilehash() + "\nměla by být " + filehash + "\nHASH NENÍ VALIDNÍ - INTEGRITA PORUŠENA!";
                invalidBlocks.add(block);
                integrity = false;
                result = false;
            } else {
                detailedLog+=("\nHash souboru " + block.getFileName() + " je validní.");
            }
            if (!block.getPreviousHash().equals(blockhash)) {
                detailedLog ="\nHodnota \"Previous Hash\" = " + block.getPreviousHash() + "\nměla by být " + blockhash;
                resultString = "Blockchain not valid!";
                integrity = false;
                result = false;
            }
            blockhash = crypto.blockHash(block);
            detailedLog += "\n------------------------";
        }
        resultString+="</html>";
        if(integrity == true){
            resultString = "<html>Blockchain valid!</html>";
            detailedLog += "\nVÝSLEDEK: Blockchain validní, integrita archivu neporušena";
        }
        else if(integrity==false){
            detailedLog += "\nVÝSLEDEK: Blockchain není validní, integrita archivu porušena!";
            detailedLog += "\nPorušené dokumenty: ";
            for (Block b:invalidBlocks
                 ) {
                detailedLog += "\n" + b.getDocName();
            }
        }
        return result;
    }


    public String getResultString() {
        return resultString;
    }

    public boolean isIntegrity() {
        return integrity;
    }

    public ArrayList getInvalidBlocks() {
        return invalidBlocks;
    }
    public String getDetailedLog() {
        return detailedLog;
    }
}
