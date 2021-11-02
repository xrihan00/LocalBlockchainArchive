package vut.fekt.archive.app;

import vut.fekt.archive.ArchiveDocument;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ShowDocument extends JFrame{
    private JTextField nazevField;
    private JTextField autorField;
    private JTextField timeField;
    private JButton openButton;
    private JTextField obsahField;
    private JTextField versionField;
    private JTextField idField;
    private JPanel panel;
    private JButton nováVerzeButton;

    private ArchiveDocument doc;

    public ShowDocument(ArchiveDocument document){


        this.doc = document;
        nazevField.setText(doc.getDocName());
        autorField.setText(doc.getAuthor());
        timeField.setText(doc.getTimestamp());
        obsahField.setText(doc.getContentName());
        nazevField.setText(doc.getDocName());
        versionField.setText(doc.getVersion());
        idField.setText(String.valueOf(doc.getId()));

        this.pack();
        this.setContentPane(this.panel);
        this.setBounds(100, 100, 300, 200);
        this.setSize(500,300);

        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Runtime.getRuntime().exec("explorer.exe /select," + doc.getContentPath());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
    }



    public ArchiveDocument getDoc() {
        return doc;
    }

    public void setDoc(ArchiveDocument doc) {
        this.doc = doc;
    }
}
