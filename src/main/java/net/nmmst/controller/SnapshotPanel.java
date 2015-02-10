/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.nmmst.controller;

import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JPanel;
import net.nmmst.tools.BasicPanel;
import net.nmmst.tools.Painter;

/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class SnapshotPanel extends JPanel {
    private static final int ROWS = 0;
    private static final int COLUMNS = 1;
    private final BufferedImage defaultImage = Painter.getStringImage("No Snaphosts", 640, 480);
    private final List<BasicPanel> currentPanel = new LinkedList();
    public SnapshotPanel() {
        setLayout(new GridLayout(ROWS, COLUMNS));
    }
    public void setOvalInformations(List<OvalInformation> ovalInfos) {
        synchronized(currentPanel) {
            for (BasicPanel panel : currentPanel) {
                remove(panel);
            }
            currentPanel.clear();
            for (OvalInformation oval : ovalInfos) {
                currentPanel.add(new BasicPanel(oval.getImage()));
            }
            if (currentPanel.isEmpty()) {
                currentPanel.add(new BasicPanel(defaultImage));
            }
            for (BasicPanel panel : currentPanel) {
                add(panel);
            }
            revalidate();
            repaint();
        }
    }
}
