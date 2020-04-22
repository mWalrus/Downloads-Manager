package org.walrus.DownloadsManager.manager;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;

public class UI {
    JFrame frame;
    public void createUI (JLabel lbl, JButton[] buttons, JPanel outPan, JPanel lblPan, JPanel btnPan, int width, int height) {
        lblPan.add(lbl);

        for (JButton b : buttons) {
            b.addActionListener(e -> frame.dispose());
            btnPan.add(b);
        }


        btnPan.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));

        outPan.add(lblPan, BorderLayout.NORTH);
        outPan.add(btnPan, BorderLayout.SOUTH);
        outPan.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        frame = new JFrame("Downloads Manager");
        frame.setLocationRelativeTo(null);
        try {
            frame.setIconImage(new ImageIcon(new URL(Sorter.class.getResource("/logo.png").toString())).getImage());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        frame.add(outPan);
        frame.setSize(width, height);
        frame.setVisible(true);
        frame.toFront();
    }

    public void dispose () {
        this.frame.dispose();
    }
}
