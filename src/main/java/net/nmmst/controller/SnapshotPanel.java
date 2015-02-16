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
import net.nmmst.tools.BasePanel;

/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class SnapshotPanel extends JPanel {
    private static final int ROWS = 0;
    private static final int COLUMNS = 2;
    private final List<BasePanel> currentPanel = new LinkedList();
    public SnapshotPanel() {
        setLayout(new GridLayout(ROWS, COLUMNS));
    }
    public void addImages(List<BufferedImage> images) {
        synchronized(currentPanel) {
            currentPanel.stream().forEach((panel) -> {
                remove(panel);
            });
            images.stream().forEach((image) -> {
                currentPanel.add(new BasePanel(image, BasePanel.Mode.FILL));
            });
            currentPanel.stream().forEach((panel) -> {
                add(panel);
            });
            revalidate();
            repaint();
        }
    }
    public void addOvalInformations(List<OvalInformation> ovalInfos) {
        List<BufferedImage> images = new LinkedList();
        ovalInfos.stream().forEach((oval) -> {
            images.add(oval.getImage());
        });
        addImages(images);
    }
    public void cleanSnapshots() {
        synchronized(currentPanel) {
            currentPanel.stream().forEach((panel) -> {
                remove(panel);
            });
            currentPanel.clear();
            revalidate();
            repaint();           
        }
    }
}
