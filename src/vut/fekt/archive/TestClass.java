package vut.fekt.archive;


import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import vut.fekt.archive.blockchain.Blockchain;
import vut.fekt.archive.blockchain.Crypto;
import vut.fekt.archive.blockchain.CryptoException;

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
       // Block block = new Block(downloaded, downloaded, 0, "a", Crypto.sign(Files.readAllBytes(Path.of("D:/Archiv/test2.pdf")),keys.getPrivate()), keys.getPublic());
       // blockchain.addBlock(block);


        //System.out.println(block.toString());
        String compareFile = "D:/Archiv/test3.pdf";

        String hash1 = Crypto.getFileHash("D:/Archiv/test2.png");
        String hash2 = Crypto.getFileHash("D:/Archiv/test3.png");
        assertEquals(hash1, hash2);
    }

    @Test
    public void ipTest() throws UnknownHostException {
        InetAddress ip = InetAddress.getByName("localhost");

        InetAddress ip2 = InetAddress.getLocalHost();
        InetAddress ip3 = InetAddress.getLoopbackAddress();
        InetAddress ip4 = InetAddress.getByName("127.0.0.1");
        System.out.println(ip.getHostAddress());
        System.out.println(ip2);
        System.out.println(ip3);
        System.out.println(ip4);

    }

    @Test
    public void hashPassTest() throws NoSuchAlgorithmException {
        String[] pass = {"heslo1","heslo2","heslo3"};
        Vector<String> hashes = new Vector<>();
        for (String s:pass
             ) {
            hashes.add(Crypto.getStringHash(s));
        }
        System.out.println(hashes);
    }

    @Test
    public void encryptTest(){
        File orig = new File("C:/Soubory/Test/TestovaciDokument.txt");
        File encrypted = new File("C:\\Users\\Tobias\\Downloads\\TestovaciDokument.txt");
       // File decrypted = new File("C:\\Diplomka\\DecrDiplomka.7z");
        try {
            Crypto.encrypt("heslo",encrypted.getName(),orig,encrypted);
            //Crypto.decrypt("hesloa",encrypted,decrypted);
        } catch (CryptoException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


    }
    @Test
    public void fileTest(){
        try {
            FileUtils.moveDirectoryToDirectory(new File("C:\\Programy\\Xampp\\htdocs\\archive\\asfasf\\"),new File("D:\\Archiv\\Upload\\upload-api\\archive\\asfasf\\"),true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void parseTest() throws Exception {
        String archive = "D:/Archiv/_Archive2/";
        Path file = Path.of(archive + "documents.txt");
        String text = Files.readString(file);
        String[] docs = text.split(",");
        for (String doc: docs) {
            File docFiles = new File(archive+"/"+doc);
            System.out.println(archive+doc);
            if(docFiles.listFiles()!=null) {
                for (File f : docFiles.listFiles()) {
                    System.out.println(f.getName());
                }
                ;
            }
        }
    }

@Test
    public void hashTest() throws NoSuchAlgorithmException {
        System.out.println(Crypto.getStringHash("aaa"));
    System.out.println(Crypto.getStringHash("aba"));
}



}
