package vut.fekt.archive.blockchain;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Random;

public class Blockchain implements Serializable {

    LinkedList<Block> blocks;           // linkedlist bloků
    Crypto crypto= new Crypto();

    //reprezentuje celý blockchain
    public Blockchain(){
        this.blocks = new LinkedList<>();
    }

    //vrátí hash posledního bloku
    public String getLastHash(){
        if(blocks==null || blocks.isEmpty()){
            return "FIRST BLOCK";
        }
        return crypto.blockHash(blocks.getLast());
    }


    //pridání bloku do blockchainu - je mu předána hodnota previoushash
    public void addBlock(Block block){
        block.setPreviousHash(getLastHash());
        blocks.add(block);
    }

    public void addRecievedBlock(Block block){
        blocks.add(block);
    }

    //nahodne vygenerovane Id - používané dříve
    public int randomId(){
        Random rng = new Random();
        return rng.nextInt(10000);
    }

    public LinkedList<Block> getBlocks() {
        return blocks;
    }
    public void setBlocks(LinkedList<Block> blocks) {
        this.blocks = blocks;
    }

    @Override
    public String toString() {
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
