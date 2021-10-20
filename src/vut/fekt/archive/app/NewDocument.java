package vut.fekt.archive.app;

import vut.fekt.archive.ArchiveDocument;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class NewDocument extends JFrame{
    private JPanel panel;
    private JTextField docName;
    private JButton pickContentButton;
    private JButton OKButton;
    private JTextField author;
    private String directory;
    private File newContent;
    private ArchiveDocument archdoc;
    private NewDocument frame;
    private String documentName;
    private String authorName;

    boolean ok = false;


    public NewDocument() {
        pickContentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                int r = fc.showOpenDialog(panel);
                if (r == JFileChooser.APPROVE_OPTION) {
                    directory = fc.getSelectedFile().getAbsolutePath();
                    newContent = new File(directory);
                }
            }
        });
        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                documentName = docName.getText();
                authorName = author.getText();
                ok=true;
            }
        });
    }

    public void init(){
        frame = new NewDocument();
        frame.pack();
        frame.setContentPane(this.panel);
        frame.setBounds(100, 100, 300, 200);
        frame.setSize(300,200);
    }

    public NewDocument getFrame() {
        return frame;
    }

    public File getNewContent() {
        return newContent;
    }

    public String getDocumentName() {
        return documentName;
    }

    public String getAuthorName() {
        return authorName;
    }

}