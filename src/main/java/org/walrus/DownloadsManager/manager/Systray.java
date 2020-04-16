package org.walrus.DownloadsManager.manager;

import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;

import static org.walrus.DownloadsManager.manager.IntSorter.*;

public class Systray {
    private Sorter s;
    Systray (Sorter sorter) throws IOException {
        s = sorter;
        initTrayPopup();
    }

    /**
     * Initializes the System Tray Icon and attaches a menu to it
     * @throws IOException
     */
    @SuppressWarnings("all")
    public void initTrayPopup() throws IOException {
        s.logToFile("info", "Initializing System Tray Icon.");
        if (SystemTray.isSupported()) {
            PopupMenu popup = new PopupMenu();
            TrayIcon trayIcon = new TrayIcon(this.createImage("/logo.png", "Tray Icon"), "Downloads manager");
            SystemTray tray = SystemTray.getSystemTray();

            // sub menu for folders in downloads
            Menu folders = new Menu("Open");
            MenuItem dlFolder = new MenuItem("Downloads");
            folders.add(dlFolder);
            for (String category : this.getExistingCategories()) {
                MenuItem cat = new MenuItem(category);
                folders.add(cat);
            }

            MenuItem changeBrowser = new MenuItem("Change Browser");
            MenuItem logs = new MenuItem("Logs");
            MenuItem info = new MenuItem("Info");
            MenuItem reload = new MenuItem("Reload");
            MenuItem exit = new MenuItem("Exit");
            popup.add(folders);
            popup.add(changeBrowser);
            popup.add(logs);
            popup.add(info);
            popup.add(reload);
            popup.add(exit);
            trayIcon.setPopupMenu(popup);

            try {
                tray.add(trayIcon);
            } catch (AWTException var9) {
                var9.printStackTrace();
            }

            folders.addActionListener((e) -> {
                String pathToOpen = e.getActionCommand() == "Downloads" ? downloadsPath : downloadsPath + "\\" + e.getActionCommand();
                try {
                    s.openPath(pathToOpen);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            changeBrowser.addActionListener((e) -> {
                s.changeBrowser();
            });

            logs.addActionListener((e) -> {
                try {
                    s.openPath(logsPath);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            info.addActionListener((e) -> {
                File infoFile = new File(infoPath);
                infoFile.getParentFile().mkdirs();
                try {
                    if (infoFile.createNewFile()) {
                        FileWriter fw = new FileWriter(infoFile);
                        fw.write(information);
                        fw.close();
                    }
                    s.openPath(infoPath);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            reload.addActionListener((e) -> {
                try {
                    s.logToFile("info", "Reloading...");
                    s.scanDownloadsFolder();
                } catch (IOException | ParseException | InterruptedException err) {
                    err.printStackTrace();
                }

            });

            exit.addActionListener((e) -> {
                try {
                    s.logToFile("info", "Stopping service.");
                } catch (IOException var5) {
                    var5.printStackTrace();
                }

                tray.remove(trayIcon);
                System.exit(0);
            });
        } else {
            s.logToFile("warn", "System Tray is not supported.");
        }
        s.logToFile("info", "Done!");
    }

    /**
     * Creates the Icon for the System Tray application
     * @param path path to image
     * @param desc image description
     * @return
     * @throws IOException
     */
    public Image createImage(String path, String desc) throws IOException {
        URL imageURL = new URL(Systray.class.getResource(path).toString());
        return (new ImageIcon(imageURL, desc)).getImage();
    }

    /**
     * Returns a list of all existing categories inside the Downloads folder
     * @return
     */
    @SuppressWarnings("all")
    public ArrayList<String> getExistingCategories () {
        ArrayList<String> foundCategories = new ArrayList<>();
        for (File file : new File(downloadsPath).listFiles()) {
            if (file.isDirectory()) foundCategories.add(file.getName());
        }
        return foundCategories;
    }
}
