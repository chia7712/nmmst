package net.nmmst.tools;

/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class NMConstants {
    public static final double WHEEL_MAX_LIMIT = 0.99f;
    public static final double WHEEL_MIN_LIMIT = -0.99f;
    public static final double PRESS_LIMIT = 1.0f;
    public static final int IMAGE_WIDTH = 640;
    public static final int IMAGE_HEIGHT = 480;
    public static final int CHECK_PERIOD = 2; 
    public static final String MOVIE_ROOT_PATH = "D://海科影片";
    public static final String MASTER_ROOT_PATH = "D://海科圖片";
    public static final String CONTROLLER_ROOT_PATH = MASTER_ROOT_PATH;
    public static final String CONTROLLER_DASHBOARD_JPG = CONTROLLER_ROOT_PATH + "//dashboard.jpg";
    public static final String CONTROLLER_STOP_JPG = CONTROLLER_ROOT_PATH + "//stop.jpg";
    public static final String CONTROLLER_OVAL_INFORMATION = "D://oval-information.conf";
    public static final String PLAYER_INFORMATION = "D://player-information.conf";
    public static final int SPECIFIC_FRAME_TIME = 60;
    public static final String PCI_1735U = "PCI-1735U,BID#0";
    public static final String PCI_1739U = "PCI-1739U,BID#15";
    public static final String MASTER_IP = "192.168.100.31";
    private NMConstants(){}
}
