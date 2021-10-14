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

    public Blockchain(PublicKey adminKey, int numberOfVoters){
        this.blocks = new LinkedList<>();
        firstBlock(adminKey, numberOfVoters);
    }

    public String getLastHash(){
        if(blocks==null || blocks.isEmpty()){
            return "FIRST BLOCK";
        }
        return crypto.blockHash(blocks.getLast());
    }       // získaní hashe posledního bloku

    private void firstBlock(PublicKey adminKey, int voters){            //vytvorenie inicializačného bloku - admin si sám sobě pošle tolik hlasů kolik je voličů
        Transaction tr = new Transaction(adminKey,adminKey, voters);
        Block block = new Block("0",tr,randomVoteId());
        blocks.add(block);
    }

    public void addBlock(Block block){                  // pridávanie blokov na blockchain
        block.setPreviousHash(getLastHash());
        blocks.add(block);
    }

    public int getBalance(PublicKey pubkey){            //zjištění kolik má peněženka s daným veřejným klíčem na účtě hlasů
        int balance = 0;
        for (Block block:blocks) {
            Transaction tr = block.getTransaction();
            if(tr.getSourcePublicKey().equals(pubkey)){
                balance -= tr.getSum();
            }
            if(tr.getDestinationPublicKey().equals(pubkey)){
                balance += tr.getSum();
            }
        }
        return balance;
    }

    public void firstTransaction(PublicKey adminKey, ArrayList<PublicKey> keys){    // prvá transakcia - poslání 1 hlasu všem vygenerovaným peněženkám
        for (PublicKey key: keys) {
            System.out.println(getBalance(adminKey));
            Transaction tr = new Transaction(adminKey,key,1);
            Block block = new Block(getLastHash(),tr, randomVoteId());
            addBlock(block);
        }
    }

    public int randomVoteId(){                      // náhodné zvolení ID hlasu
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
