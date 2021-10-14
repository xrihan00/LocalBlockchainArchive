package vut.fekt.archive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Random;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class ArchiveDocument {
    File metadata;
    File content;
    FileOutputStream docFile;
    Path docFolder;

    String author;
    String docName;
    int id;
    String version;
    String timestamp;

    public ArchiveDocument(File content, String archiveFolder, String author, String docName, String version) throws Exception {
        this.author = author;
        this.docName = docName;
        this.version = version;
        this.content = content;
        Random rng = new Random();
        this.id = rng.nextInt(99000)+1000;
        timestamp = new SimpleDateFormat("HH:mm:ss dd. MM. yyyy").format(new java.util.Date());
        docFolder = Files.createDirectory(Path.of(archiveFolder+"/"+String.valueOf(id)));
        createDocFiles(content);
    }

    private void createDocFiles(File content) throws IOException, ParserConfigurationException, TransformerException, NoSuchAlgorithmException {
        docFile = new FileOutputStream(docFolder+"/"+content.getName());
        Files.copy(content.getAbsoluteFile().toPath(), docFile);
        createMetadata();
    }

    private void createMetadata() throws ParserConfigurationException, IOException, TransformerException, NoSuchAlgorithmException {
        metadata = new File(docFolder+"/"+id+".xml");
        DocumentBuilderFactory dbFactory =
        DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.newDocument();

        Element root= doc.createElement("metadata");
        doc.appendChild(root);

        root.setAttribute("author", author);
        root.setAttribute("docName", docName);
        root.setAttribute("version", version);
        root.setAttribute("added", timestamp);
        root.setAttribute("id", String.valueOf(id));
        root.setAttribute("hash",FileUtils.getFileHash(content.getAbsolutePath()));

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(metadata);
        transformer.transform(source, result);

        // Output to console for testing
        StreamResult consoleResult = new StreamResult(System.out);
        transformer.transform(source, consoleResult);

    }
}
