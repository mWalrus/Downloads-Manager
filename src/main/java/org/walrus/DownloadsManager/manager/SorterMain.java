package org.walrus.DownloadsManager.manager;

import org.json.simple.parser.ParseException;

import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.UIManager;

import java.io.IOException;

public class SorterMain extends Sorter {
    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        stylePrompts();
        Sorter s = new Sorter();
        new Systray(s);
        s.scanDownloadsFolder();
        s.listenForDownloads();
    }

    public static void stylePrompts () {
        UIManager.put("OptionPane.background", Color.decode("#303036"));
        UIManager.put("Panel.background", Color.decode("#303036"));
    }
}