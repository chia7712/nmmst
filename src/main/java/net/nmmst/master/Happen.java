package net.nmmst.master;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.nmmst.player.NodeInformation;
import net.nmmst.register.RegisterClient;
import net.nmmst.request.Request;
import net.nmmst.tools.Ports;
import net.nmmst.tools.SerialStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class Happen {
    private Happen(){}
    private static final Logger LOG = LoggerFactory.getLogger(Happen.class);  
    private static final long GRAY_SCREEN_MICRTIME = (2 * 60 + 37) * 1000 * 1000;//micrs
    private static final long MOVIE_TIME_1 = (5 * 60 + 19) * 1000 * 1000;
    private static final long MOVIE_TIME_2 = (0 * 60 + 58) * 1000 * 1000;
    private static final long MOVIE_TIME_3 = (4 * 60 + 19) * 1000 * 1000;
    private static final long MOVIE_TIME_4 = (0 * 60 + 34) * 1000 * 1000;
    private static final long MOVIE_TIME_5 = (3 * 60 + 16) * 1000 * 1000;
    private static final long SUBMINE_GONE = (1 * 60 + 53) * 1000 * 1000;//MOVIE_TIME_5 - (45 * 1000 * 1000);
    private static final boolean[]  values = {
        true,
        true,
        false,
        true,
        true,
        false,
        true
    };
    public static void setValues(int index, boolean value) {
        if (index >= values.length) {
            return;
        }
        values[index] = value;
        if (LOG.isDebugEnabled()) {
            LOG.debug(index + ", " + value);
        }
    }
    private static void initValues() {
//        values[0] = true;
//        values[1] = true;
//        values[2] = false;
//        values[3] = true;
//        values[4] = true;
//        values[5] = false;
//        values[6] = true;
    }
    private static void start0(RegisterClient server, DioInterface dio) throws InterruptedException {
        final long startTime = System.currentTimeMillis();
        dio.light(0);
//        DONT TOUCH GRAY
        TimeUnit.MICROSECONDS.sleep(GRAY_SCREEN_MICRTIME);
        dio.grayUptoEnd();
        final long spendTime = System.currentTimeMillis() - startTime;
        TimeUnit.MICROSECONDS.sleep(MOVIE_TIME_1 - (spendTime * 1000));
    }
    private static void start1(RegisterClient server, DioInterface dio) throws InterruptedException {
        final long startTime = System.currentTimeMillis();
        if (values[1]) {
            dio.light(1);
        } else if (values[2]) {
            dio.light(2);
        } else {
            dio.light(1);
        }
        final long spendTime = System.currentTimeMillis() - startTime;
        TimeUnit.MICROSECONDS.sleep(MOVIE_TIME_2 - (spendTime * 1000));
    }
    private static void start2(RegisterClient server, DioInterface dio) throws InterruptedException {
        final long startTime = System.currentTimeMillis();
        dio.light(3);
        final long spendTime = System.currentTimeMillis() - startTime;
        TimeUnit.MICROSECONDS.sleep(MOVIE_TIME_3 - (spendTime * 1000));
    }
    private static void start3(RegisterClient server, DioInterface dio) throws InterruptedException {
        final long startTime = System.currentTimeMillis();
        if (values[4]) {
            dio.light(4);
        } else if (values[5]) {
            dio.light(5);
        } else {
            dio.light(4);
        }
        final long spendTime = System.currentTimeMillis() - startTime;
        TimeUnit.MICROSECONDS.sleep(MOVIE_TIME_4 - (spendTime * 1000));
    }
    private static void start4(RegisterClient server, DioInterface dio) throws InterruptedException {
        final long startTime = System.currentTimeMillis();
        dio.light(6);
        TimeUnit.MICROSECONDS.sleep(SUBMINE_GONE);
        dio.submarineGotoEnd();
        final long spendTime = System.currentTimeMillis() - startTime;
        TimeUnit.MICROSECONDS.sleep(MOVIE_TIME_5 - (spendTime * 1000));
        dio.lightOff();
    }
    public static boolean start(List<NodeInformation> nodeInformations, RegisterClient server, DioInterface dio) throws IOException, InterruptedException {
        try {
            if (!server.isBuffered()) {
                return false;
            }
            if (!SerialStream.sendAll(nodeInformations, new Request(Request.Type.START), Ports.REQUEST_OTHERS.get())) {
                return false;
            }
            if (values[0]) {
                start0(server, dio);
            }
            if (values[1] || values[2]) {
                start1(server, dio);
            }
            if (values[3]) {
                start2(server, dio);
            }
            if (values[4] || values[5]) {
                start3(server, dio);
            }
            if (values[6]) {
                start4(server, dio);
            }
            return true;
        } finally {
            initValues();
        }
    }
}
