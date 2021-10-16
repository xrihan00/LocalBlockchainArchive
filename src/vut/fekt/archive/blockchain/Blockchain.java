package vut.fekt.archive.blockchain;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class Blockchain implements Serializable {
    LinkedList<Block> blocks;           // prepojený list blokov
    Crypto crypto= new Crypto();

    public Blockchain(){
        this.blocks = new LinkedList<>();
    }


  public String getLastHash(){
        if(blocks==null || blocks.isEmpty()){
            return "FIRST BLOCK";
        }
        return crypto.blockHash(blocks.getLast());
    }       // získaní hashe posledního bloku


    public void addBlock(Block block){                  // pridávanie blokov na blockchain
        block.setPreviousHash(getLastHash());
        blocks.add(block);
    }

    public int randomId(){                      // náhodné zvolení ID hlasu
        Random rng = new Random();
        return rng.nextInt(10000);
    }

    public LinkedList<Block> getBlocks() {
        return blocks;
    }       //getter pre bloky
    public void setBlocks(LinkedList<Block> blocks) {
        this.blocks = blocks;
    } //setter pre bloky

    @Override
    public String toString() {                      // prevedení pro výpis cez metodu toString
        StringBuilder sb = new StringBuilder();
        sb.append("---------Blockchain---------\n");
        for (int i = 0; i < getBlocks().size(); i++) {
            sb.append("_________________________\n");
            sb.append("\nBlock " + i + ":");
            sb.append(blocks.get(i).toString());
            sb.append("\nHash tohoto bloku: " + crypto.blockHash(blocks.get(i)));
            sb.append("\n----------End of Block------------\n");
        }
        return sb.toString();
    }
}
