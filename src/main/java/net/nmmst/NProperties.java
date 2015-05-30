package net.nmmst;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import javafx.util.Pair;
import net.nmmst.NodeInformation.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NProperties {
    /**
     * Log.
     */
    private static final Logger LOG
            = LoggerFactory.getLogger(NProperties.class);  
    /**
     * The default width of frame.
     */
    public static final int FRAME_WIDTH = -1;
    /**
     * The default height of frame.
     */
    public static final int FRAME_HEIGHT = -1;
    /**
     * The root directory for all videos.
     */
    private static final String MOVIE_ROOT_PATH = "D://海科影片";
    /**
     * The root directory for all images.
     */
    private static final String IMAGE_ROOT_DIR = "D://海科圖片";
    /**
     * The defualt size for {@link StringBuilder}.
     */
    private static final int DEFAULT_BUILD_LENGTH = 200;
    /**
     * The default font size for generating string image.
     * @see net.nmmst.utils.Painter
     */
    private static final int GENERATED_FONT_SIZE = 200;
    /**
     * The default width for generating string image.
     * @see net.nmmst.utils.Painter
     */
    private static final int GENERATED_IMAGE_WIDTH = 1920;
    /**
     * The default height for generating string image.
     * @see net.nmmst.utils.Painter
     */
    private static final int GENERATED_IMAGE_HEIGHT = 1080;
    /**
     * The divider char of first layer. It is used for transforming
     * {@link NodeInformation}, and {@link #MOVIE_SELECT}
     * to <code>string</code>
     */
    private static final String DIVIDER_FIRST = ",";
    /**
     * The divider char of second layer. It is used for transforming
     * {@link NodeInformation}, and {@link #MOVIE_SELECT}
     * to <code>string</code>
     */
    private static final String DIVIDER_SECOND = ":";
    /**
     * The default path of movie.
     */
    private static final List<File> MOVIE_PATH
        =  Arrays.asList(
            new File(MOVIE_ROOT_PATH, "1.mpg"),
            new File(MOVIE_ROOT_PATH, "2A.mpg"),
            new File(MOVIE_ROOT_PATH, "3B.mpg"),
            new File(MOVIE_ROOT_PATH, "4.mpg"),
            new File(MOVIE_ROOT_PATH, "5A.mpg"),
            new File(MOVIE_ROOT_PATH, "6B.mpg"),
            new File(MOVIE_ROOT_PATH, "7.mpg"));
    /**
     * The default order for playing movie.
     */
    private static final List<Integer> MOVIE_ORDER
        = Arrays.asList(0, 1, 3, 4, 6);
    /**
     * The movie index and it's selectable index.
     */
    private static final Map<Integer, Pair<Integer, Integer>> MOVIE_SELECT
        = new TreeMap();
    static {
        MOVIE_SELECT.put(0, new Pair(1, 2));
        MOVIE_SELECT.put(3, new Pair(4, 5));
    }
    /**
     * The node information includes fusion nodes, control node, master node
     * and projectors.
     */
    private static final List<NodeInformation> NODE_INFORMATION
        = Arrays.asList(
            new NodeInformation(NodeInformation.Location.LU,
                                "192.168.100.1",
                                "00-0B-AB-6D-7D-25",
                                10000, 10001),
            new NodeInformation(NodeInformation.Location.RU,
                                "192.168.100.2",
                                "00-0B-AB-67-4E-83",
                                10002, 10003),
            new NodeInformation(NodeInformation.Location.LD,
                                "192.168.100.3",
                                "00-0B-AB-67-4E-73",
                                10004, 10005),
            new NodeInformation(NodeInformation.Location.RD,
                                "192.168.100.4",
                                "00-0B-AB-67-4E-70",
                                10006, 10007),
            new NodeInformation(NodeInformation.Location.CONTROLLER,
                                "192.168.100.38",
                                "00-0B-AB-67-4E-7F",
                                10008, 10009),
            new NodeInformation(NodeInformation.Location.MASTER,
                                "192.168.100.31",
                                "00-0B-AB-67-4E-7F",
                                10010, 10011),
            new NodeInformation(NodeInformation.Location.LU_P,
                                "192.168.100.11",
                                "00-0B-AB-67-4E-7F",
                                -1, -1),
            new NodeInformation(NodeInformation.Location.RU_P,
                                "192.168.100.12",
                                "00-0B-AB-67-4E-7F",
                                -1, -1),
            new NodeInformation(NodeInformation.Location.LD_P,
                                "192.168.100.13",
                                "00-0B-AB-67-4E-7F",
                                -1, -1),
            new NodeInformation(NodeInformation.Location.RD_P,
                                "192.168.100.14",
                                "00-0B-AB-67-4E-7F",
                                -1, -1));
    /**
     * The max size for buffering decoded frame.
     * @see net.nmmst.media.BufferFactory
     */
    private static final int FRAME_QUEUE_SIZE = 100;
    /**
     * The lower limit of frame buffer for starting play.
     * @see net.nmmst.media.BufferFactory
     * @see net.nmmst.utils.RegisterUtil.Watcher#isBufferInsufficient()
     */
    private static final double FRAME_BUFFER_LOWERLIMIT = 0.9f;
    /**
     * The max microtime for selecting the direction from control node.
     * @see net.nmmst.controller.WheelTrigger
     */
    private static final long WHEEL_ENABLE_MAX_MICROTIME_PERIOD
            = 10 * 1000 * 1000;
    /**
     * The min microtime for selecting the direction from control node.
     * @see net.nmmst.controller.WheelTrigger
     */
    public static final long WHEEL_ENABLE_MIN_MICROTIME_PERIOD
            = 5 * 1000 * 1000;
    /**
     * The valid max value for shifting wheel.
     * @see net.nmmst.controller.WheelTrigger
     */
    private static final double WHEEL_MAX_VALUE = 0.9f;
    /**
     * The valid min value for shifting wheel.
     * @see net.nmmst.controller.WheelTrigger
     */
    private static final double WHEEL_MIN_VALUE = -0.9f;
    /**
     * The valid max value for shifting wheel.
     * @see net.nmmst.controller.WheelTrigger
     */
    private static final double WHEEL_MAX_INIT_VALUE = 0.1;
    /**
     * The valid min value for shifting wheel.
     * @see net.nmmst.controller.WheelTrigger
     */
    private static final double WHEEL_MIN_INIT_VALUE = -0.1;
    /**
     * The valid max value for shifting stick.
     * @see net.nmmst.controller.StickTrigger
     */
    private static final double STICK_MAX_VALUE = 1.0f;
    /**
     * The valid min value for shifting stick.
     * @see net.nmmst.controller.StickTrigger
     */
    private static final double STICK_MIN_VALUE = -1.0f;
    /**
     * The valid init max value for shifting stick.
     * @see net.nmmst.controller.StickTrigger
     */
    private static final double STICK_MAX_INIT_VALUE = 0.1;
    /**
     * The valid init min value for shifting stick.
     * @see net.nmmst.controller.StickTrigger
     */
    private static final double STICK_MIN_INIT_VALUE = -0.1;
    /**
     * The valid value for pressing stick.
     * @see net.nmmst.controller.StickTrigger
     */
    private static final double STICK_PRESS_VALUE = 1.0f;
    /**
     * The scale for snapshot image.
     */
    private static final double SNAPSHOT_SCALE = 0.25;
    /**
     * Indicates whether wheel and stick are enable. 
     */
    private static final boolean CONTROLLER_ENABLE = true;
    /**
     * The default name of PCI 1735u.
     */
    private static final String PCI_1735U_NAME
            = "PCI-1735U,BID#0";
    /**
     * The default name of PCI 1739u.
     */
    private static final String PCI_1739U_NAME
            = "PCI-1739U,BID#15";
    /**
     * The keyboard value for switching the panel on the control node.
     */
    private static final String CONTROL_KEYBOARD
            = new StringBuilder()
            .append("a")
            .append(DIVIDER_FIRST)
            .append("b")
            .append(DIVIDER_FIRST)
            .append("c")
            .append(DIVIDER_FIRST)
            .append("e")
            .append(DIVIDER_FIRST)
            .toString();
    /**
     * Saving all properties by formating to java {@link Properties}.
     */
    private final Properties properties;
    /**
     * Constructs a default NProperties.
     */
    public NProperties() {
        this(new File(NProperties.class.getName()));
    }
    /**
     * Initializes a newly created NProperties object so that it represents the same
     * properties as the argument
     * @param nproperties Another properties
     */
    public NProperties(NProperties nproperties) {
        properties = new Properties(nproperties.properties);
    }
    /**
     * Initializes a newly created NProperties object so that it loads the properties
     * from the local file.
     * @param propertyFile The properties file
     */
    public NProperties(File propertyFile) {
        properties = new Properties();
        init(propertyFile);
    }
    /**
     * Returns a string list to which the specified key is mapped, or RuntimeException if this
     * properties contains no mapping for the key.
     * @param key The key whose associated value is to be returned
     * @return A string list to which the specified key is mapped,
     * or RuntimeException if this properties contains no mapping for the key.
     */
    public List<String> getStrings(final String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw throwException(key);
        }
        return Arrays.asList(value.split(DIVIDER_FIRST));
    }
    /**
     * Returns a boolean value to which the specified key is mapped, or RuntimeException if this
     * properties contains no mapping for the key.
     * @param key The key whose associated value is to be returned
     * @return A boolean value to which the specified key is mapped,
     * or RuntimeException if this properties contains no mapping for the key.
     */
    public boolean getBoolean(final String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw throwException(key);
        }
        return Boolean.valueOf(value);
    }
    /**
     * Returns a double value to which the specified key is mapped, or RuntimeException if this
     * properties contains no mapping for the key.
     * @param key The key whose associated value is to be returned
     * @return A double value to which the specified key is mapped,
     * or RuntimeException if this properties contains no mapping for the key.
     */
    public double getDouble(final String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw throwException(key);
        }
        return Double.valueOf(value);
    }
    /**
     * Returns a long value to which the specified key is mapped, or RuntimeException if this
     * properties contains no mapping for the key.
     * @param key The key whose associated value is to be returned
     * @return A long value to which the specified key is mapped,
     * or RuntimeException if this properties contains no mapping for the key.
     */
    public long getLong(final String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw throwException(key);
        }
        return Long.valueOf(value);
    }
    /**
     * Returns a integer list to which the specified key is mapped, or RuntimeException if this
     * properties contains no mapping for the key.
     * @param key The key whose associated value is to be returned
     * @return A integer list to which the specified key is mapped,
     * or RuntimeException if this properties contains no mapping for the key.
     */
    public List<Integer> getIntegers(final String key) {
        List<Integer> rval = new LinkedList();
        getStrings(key).stream()
                       .forEach(value -> rval.add(Integer.valueOf(value)));
        return rval;
    }
    /**
     * Returns a int value to which the specified key is mapped, or RuntimeException if this
     * properties contains no mapping for the key.
     * @param key The key whose associated value is to be returned
     * @return A int value to which the specified key is mapped,
     * or RuntimeException if this properties contains no mapping for the key.
     */
    public int getInteger(final String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw throwException(key);
        }
        return Integer.valueOf(value);
    }
    /**
     * Returns a string to which the specified key is mapped, or RuntimeException if this
     * properties contains no mapping for the key.
     * @param key The key whose associated value is to be returned
     * @return A string to which the specified key is mapped,
     * or RuntimeException if this properties contains no mapping for the key.
     */
    public String getString(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw throwException(key);
        }
        return value;
    }
    /**
     * Throws a {@link RuntimeException} if failed to return the property.
     * @param key The key whose associated value is to be returned
     * @return RuntimeException
     */
    private RuntimeException throwException(final String key) {
        return new RuntimeException(
            "No corresponding value for " + key);
    }
    /**
     * Sets value if there are no mapped key.
     * @param key The key to be placed into this property list
     * @param value tHE value corresponding to key
     */
    private void setIfAbsent(final String key, final String value) {
        if (!properties.containsKey(key)) {
            properties.setProperty(key, value);
        }
    }
    /**
     * Initializes this properties by loading from file.
     * @param file The proeperties file
     */
    private void init(final File file) {
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                properties.load(reader);
            } catch (IOException e) {
                properties.clear();
            }
        }
        setIfAbsent(
            NConstants.FRAME_WIDTH,
            String.valueOf(FRAME_WIDTH));
        setIfAbsent(
            NConstants.FRAME_HEIGHT,
            String.valueOf(FRAME_HEIGHT));
        setIfAbsent(
            NConstants.MOVIE_PATH,
            moviesToString(MOVIE_PATH));
        setIfAbsent(
            NConstants.MOVIE_ORDER,
            orderToString(MOVIE_ORDER));
        setIfAbsent(
            NConstants.MOVIE_SELECT,
            selectableToString(MOVIE_SELECT));
        setIfAbsent(
            NConstants.NODE_INFORMATION,
            nodesToString(NODE_INFORMATION));
        setIfAbsent(
            NConstants.IMAGE_CONTROL_DASHBOARD,
            new File(IMAGE_ROOT_DIR, "dashboard.jpg").getAbsolutePath());
        setIfAbsent(
            NConstants.IMAGE_CONTROL_STOP,
            new File(IMAGE_ROOT_DIR, "stop.jpg").getAbsolutePath());
        setIfAbsent(
            NConstants.IMAGE_MASTER_START,
            new File(IMAGE_ROOT_DIR, "m_start.jpg").getAbsolutePath());
        setIfAbsent(
            NConstants.IMAGE_MASTER_STOP,
            new File(IMAGE_ROOT_DIR, "m_stop.jpg").getAbsolutePath());
        setIfAbsent(
            NConstants.IMAGE_MASTER_REFRESH,
            new File(IMAGE_ROOT_DIR, "m_refresh.jpg").getAbsolutePath());
        setIfAbsent(
            NConstants.IMAGE_MASTER_BACKGROUND,
            new File(IMAGE_ROOT_DIR, "m_background_all.jpg")
                    .getAbsolutePath());
        setIfAbsent(
            NConstants.IMAGE_MASTER_TEST,
            new File(IMAGE_ROOT_DIR, "m_test.jpg").getAbsolutePath());
        setIfAbsent(
            NConstants.GENERATED_FONT_SIZE,
            String.valueOf(GENERATED_FONT_SIZE));
        setIfAbsent(
            NConstants.GENERATED_IMAGE_WIDTH,
            String.valueOf(GENERATED_IMAGE_WIDTH));
        setIfAbsent(
            NConstants.GENERATED_IMAGE_HEIGHT,
            String.valueOf(GENERATED_IMAGE_HEIGHT));
        setIfAbsent(
            NConstants.FRAME_QUEUE_SIZE,
            String.valueOf(FRAME_QUEUE_SIZE));
        setIfAbsent(
            NConstants.FRAME_BUFFER_LOWERLIMIT,
            String.valueOf(FRAME_BUFFER_LOWERLIMIT));
        setIfAbsent(NConstants.WHEEL_ENABLE_MAX_MICROTIME_PERIOD,
            String.valueOf(WHEEL_ENABLE_MAX_MICROTIME_PERIOD));
        setIfAbsent(NConstants.WHEEL_ENABLE_MIN_MICROTIME_PERIOD,
            String.valueOf(WHEEL_ENABLE_MIN_MICROTIME_PERIOD));
        setIfAbsent(NConstants.WHEEL_MAX_VALUE,
            String.valueOf(WHEEL_MAX_VALUE));
        setIfAbsent(NConstants.WHEEL_MIN_VALUE,
            String.valueOf(WHEEL_MIN_VALUE));
        setIfAbsent(NConstants.WHEEL_MAX_INIT_VALUE,
            String.valueOf(WHEEL_MAX_INIT_VALUE));
        setIfAbsent(NConstants.WHEEL_MIN_INIT_VALUE,
            String.valueOf(WHEEL_MIN_INIT_VALUE));
        setIfAbsent(NConstants.STICK_MAX_VALUE,
            String.valueOf(STICK_MAX_VALUE));
        setIfAbsent(NConstants.STICK_MIN_VALUE,
            String.valueOf(STICK_MIN_VALUE));
        setIfAbsent(NConstants.STICK_MAX_INIT_VALUE,
            String.valueOf(STICK_MAX_INIT_VALUE));
        setIfAbsent(NConstants.STICK_MIN_INIT_VALUE,
            String.valueOf(STICK_MIN_INIT_VALUE));
        setIfAbsent(
            NConstants.STICK_PRESS_VALUE,
            String.valueOf(STICK_PRESS_VALUE));
        setIfAbsent(
            NConstants.SNAPSHOT_SCALE,
            String.valueOf(SNAPSHOT_SCALE));
        setIfAbsent(
            NConstants.CONTROLLER_ENABLE,
            String.valueOf(CONTROLLER_ENABLE));
        setIfAbsent(
            NConstants.PCI_1735U_NAME,
            PCI_1735U_NAME);
        setIfAbsent(
            NConstants.PCI_1739U_NAME,
            PCI_1739U_NAME);
        setIfAbsent(
            NConstants.CONTROL_KEYBOARD,
            String.valueOf(CONTROL_KEYBOARD));
        save(file);
    }
    /**
     * Saves current properties to local file.
     * @param file The file
     */
    private void save(final File file) {
        if (file.exists()
            && !file.delete()) {
            return;
        }
        try (FileWriter writer = new FileWriter(file)) {
            properties.store(writer, null);
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
    }
    public static String nodeToString(final NodeInformation node) {
        StringBuilder builder = new StringBuilder();
        return builder.append(node.getLocation())
                      .append(DIVIDER_SECOND)
                      .append(node.getIP())
                      .append(DIVIDER_SECOND)
                      .append(node.getMac())
                      .append(DIVIDER_SECOND)
                      .append(node.getRequestPort())
                      .append(DIVIDER_SECOND)
                      .append(node.getRegisterPort())
                      .toString();
    }
    public static List<NodeInformation> stringToNodes(final String str) {
        List<NodeInformation> nodes = new LinkedList();
        final int locationIndex = 0;
        final int ipIndex = 1;
        final int macIndex = 2;
        final int requestIndex = 3;
        final int registerIndex = 4;
        for (String s : str.split(DIVIDER_FIRST)) {
            String[] args = s.split(DIVIDER_SECOND);
            Location.match(args[locationIndex]).ifPresent(location -> {
                nodes.add(new NodeInformation(location,
                args[ipIndex], args[macIndex],
                Integer.valueOf(args[requestIndex]),
                Integer.valueOf(args[registerIndex])));
            });
        }
        return nodes;
    }
    public static String nodesToString(final List<NodeInformation> nodes) {
        StringBuilder builder = new StringBuilder(DEFAULT_BUILD_LENGTH);
        nodes.stream().forEach((node) -> {
            builder.append(nodeToString(node))
                    .append(DIVIDER_FIRST);
        });
        return builder.toString();
    }
    public static String moviesToString(
            final List<File> movieList) {
        StringBuilder builder = new StringBuilder(DEFAULT_BUILD_LENGTH);
        movieList.stream().forEach((file) -> {
            builder.append(file.getAbsolutePath())
                    .append(DIVIDER_FIRST);
        });
        return builder.toString();
    }
    public static String orderToString(
            final List<Integer> orderList) {
        StringBuilder builder = new StringBuilder(DEFAULT_BUILD_LENGTH);
        orderList.stream().forEach((i) -> {
            builder.append(i)
                    .append(DIVIDER_FIRST);
        });
        return builder.toString();
    }
    public static Map<Integer, Pair<Integer, Integer>> stringToSelectable(
        final String str) {
        Map<Integer, Pair<Integer, Integer>> selectable
            = new TreeMap();
        final int movieIndex = 0;
        final int leftIndex = 1;
        final int rightIndex = 2;
        for (String s : str.split(DIVIDER_FIRST)) {
            String[] args = s.split(DIVIDER_SECOND);
            selectable.put(Integer.valueOf(args[movieIndex]),
                new Pair(Integer.valueOf(args[leftIndex]),
                         Integer.valueOf(args[rightIndex])));
        }
        return selectable;
    }
    public static String selectableToString(
            final Map<Integer, Pair<Integer, Integer>> movieSelectable) {
        StringBuilder builder = new StringBuilder(DEFAULT_BUILD_LENGTH);
        movieSelectable.entrySet().stream().forEach(entry -> {
            builder.append(entry.getKey())
                   .append(DIVIDER_SECOND)
                   .append(entry.getValue().getKey())
                   .append(DIVIDER_SECOND)
                   .append(entry.getValue().getValue())
                   .append(DIVIDER_FIRST);
        });
        return builder.toString();
    }
}
