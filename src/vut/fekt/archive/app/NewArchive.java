package vut.fekt.archive.app;

import vut.fekt.archive.Archive;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class NewArchive extends JFrame {
    private JTextField archiveName;
    private JButton pickDirectoryButton;
    private JLabel directoryLabel;
    public JButton OKButton;
    public JPanel panel;
    public NewArchive frame;

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
    public boolean ok = false;

    public NewArchive() {
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
        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ok = true;
                frame.setVisible(false);
            }
        });


    }

    public void init(){
        frame = new NewArchive();
        frame.pack();

        frame.setContentPane(this.panel);
        frame.setBounds(100, 100, 300, 200);
        frame.setSize(300,200);
    }

    public void pressOk(){


    }

    public String setAndGetDirectory(){

        return directory;
    }
}
