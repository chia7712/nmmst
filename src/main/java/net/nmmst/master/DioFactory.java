/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.nmmst.master;

import java.io.IOException;

/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class DioFactory {
    public static DioInterface getDefault() throws Exception {
        return new DioBdaq();
    }
    public static DioInterface getTest() {
        return new DioInterface() {
            @Override
            public void grayUptoEnd() throws InterruptedException {
            }
            @Override
            public void lightWork() throws InterruptedException {
            }
            @Override
            public void light(int mode) throws InterruptedException {
            }
            @Override
            public void lightParty(int mode) throws InterruptedException {
            }
            @Override
            public void lightOff() throws InterruptedException {
            }
            @Override
            public void initializeSubmarineAndGray() throws InterruptedException {
            }
            @Override
            public void submarineGotoEnd() throws InterruptedException {
            }
            @Override
            public void stoneGotoRight() throws InterruptedException {
            }
            @Override
            public void stoneGotoLeft() throws InterruptedException {
            }
            @Override
            public void close() throws IOException {
            }
        };
    }
    private DioFactory(){}
}
