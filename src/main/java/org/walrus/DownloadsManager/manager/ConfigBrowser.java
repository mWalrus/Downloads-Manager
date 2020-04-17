package org.walrus.DownloadsManager.manager;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
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
        try {
            Object[] choices = {"Chrome", "Firefox"};
            String input = (String) JOptionPane.showInputDialog(null,
                    "Select your browser",
                    "Downloads Manager",
                    JOptionPane.QUESTION_MESSAGE,
                    new ImageIcon(new URL(Sorter.class.getResource("/logo.png").toString())),
                    choices,
                    choices[0]);
            setBrowser(input);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }
}
