/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.nmmst.views;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import net.nmmst.NProperties;
import net.nmmst.NodeInformation;
import net.nmmst.controller.DioFactory;
import net.nmmst.controller.DioInterface;
import net.nmmst.media.BasePanel;
import net.nmmst.media.MediaWorker;
import net.nmmst.media.MovieInfo;
import net.nmmst.threads.AtomicCloser;
import net.nmmst.threads.BaseTimer;
import net.nmmst.threads.Closer;
import net.nmmst.utils.ProjectorUtil;
import net.nmmst.utils.RegisterUtil;
import net.nmmst.utils.RequestUtil;
import net.nmmst.utils.SerialStream;
import net.nmmst.utils.WolUtil;

public class MasterFrameData implements FrameData {
    private final NProperties properties = new NProperties();
    private final Closer closer = new AtomicCloser();
    private final NodeInformation selfInformation
        = NodeInformation.getNodeInformationByAddress(properties);
    private final BlockingQueue<RequestUtil.Request> requestQueue
        = RequestUtil.createRemoteQueue(selfInformation, closer);
    private final DioInterface dio = DioFactory.getDefault(properties);
    private final RegisterUtil.Watcher watcher = RegisterUtil.createWatcher(closer,
        new BaseTimer(TimeUnit.SECONDS, 2), properties);
    private final MovieInfo order = new MovieInfo(properties);
    private final StartFlow flow = new StartFlow(properties,
            watcher, order, dio);
    private final PanelController panelController
            = new PanelController(properties, requestQueue);
    private final Collection<NodeInformation> videoNodes
            = NodeInformation.getVideoNodes(properties);
    private final Map<RequestUtil.RequestType, RequestFunction> functions
            = new TreeMap();
    public MasterFrameData() throws IOException {
        Arrays.asList(RequestUtil.RequestType.values()).stream().forEach(type -> {
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
                        if (request instanceof RequestUtil.SelectRequest
                            && data instanceof MasterFrameData) {
                            RequestUtil.SelectRequest select
                                    = (RequestUtil.SelectRequest)request;
                            if (!watcher.isConflictWithBuffer(
                                    select.getIndex())) {
                                SerialStream.sendAll(
                                    videoNodes,
                                    select);
                                ((MasterFrameData)data).getFlow()
                                        .setNextFlow(select.getIndex());
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
                case PARTY1: 
                    functions.put(type, (data, request)
                        -> {
                        dio.grayUptoEnd();
                        dio.stoneGotoRight();
                        dio.lightParty1();
                    });
                    break;
                case PARTY2: 
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
                            WolUtil.wakeup(NodeInformation.getBroadCast(data.getNodeInformation()),
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
    public StartFlow getFlow() {
        return flow;
    }
    @Override
    public Map<RequestUtil.RequestType, RequestFunction> getFunctions() {
        return functions;
    }
    @Override
    public BlockingQueue<RequestUtil.Request> getQueue() {
        return requestQueue;
    }
    @Override
    public BasePanel getMainPanel() {
        return panelController.getMainPanel();
    }
    @Override
    public MediaWorker getMediaWorker() {
        throw new UnsupportedOperationException(
                "Not supported yet.");
    }
    @Override
    public Closer getCloser() {
        return closer;
    }
    @Override
    public NodeInformation getNodeInformation() {
        return selfInformation;
    }
    @Override
    public NProperties getNProperties() {
        return properties;
    }
}
