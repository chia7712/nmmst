package tw.gov.nmmst;

/**
 * The configuration constants.
 */
public final class NConstants {
    /**
     * The default time for waiting the submarine to init.
     */
    public static final String ELAPSED_INIT_SUBMARINE
            = "elapsed.init.submarine";
    /**
     * The default width of frame.
     */
    public static final String FRAME_WIDTH = "frame.width";
    /**
     * The default height of frame.
     */
    public static final String FRAME_HEIGHT = "frame.height";
    /**
     * The default path of movie.
     */
    public static final String MOVIE_PATH = "movie.path";
    /**
     * The default order for playing movie.
     */
    public static final String MOVIE_ORDER = "movie.order";
    /**
     * The selectable movie index and it's selected index.
     */
    public static final String MOVIE_SELECT = "movie.select";
    /**
     * The node information includes fusion nodes, control node, master node
     * and projectors.
     * @see NodeInformation
     */
    public static final String NODE_INFORMATION = "node.information";
    /**
     * The path of dashboard image for control node.
     */
    public static final String IMAGE_CONTROL_DASHBOARD
            = "image.control.dashboard";
    /**
     * The path of stop image for control node.
     */
    public static final String IMAGE_CONTROL_STOP
            = "image.control.stop";
    /**
     * The path of start image for master node.
     */
    public static final String IMAGE_MASTER_START
            = "image.master.start";
    /**
     * The path of  stop image for master node.
     */
    public static final String IMAGE_MASTER_STOP
            = "image.master.stop";
    /**
     * The path of refresh image for master node.
     */
    public static final String IMAGE_MASTER_REFRESH
            = "image.master.refresh";
    /**
     * The path of background mage for master node.
     */
    public static final String IMAGE_MASTER_BACKGROUND
            = "image.master.background";
    /**
     * The path of test mage for master node.
     */
    public static final String IMAGE_MASTER_TEST
            = "image.master.test";
    /**
     * The default font size for generating string image.
     * @see net.nmmst.utils.Painter
     */
    public static final String GENERATED_FONT_SIZE
            = "generated.font.size";
    /**
     * The default width for generating string image.
     * @see net.nmmst.utils.Painter
     */
    public static final String GENERATED_IMAGE_WIDTH
            = "generated.image.width";
    /**
     * The default height for generating string image.
     * @see net.nmmst.utils.Painter
     */
    public static final String GENERATED_IMAGE_HEIGHT
            = "generated.image.height";
/**
     * The max size for buffering decoded frame.
     * @see net.nmmst.media.BufferFactory
     */
    public static final String FRAME_QUEUE_SIZE = "frame.queue.size";
    /**
     * The lower limit of frame buffer for starting play.
     * @see net.nmmst.media.BufferFactory
     * @see net.nmmst.utils.RegisterUtil.Watcher#isBufferInsufficient()
     */
    public static final String FRAME_BUFFER_LOWERLIMIT
            = "frame.buffer.lowerLimit";
    /**
     * Indicates whether we enable the wheel trigger.
     */
    public static final String WHEEL_ENABLE = "wheel.enable";
    /**
     * The max microtime for selecting the direction from control node.
     * @see net.nmmst.controller.WheelTrigger
     */
    public static final String WHEEL_ENABLE_MAX_MICROTIME_PERIOD
            = "wheel.enable.max.microtime.period";
    /**
     * The min microtime for selecting the direction from control node.
     * @see net.nmmst.controller.WheelTrigger
     */
    public static final String WHEEL_ENABLE_MIN_MICROTIME_PERIOD
            = "wheel.enable.min.microtime.period";
    /**
     * The valid max value for shifting wheel.
     * @see net.nmmst.controller.WheelTrigger
     */
    public static final String WHEEL_MAX_VALUE = "wheel.max.value";
    /**
     * The valid min value for shifting wheel.
     * @see net.nmmst.controller.WheelTrigger
     */
    public static final String WHEEL_MIN_VALUE = "wheel.min.value";
    /**
     * The valid max value for shifting wheel.
     * @see net.nmmst.controller.WheelTrigger
     */
    public static final String WHEEL_MAX_INIT_VALUE = "wheel.max.init.value";
    /**
     * The valid min value for shifting wheel.
     * @see net.nmmst.controller.WheelTrigger
     */
    public static final String WHEEL_MIN_INIT_VALUE = "wheel.min.init.value";
    /**
     * Indicates whether we enable the stick trigger.
     */
    public static final String STICK_ENABLE = "stick.enable";
    /**
     * The valid max value for shifting stick.
     * @see net.nmmst.controller.StickTrigger
     */
    public static final String STICK_MAX_VALUE = "stick.max.value";
    /**
     * The valid min value for shifting stick.
     * @see net.nmmst.controller.StickTrigger
     */
    public static final String STICK_MIN_VALUE = "stick.min.value";
    /**
     * The valid init max value for shifting stick.
     * @see net.nmmst.controller.StickTrigger
     */
    public static final String STICK_MAX_INIT_VALUE = "stick.max.init.value";
    /**
     * The valid init min value for shifting stick.
     * @see net.nmmst.controller.StickTrigger
     */
    public static final String STICK_MIN_INIT_VALUE = "stick.min.init.value";
    /**
     * The valid value for pressing stick.
     * @see net.nmmst.controller.StickTrigger
     */
    public static final String STICK_PRESS_VALUE = "stick.press.value";
    /**
     * The scale for snapshot image.
     */
    public static final String SNAPSHOT_SCALE = "snapshot.scale";
    /**
     * Indicates whether wheel and stick are enable.
     */
    public static final String CONTROLLER_ENABLE = "controller.enable";
    /**
     * The default name of PCI 1735u.
     */
    public static final String PCI_1735U_NAME
            = "pci.1735u.name";
    /**
     * The default name of PCI 1739u.
     */
    public static final String PCI_1739U_NAME
            = "pci.1739u.name";
    /**
     * The keyboard value for switching the panel on the control node.
     */
    public static final String CONTROL_KEYBOARD
            = "control.keyboard";
    /**
     * The keyword value for staring the submarine.
     */
    public static final String MASTER_SUBMARINE_START
            = "master.submarine.start";
    /**
     * The keyword value for ending the submarine.
     */
    public static final String MASTER_SUBMARINE_END
            = "master.submarine.end";
    /**
     * Can't be instantiated with this ctor.
     */
    private NConstants() {
    }
}
