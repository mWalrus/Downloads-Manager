package org.walrus.DownloadsManager.manager;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

public class ConfigBrowser {
    File browserFile = new File("./browser/user-browser.json");

    JFrame frame;

    ConfigBrowser() {
        if (!fileExists()) promptForBrowser();
    }

    public String getBrowser() {
        String browser = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(browserFile));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null){
                sb.append(line);
            }
            String JSONString = sb.toString();
            JSONObject object = (JSONObject) new JSONParser().parse(JSONString);
            browser = object.get("browser").toString();
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        return browser;
    }

    @SuppressWarnings("all")
    private void setBrowser(String browser) {
        try {
            if (!fileExists()) {
                browserFile.getParentFile().mkdirs();
                browserFile.createNewFile();
            }
            FileWriter fw = new FileWriter(browserFile);
            fw.write("{\"browser\":\"" + browser + "\"}");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean fileExists () {
        return browserFile.exists();
    }

    public void promptForBrowser() {
        JButton chrome = new JButton("Chrome");
        JButton fireFox = new JButton("Firefox");
        JButton[] buttons = {chrome, fireFox};

        for (JButton b : buttons) {
            b.setBackground(Color.decode("#8367c7"));
            b.setForeground(Color.decode("#fffffa"));
            b.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            b.setFocusPainted(false);
            b.addActionListener(e -> {
                setBrowser(e.getActionCommand());
                frame.dispose();
            });
        }

        JLabel label = new JLabel("Select your browser:", SwingConstants.CENTER);
        label.setBackground(Color.decode("#2c2c34"));
        label.setForeground(Color.decode("#fff4ff"));

        JPanel textPanel = new JPanel(new BorderLayout());
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        JPanel outerPanel = new JPanel(new BorderLayout());

        UI frame = new UI();
        frame.createUI(label, buttons, outerPanel, textPanel, btnPanel, 300, 150);
    }
}
