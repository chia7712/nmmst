package net.nmmst.master;

import java.util.concurrent.TimeUnit;

import Automation.BDaq.DeviceInformation;
import Automation.BDaq.InstantDoCtrl;
import net.nmmst.tools.NMConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class DioAction  {
    private static final Logger LOG = LoggerFactory.getLogger(DioAction.class);  
    private final InstantDoCtrl do_1735U = new InstantDoCtrl();
    private final InstantDoCtrl do_1739U = new InstantDoCtrl();
    public DioAction() throws Exception {
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
    public void grayUpToEnd() throws InterruptedException {
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
    public void light_work()throws InterruptedException {
        do_1739U.Write(2, (byte)0xEF);
        TimeUnit.MILLISECONDS.sleep(500);
        do_1739U.Write(2, (byte)0xFF);
    }
    //PCI-1739U	CN1	21 light-party-1
    public void light_party1() throws InterruptedException {
        do_1739U.Write(2, (byte)0xDF);
        TimeUnit.MILLISECONDS.sleep(500);
        do_1739U.Write(2, (byte)0xFF);
    }
    //PCI-1739U	CN1	21 light-party-2
    public void light_party2() throws InterruptedException {
        do_1739U.Write(2, (byte)0xBF);
        TimeUnit.MILLISECONDS.sleep(500);
        do_1739U.Write(2, (byte)0xFF);
    }
    //PCI-1739U	CN1	11 light-off
    public void light_off() throws InterruptedException {
        do_1739U.Write(1, (byte)0xF7);
        TimeUnit.MILLISECONDS.sleep(500);
        do_1739U.Write(1, (byte)0xFF);
    }
    //PCI-1739U	CN1	12 one-movie
    public void light_1() throws InterruptedException {
        do_1739U.Write(1, (byte)0xEF);
        TimeUnit.MILLISECONDS.sleep(500);
        do_1739U.Write(1, (byte)0xFF);
    }
    //PCI-1739U	CN1	13 two-movie
    public void light_2() throws InterruptedException {
        do_1739U.Write(1, (byte)0xDF);
        TimeUnit.MILLISECONDS.sleep(500);
        do_1739U.Write(1, (byte)0xFF);
    }
    //PCI-1739U	CN1	14 three-movie
    public void light_3() throws InterruptedException {
        do_1739U.Write(1, (byte)0xBF);
        TimeUnit.MILLISECONDS.sleep(500);
        do_1739U.Write(1, (byte)0xFF);
    }
    //PCI-1739U	CN1	15 four-movie
    public void light_4() throws InterruptedException {
        do_1739U.Write(1, (byte)0x7F);
        TimeUnit.MILLISECONDS.sleep(500);
        do_1739U.Write(1, (byte)0xFF);
    }
    //PCI-1739U	CN1	16 five-movie
    public void light_5() throws InterruptedException {
        do_1739U.Write(2, (byte)0xFE);
        TimeUnit.MILLISECONDS.sleep(500);
        do_1739U.Write(2, (byte)0xFF);
    }
    //PCI-1739U	CN1	17 six-movie
    public void light_6() throws InterruptedException {
        do_1739U.Write(2, (byte)0xFD);
        TimeUnit.MILLISECONDS.sleep(500);
        do_1739U.Write(2, (byte)0xFF);
    }
    //PCI-1739U	CN1	18 sevent-movie
    public void light_7() throws InterruptedException {
        do_1739U.Write(2, (byte)0xFB);
        TimeUnit.MILLISECONDS.sleep(500);
        do_1739U.Write(2, (byte)0xFF);
    }
    private void submarineBackardAndUp() {
        do_1739U.Write(0, (byte)0xf3);
    }
    private void submarineStop() {
        do_1739U.Write(0, (byte)0xff);
    }
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

    public void submarineFinal() throws InterruptedException {
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
    public void stoneToRight() throws InterruptedException {
        do_1735U.Write(0, (byte)0x0A);
        TimeUnit.SECONDS.sleep(35);
        do_1735U.Write(0, (byte)0x00);
    }
    public void stoneToLeft() throws InterruptedException {
        do_1735U.Write(0, (byte)0x05);
        TimeUnit.SECONDS.sleep(35);
        do_1735U.Write(0, (byte)0x00);
    }
	
}
