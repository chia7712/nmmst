package tw.gov.nmmst.controller;

import java.util.concurrent.TimeUnit;

import Automation.BDaq.DeviceInformation;
import Automation.BDaq.InstantDoCtrl;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import tw.gov.nmmst.NConstants;
import tw.gov.nmmst.NProperties;
/**
 * Controls the digital Input/Output.
 * The default corresponding device includes the PCI_1735U adn PCI_1739U.
 */
public final class DioBdaq implements DioInterface {
    /**
     * Log.
     */
    private static final Log LOG
            = LogFactory.getLog(DioBdaq.class);
    /**
     * Dio 1735u.
     */
    private final InstantDoCtrl pci1735u = new InstantDoCtrl();
    /**
     * Dio 1739u.
     */
    private final InstantDoCtrl pci1739u = new InstantDoCtrl();
    /**
     * The submarine start time.
     */
    private final long submarineStart;
    /**
     * The submarine end time.
     */
    private final long submarineEnd;
    /**
     * The time for waiting the submarine to init.
     */
    private final long elapsedInit;
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
            submarineStart = properties.getLong(
                    NConstants.MASTER_SUBMARINE_START);
            submarineEnd = properties.getLong(
                    NConstants.MASTER_SUBMARINE_END);
            elapsedInit = properties.getLong(
                    NConstants.ELAPSED_INIT_SUBMARINE);
        } catch (Exception | UnsatisfiedLinkError e) {
            LOG.error(e);
            pci1735u.Cleanup();
            pci1739u.Cleanup();
            throw new Exception(e);
        }
    }
    @Override
    public void submarineGotoEnd() throws InterruptedException {
        //submarine light on
        pci1739u.Write(0, (byte) 0x9f);
        TimeUnit.MILLISECONDS.sleep(600);
        pci1739u.Write(0, (byte) 0xff);
        TimeUnit.MILLISECONDS.sleep(600);
        //submarine forward
        pci1739u.Write(0, (byte) 0xfd);
        TimeUnit.SECONDS.sleep(5);
        pci1739u.Write(0, (byte) 0xff);
        TimeUnit.MILLISECONDS.sleep(600);
        //submarine down and forward
        pci1739u.Write(0, (byte) 0xed);
        TimeUnit.SECONDS.sleep(15);
        pci1739u.Write(0, (byte) 0xff);
        TimeUnit.MILLISECONDS.sleep(600);
        //submarine up and forward
        pci1739u.Write(0, (byte) 0xf5);
        TimeUnit.SECONDS.sleep(20);
        pci1739u.Write(0, (byte) 0xff);
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
    /**
     * None.
     * @throws InterruptedException error
     */
    void submarineGotoForware() throws InterruptedException {
        pci1739u.Write(0, (byte) 0xfd);
        TimeUnit.SECONDS.sleep(40);
        pci1739u.Write(0, (byte) 0xff);
    }
     /**
     * None.
     * @throws InterruptedException error
     */
    void submarineGotoDown() throws InterruptedException {
        pci1739u.Write(0, (byte) 0xef);
        TimeUnit.SECONDS.sleep(40);
        pci1739u.Write(0, (byte) 0xff);
    }
     /**
     * None.
     * @throws InterruptedException error
     */
    void submarineGotoUp() throws InterruptedException {
        pci1739u.Write(0, (byte) 0xf7);
        TimeUnit.SECONDS.sleep(40);
        pci1739u.Write(0, (byte) 0xff);
    }
    @Override
    public void initializeSubmarineAndGray() throws InterruptedException {
        try {
            pci1739u.Write(0, (byte) 0xfb);
            grayDown();
            TimeUnit.SECONDS.sleep(elapsedInit);
        } finally {
            pci1739u.Write(0, (byte) 0xff);
            grayStop();
            TimeUnit.MILLISECONDS.sleep(600);
            //submarine light off
            pci1739u.Write(0, (byte) 0x9f);
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
        final int nanoToMicro = 1000;
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
                final long p6Time = System.nanoTime();
                pci1739u.Write(2, (byte) 0xFB);
                TimeUnit.MILLISECONDS.sleep(sleepTime);
                pci1739u.Write(2, (byte) 0xFF);
                TimeUnit.MICROSECONDS.sleep(submarineStart
                    - (System.nanoTime() - p6Time) / nanoToMicro);
                submarineGotoEnd();
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
}
