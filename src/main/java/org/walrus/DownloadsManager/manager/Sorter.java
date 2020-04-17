package org.walrus.DownloadsManager.manager;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.swing.*;

public class Sorter implements IntSorter {

    String browser;

    public Sorter(){
        ConfigBrowser db = new ConfigBrowser();
        this.browser = db.getBrowser();
    }

    /**
     * Listens for user initiated downloads by checking if the Downloads folder is getting modified at any given moment
     * @throws IOException
     */
    public void listenForDownloads() throws IOException {
        this.logToFile("info", "Listening for downloads...");
        try {
            WatchService ws = FileSystems.getDefault().newWatchService();
            Path downloads = Paths.get(downloadsPath);
            downloads.register(ws, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
            while (true) {
                boolean crDownloadDeleted = false;
                WatchKey wk = ws.take();

                for (WatchEvent<?> event : wk.pollEvents()) {
                    ArrayList<String> info = this.getKindAndFileInfo(event);
                    String kind = info.get(0), fileName = info.get(1), ext = info.get(2);
                    if (this.browser.equals("Chrome")) {
                        crDownloadDeleted = this.handleChromeDownloads(kind, fileName, ext, crDownloadDeleted);
                    }
                    else if (this.browser.equals("Firefox")) this.handleFirefoxDownloads(kind, fileName, ext);
                }

                wk.reset();
            }
        } catch (InterruptedException | ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the file categorization process
     * @param fileName name of the file (including extension)
     * @param wasDownloadedNow true if file was downloaded just before (exists to decide whether to open the file after categorization)
     * @throws IOException
     * @throws ParseException
     * @throws InterruptedException
     */
    public void checkFile(String fileName, boolean wasDownloadedNow) throws IOException, ParseException, InterruptedException {
        logToFile("info", "Checking if entry is folder...");
        File newFile = new File(downloadsPath + "\\" + fileName);
        if (!newFile.isDirectory()) {
            this.logToFile("info", "Found file is not a folder. Determining category for " + fileName);
            String[] splitTemp = fileName.split("\\.");
            String ext = splitTemp[splitTemp.length - 1].toLowerCase();
            String category = this.findCategory(ext);
            Path temp = Paths.get(downloadsPath + "\\" + category);
            if (!Files.exists(temp)) {
                this.logToFile("info", "Couldn't find folder \"" + category + "\". Trying to create it...");
                this.createCategoryDirectory(category);
            }

            this.sortFileToFolder(category, fileName, wasDownloadedNow);
        }

    }

    /**
     * Moves file to determined folder after categorization
     * @param folderToMoveTo name of category folder
     * @param fileName name of file being handled
     * @param wasDownloadedNow true if file was downloaded just before this process
     * @throws IOException
     * @throws InterruptedException
     */
    public void sortFileToFolder(String folderToMoveTo, String fileName, boolean wasDownloadedNow) throws IOException, InterruptedException {
        this.logToFile("info", "Moving file to \"" + folderToMoveTo + "\" folder.");

        try {
            Files.move(Paths.get(downloadsPath + "\\" + fileName), Paths.get(downloadsPath + "\\" + folderToMoveTo + "\\" + fileName), StandardCopyOption.REPLACE_EXISTING);
            this.logToFile("info", "Moved file successfully.");
        } catch (IOException e) {
            this.logToFile("error", e.toString());
        }

        if (wasDownloadedNow) {
            TimeUnit.SECONDS.sleep(1);
            String categoryPath = downloadsPath + "\\" + folderToMoveTo;
            this.displayDialog(categoryPath, fileName);
        }

    }

    /**
     * Iterates through predefined categories to find the category the file's extension falls under.
     * If no category is found the file gets sorted in "Other" category.
     * @param extension current file's extension
     * @return String category
     * @throws IOException
     * @throws ParseException
     */
    public String findCategory(String extension) throws IOException, ParseException {
        boolean foundCategory = false;
        String category = "";
        JSONObject categories = getCategories();
        for (Object currentCategory : categories.keySet()) {
            String exts = categories.get(currentCategory).toString();
            exts = exts.replace("[", "").replace("]", "");
            String[] split = exts.split(",");
            for (String ext : split) {
                ext = ext.replace("\"", "");
                if (extension.equals(ext)) {
                    category = currentCategory.toString();
                    foundCategory = true;
                    logToFile("info", "Found category \"" + category + "\" for file with ." + extension + " extension.");
                    break;
                }
            }
            if (foundCategory) break;
        }
        if (!foundCategory) {
            category = "other";
            logToFile("info", "Couldn't find category for ." + extension +", sorting in \"Other\"");
        }
        return category;
    }

    /**
     * Returns the predefined category objects containing extensions to compare current file's extension to
     * @return JSONObject categories
     * @throws IOException
     * @throws ParseException
     */
    public JSONObject getCategories() throws IOException, ParseException {
        InputStream input = Sorter.class.getResourceAsStream("/categories.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        String jsonString = builder.toString();
        JSONObject object = (JSONObject) new JSONParser().parse(jsonString);
        return (JSONObject) object.get("categories");
    }

    /**
     * If the category folder is nonexistent a new folder is created with it's name
     * @param dirName Name of the directory which will be created
     * @throws IOException
     */
    public void createCategoryDirectory(String dirName) throws IOException {
        try {
            String capDirName = dirName.substring(0, 1).toUpperCase() + dirName.substring(1);
            File newDir = new File(downloadsPath + "\\" + capDirName);
            boolean created = newDir.mkdir();
            if (!created) {
                throw new IOException();
            }

            this.logToFile("info", "Created folder successfully.");
        } catch (IOException e) {
            this.logToFile("error", e.toString());
        }

    }

    /**
     * Logs events to text file
     * @param type type of message
     * @param message message to be logged
     * @throws IOException
     */
    @SuppressWarnings("all")
    public void logToFile(String type, String message) throws IOException {
        File logs = new File(logsPath);
        logs.getParentFile().mkdirs();
        logs.createNewFile();
        LocalDateTime dt = LocalDateTime.now();

        String entry = dtf.format(dt) + " [" + type.toUpperCase() + "] " + message;

        BufferedWriter writer = new BufferedWriter(new FileWriter(logsPath, true));
        writer.write(entry);
        writer.newLine();
        writer.close();
    }

    /**
     * Opens a specific path with it's standard application
     * @param path path to open
     * @throws IOException
     */
    public void openPath(String path) throws IOException {
        Desktop.getDesktop().open(new File(path));
    }

    /**
     * Scans downloads folder for unsorted files
     * @throws IOException
     * @throws ParseException
     * @throws InterruptedException
     */
    public void scanDownloadsFolder() throws IOException, ParseException, InterruptedException {
        this.logToFile("info", "Scanning Downloads folder for unhandled files...");
        File downloadsFolder = new File(downloadsPath);
        File[] files = Objects.requireNonNull(downloadsFolder.listFiles());

        for (File currentFile : files) {
            if (!currentFile.isDirectory()) {
                this.checkFile(currentFile.getName(), false);
            }
        }

        this.logToFile("info", "Done!");
    }

    /**
     * Creates a window with three options
     * (open folder of newly downloaded file, open that file with standard application or close window)
     * @param folderPath folder to open
     * @param fileName file to open
     * @throws IOException
     */
    @Override
    public void displayDialog(String folderPath, String fileName) throws IOException {
        JButton openFolder = new JButton("Open folder");
        JButton openFile = new JButton("Open file");
        JButton close = new JButton("Close");
        JButton[] options = {
          openFolder,
                openFile,
                close

        };

        for (JButton b : options) {
            b.setBackground(Color.decode("#6f2dbd"));
            b.setForeground(Color.decode("#fffffa"));
            b.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        }

        openFolder.addActionListener(e -> {
            try {
                this.openPath(folderPath);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        openFile.addActionListener(e -> {
            try {
                this.openPath(folderPath + "\\" + fileName);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        close.addActionListener(e -> JOptionPane.getRootFrame().dispose());

        JLabel label = new JLabel(fileName + " was downloaded. Select action:");
        label.setForeground(Color.decode("#fffaff"));

        JOptionPane.showOptionDialog(null,
                label,
                "Downloads Manager",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                new ImageIcon(new URL(Sorter.class.getResource("/logo.png").toString())),
                options,
                options[0]);
    }

    private boolean handleChromeDownloads (String kind, String fileName, String ext, boolean crDownloadDeleted) throws IOException, ParseException, InterruptedException {
        boolean cdl = crDownloadDeleted;
        if (ext.equals("tmp") && kind.equals("ENTRY_CREATE")) {
            this.logToFile("info", "A new download has been detected, waiting for it to finish...");
        }

        if (ext.equals("crdownload") && kind.equals("ENTRY_DELETE")) {
            this.logToFile("info", "Download finished!");
            cdl = true;
        }

        if (crDownloadDeleted && !ext.equals("tmp") && !ext.equals("crdownload")) {
            this.checkFile(fileName, true);
            cdl = false;
        }
        return cdl;
    }

    private void handleFirefoxDownloads(String kind, String fileName, String ext) {
        System.out.println("kind: " + kind + "\nfile name: " + fileName);
    }

    ArrayList<String> getKindAndFileInfo (WatchEvent<?> event) {
        ArrayList<String> collection = new ArrayList<>();
        String kind = event.kind().toString();
        String fileName = event.context().toString();
        String[] fileNameSplit = fileName.split("\\.");
        String ext = fileNameSplit[fileNameSplit.length - 1];
        collection.add(kind);
        collection.add(fileName);
        collection.add(ext);
        return collection;
    }

    void changeBrowser () {
        ConfigBrowser db = new ConfigBrowser();
        db.promptForBrowser();
        this.browser = db.getBrowser();
    }
}
