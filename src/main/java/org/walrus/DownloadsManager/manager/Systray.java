package org.walrus.DownloadsManager.manager;

import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.walrus.DownloadsManager.manager.IntSorter.*;

public class Systray {
    private Sorter s;
    SystemTray tray;
    TrayIcon trayIcon;

    private final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    Systray (Sorter sorter) throws IOException {

        EventLogger myLogger = new EventLogger();
        myLogger.setup();
        LOGGER.setLevel(Level.INFO);

        s = sorter;
        initTrayPopup();
    }

    /**
     * Initializes the System Tray Icon and attaches a menu to it
     * @throws IOException
     */
    @SuppressWarnings("all")
    public void initTrayPopup() throws IOException {
        LOGGER.info("Checking if System Tray is supported...");
        if (SystemTray.isSupported()) {
            LOGGER.info("System Tray is supported");
            LOGGER.info("Initializing Popup menu...");
            PopupMenu popup = new PopupMenu();
            trayIcon = new TrayIcon(this.createImage("/logo.png", "Tray Icon"), "Downloads manager");
            tray = SystemTray.getSystemTray();
            LOGGER.info("Popup created");

            LOGGER.info("Adding items to the popup menu");
            // sub menu for folders in downloads
            Menu folders = new Menu("Open");
            MenuItem dlFolder = new MenuItem("Downloads");
            folders.add(dlFolder);
            for (String category : this.getExistingCategories()) {
                MenuItem cat = new MenuItem(category);
                folders.add(cat);
            }

            MenuItem logs = new MenuItem("Logs");
            MenuItem info = new MenuItem("Info");
            MenuItem reload = new MenuItem("Reload");
            MenuItem exit = new MenuItem("Exit");
            popup.add("v1.6-SNAPSHOT");
            popup.add(folders);
            popup.add(logs);
            popup.add(info);
            popup.add(reload);
            popup.add(exit);
            LOGGER.info("Done, attaching popup to Tray Icon");
            trayIcon.setPopupMenu(popup);
            LOGGER.info("Done");

            try {
                LOGGER.info("Attaching Tray Icon to System Tray");
                tray.add(trayIcon);
            } catch (AWTException e) {
                LOGGER.severe(e.toString());
            }

            folders.addActionListener((e) -> {
                String pathToOpen = e.getActionCommand() == "Downloads" ? downloadsPath : downloadsPath + "\\" + e.getActionCommand();
                try {
                    s.openPath(pathToOpen);
                } catch (IOException ex) {
                    LOGGER.severe(ex.toString());
                }
            });

            logs.addActionListener((e) -> {
                try {
                    s.openPath(logsPath);
                } catch (IOException ex) {
                    LOGGER.severe(ex.toString());
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
                    LOGGER.severe(ex.toString());
                }
            });

            reload.addActionListener((e) -> {
                LOGGER.info("Rescanning Downloads folder");
                try {
                    s.scanDownloadsFolder();
                } catch (IOException | ParseException | InterruptedException err) {
                    LOGGER.severe(err.toString());
                }

            });

            exit.addActionListener((e) -> {
                LOGGER.info("Shutting down");
                tray.remove(trayIcon);
                System.exit(0);
            });
        } else {
            // systray no supported
        }
    }

    /**
     * Creates the Icon for the System Tray application
     * @param path path to image
     * @param desc image description
     * @return
     * @throws IOException
     */
    public Image createImage(String path, String desc) throws IOException {
        LOGGER.info("Creating Tray Icon");
        URL imageURL = new URL(Systray.class.getResource(path).toString());
        return (new ImageIcon(imageURL, desc)).getImage();
    }

    /**
     * Returns a list of all existing categories inside the Downloads folder
     * @return
     */
    @SuppressWarnings("all")
    public ArrayList<String> getExistingCategories () {
        LOGGER.info("Getting current categories in Downloads");
        ArrayList<String> foundCategories = new ArrayList<>();
        for (File file : new File(downloadsPath).listFiles()) {
            if (file.isDirectory()) foundCategories.add(file.getName());
        }
        return foundCategories;
    }

    public void reloadTrayMenu () {
        LOGGER.info("Reloading Tray App");
        tray.remove(trayIcon);
        try {
            initTrayPopup();
        } catch (IOException e) {
            LOGGER.severe(e.toString());
        }
    }
}
