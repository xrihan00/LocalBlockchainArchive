package vut.fekt.archive.app;

import vut.fekt.archive.Archive;

import javax.swing.*;

import javax.swing.JFileChooser;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class MainApp {
    private JPanel panel1;
    private JButton newArchiveButton;
    private JButton loadArchiveButton;
    private JLabel FileLabel;
    private Archive archive;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Local Archive");
        frame.pack();
        frame.setVisible(true);
        frame.setContentPane(new MainApp().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(100, 100, 500, 500);
        frame.setSize(500,500);


    }

    public MainApp() {
        newArchiveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    archive = newArchiveFrame();
                    FileLabel.setText(archive.getName());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        loadArchiveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                int r = fc.showOpenDialog(panel1);
                if (r == JFileChooser.APPROVE_OPTION)
                {
                    // set the label to the path of the selected file

                    try {
                        archive = new Archive(fc.getSelectedFile().getName(),fc.getSelectedFile().getAbsolutePath());
                        FileLabel.setText("Archive " + archive.getName() + " loaded!");
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
                // if the user cancelled the operation
                else{
                    FileLabel.setText("the user cancelled the operation");}

            }

        });
    }

    public static Archive newArchiveFrame() throws IOException {
        JFrame frame = new JFrame("New Archive");
        frame.pack();
        frame.setVisible(true);
        NewArchive na = new NewArchive();
        frame.setContentPane(na.panel);
        frame.setBounds(100, 100, 300, 200);
        frame.setSize(300,200);

        String dir = na.setAndGetDirectory();
        //String name;
        //String dir;

        Archive arch = new Archive(na.getName(),dir);
      //  frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        return arch;

    }
}
