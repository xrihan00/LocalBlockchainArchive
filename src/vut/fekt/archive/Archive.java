package vut.fekt.archive;

import org.xml.sax.SAXException;
import vut.fekt.archive.blockchain.Blockchain;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Archive implements Serializable {
    String name;
    Blockchain blockchain;
    String archiveFolder;

    List<ArchiveDocument> documents = new ArrayList<>();

    public Archive(String name, String folder) throws IOException {
        this.archiveFolder = folder  + "/" + name;
        new File(archiveFolder).mkdirs();
    }

    public void addDocument(File content, String author, String docName, String version) throws ParserConfigurationException, TransformerException, SAXException, IOException {
        documents.add(new ArchiveDocument(content, archiveFolder, author, docName, version));
    }


}
