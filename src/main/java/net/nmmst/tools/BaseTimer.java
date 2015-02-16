/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.nmmst.tools;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class BaseTimer implements Timer {
    private final TimeUnit timeUnit;
    private final int sleepTime;
    public BaseTimer(int sleepTime, TimeUnit timeUnit) {
        this(timeUnit, sleepTime);
    }
    public BaseTimer(TimeUnit timeUnit, int sleepTime) {
        this.timeUnit = timeUnit;
        this.sleepTime = sleepTime;
    }
    @Override
    public void sleep() throws InterruptedException {
        timeUnit.sleep(sleepTime);
    }
}
