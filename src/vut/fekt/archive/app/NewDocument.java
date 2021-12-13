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
    private JLabel path;
    private String directory;
    private File newContent;
    private ArchiveDocument archdoc;
    private NewDocument frame;
    private String documentName;
    private String authorName;
    private String version;

    boolean ok = false;

    //okno vytváření nového dokumentu
    public NewDocument() {
        //výběr souboru obsahu
        pickContentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                //fc.setCurrentDirectory(new File("D:/Archiv/"));
                int r = fc.showOpenDialog(panel);
                if (r == JFileChooser.APPROVE_OPTION) {
                    directory = fc.getSelectedFile().getAbsolutePath();
                    newContent = new File(directory);
                }
                path.setText(directory);
            }
        });
        //ok tlačítko změní boolean ok na true, což zachytí thread ve třídě App
        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                documentName = docName.getText();
                authorName = author.getText();
                frame.setVisible(false);
                ok=true;
            }
        });
    }

    //inicializace
    public void init(){
        frame = new NewDocument();
        frame.setTitle("Nový dokument");
        docName.setText("");
        author.setText("");
        docName.setEditable(true);
        author.setEditable(true);
        version = "1.0";
        pickContentButton.setText("Vyberte obsah");
        newContent = null;
        frame.pack();
        frame.setContentPane(this.panel);
        frame.setBounds(100, 100, 300, 200);
        frame.setSize(300,250);
    }

    //inicializace pokud se jedná o novou verzi
    public void initVersion(String name, String auth,String newVersion){
        frame = new NewDocument();
        version = newVersion;
        docName.setText(name);
        author.setText(auth);
        docName.setEditable(false);
        author.setEditable(false);
        newContent = null;
        pickContentButton.setText("Vyberte obsah nové verze");
        frame.pack();
        frame.setContentPane(this.panel);
        frame.setBounds(100, 100, 300, 200);
        frame.setSize(300,250);
        frame.setVisible(true);
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
    public String getVersion() {
        return version;
    }

}
