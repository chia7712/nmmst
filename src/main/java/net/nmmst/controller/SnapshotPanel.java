/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.nmmst.controller;

import java.awt.GridLayout;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JPanel;
import net.nmmst.tools.BasicPanel;

/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class SnapshotPanel extends JPanel {
    private static final int ROWS = 0;
    private static final int COLUMNS = 3;
    private final List<BasicPanel> currentPanel = new LinkedList();
    public SnapshotPanel() {
        this.setLayout(new GridLayout(ROWS, COLUMNS));
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
            for (BasicPanel panel : currentPanel) {
                add(panel);
            }
            repaint();
        }
    }
}
