package org.walrus.DownloadsManager.manager;

import org.json.simple.parser.ParseException;

import java.io.IOException;

public class SorterMain extends Sorter {
    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        Sorter s = new Sorter();
        new Systray(s);
        s.scanDownloadsFolder();
        s.listenForDownloads();
    }
}