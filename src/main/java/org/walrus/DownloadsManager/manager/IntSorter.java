package org.walrus.DownloadsManager.manager;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.awt.Image;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public interface IntSorter {
    String downloadsPath = "C:\\Users\\" + System.getProperty("user.name") + "\\Downloads";
    String infoPath = "logs/info.txt";
    String logsPath = "logs/logs.txt";
    String information = "A small Systray program that monitors and sorts your Downloads folder both on startup " +
            "and whenever a new download is detected. ";
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd @ HH:mm:ss");

    void checkFile(String var1, boolean var2) throws IOException, ParseException, InterruptedException;

    void sortFileToFolder(String var1, String var2, boolean var3) throws IOException, InterruptedException;

    String findCategory(String var1) throws IOException, ParseException;

    JSONObject getCategories() throws IOException, ParseException;

    void createCategoryDirectory(String var1) throws IOException;

    void logToFile(String type, String message) throws IOException;

    void openPath(String filePath) throws IOException;

    void scanDownloadsFolder() throws IOException, ParseException, InterruptedException;

    void displayDialog(String folderPath, String fileName) throws IOException;
}