package vut.fekt.archive;

import vut.fekt.archive.blockchain.Crypto;
import vut.fekt.archive.blockchain.CryptoException;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;

public class Decryptor extends JDialog {
    private JPanel contentPane;
    private JButton loadFileButton;
    private JButton decryptButton;
    private JTextField fileField;
    private JTextField passField;
    private JLabel status;
    private JTextField docnameField;

    private File inputFile;
    private String password;

    public Decryptor() {
        setContentPane(contentPane);
        setModal(true);

        loadFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                //fc.setCurrentDirectory(new File("D:/Archiv/"));
                int r = fc.showOpenDialog(contentPane);
                if (r == JFileChooser.APPROVE_OPTION) {
                    inputFile = fc.getSelectedFile();
                    fileField.setText(inputFile.getAbsolutePath());
                    status.setText("Soubor načten");
                }
            }
        });
        decryptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(passField.getText().isEmpty()||fileField.getText().isEmpty()||docnameField.getText().isEmpty()){
                    status.setText("Chybí některý z údajů");
                }
                else {
                    File folder = inputFile.getParentFile();
                    try {
                        Crypto.decrypt(passField.getText(),inputFile, new File(folder.getAbsolutePath()+"/Decrypted-"+inputFile.getName()),docnameField.getText());
                    } catch (CryptoException ex) {
                        status.setText("Špatné heslo!");
                        //ex.printStackTrace();
                    }
                    status.setText("Soubor dešifrován!");
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
