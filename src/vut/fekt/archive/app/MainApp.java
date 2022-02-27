package vut.fekt.archive.app;

import org.xml.sax.SAXException;
import vut.fekt.archive.Archive;
import vut.fekt.archive.ArchiveDocument;
import vut.fekt.archive.BlockchainValidator;
import vut.fekt.archive.blockchain.Block;
import vut.fekt.archive.blockchain.Blockchain;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

//hlavní okno aplikace
public class MainApp extends JFrame {
    private JPanel panel1;
    public JButton newArchiveButton;
    private JButton loadArchiveButton;
    public JLabel FileLabel;
    private JButton addDocumentButton;
    private JTextPane textPane1;
    private JButton printBlockchainButton;
    private JButton validateBlockchainButton;
    private JTable documentTable;
    public JLabel archiveLabel;
    private JTextPane vypis;
    private JButton createBlockchainButton;
    private JTextField urlField;
    DefaultTableModel tableModel = new DefaultTableModel();
    //documentTable.addColumn();

    private Archive archive;
    public MainApp frame;
    private NewArchive na;
    private NewDocument nd;
    private ShowDocument sd;
    private Map<String, ArchiveDocument> documentTimeMap = new HashMap<>();

    //inicializace framu
    public void initMainapp(){

        frame = new MainApp(this.na, this.nd,this.sd);
        frame.setTitle("Blockchain Archiv");
        frame.pack();
        frame.setContentPane(this.panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(100, 100, 500, 500);
        frame.setSize(500,500);
        initTable();

    }

    public void setVypis(String s){
        vypis.setText(s);
    }

    public String getVypis(){
        return vypis.getText();
    }

    //inicializace vypisu dokumentu
    public void initTable(){
        String column[] = {"NÁZEV","VERZE","AUTOR","VYTVOŘENO","ID"};

        for(String columnName : column){
            tableModel.addColumn(columnName);
        }

        //aby nešlo menit hodnoty v tabulce
        documentTable.setDefaultEditor(Object.class, null);
        documentTable.setModel(tableModel);

    }

    //update vypisu dokumentu
    public void updateList() throws ParserConfigurationException, IOException, SAXException {
        LinkedList<Block> blocks = archive.getBlockchain().getBlocks(); //nacteni bloků z blockchainu
        int size = blocks.size();
        String[] documentNames = new String[size];
        int i =0;
        documentTimeMap = new HashMap<>(); //mapa, dokument-timestamp
        tableModel.setRowCount(0); //vyresetuje tabulku
        //pro každý blok je přidán nový řádek
        for (Block block:blocks) {
            //z bloku se vezmou cesty k souborům
            File content = new File(block.getFilepath());
            File metadata = new File(block.getMetapath());
            //inicializuje se ArchiveDocument
            ArchiveDocument ad = new ArchiveDocument();
            ad.loadDocument(content,metadata);
            //z ArchiveDocumentu získáme udaje pro výpis
            String[] row = {ad.getDocName(),ad.getVersion(),ad.getAuthor(),ad.getTimestamp(), String.valueOf(ad.getId())};
            tableModel.addRow(row);
            documentTimeMap.put(ad.getTimestamp(),ad);
            documentNames[i]=ad.getDocName();
            i++;
        }
        documentTable.setModel(tableModel);

    }

    public MainApp(NewArchive narch, NewDocument ndoc, ShowDocument showdoc){
        this.na = narch;
        this.nd = ndoc;
        this.sd = showdoc;
        //tlačítko Nový archiv zviditelní dané okno
        newArchiveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                na.frame.setVisible(true);

            }
        });

        //Tlačítko načíst archiv
        loadArchiveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); //vybíráme jenom složky
                //fc.setCurrentDirectory(new File("D:/Archiv/")); //kde se průzkumník otevře
                int r = fc.showOpenDialog(panel1);
                if (r == JFileChooser.APPROVE_OPTION)
                {
                    try {
                        //inicializace archivu na základě vybrané složky
                        archive = new Archive(fc.getSelectedFile().getName(),fc.getSelectedFile().getAbsolutePath());
                        archive.loadKeyPair(new File(archive.getArchiveFolder()+"/KeyPair.txt"));
                        archive.loadArchiveBlockchain(new File(archive.getArchiveFolder()+"/serializedBlockchain.txt"));
                        FileLabel.setText("Archiv " + archive.getName() + " načten!");
                        archiveLabel.setText(archive.getName());
                        updateList();
                    } catch (IOException | ClassNotFoundException | ParserConfigurationException | SAXException ioException) {
                        ioException.printStackTrace();
                    }
                }
                else{
                    FileLabel.setText("Uživatel zrušil operaci");}
            }
        });
        //tlačítko nový dokument
        addDocumentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nd.init();
                nd.getFrame().setVisible(true);

            }
        });
        //tlačítko výpis blockchainu
        printBlockchainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textPane1.setText(getArchive().getBlockchain().toString());
            }
        });
        //otevření detailu pro vybraný dokument
        documentTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent event) {
                if ((!event.getValueIsAdjusting())&&(documentTable.getRowCount()!=0)){
                    try {
                        sd.init(documentTimeMap.get(documentTable.getValueAt(documentTable.getSelectedRow(), 3))); //výběr je podle timestampu který je ve 4 sloupic
                        sd.getFrame().setVisible(true);
                    }
                    catch (Exception e){
                        System.out.println("Divná chyba");
                    }
                }
            }
        });
        //tlačítko validovat blockchain
        validateBlockchainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileLabel.setText("Validating blockchain of archive " + archive.getName() + "...");
                BlockchainValidator bv = new BlockchainValidator(archive.getBlockchain());
                try {
                    bv.validate();
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
                String result = bv.getResultString();
                FileLabel.setText(result);
                textPane1.setText(bv.getDetailedLog());

            }
        });
        createBlockchainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String folder = urlField.getText();
                    Blockchain chain = new Blockchain();
                    System.out.println(folder);
                    if (folder != null) {
                        Path file = Path.of(folder + "documents.txt");
                        String text = Files.readString(file);
                        String[] docs = text.split(",");
                        for (String doc : docs) {
                            File docFiles = new File(folder + "/" + doc);
                            System.out.println(folder + doc);
                            if (docFiles.listFiles() != null) {
                                for (File f : docFiles.listFiles()) {
                                    System.out.println(f.getName());
                                }
                            }
                        }
                    }
                }
                catch (Exception eee){
                    eee.printStackTrace();
                }
            }
        });
    }


    public Archive getArchive() {
        return archive;
    }

    public void setArchive(Archive archive) {
        this.archive = archive;
        FileLabel.setText("Archiv " +archive.getName()+" vytvořen!");
    }

}
