package org.walrus.DownloadsManager.manager;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

public class ConfigBrowser {
    File browserFile = new File("./browser/user-browser.json");

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
            b.addActionListener(e -> {
                setBrowser(e.getActionCommand());
                JOptionPane.getRootFrame().dispose();
            });
        }

        JLabel label = new JLabel("Select your browser:");
        label.setBackground(Color.decode("#2c2c34"));
        label.setForeground(Color.decode("#fff4ff"));
        label.setHorizontalAlignment(JLabel.CENTER);

        try {
            JOptionPane.showOptionDialog(null,
                    label,
                    "Downloads Manager",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    new ImageIcon(new URL(Sorter.class.getResource("/logo.png").toString())),
                    buttons,
                    buttons[0]
            );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
