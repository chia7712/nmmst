/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.nmmst.processor;

import java.util.List;

/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public interface TimeFrameProcessor extends FrameProcessor {
    public void setTimeLocation(List<TimeLocation> timeLocations);
}
