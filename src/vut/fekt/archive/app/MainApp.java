package vut.fekt.archive.app;

import org.xml.sax.SAXException;
import vut.fekt.archive.Archive;
import vut.fekt.archive.ArchiveDocument;
import vut.fekt.archive.BlockchainValidator;
import vut.fekt.archive.blockchain.Block;
import vut.fekt.archive.blockchain.Crypto;

import javax.swing.*;

import javax.swing.JFileChooser;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class MainApp extends javax.swing.JFrame {
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
    DefaultTableModel tableModel = new DefaultTableModel();
    //documentTable.addColumn();

    private Archive archive;
    public MainApp frame;
    private NewArchive na;
    private NewDocument nd;
    private ShowDocument sd;
    private Map<String, ArchiveDocument> documentTimeMap = new HashMap<>();

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

    public void initTable(){
        String column[] = {"NÁZEV","AUTOR","VYTVOŘENO"};

        for(String columnName : column){
            tableModel.addColumn(columnName);
        }

        documentTable.setModel(tableModel);

    }

    public void hideFrame(){
        this.setVisible(false);
    }

    public void updateList() throws ParserConfigurationException, IOException, SAXException {
        LinkedList<Block> blocks = archive.getBlockchain().getBlocks();
        int size = blocks.size();
        String[] documentNames = new String[size];
        int i =0;
        documentTimeMap = new HashMap<>();
        tableModel.setRowCount(0);
        for (Block block:blocks) {
            File content = new File(block.getFilepath());
            File metadata = new File(block.getMetapath());
            ArchiveDocument ad = new ArchiveDocument();
            ad.loadDocument(content,metadata);
            String[] row = {ad.getDocName(),ad.getAuthor(),ad.getTimestamp()};
            tableModel.addRow(row);
            documentTimeMap.put(ad.getTimestamp(),ad);
            documentNames[i]=ad.getDocName();
            i++;
        }
        documentTable.setModel(tableModel);


    }

    public MainApp(NewArchive narch, NewDocument ndoc, ShowDocument showdoc) {
        this.na = narch;
        this.nd = ndoc;
        this.sd = showdoc;
        newArchiveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                na.frame.setVisible(true);

            }
        });
;
        loadArchiveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fc.setCurrentDirectory(new File("D:/Archiv/"));
                int r = fc.showOpenDialog(panel1);
                if (r == JFileChooser.APPROVE_OPTION)
                {
                    try {
                        archive = new Archive(fc.getSelectedFile().getName(),fc.getSelectedFile().getAbsolutePath());
                        archive.loadArchiveBlockchain(new File(archive.getArchiveFolder()+"/serializedBlockchain.txt"));
                        FileLabel.setText("Archiv " + archive.getName() + " načten!");
                        archiveLabel.setText(archive.getName());
                        updateList();
                    } catch (IOException | ClassNotFoundException | ParserConfigurationException | SAXException ioException) {
                        ioException.printStackTrace();
                    }
                }
                // if the user cancelled the operation
                else{
                    FileLabel.setText("Uživatel zrušil operaci");}


            }

        });
        addDocumentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nd.init();
                nd.getFrame().setVisible(true);

            }
        });
        printBlockchainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textPane1.setText(getArchive().getBlockchain().toString());
            }
        });
        documentTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent event) {
                if ((!event.getValueIsAdjusting())&&(documentTable.getRowCount()!=0)){
                    sd.init(documentTimeMap.get(documentTable.getValueAt(documentTable.getSelectedRow(),2)));
                    sd.getFrame().setVisible(true);
                }
            }
        });
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
    }


    public Archive getArchive() {
        return archive;
    }

    public void setArchive(Archive archive) {
        this.archive = archive;
        FileLabel.setText("Archiv " +archive.getName()+" vytvořen!");
    }

}
