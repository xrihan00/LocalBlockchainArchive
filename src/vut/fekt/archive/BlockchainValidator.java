package vut.fekt.archive;

import vut.fekt.archive.blockchain.Block;
import vut.fekt.archive.blockchain.Blockchain;
import vut.fekt.archive.blockchain.Crypto;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;

public class BlockchainValidator {
    private String resultString ="<html>";
    private String detailedLog="";
    private boolean integrity=true;
    private ArrayList<Block> invalidBlocks = new ArrayList();
    private Blockchain blockchain;

    //validátor blockchainu
    public BlockchainValidator(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    //metoda validace - vrací true nebo false a ukládá výpis ve Stringu detailedLog
    public boolean validate() throws Exception {
        boolean result = true;
        LinkedList<Block> blocks = blockchain.getBlocks();
        Crypto crypto = new Crypto();
        String blockhash = "FIRST BLOCK";
        detailedLog = "VYPIS VALIDACE BLOCKCHAINU\n--------------------";

        //for loop který prochází blok po bloku
        for (Block block : blocks) {
            detailedLog+="\nDokument " + block.getDocName() + "\n";
            String filehash = Crypto.getFileHash(block.getFilepath());
            System.out.println(filehash);
            boolean alreadyAddedToInvalid=false; //jestli už je blok v listu nevalidních bloků
            //test jestli se shoduje hodnota hashe souboru s opravdovým hashem souboru
            if (!filehash.equals(block.getFilehash())) {
                detailedLog += "\nHash souboru:\n " + block.getFilehash() + "\nměla by být:\n " + filehash + "\nHASH NENÍ VALIDNÍ - INTEGRITA PORUŠENA!\n";
                invalidBlocks.add(block);
                integrity = false;
                alreadyAddedToInvalid = true;
                result = false;
            } else {
                detailedLog+=("\nHash souboru " + block.getFileName() + " je validní.");
            }
            //ověření validity podpisu
            boolean signature = Crypto.verify(filehash.getBytes(StandardCharsets.UTF_8),block.getSignature(),block.getPubKey());
            if (!signature) {
                detailedLog += "\nPodpis:  " + block.getSignature() + "\nPODPIS NENÍ VALIDNÍ - INTEGRITA PORUŠENA!";
                if(!alreadyAddedToInvalid) invalidBlocks.add(block);
                integrity = false;
                result = false;
            } else {
                detailedLog+=("\nPodpis souboru " + block.getFileName() + " je validní.");
            }
            //ověření zda sedí hash předchozího bloku
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
            resultString = "<html>Blockchain je validní!</html>";
            detailedLog += "\nVÝSLEDEK: Blockchain validní, integrita archivu neporušena";
        }
        else if(integrity==false){
            resultString = "<html>Blockchain není validní! Integrita archivu porušena!</html>";
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
    public String getDetailedLog() {
        return detailedLog;
    }
}