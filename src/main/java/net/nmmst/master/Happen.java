package net.nmmst.master;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import net.nmmst.player.PlayerInformation;
import net.nmmst.register.RegisterClient;
import net.nmmst.request.Request;
import net.nmmst.tools.Ports;
import net.nmmst.tools.SerialStream;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class Happen {
    private Happen(){}
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
    }
    private static void initValues() {
        values[0] = true;
        values[1] = true;
        values[2] = false;
        values[3] = true;
        values[4] = true;
        values[5] = false;
        values[6] = true;
    }
    private static void start1(RegisterClient server, DioAction dioAction) throws InterruptedException {
        final long startTime = System.currentTimeMillis();
        dioAction.light_1();
        TimeUnit.MICROSECONDS.sleep(GRAY_SCREEN_MICRTIME);
        dioAction.grayUpToEnd();
        final long spendTime = System.currentTimeMillis() - startTime;
        TimeUnit.MICROSECONDS.sleep(MOVIE_TIME_1 - (spendTime * 1000));
    }
    private static void start2(RegisterClient server, DioAction dioAction) throws InterruptedException {
        final long startTime = System.currentTimeMillis();
        if (values[1]) {
            dioAction.light_2();
        } else if (values[2]) {
            dioAction.light_3();
        } else {
            dioAction.light_2();
        }
        final long spendTime = System.currentTimeMillis() - startTime;
        TimeUnit.MICROSECONDS.sleep(MOVIE_TIME_2 - (spendTime * 1000));
    }
    private static void start3(RegisterClient server, DioAction dioAction) throws InterruptedException {
        final long startTime = System.currentTimeMillis();
        dioAction.light_4();
        final long spendTime = System.currentTimeMillis() - startTime;
        TimeUnit.MICROSECONDS.sleep(MOVIE_TIME_3 - (spendTime * 1000));
    }
    private static void start4(RegisterClient server, DioAction dioAction) throws InterruptedException {
        final long startTime = System.currentTimeMillis();
        if (values[4]) {
            dioAction.light_5();
        } else if (values[5]) {
            dioAction.light_6();
        } else {
            dioAction.light_5();
        }
        final long spendTime = System.currentTimeMillis() - startTime;
        TimeUnit.MICROSECONDS.sleep(MOVIE_TIME_4 - (spendTime * 1000));
    }
    private static void start5(RegisterClient server, DioAction dioAction) throws InterruptedException {
        final long startTime = System.currentTimeMillis();
        dioAction.light_7();
        TimeUnit.MICROSECONDS.sleep(SUBMINE_GONE);
        dioAction.submarineFinal();
        final long spendTime = System.currentTimeMillis() - startTime;
        TimeUnit.MICROSECONDS.sleep(MOVIE_TIME_5 - (spendTime * 1000));
        dioAction.light_off();
    }
    public static boolean start(PlayerInformation[] playerInformations, RegisterClient server, DioAction dioAction) throws IOException, InterruptedException {
        try {
            if (!server.isBuffered()) {
                System.out.println("no buffer");
                return false;
            }
            if (!SerialStream.sendAll(playerInformations, new Request(Request.Type.START), Ports.REQUEST.get())) {
                return false;
            }
            start1(server, dioAction);
            start2(server, dioAction);
            start3(server, dioAction);
            start4(server, dioAction);
            start5(server, dioAction);
            return true;
        } finally {
            initValues();
        }
    }
}
