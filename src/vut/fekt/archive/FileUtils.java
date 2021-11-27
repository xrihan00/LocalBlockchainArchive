package vut.fekt.archive;


import java.io.*;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtils {


    public FileUtils() throws NoSuchAlgorithmException {
    }

    public static String getFileHash(String filepath) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        System.out.println("Hashing "+filepath);
        FileInputStream fis =new FileInputStream(filepath);
        BufferedInputStream bis = new BufferedInputStream(fis);
        try (DigestInputStream dis = new DigestInputStream(bis, md)) {
            while (dis.read() != -1) ; //empty loop to clear the data
            md = dis.getMessageDigest();
        }
        System.out.println("aaa");
        // bytes to hex
        StringBuilder result = new StringBuilder();
        for (byte b : md.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();

    }

}
