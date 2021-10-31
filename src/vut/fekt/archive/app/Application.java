package vut.fekt.archive.app;

import org.junit.Test;
import vut.fekt.archive.Archive;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

public class Application {
    public static String archivPath = "D:/Archiv/";


    @Test
    public void archiveTest(){
        try {
            Archive archive = new Archive("Archiv 1",archivPath);

            String testpath = archivPath+"TestFiles/";
            ArrayList<String> files = new ArrayList();
            files.add(testpath+"test1.docx");
            files.add(testpath+"test2.xlsx");
            files.add(testpath+"test3.txt");
            files.add(testpath+"test4.pdf");
            files.add(testpath+"test5.jpg");
            for (int i = 0; i < 5; i++) {
                File content = new File(files.get(i));
                archive.addDocument(content, "User","Test Document " + (i+1), "1");
            }
            archive.saveArchiveBlockchain();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }




}
