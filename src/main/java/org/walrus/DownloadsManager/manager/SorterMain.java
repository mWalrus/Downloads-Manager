package org.walrus.DownloadsManager.manager;

import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;

public class SorterMain extends Sorter {
    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        Sorter s = new Sorter();
        new Systray(s);
        s.scanDownloadsFolder();
        listenForDownloads(s);
    }

    /**
     * Listens for user initiated downloads by checking if the Downloads folder is getting modified at any given moment
     * @param s Sorter class
     * @throws IOException
     */
    static void listenForDownloads(Sorter s) throws IOException {
        s.logToFile("info", "Listening for downloads...");
        try {
            WatchService ws = FileSystems.getDefault().newWatchService();
            Path downloads = Paths.get(downloadsPath);
            downloads.register(ws, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_CREATE);
            boolean crDownloadDeleted = false;

            while(true) {
                WatchKey wk = ws.take();

                for (WatchEvent<?> event : wk.pollEvents()) {
                    Kind<?> kind = event.kind();
                    String fileName = event.context().toString();
                    String[] fileNameSplit = fileName.split("\\.");
                    String ext = fileNameSplit[fileNameSplit.length - 1];
                    if (ext.equals("tmp") && StandardWatchEventKinds.ENTRY_CREATE.equals(kind)) {
                        s.logToFile("info", "A new download has been detected, waiting for it to finish...");
                    }

                    if (ext.equals("crdownload") && StandardWatchEventKinds.ENTRY_DELETE.equals(kind)) {
                        s.logToFile("info", "Download finished!");
                        crDownloadDeleted = true;
                    }

                    if (crDownloadDeleted && !ext.equals("tmp") && !ext.equals("crdownload")) {
                        s.checkFile(fileName, true);
                        crDownloadDeleted = false;
                    }
                }

                wk.reset();
            }
        } catch (InterruptedException | IOException | ParseException e) {
            s.logToFile("error", e.toString());
        }
    }
}