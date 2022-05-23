package vut.fekt.archive;

import vut.fekt.archive.blockchain.Crypto;
import vut.fekt.archive.blockchain.CryptoException;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;

//aplikace Decrpytor
public class Decryptor extends JDialog {
    private JPanel contentPane;
    private JButton loadFileButton;
    private JButton decryptButton;
    private JTextField fileField;
    private JTextField passField;
    private JLabel status;

    private File inputFile;
    private String password;


    public Decryptor() {
        setContentPane(contentPane);
        setModal(true);
        //tlačítko načíst soubor
        loadFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                //fc.setCurrentDirectory(new File("D:/Archiv/"));
                int r = fc.showOpenDialog(contentPane);
                if (r == JFileChooser.APPROVE_OPTION) {
                    inputFile = fc.getSelectedFile();
                    fileField.setText(inputFile.getAbsolutePath());
                    status.setText("Soubor načten");
                }
            }
        });
        //tlačítko dešifrovat
        decryptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(passField.getText().isEmpty()||fileField.getText().isEmpty()){
                    status.setText("Chybí některý z údajů");
                }
                else {
                    File folder = inputFile.getParentFile();
                    File decrypt = new File(folder.getAbsolutePath()+"/Decrypted-"+inputFile.getName());
                    try {
                        Crypto.decrypt(passField.getText(),inputFile,decrypt );
                        status.setText("Soubor dešifrován!");
                        decrypt=null;
                    } catch (CryptoException | NoSuchAlgorithmException ex) {
                        status.setText("Špatné heslo!");
                        decrypt.delete();
                        //ex.printStackTrace();
                    }

                }
            }
        });
    }

    public static void main(String[] args) {
        Decryptor dialog = new Decryptor();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
