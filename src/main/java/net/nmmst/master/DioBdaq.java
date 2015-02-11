package net.nmmst.master;

import java.util.concurrent.TimeUnit;

import Automation.BDaq.DeviceInformation;
import Automation.BDaq.InstantDoCtrl;
import java.io.IOException;
import net.nmmst.tools.NMConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class DioBdaq implements DioInterface {
    private static final Logger LOG = LoggerFactory.getLogger(DioBdaq.class);  
    private final InstantDoCtrl do_1735U = new InstantDoCtrl();
    private final InstantDoCtrl do_1739U = new InstantDoCtrl();
    public DioBdaq() throws Exception {
        try {
            do_1735U.setSelectedDevice(new DeviceInformation(NMConstants.PCI_1735U));
            do_1739U.setSelectedDevice(new DeviceInformation(NMConstants.PCI_1739U));           
        } catch(Exception e) {
            LOG.error(e.getMessage());
            do_1735U.Cleanup();
            do_1739U.Cleanup();
            throw e;
        }
    }
    @Override
    public void submarineGotoEnd() throws InterruptedException {
        try {
            do_1739U.Write(0, (byte)0xbf);
            TimeUnit.MILLISECONDS.sleep(600);
            do_1739U.Write(0, (byte)0xff);
            TimeUnit.MILLISECONDS.sleep(600);
            do_1739U.Write(0, (byte)0xdf);
            TimeUnit.MILLISECONDS.sleep(600);
            do_1739U.Write(0, (byte)0xff);
            TimeUnit.MILLISECONDS.sleep(1000);
            do_1739U.Write(0, (byte)0xfd);
            TimeUnit.MILLISECONDS.sleep(500);
            do_1739U.Write(0, (byte)0xed);
            TimeUnit.SECONDS.sleep(20);
            do_1739U.Write(0, (byte)0xff);
            TimeUnit.MILLISECONDS.sleep(600);
            do_1739U.Write(0, (byte)0xf5);
            TimeUnit.SECONDS.sleep(20);
        } finally {
            submarineStop();
        }
    }
    @Override
    public void stoneGotoRight() throws InterruptedException {
        do_1735U.Write(0, (byte)0x0A);
        TimeUnit.SECONDS.sleep(35);
        do_1735U.Write(0, (byte)0x00);
    }
    @Override
    public void stoneGotoLeft() throws InterruptedException {
        do_1735U.Write(0, (byte)0x05);
        TimeUnit.SECONDS.sleep(35);
        do_1735U.Write(0, (byte)0x00);
    }
    @Override
    public void initializeSubmarineAndGray() throws InterruptedException {
        try {
            submarineBackardAndUp();
            grayDown();
            TimeUnit.SECONDS.sleep(60);
        } finally {
            submarineStop();
            //don't touch gray
            grayStop();
            TimeUnit.MILLISECONDS.sleep(600);
            do_1739U.Write(0, (byte)0xbf);
            TimeUnit.MILLISECONDS.sleep(600);
            do_1739U.Write(0, (byte)0xff);
            TimeUnit.MILLISECONDS.sleep(600);
            do_1739U.Write(0, (byte)0xdf);
            TimeUnit.MILLISECONDS.sleep(600);
            do_1739U.Write(0, (byte)0xff);
        }
    }
    @Override
    //PCI-1739U	CN1	11 light-off
    public void lightOff() throws InterruptedException {
        do_1739U.Write(1, (byte)0xF7);
        TimeUnit.MILLISECONDS.sleep(500);
        do_1739U.Write(1, (byte)0xFF);
    }
    @Override
    public void lightWork()throws InterruptedException {
        do_1739U.Write(2, (byte)0xEF);
        TimeUnit.MILLISECONDS.sleep(500);
        do_1739U.Write(2, (byte)0xFF);
    }
    @Override
    public void grayUptoEnd() throws InterruptedException {
        grayPowerOn();
        do_1735U.Write(0, (byte)0x10);
        int count_time = 0;
        try {
            while (count_time <= 45) {
                TimeUnit.SECONDS.sleep(10);
                count_time += 10;
            }
        } finally {
            grayStop();   
        }
    }
    @Override
    public void lightParty(int mode)throws InterruptedException {
        final int sleepTime = 500;
        switch(mode) {
            case 1://PCI-1739U	CN1	21 light-party-1
                do_1739U.Write(2, (byte)0xDF);
                TimeUnit.MILLISECONDS.sleep(500);
                do_1739U.Write(2, (byte)0xFF);
                break;
            case 2://PCI-1739U	CN1	21 light-party-2
                do_1739U.Write(2, (byte)0xBF);
                TimeUnit.MILLISECONDS.sleep(500);
                do_1739U.Write(2, (byte)0xFF);
                break;
            default:
                throw new RuntimeException("Error mode of light");
        }
    }
    @Override
    public void light(int mode)throws InterruptedException {
        final int sleepTime = 500;
        switch(mode) {
            case 1://PCI-1739U	CN1	12 one-movie
                do_1739U.Write(1, (byte)0xEF);
                TimeUnit.MILLISECONDS.sleep(sleepTime);
                do_1739U.Write(1, (byte)0xFF);
                break;
            case 2://PCI-1739U	CN1	13 two-movie
                do_1739U.Write(1, (byte)0xDF);
                TimeUnit.MILLISECONDS.sleep(sleepTime);
                do_1739U.Write(1, (byte)0xFF);
                break;
            case 3://PCI-1739U	CN1	14 three-movie
                do_1739U.Write(1, (byte)0xBF);
                TimeUnit.MILLISECONDS.sleep(sleepTime);
                do_1739U.Write(1, (byte)0xFF);
                break;
            case 4://PCI-1739U	CN1	15 four-movie
                do_1739U.Write(1, (byte)0x7F);
                TimeUnit.MILLISECONDS.sleep(sleepTime);
                do_1739U.Write(1, (byte)0xFF);
                break;
            case 5://PCI-1739U	CN1	16 five-movie
                do_1739U.Write(2, (byte)0xFE);
                TimeUnit.MILLISECONDS.sleep(sleepTime);
                do_1739U.Write(2, (byte)0xFF);
                break;
            case 6://PCI-1739U	CN1	17 six-movie
                do_1739U.Write(2, (byte)0xFD);
                TimeUnit.MILLISECONDS.sleep(sleepTime);
                do_1739U.Write(2, (byte)0xFF);
                break;
            case 7://PCI-1739U	CN1	18 sevent-movie
                do_1739U.Write(2, (byte)0xFB);
                TimeUnit.MILLISECONDS.sleep(sleepTime);
                do_1739U.Write(2, (byte)0xFF);
                break;
            default:
                throw new RuntimeException("Error mode of light");
        }
    }
    @Override
    public void close() throws IOException {
        do_1735U.Cleanup();
        do_1739U.Cleanup();
    }
    //0001,0000
    private void grayPowerOn() {
        do_1735U.Write(1, (byte)0x80);
    }
    private void grayStop() {
        do_1735U.Write(0, (byte)0x00);
    }
    private void grayDown() {
        grayPowerOn();
        do_1735U.Write(0, (byte)0x20);
    }
    private void submarineBackardAndUp() {
        do_1739U.Write(0, (byte)0xf3);
    }
    private void submarineStop() {
        do_1739U.Write(0, (byte)0xff);
    }
}
