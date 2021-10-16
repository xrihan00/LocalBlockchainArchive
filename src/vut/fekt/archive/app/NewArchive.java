package vut.fekt.archive.app;

import vut.fekt.archive.Archive;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class NewArchive {
    private JTextField archiveName;
    private JButton pickDirectoryButton;
    private JLabel directoryLabel;
    public JButton OKButton;
    public JPanel panel;

    public JButton getOKButton() {
        return OKButton;
    }

    public String getName() {
        return name;
    }

    public String getDirectory() {
        return directory;
    }

    public String name;
    public String directory;

    public NewArchive() {


    }

    public void pressOk(){
        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

    }

    public String setAndGetDirectory(){
        pickDirectoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                int r = fc.showOpenDialog(panel);
                if (r == JFileChooser.APPROVE_OPTION) {
                    directory = fc.getSelectedFile().getAbsolutePath() + "/" + archiveName.getText();
                    directoryLabel.setText(directory);
                    name = archiveName.getText();
                }
            }
        });
        return directory;
    }
}
