package tw.gov.nmmst.controller;

import java.util.concurrent.TimeUnit;

import Automation.BDaq.DeviceInformation;
import Automation.BDaq.InstantDoCtrl;
import java.io.IOException;
import tw.gov.nmmst.NConstants;
import tw.gov.nmmst.NProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Controls the digital Input/Output.
 * The default corresponding device includes the PCI_1735U adn PCI_1739U.
 */
public final class DioBdaq implements DioInterface {
    /**
     * Log.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DioBdaq.class);
    /**
     * Dio 1735u.
     */
    private final InstantDoCtrl pci1735u = new InstantDoCtrl();
    /**
     * Dio 1739u.
     */
    private final InstantDoCtrl pci1739u = new InstantDoCtrl();
    /**
     * Constructs a DioBdaq with pci 1735u and 1739u.
     * @param properties DioBdaq
     * @throws Exception If failed to control the dio
     */
    public DioBdaq(final NProperties properties) throws Exception {
        try {
            pci1735u.setSelectedDevice(new DeviceInformation(
                    properties.getString(NConstants.PCI_1735U_NAME)));
            pci1739u.setSelectedDevice(new DeviceInformation(
                    properties.getString(NConstants.PCI_1739U_NAME)));
        } catch (Exception | UnsatisfiedLinkError e) {
            LOG.error(e.getMessage());
            pci1735u.Cleanup();
            pci1739u.Cleanup();
            throw new Exception(e);
        }
    }
    @Override
    public void submarineGotoEnd() throws InterruptedException {
        try {
            pci1739u.Write(0, (byte) 0xbf);
            TimeUnit.MILLISECONDS.sleep(600);
            pci1739u.Write(0, (byte) 0xff);
            TimeUnit.MILLISECONDS.sleep(600);
            pci1739u.Write(0, (byte) 0xdf);
            TimeUnit.MILLISECONDS.sleep(600);
            pci1739u.Write(0, (byte) 0xff);
            TimeUnit.MILLISECONDS.sleep(1000);
            pci1739u.Write(0, (byte) 0xfd);
            TimeUnit.MILLISECONDS.sleep(500);
            pci1739u.Write(0, (byte) 0xed);
            TimeUnit.SECONDS.sleep(20);
            pci1739u.Write(0, (byte) 0xff);
            TimeUnit.MILLISECONDS.sleep(600);
            pci1739u.Write(0, (byte) 0xf5);
            TimeUnit.SECONDS.sleep(20);
        } finally {
            submarineStop();
        }
    }
    @Override
    public void stoneGotoRight() throws InterruptedException {
        pci1735u.Write(0, (byte) 0x0A);
        TimeUnit.SECONDS.sleep(35);
        pci1735u.Write(0, (byte) 0x00);
    }
    @Override
    public void stoneGotoLeft() throws InterruptedException {
        pci1735u.Write(0, (byte) 0x05);
        TimeUnit.SECONDS.sleep(35);
        pci1735u.Write(0, (byte) 0x00);
    }
    @Override
    public void initializeSubmarineAndGray() throws InterruptedException {
        try {
            submarineBackardAndUp();
            grayDown();
            TimeUnit.SECONDS.sleep(60);
        } finally {
            submarineStop();
            grayStop();
            TimeUnit.MILLISECONDS.sleep(600);
            pci1739u.Write(0, (byte) 0xbf);
            TimeUnit.MILLISECONDS.sleep(600);
            pci1739u.Write(0, (byte) 0xff);
            TimeUnit.MILLISECONDS.sleep(600);
            pci1739u.Write(0, (byte) 0xdf);
            TimeUnit.MILLISECONDS.sleep(600);
            pci1739u.Write(0, (byte) 0xff);
        }
    }
    @Override
    //PCI-1739U CN1 11 light-off
    public void lightOff() throws InterruptedException {
        pci1739u.Write(1, (byte) 0xF7);
        TimeUnit.MILLISECONDS.sleep(500);
        pci1739u.Write(1, (byte) 0xFF);
    }
    @Override
    public void lightWork()throws InterruptedException {
        pci1739u.Write(2, (byte) 0xEF);
        TimeUnit.MILLISECONDS.sleep(500);
        pci1739u.Write(2, (byte) 0xFF);
    }
    @Override
    public void grayUptoEnd() throws InterruptedException {
        grayPowerOn();
        pci1735u.Write(0, (byte) 0x10);
        int countTime = 0;
        try {
            while (countTime <= 45) {
                TimeUnit.SECONDS.sleep(10);
                countTime += 10;
            }
        } finally {
            grayStop();
        }
    }
    /**
     * PCI-1739U CN1 21 light-party-1.
     * @throws InterruptedException If anyone breaks up the sleep
     */
    @Override
    public void lightParty1()throws InterruptedException {
        final int sleepTime = 500;
        pci1739u.Write(2, (byte) 0xDF);
        TimeUnit.MILLISECONDS.sleep(sleepTime);
        pci1739u.Write(2, (byte) 0xFF);
    }
    /**
     * PCI-1739U CN1 21 light-party-2.
     * @throws InterruptedException If anyone breaks up the sleep
     */
    @Override
    public void lightParty2()throws InterruptedException {
        final int sleepTime = 500;
        pci1739u.Write(2, (byte) 0xBF);
        TimeUnit.MILLISECONDS.sleep(sleepTime);
        pci1739u.Write(2, (byte) 0xFF);
    }
    @Override
    public void light(final int mode) throws InterruptedException {
        final int sleepTime = 500;
        switch (mode) {
            case 0://PCI-1739U CN1 12 one-movie
                pci1739u.Write(1, (byte) 0xEF);
                TimeUnit.MILLISECONDS.sleep(sleepTime);
                pci1739u.Write(1, (byte) 0xFF);
                break;
            case 1://PCI-1739U CN1 13 two-movie
                pci1739u.Write(1, (byte) 0xDF);
                TimeUnit.MILLISECONDS.sleep(sleepTime);
                pci1739u.Write(1, (byte) 0xFF);
                break;
            case 2://PCI-1739U CN1 14 three-movie
                pci1739u.Write(1, (byte) 0xBF);
                TimeUnit.MILLISECONDS.sleep(sleepTime);
                pci1739u.Write(1, (byte) 0xFF);
                break;
            case 3://PCI-1739U CN1 15 four-movie
                pci1739u.Write(1, (byte) 0x7F);
                TimeUnit.MILLISECONDS.sleep(sleepTime);
                pci1739u.Write(1, (byte) 0xFF);
                break;
            case 4://PCI-1739U CN1 16 five-movie
                pci1739u.Write(2, (byte) 0xFE);
                TimeUnit.MILLISECONDS.sleep(sleepTime);
                pci1739u.Write(2, (byte) 0xFF);
                break;
            case 5://PCI-1739U CN1 17 six-movie
                pci1739u.Write(2, (byte) 0xFD);
                TimeUnit.MILLISECONDS.sleep(sleepTime);
                pci1739u.Write(2, (byte) 0xFF);
                break;
            case 6://PCI-1739U CN1 18 sevent-movie
                pci1739u.Write(2, (byte) 0xFB);
                TimeUnit.MILLISECONDS.sleep(sleepTime);
                pci1739u.Write(2, (byte) 0xFF);
                break;
            default:
                throw new RuntimeException("Error mode of light");
        }
    }
    @Override
    public void close() throws IOException {
        pci1735u.Cleanup();
        pci1739u.Cleanup();
    }
    /**
     * Powers on the gray.
     */
    private void grayPowerOn() {
        pci1735u.Write(1, (byte) 0x80);
    }
    /**
     * Stops the gray.
     */
    private void grayStop() {
        pci1735u.Write(0, (byte) 0x00);
    }
    /**
     * Downs the gray.
     */
    private void grayDown() {
        grayPowerOn();
        pci1735u.Write(0, (byte) 0x20);
    }
    /**
     * Positions the submarine to up-back location.
     */
    private void submarineBackardAndUp() {
        pci1739u.Write(0, (byte) 0xf3);
    }
    /**
     * Stops the submarine.
     */
    private void submarineStop() {
        pci1739u.Write(0, (byte) 0xff);
    }
}
