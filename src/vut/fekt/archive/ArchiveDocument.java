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

import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class ArchiveDocument {
    File metadata;
    File content;
    File docuFile;
    Path docFolder;

    String author;
    String docName;
    int id;
    String version;
    String timestamp;
    String hash;
    String contentName;

    public ArchiveDocument(File content, String archiveFolder, String author, String docName, String version) throws Exception {
        this.author = author;
        this.docName = docName;
        this.version = version;
        this.content = content;
        this.hash = FileUtils.getFileHash(content.getAbsolutePath());
        this.contentName = content.getName();
        Random rng = new Random();
        this.id = rng.nextInt(99000)+1000;
        timestamp = new SimpleDateFormat("HH:mm:ss dd. MM. yyyy").format(new java.util.Date());
        docFolder = Files.createDirectory(Path.of(archiveFolder+"/"+String.valueOf(id)));
        createDocFiles(content);
    }

    public ArchiveDocument(){
    }

    private void createDocFiles(File content) throws IOException, ParserConfigurationException, TransformerException, NoSuchAlgorithmException {
        FileOutputStream docFile = new FileOutputStream(docFolder+"/"+content.getName());
        Files.copy(content.getAbsoluteFile().toPath(), docFile);
        docuFile = new File(docFolder+"/"+content.getName());
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
        root.setAttribute("hash",hash);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(metadata);
        transformer.transform(source, result);

        // Output to console for testing
        StreamResult consoleResult = new StreamResult(System.out);
        transformer.transform(source, consoleResult);

    }

    public void loadDocument(File content, File meta) throws ParserConfigurationException, IOException, SAXException {
        this.content = content;
        this.metadata = meta;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(meta);
        doc.getDocumentElement().normalize();

        NodeList list = doc.getElementsByTagName("metadata");
        Node node = list.item(0);
        Element element = (Element) node;
        //Element element = doc.getElementById("metadata");
        this.author = element.getAttribute("author");
        this.docName = element.getAttribute("docName");
        this.version = element.getAttribute("version");
        this.timestamp = element.getAttribute("added");
        this.id = Integer.parseInt(element.getAttribute("id"));
        this.hash = element.getAttribute("hash");
    }

    public String getAuthor() {
        return author;
    }

    public String getDocName() {
        return docName;
    }

    public int getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getHash() {
        return hash;
    }
    public String getContentPath() {
        return content.getAbsolutePath();
    }
    public String getContentName(){
        return contentName;
    }
}
