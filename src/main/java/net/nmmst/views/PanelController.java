/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.nmmst.views;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import javax.swing.JButton;
import javax.swing.JPanel;
import net.nmmst.NConstants;
import net.nmmst.NProperties;
import net.nmmst.media.BasePanel;
import net.nmmst.utils.Painter;
import net.nmmst.utils.RequestUtil;

/**
 *
 * @author 嘉平
 */
public class PanelController {
    private final BasePanel startPanel;
    private final BasePanel stopPanel;
    private final BasePanel refreshPanel;
    private final BasePanel backgroundPanel;
    private final BasePanel testPanel;
    private final JButton initButton = new JButton("展演準備");
    private final JButton shurdownButton = new JButton("睡覺");
    private final JButton wakeupButton= new JButton("起床");
    private final JButton party1Button= new JButton("宴會一");
    private final JButton party2Button = new JButton("宴會二");
    private final JButton lightOffButton = new JButton("關閉燈光");
    private final List<Component> componentList
            = new LinkedList();
    private void addListener(final JPanel panel,
            final MouseReleasedListener listener) {
        panel.addMouseListener(listener);
        componentList.add(panel);
    }
    private void addListener(final JButton button,
            final ActionListener listener) {
        button.addActionListener(listener);
        componentList.add(button);
    }
    public BasePanel getMainPanel() {
        return backgroundPanel;
    }
    public PanelController(final NProperties properties,
            final BlockingQueue<RequestUtil.Request> requestQueue) {
        startPanel = new BasePanel(Painter.loadOrStringImage(
            properties, NConstants.IMAGE_MASTER_START),
            BasePanel.Mode.FILL);
        startPanel.setPreferredSize(new Dimension(400, 400));
        addListener(startPanel, event
            -> requestQueue.offer(new RequestUtil.Request(RequestUtil.RequestType.START)));
        stopPanel = new BasePanel(Painter.loadOrStringImage(
            properties, NConstants.IMAGE_MASTER_STOP),
            BasePanel.Mode.FILL);
        stopPanel.setPreferredSize(new Dimension(100, 100));
        addListener(stopPanel, event
            -> requestQueue.offer(new RequestUtil.Request(RequestUtil.RequestType.STOP)));
        refreshPanel = new BasePanel(Painter.loadOrStringImage(
            properties, NConstants.IMAGE_MASTER_REFRESH),
            BasePanel.Mode.FILL);
        refreshPanel.setPreferredSize(new Dimension(100, 100));
        addListener(refreshPanel, event
            -> requestQueue.offer(new RequestUtil.Request(RequestUtil.RequestType.REBOOT)));
        testPanel = new BasePanel(Painter.loadOrStringImage(
            properties, NConstants.IMAGE_MASTER_TEST),
            BasePanel.Mode.FILL);     
        testPanel.setPreferredSize(new Dimension(100, 100));
        addListener(testPanel, event 
            -> {});
        addListener(initButton, event
            -> requestQueue.offer(new RequestUtil.Request(RequestUtil.RequestType.INIT)));
        addListener(shurdownButton, event
            -> requestQueue.offer(new RequestUtil.Request(RequestUtil.RequestType.SHUTDOWN)));
        addListener(wakeupButton, event
            -> requestQueue.offer(new RequestUtil.Request(RequestUtil.RequestType.WOL)));
        addListener(party1Button, event
            -> requestQueue.offer(new RequestUtil.Request(RequestUtil.RequestType.PARTY1)));          
        addListener(party2Button, event
            -> requestQueue.offer(new RequestUtil.Request(RequestUtil.RequestType.PARTY2)));       
        addListener(lightOffButton, event
            -> requestQueue.offer(new RequestUtil.Request(RequestUtil.RequestType.LIGHT_OFF)));   

        backgroundPanel = new BasePanel(Painter.loadOrStringImage(
            properties, NConstants.IMAGE_MASTER_BACKGROUND),
            BasePanel.Mode.FILL);
        componentList.stream().forEach(backgroundPanel::add);
    }
    private interface MouseReleasedListener extends MouseListener {
        @Override
        public default void mouseClicked(MouseEvent e) {
        }
        @Override
        public default void mousePressed(MouseEvent e) {
        }
        @Override
        public default void mouseEntered(MouseEvent e){
        }
        @Override
        public default void mouseExited(MouseEvent e){
        }
    }
}
