package vut.fekt.archive.app;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import vut.fekt.archive.ArchiveDocument;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class ShowDocument extends JFrame{
    private JTextField nazevField;
    private JTextField autorField;
    private JTextField timeField;
    private JButton openButton;
    private JTextField obsahField;
    private JTextField idField;
    private JPanel panel;
    private JButton potvrditDokumentButton;
    private JButton odstranitDokumentButton;
    private JLabel encrypt;
    private JTextField encryptField;
    private String newVersionCount;
    public ShowDocument frame;

    public String doc;
    public boolean newVersion = false;
    public String result = "";

    public ShowDocument(){
        potvrditDokumentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = "confirmed";
            }
        });
        odstranitDokumentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = "rejected";
            }
        });
    }

    public void init(String url, String newDoc) throws IOException {
        frame = new ShowDocument();
        frame.setTitle("Nový dokument");

        parseDoc(url,newDoc);

        frame.pack();
        frame.setContentPane(this.panel);
        frame.setBounds(100, 100, 300, 200);
        frame.setSize(500,300);
    }

    public void parseDoc(String url, String doc) throws IOException {
        String metaPath = "http://"+url+"/"+doc+"/metadata.json";
        FileInputStream fis =new FileInputStream(metaPath);
        BufferedInputStream bis = new BufferedInputStream(fis);
        String jsonFile = IOUtils.toString(bis, "UTF-8");
        JSONObject json = new JSONObject(jsonFile);
        nazevField.setText(doc);
        autorField.setText(json.getString("author"));
        timeField.setText(json.getString("date"));
        encryptField.setText(json.getString("encrypted"));
        autorField.setText(json.getString("author"));
    }

    public ShowDocument getFrame() {
        return frame;
    }
}
