package vut.fekt.archive;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import org.junit.Test;
import vut.fekt.archive.blockchain.Block;
import vut.fekt.archive.blockchain.Blockchain;
import vut.fekt.archive.blockchain.Crypto;

import static org.junit.Assert.assertEquals;


public class TestClass {

    @Test
    public void urlTest() throws Exception {
        //String url = "http://localhost/test1/test.txt";
        String url = "https://h-france.net/Salon/McPheeedited.pdf";
        String downloaded = "D:/Archiv/test2.pdf";
        BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(downloaded);
        byte dataBuffer[] = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
        }
        KeyPair keys = Crypto.generateKeyPair();
        Blockchain blockchain = new Blockchain();
        Block block = new Block(downloaded, downloaded, 0, "a", Crypto.sign(Files.readAllBytes(Path.of("D:/Archiv/test2.pdf")),keys.getPrivate()), keys.getPublic());
        blockchain.addBlock(block);


        System.out.println(block.toString());
        String compareFile = "D:/Archiv/test3.pdf";

        String hash1 = Crypto.getFileHash("D:/Archiv/test2.png");
        String hash2 = Crypto.getFileHash("D:/Archiv/test3.png");
        assertEquals(hash1, hash2);
    }
}
