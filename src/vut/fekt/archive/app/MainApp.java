package vut.fekt.archive.app;

import javax.swing.*;

import javax.swing.JFileChooser;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainApp {
    private JPanel panel1;
    private JButton newArchiveButton;
    private JButton loadArchiveButton;
    private JLabel FileLabel;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Lets go volit");
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

            }
        });
        loadArchiveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                int r = fc.showOpenDialog(panel1);
                if (r == JFileChooser.APPROVE_OPTION)
                {
                    // set the label to the path of the selected file
                    FileLabel.setText(fc.getSelectedFile().getAbsolutePath());
                }
                // if the user cancelled the operation
                else
                    FileLabel.setText("the user cancelled the operation");
            }

        });
    }
}
