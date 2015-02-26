package net.nmmst.tools;

import java.awt.Dimension;

/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class NMConstants {
    public static final boolean TESTS = true;
    public static final int MAX_SNAPSHOTS = 4;
    public static final Dimension FRAME_Dimension = new Dimension(1000, 1000);
    public static final int FONT_SIZE = 200;
    public static final int FRAME_QUEUE_LIMIT = 100;
    public static final double WHEEL_MAX_LIMIT = 1.0f;
    public static final double WHEEL_MIN_LIMIT = -1.0f;
    public static final double PRESS_LIMIT = 1.0f;
    public static final int IMAGE_WIDTH = 1920;
    public static final int IMAGE_HEIGHT = 1080;
    public static final String MOVIE_ROOT_PATH = "D://海科影片";
    public static final String MASTER_ROOT_PATH = "D://海科圖片";
    public static final String CONTROLLER_ROOT_PATH = MASTER_ROOT_PATH;
    public static final String CONTROLLER_DASHBOARD_JPG = CONTROLLER_ROOT_PATH + "//dashboard.jpg";
    public static final String CONTROLLER_STOP_JPG = CONTROLLER_ROOT_PATH + "//stop.jpg";
    public static final String CONTROLLER_OVAL_INFORMATION = "D://oval-information.conf";
    public static final String PLAYER_INFORMATION = "D://player-information.conf";
    public static final double PLAYER_BUFFER_LOWBOUND_LIMIT = 0.9f;
    public static final int SPECIFIC_FRAME_TIME = 60;
    public static final String PCI_1735U = "PCI-1735U,BID#0";
    public static final String PCI_1739U = "PCI-1739U,BID#15";
    private NMConstants(){}
}
