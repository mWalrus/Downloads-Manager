package org.walrus.DownloadsManager.manager;

import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class SorterMain extends Sorter {
    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        UIManager.put("OptionPane.background", Color.decode("#2c2c34"));
        UIManager.put("Panel.background", Color.decode("#2c2c34"));
        Sorter s = new Sorter();
        s.scanDownloadsFolder();
        s.listenForDownloads();
    }
}