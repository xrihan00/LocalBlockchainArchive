package vut.fekt.archive.app;

import vut.fekt.archive.ArchiveDocument;

import javax.swing.*;

public class ShowDocument extends JFrame{
    private JTextField nazevField;
    private JTextField autorField;
    private JTextField timeField;
    private JButton openButton;
    private JTextField obsahField;
    private JTextField versionField;
    private JTextField idField;
    private JPanel panel;

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

    }



    public ArchiveDocument getDoc() {
        return doc;
    }

    public void setDoc(ArchiveDocument doc) {
        this.doc = doc;
    }
}
