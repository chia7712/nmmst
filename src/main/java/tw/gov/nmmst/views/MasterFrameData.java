/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.gov.nmmst.views;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import tw.gov.nmmst.NProperties;
import tw.gov.nmmst.NodeInformation;
import tw.gov.nmmst.controller.DioFactory;
import tw.gov.nmmst.controller.DioInterface;
import tw.gov.nmmst.media.BasePanel;
import tw.gov.nmmst.media.MediaWorker;
import tw.gov.nmmst.media.MovieInfo;
import tw.gov.nmmst.threads.AtomicCloser;
import tw.gov.nmmst.threads.BaseTimer;
import tw.gov.nmmst.threads.Closer;
import tw.gov.nmmst.utils.ProjectorUtil;
import tw.gov.nmmst.utils.RegisterUtil;
import tw.gov.nmmst.utils.RequestUtil;
import tw.gov.nmmst.utils.RequestUtil.SelectRequest;
import tw.gov.nmmst.utils.SerialStream;
import tw.gov.nmmst.utils.WolUtil;
/**
 * The data of master frame.
 */
public class MasterFrameData implements FrameData {
    /**
     * NProperties.
     */
    private final NProperties properties;
    /**
     * Closer.
     */
    private final Closer closer = new AtomicCloser();
    /**
     * Master information.
     */
    private final NodeInformation selfInformation;
    /**
     * Request queue.
     */
    private final BlockingQueue<RequestUtil.Request> requestQueue;
    /**
     * Digital I/O.
     */
    private final DioInterface dio;
    /**
     * Watchs the buffer status.
     */
    private final RegisterUtil.Watcher watcher;
    /**
     * Provides the movie duration.
     */
    private final MovieInfo order;
    /**
     * Start flow is used for triggering the digital I/O to play the audio.
     */
    private final StartFlow flow;
    /**
     * Deploys the panel.
     */
    private final PanelController panelController;
    /**
     * All video nodes.
     */
    private final Collection<NodeInformation> videoNodes;
    /**
     * Request functions.
     */
    private final Map<RequestUtil.RequestType, RequestFunction> functions
            = new TreeMap();
    /**
     * Constructs a data of master frame.
     * @param file The initial properties
     * @throws IOException If failed to open movie
     */
    public MasterFrameData(final File file) throws IOException {
        if (file == null) {
            properties = new NProperties();
        } else {
            properties = new NProperties(file);
        }
        selfInformation
            = NodeInformation.getNodeInformationByAddress(properties);
        requestQueue = RequestUtil.createRemoteQueue(selfInformation, closer);
        dio = DioFactory.getDefault(properties);
        watcher = RegisterUtil.createWatcher(closer,
            new BaseTimer(TimeUnit.SECONDS, 2), properties);
        order = new MovieInfo(properties);
        flow = new StartFlow(properties, watcher, order, dio);
        panelController = new PanelController(properties, requestQueue);
        videoNodes = NodeInformation.getVideoNodes(properties);
        Arrays.asList(RequestUtil.RequestType.values())
              .stream()
              .forEach(type -> {
            switch (type) {
                case START:
                    functions.put(type, (data, request)-> flow.start());
                    break;
                case STOP:
                    functions.put(type, (data, request) -> {
                        flow.stop();
                        dio.lightOff();
                        SerialStream.sendAll(videoNodes,
                            new RequestUtil.Request(type));
                    });
                    break;
                case SELECT:
                    functions.put(type, (data, request) -> {
                        if (request.getClass() == SelectRequest.class) {
                            SelectRequest select = (SelectRequest) request;
                            if (!watcher.isConflictWithBuffer(
                                    select.getIndex())) {
                                SerialStream.sendAll(
                                    videoNodes,
                                    select);
                                data.setNextFlow(select.getIndex());
                            }
                        }
                    });
                    break;
                case REBOOT:
                    functions.put(type, (data, request) -> {
                        if (flow.isStart()) {
                            dio.lightOff();
                        }
                        SerialStream.sendAll(
                            videoNodes,
                            new RequestUtil.Request(type));
                    });
                    break;
                case SHUTDOWN:
                    functions.put(type, (data, request)
                        -> {
                            SerialStream.sendAll(
                                videoNodes,
                                new RequestUtil.Request(type));
                            ProjectorUtil.enableAllMachine(properties, false);
                        });
                    break;
                case INIT:
                    functions.put(type, (data, request)
                        -> {
                        dio.stoneGotoLeft();
                        dio.lightWork();
                        dio.initializeSubmarineAndGray();
                    });
                    break;
                case LIGHT_OFF:
                    functions.put(type, (data, request)
                        -> {
                        dio.lightOff();
                    });
                    break;
                case PARTY_1:
                    functions.put(type, (data, request)
                        -> {
                        dio.grayUptoEnd();
                        dio.stoneGotoRight();
                        dio.lightParty1();
                    });
                    break;
                case PARTY_2:
                    functions.put(type, (data, request)
                        -> {
                        dio.grayUptoEnd();
                        dio.stoneGotoRight();
                        dio.lightParty2();
                    });
                    break;
                case WOL:
                    functions.put(type, (data, request)
                        -> {
                        for (NodeInformation nodeInformation : videoNodes) {
                            WolUtil.wakeup(
                                NodeInformation.getBroadCast(
                                    data.getNodeInformation()),
                                nodeInformation.getMac());
                        }
                        ProjectorUtil.enableAllMachine(properties, true);
                    });
                    break;
                default:
                    break;
            }
        });
    }

    @Override
    public final void setNextFlow(final int index) {
        flow.setNextFlow(index);
    }
    @Override
    public final Map<RequestUtil.RequestType, RequestFunction> getFunctions() {
        return functions;
    }
    @Override
    public final BlockingQueue<RequestUtil.Request> getQueue() {
        return requestQueue;
    }
    @Override
    public final BasePanel getMainPanel() {
        return panelController.getMainPanel();
    }
    @Override
    public final MediaWorker getMediaWorker() {
        throw new UnsupportedOperationException(
                "Not supported yet.");
    }
    @Override
    public final Closer getCloser() {
        return closer;
    }
    @Override
    public final NodeInformation getNodeInformation() {
        return selfInformation;
    }
    @Override
    public final NProperties getNProperties() {
        return properties;
    }
}
