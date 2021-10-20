package vut.fekt.archive.app;

import vut.fekt.archive.Archive;

import javax.swing.*;

import javax.swing.JFileChooser;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class MainApp extends javax.swing.JFrame {
    private JPanel panel1;
    public JButton newArchiveButton;
    private JButton loadArchiveButton;
    public JLabel FileLabel;
    private JButton addDocumentButton;
    private JTextPane textPane1;
    private JButton printBlockchainButton;
    private Archive archive;
    public MainApp frame;
    private NewArchive na;
    private NewDocument nd;

    public void initMainapp(){
        frame = new MainApp(this.na, this.nd);
        frame.pack();
        frame.setContentPane(this.panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(100, 100, 500, 500);
        frame.setSize(500,500);

    }

    public void hideFrame(){
        this.setVisible(false);
    }

    public MainApp(NewArchive narch, NewDocument ndoc) {
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

                int r = fc.showOpenDialog(panel1);
                if (r == JFileChooser.APPROVE_OPTION)
                {
                    try {
                        archive = new Archive(fc.getSelectedFile().getName(),fc.getSelectedFile().getAbsolutePath());
                        FileLabel.setText("Archiv " + archive.getName() + " načten!");
                    } catch (IOException ioException) {
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
    }


    public Archive getArchive() {
        return archive;
    }

    public void setArchive(Archive archive) {
        this.frame.archive = archive;
        FileLabel.setText("Archiv " +archive.getName()+" vytvořen!");
    }
}
