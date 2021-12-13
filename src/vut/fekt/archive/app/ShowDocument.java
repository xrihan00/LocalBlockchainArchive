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
    private JButton novaVerzeButton;
    private String newVersionCount;
    public ShowDocument frame;

    private ArchiveDocument doc;
    public boolean newVersion = false;

    public ShowDocument(){
        //tlačítko Otevřít obsah, otevře průzkumník ve složce kde se nachází obsah
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
        //tlačítko Nová verze
        //schova okno, boolean newVersion je true, což zachyti thread ve třídě App a spustí vytváření nové verze
        novaVerzeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);
                newVersion = true;
            }
        });
    }

    public void init(ArchiveDocument document){
        frame = new ShowDocument();
        frame.setTitle("Detail dokumentu");
        doc = document;
        nazevField.setText(doc.getDocName());
        autorField.setText(doc.getAuthor());
        timeField.setText(doc.getTimestamp());
        obsahField.setText(doc.getContentName());
        nazevField.setText(doc.getDocName());
        versionField.setText(doc.getVersion());
        idField.setText(String.valueOf(doc.getId()));

        frame.pack();
        frame.setContentPane(this.panel);
        frame.setBounds(100, 100, 300, 200);
        frame.setSize(500,300);
    }

    public String getNewVersionCount(){
        String current = doc.getVersion();
        double currentDouble = Double.valueOf(current);
        double newDouble = currentDouble+1.0;
        return String.valueOf(newDouble);
    }

    public ArchiveDocument getDoc() {
        return doc;
    }

    public void setDoc(ArchiveDocument doc) {
        this.doc = doc;
    }


    public ShowDocument getFrame() {
        return frame;
    }
}
