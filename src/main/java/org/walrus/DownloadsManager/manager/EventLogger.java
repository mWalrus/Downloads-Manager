package org.walrus.DownloadsManager.manager;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static org.walrus.DownloadsManager.manager.IntSorter.logsPath;

public class EventLogger {
    private FileHandler logFile;

    public void setup () {
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

        logger.setLevel(Level.INFO);
        try {
            logFile = new FileHandler(logsPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SimpleFormatter formatter = new SimpleFormatter();
        logFile.setFormatter(formatter);
        logger.addHandler(logFile);
    }
}
