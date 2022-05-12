package vut.fekt.archive.app;

import vut.fekt.archive.ArchiveDocument;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;


public class NewUser extends JFrame{
    private JPanel panel;
    private JTextField usernameField;
    private JButton OKButton;
    private JTextField passwordField;
    private JLabel path;
    public JRadioButton admin;
    private JButton cancelButton;
    private String directory;
    private File newContent;
    private ArchiveDocument archdoc;
    private NewUser frame;
    private String username;
    private String password;
    private String version;

    boolean ok = false;

    //okno vytváření nového dokumentu
    public NewUser() {
        //výběr souboru obsahu

        //ok tlačítko změní boolean ok na true, což zachytí thread ve třídě App
        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                username = usernameField.getText();
                password = passwordField.getText();
                frame.setVisible(false);
                ok=true;
            }
        });
    }

    //inicializace
    public void init(){
        frame = new NewUser();
        frame.setTitle("Nový dokument");
        usernameField.setText("");
        passwordField.setText("");
        usernameField.setEditable(true);
        passwordField.setEditable(true);
        version = "1.0";
        newContent = null;
        frame.pack();
        frame.setContentPane(this.panel);
        frame.setBounds(100, 100, 300, 200);
        frame.setSize(300,250);
    }

    public NewUser getFrame() {
        return frame;
    }

    public File getNewContent() {
        return newContent;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
    public String getVersion() {
        return version;
    }

}