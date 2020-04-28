package org.walrus.DownloadsManager.manager;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.walrus.DownloadsManager.manager.EventLogger;

import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

public class Sorter implements IntSorter {
    File currentFile;
    boolean foundFile;
    Systray systray;

    private final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    Sorter () {
        EventLogger myLogger = new EventLogger();
        myLogger.setup();
        LOGGER.setLevel(Level.INFO);
        LOGGER.info("Starting application");
        try {
            systray = new Systray(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Listens for user initiated downloads by checking if the Downloads folder is getting modified at any given moment
     * @throws IOException
     */
    public void listenForDownloads() throws IOException {
        try {
            LOGGER.info("Listening for downloads...");
            WatchService ws = FileSystems.getDefault().newWatchService();
            Path downloads = Paths.get(downloadsPath);
            downloads.register(ws, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
            while (true) {
                WatchKey wk = ws.take();

                handleDownloads(wk.pollEvents());

                wk.reset();
            }
        } catch (InterruptedException | ParseException e) {
            LOGGER.severe(e.toString());
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
        LOGGER.info("Checking " + fileName);
        File newFile = new File(downloadsPath + "\\" + fileName);
        if (!newFile.isDirectory()) {
            LOGGER.info("Entry is not a directory, determining file type...");
            String[] splitTemp = fileName.split("\\.");
            String ext = splitTemp[splitTemp.length - 1].toLowerCase();
            String category = this.findCategory(ext);
            Path temp = Paths.get(downloadsPath + "\\" + category);
            if (!Files.exists(temp)) {
                this.createCategoryDirectory(category);
            }

            // when a website is downloaded (from at least firefox) a folder with the site's style is also downloaded
            // this part handles that folder on a new download
            if (ext.equals("htm") || ext.equals("html")) {
                LOGGER.info("File downloaded is a web page, looking for resource folder...");
                String resourceName = this.checkForResourceFolder(fileName, ext);
                if (resourceName != null) {
                    this.sortFileToFolder(category, resourceName, false);
                } else {
                    LOGGER.info("Did not find any resources");
                }
            }

            this.sortFileToFolder(category, fileName, wasDownloadedNow);
        } else {
            LOGGER.info("Entry is a folder, checking if it is an unhandled resource folder.");
            // this section handles left over _files folders in downloads and sorts them into the web category
            if (fileName.matches("(.*)_files$")) {
                LOGGER.info("File is a resource folder, sorting");
                System.out.println(fileName);
                this.sortFileToFolder("web", fileName, wasDownloadedNow);
            } else {
                LOGGER.info("File is not a resource folder, skipping.");
            }
        }

    }

    /**
     * Checks if the newly downloaded website came with a _files folder, if so the name of the folder is returned
     * @param fileName name of the web file downloaded
     * @param ext extension used for removing it from the file name
     * @return resource folder name
     */
    @SuppressWarnings("all")
    private String checkForResourceFolder(String fileName, String ext) {
        fileName = fileName.replace("." + ext, "");
        for (File file : new File(downloadsPath).listFiles()) {
            if (file.isDirectory() && file.getName().equals(fileName + "_files")){
                LOGGER.info("Found " + file.getName());
                return file.getName();
            }
        }
        return null;
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
        LOGGER.info("Sorting " + fileName + " to \"" + folderToMoveTo + "\"");
        try {
            Files.move(Paths.get(downloadsPath + "\\" + fileName), Paths.get(downloadsPath + "\\" + folderToMoveTo + "\\" + fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.severe(e.toString());
        }

        if (wasDownloadedNow) {
            TimeUnit.SECONDS.sleep(1);
            String categoryPath = downloadsPath + "\\" + folderToMoveTo;
            this.displayDialog(categoryPath, fileName);
            foundFile = false;
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
        LOGGER.info("Beginning categorization");
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
                    LOGGER.info("Found category " + category + " for " + extension + " extension");
                    break;
                }
            }
            if (foundCategory) break;
        }
        if (!foundCategory) {
            LOGGER.info("Could not find a category for " + extension + " extension, sorting in Other");
            category = "other";
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
        LOGGER.info("Getting categories...");
        InputStream input = Sorter.class.getResourceAsStream("/categories.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        String jsonString = builder.toString();
        JSONObject object = (JSONObject) new JSONParser().parse(jsonString);
        LOGGER.info("Done getting categories");
        return (JSONObject) object.get("categories");
    }

    /**
     * If the category folder is nonexistent a new folder is created with it's name
     * @param dirName Name of the directory which will be created
     */
    public void createCategoryDirectory (String dirName) {
        LOGGER.info("Trying to create category folder...");
        try {
            String capDirName = dirName.substring(0, 1).toUpperCase() + dirName.substring(1);
            File newDir = new File(downloadsPath + "\\" + capDirName);
            boolean created = newDir.mkdir();
            if (!created) {
                throw new IOException();
            } else {
                LOGGER.info("Folder successfully created");
            }
            systray.reloadTrayMenu();
        } catch (IOException e) {
            LOGGER.severe(e.toString());
        }

    }

    /**
     * Opens a specific path with it's standard application
     * @param path path to open
     * @throws IOException
     */
    public void openPath(String path) throws IOException {
        LOGGER.info("Opening " + path);
        Desktop.getDesktop().open(new File(path));
    }

    /**
     * Scans downloads folder for unsorted files
     * @throws IOException
     * @throws ParseException
     * @throws InterruptedException
     */
    public void scanDownloadsFolder() throws IOException, ParseException, InterruptedException {
        LOGGER.info("Scanning Downloads folder");
        File downloadsFolder = new File(downloadsPath);
        File[] files = Objects.requireNonNull(downloadsFolder.listFiles());

        for (File currentFile : files) {
            LOGGER.info("Found file " + currentFile.getName());
            this.checkFile(currentFile.getName(), false);
        }
    }

    /**
     * Creates a window with three options
     * (open folder of newly downloaded file, open that file with standard application or close window)
     * @param folderPath folder to open
     * @param fileName file to open
     */
    @Override
    public void displayDialog (String folderPath, String fileName) {
        LOGGER.info("Preparing prompt UI");
        UI frame = new UI();

        LOGGER.info("Generating options");
        JButton openFolder = new JButton("Open folder");
        JButton openFile = new JButton("Open file");
        JButton close = new JButton("Close");
        JButton[] options = {
          openFolder,
                openFile,
                close

        };

        for (JButton b : options) {
            b.setBackground(Color.decode("#8367c7"));
            b.setForeground(Color.decode("#fffffa"));
            b.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            b.setSize(new Dimension(80, 20));
        }

        LOGGER.info("Adding action listeners");
        openFolder.addActionListener(e -> {
            try {
                this.openPath(folderPath);
            } catch (IOException ex) {
                LOGGER.severe(ex.toString());
            }
        });
        openFile.addActionListener(e -> {
            try {
                this.openPath(folderPath + "\\" + fileName);
            } catch (IOException ex) {
                LOGGER.severe(ex.toString());
            }
        });
        close.addActionListener(e -> frame.dispose());

        LOGGER.info("Creating label");
        JLabel label = new JLabel("<html>" + fileName + " was downloaded. <br>Select action:<html>", SwingConstants.CENTER);
        label.setForeground(Color.decode("#fffaff"));
        label.setBackground(Color.decode("#2c2c34"));

        LOGGER.info("Setting button borders");
        JPanel textPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new GridLayout(1,3,40,0));
        JPanel outerPanel = new JPanel(new BorderLayout());

        LOGGER.info("Creating UI");
        frame.createUI(label, options, outerPanel, textPanel, buttonPanel, 500, 170);
    }

    /**
     * Handles incoming downloads by finding the file being downloaded and preparing for file size comparison
     * @param events watch key events polled in downloads folder
     * @throws InterruptedException
     * @throws ParseException
     * @throws IOException
     */
    private void handleDownloads (List<WatchEvent<?>> events) throws InterruptedException, ParseException, IOException {
        String fileName = "";
        for (WatchEvent<?> event : events){
            String[] split = event.context().toString().split("\\.");
            String temp = split[split.length - 1];
            if (!temp.equals("tmp") && !temp.equals("crdownload") && !temp.equals("part")) {
                fileName = event.context().toString();
                LOGGER.info("Found file " + fileName);
            }
        }
        if (fileName.length() > 0 && !foundFile) {
            File temp = new File(downloadsPath + "\\" + fileName);
            if (!temp.equals(currentFile)) {
                currentFile = new File(downloadsPath + "\\" + fileName);
                foundFile = true;
            }
        }
        checkFileSize(fileName);
    }

    /**
     * checks file size, waits for .5 seconds and tries again, if the numbers are the same and also larger than 0
     * the file is finished downloaded
     * @param fileName String file name
     * @throws InterruptedException
     * @throws IOException
     * @throws ParseException
     */
    private void checkFileSize (String fileName) throws InterruptedException, IOException, ParseException {
        if (foundFile) {
            long size1 = currentFile.length();
            LOGGER.info("File size before: " + size1);
            Thread.sleep(1500);
            long size2 = currentFile.length();
            LOGGER.info("File size before: " + size2);
            if (size1 == size2 && size1 != 0) {
                LOGGER.info("File size is the same, assuming " + fileName + " is done downloading");
                Thread.sleep(500);
                checkFile(fileName, true);
            }
        }
    }
}
