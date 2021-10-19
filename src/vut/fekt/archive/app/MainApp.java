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
    private Archive archive;
    public MainApp frame;
    private NewArchive na;


    public void initMainapp(){
        frame = new MainApp(this.na);
        frame.pack();
        frame.setContentPane(this.panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(100, 100, 500, 500);
        frame.setSize(500,500);

    }

    public void hideFrame(){
        this.setVisible(false);
    }

    public MainApp(NewArchive narch) {
        this.na = narch;
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
                        FileLabel.setText("Archive " + archive.getName() + " loaded!");
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
                // if the user cancelled the operation
                else{
                    FileLabel.setText("the user cancelled the operation");}

            }

        });
    }
}
