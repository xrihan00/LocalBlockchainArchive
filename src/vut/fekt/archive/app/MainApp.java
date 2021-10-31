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
import javax.swing.table.TableColumn;
import javax.swing.text.DefaultCaret;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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
    private JList documentList;
    private JButton validateBlockchainButton;
    private JTable documentTable;
    DefaultTableModel tableModel = new DefaultTableModel();
    //documentTable.addColumn();

    private Archive archive;
    public MainApp frame;
    private NewArchive na;
    private NewDocument nd;
    private Map<String, ArchiveDocument> documentNameMap = new HashMap<>();

    public void initMainapp(){

        frame = new MainApp(this.na, this.nd);
        frame.pack();
        frame.setContentPane(this.panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(100, 100, 500, 500);
        frame.setSize(500,500);
        initTable();
        documentList.setVisible(true);

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
        documentNameMap = new HashMap<>();

        for (Block block:blocks) {
            File content = new File(block.getFilepath());
            File metadata = new File(block.getMetapath());
            ArchiveDocument ad = new ArchiveDocument();
            ad.loadDocument(content,metadata);
            String[] row = {ad.getDocName(),ad.getAuthor(),ad.getTimestamp()};
            tableModel.addRow(row);
            documentNameMap.put(ad.getDocName(),ad);
            documentNames[i]=ad.getDocName();
            i++;
        }
        documentList.setListData(documentNames);
        documentList.setVisible(true);
        documentTable.setModel(tableModel);


    }

    public MainApp(NewArchive narch, NewDocument ndoc) {
//        DefaultCaret caret = (DefaultCaret)textPane1.getCaret();
//        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        this.na = narch;
        this.nd = ndoc;
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
        documentList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()){
                    ShowDocument sd = new ShowDocument(documentNameMap.get(documentList.getSelectedValue()));
                    sd.setVisible(true);

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

    public boolean validateChain(){      //validace blockchainu - vyzkoušení zda hashe odpovídají
        LinkedList<Block> blocks = this.archive.getBlockchain().getBlocks();
        for (int i = 1; i < blocks.size(); i++) {
            String hash = blocks.get(i).getPreviousHash();
            String check = Crypto.blockHash(blocks.get(i - 1));
            if(!hash.equals(check)){
                return false;
            }
        }
        return true;
    }

}
