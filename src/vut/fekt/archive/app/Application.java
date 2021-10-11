package vut.fekt.archive.app;

import org.junit.Test;
import org.xml.sax.SAXException;
import vut.fekt.archive.Archive;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;

public class Application {

    @Test
    public void archiveTest(){
        try {
            Archive archive = new Archive("Archiv 1","D:/Archiv");
            File content = new File("D:/new.tif");
            archive.addDocument(content, "User","Test Document", "1");

        } catch (IOException | ParserConfigurationException | TransformerException | SAXException e) {
            e.printStackTrace();
        }

    }




}
