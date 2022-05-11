package vut.fekt.archive;

import vut.fekt.archive.blockchain.Block;
import vut.fekt.archive.blockchain.Blockchain;
import vut.fekt.archive.blockchain.Crypto;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.LinkedList;

public class BlockchainValidator {
    private String resultString ="<html>";
    private String detailedLog="";
    private boolean integrity=true;
    private ArrayList<Block> invalidBlocks = new ArrayList();
    private ArrayList<PublicKey> validPublicKeys = new ArrayList<>();
    private Blockchain blockchain;
    private String hostname;

    //validátor blockchainu
    public BlockchainValidator(Blockchain blockchain, String hostname, ArrayList<PublicKey> validPublicKeys ) {
        this.blockchain = blockchain;
        this.hostname = hostname;
        this.validPublicKeys = validPublicKeys;
    }

    public boolean validateNewBlock(Blockchain chain) throws Exception {
        detailedLog = "VALIDACE NOVÉHO BLOKU...";
        boolean result = true;
        integrity = true;
        LinkedList<Block> blocks = chain.getBlocks();
        Block newBlock = blocks.getLast();
        System.out.println(newBlock.getDocName());
        String secondToLastHash = "FIRST BLOCK";
        if(blocks.size()>1) {
            secondToLastHash = Crypto.blockHash(blocks.get(blocks.size() - 2));
        }
        //validace hashe předchozího bloku
        if(!newBlock.getPreviousHash().equals(secondToLastHash)){
            detailedLog +="\nHodnota \"Previous Hash\" = " + newBlock.getPreviousHash() + "\nměla by být " + secondToLastHash;
            resultString = "Blockchain not valid!";
            integrity = false;
            result = false;
        }
        //validace hashe souboru
        String filehash = Crypto.getHashOfUrls(newBlock.getFilepath(),hostname, newBlock.getDocName());
        if(!filehash.equals(newBlock.getFilehash())){
            detailedLog += "\nHash souboru:\n " + newBlock.getFilehash() + "\nměla by být:\n " + filehash + "\nHASH NENÍ VALIDNÍ - INTEGRITA PORUŠENA!\n";
            integrity = false;
            result = false;
        }
        //validace platnosti klíče
        boolean validKey = validPublicKeys.contains(newBlock.getPubKey());
        if (!validKey) {
            detailedLog += "\nVeřejný klíč:  " + newBlock.getPubKey() + "\nVEŘEJNÝ KLĆ NENÍ VALIDNÍ!";
            integrity = false;
            result = false;
        }
        //va
        //validace podpisu
        boolean signature = Crypto.verify(filehash.getBytes(StandardCharsets.UTF_8),newBlock.getSignature(),newBlock.getPubKey());
        if (!signature) {
            detailedLog += "\nPodpis:  " + newBlock.getSignature() + "\nPODPIS NENÍ VALIDNÍ - INTEGRITA PORUŠENA!";
            integrity = false;
            result = false;
        }
        //validace kompatiblity
        if(!newBlock.getPreviousHash().equals(blockchain.getLastHash())){
            detailedLog += "\nNový blok není kompatibiní.\nPrevious hash: "+ newBlock.getPreviousHash()+"\nLast hash: "+blockchain.getLastHash();
            integrity = false;
            result = false;
        }
        if(integrity){
            detailedLog += "\nVÝSLEDEK: Nový blok je validní, přidávám do blockchainu.";
        }
        if(!integrity){
            detailedLog += "\nVÝSLEDEK: Nový blok NENÍ validní,zahazuji.";
        }
        return result;
    }

    //metoda validace - vrací true nebo false a ukládá výpis ve Stringu detailedLog
    public boolean validateBlockHashes() throws Exception {
        boolean result = true;
        LinkedList<Block> blocks = blockchain.getBlocks();
        Crypto crypto = new Crypto();
        String blockhash = "FIRST BLOCK";
        detailedLog = "VYPIS VALIDACE BLOCKCHAINU\n--------------------";

        //for loop který prochází blok po bloku
        for (Block block : blocks) {
            detailedLog+="\nDokument " + block.getDocName() + "\n";
            //ověření zda sedí hash předchozího bloku
            if (!block.getPreviousHash().equals(blockhash)) {
                detailedLog +="\nHodnota \"Previous Hash\" = " + block.getPreviousHash() + "\nměla by být " + blockhash;
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
        }
        return result;
    }

    //metoda validace - vrací true nebo false a ukládá výpis ve Stringu detailedLog
    public boolean validateAll() throws Exception {
        boolean result = true;
        LinkedList<Block> blocks = blockchain.getBlocks();
        Crypto crypto = new Crypto();
        String blockhash = "FIRST BLOCK";
        detailedLog = "VYPIS VALIDACE BLOCKCHAINU\n--------------------";

        //for loop který prochází blok po bloku
        for (Block block : blocks) {
            detailedLog+="\nDokument " + block.getDocName() + "\n";
            String filehash = Crypto.getHashOfUrls(block.getFilepath(),hostname, block.getDocName());
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
                detailedLog+=("\nHash je validní.");
            }
            //ověření validity podpisu
            boolean validKey = validPublicKeys.contains(block.getPubKey());
            if (!validKey) {
                detailedLog += "\nVeřejný klíč:  " + block.getPubKey() + "\nVEŘEJNÝ KLĆ NENÍ VALIDNÍ!";
                integrity = false;
                result = false;
            }
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
                detailedLog +="\nHodnota \"Previous Hash\" = " + block.getPreviousHash() + "\nměla by být " + blockhash;
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
    public boolean containsBlock(Block block){
        if(blockchain.getLastHash().equals(Crypto.blockHash(block))){
            return true;
        }
        else return false;
    }

    public String getResultString() {
        return resultString;
    }
    public String getDetailedLog() {
        return detailedLog;
    }
}