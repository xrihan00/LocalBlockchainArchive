package vut.fekt.archive.app;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import vut.fekt.archive.ArchiveDocument;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;

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

    public String docName;
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
        String metaPath = "http://"+url+"/archive/"+doc+"/metadata.json";
        URL path = new URL(metaPath);
        InputStream in = path.openStream();
        BufferedInputStream bis = new BufferedInputStream(in);
        String jsonFile = IOUtils.toString(bis, "UTF-8");
        JSONObject json = new JSONObject(jsonFile);
        nazevField.setText(doc);
        docName = doc;
        autorField.setText(json.getString("author"));
        timeField.setText(json.getString("added"));
        encryptField.setText(String.valueOf(json.getBoolean("encrypted")));
        autorField.setText(json.getString("author"));
        idField.setText(String.valueOf(json.getInt("id")));
        obsahField.setText(String.valueOf(json.get("files")));
    }

    public ShowDocument getFrame() {
        return frame;
    }
}
