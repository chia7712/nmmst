/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.nmmst.processor;

import net.nmmst.movie.Frame;

/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class TimeLocation {
    protected final int index;
    protected final long minMicroTime;
    protected final long maxMicroTime;
    public static TimeLocation reverse(TimeLocation location) {
        return new TimeLocationReverse(location);
    }
    public TimeLocation(int index) {
        this(index, 0, Long.MAX_VALUE);
    }
    public TimeLocation(int index, long minMicroTime, long maxMicroTime) {
        this.index = index;
        this.minMicroTime = Math.min(minMicroTime, maxMicroTime);
        this.maxMicroTime = Math.max(minMicroTime, maxMicroTime);
    }
    public boolean include(Frame frame) {
        return frame.getMovieAttribute().getIndex() == getIndex() && (frame.getTimestamp() >= minMicroTime) && (frame.getTimestamp() <= maxMicroTime);
    }
    public long getMaxMicroTime() {
        return maxMicroTime;
    }
    public long getMinMicroTime() {
        return minMicroTime;
    }
    public int getIndex() {
        return index;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof TimeLocation) {
            TimeLocation other = (TimeLocation)obj;
            return getIndex() == other.getIndex() && getMinMicroTime() == other.getMinMicroTime() && getMaxMicroTime() == other.getMaxMicroTime();
        }
        return false;
    }
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.index;
        hash = 97 * hash + (int) (this.minMicroTime ^ (this.minMicroTime >>> 32));
        hash = 97 * hash + (int) (this.maxMicroTime ^ (this.maxMicroTime >>> 32));
        return hash;
    }
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        return buffer.append("include : ")
              .append(index)
              .append(minMicroTime)
              .append(maxMicroTime).toString();
    }
    private static class TimeLocationReverse extends TimeLocation {
        public TimeLocationReverse(TimeLocation location) {
            super(location.getIndex(), location.getMinMicroTime(), location.getMaxMicroTime());
        }
        @Override
        public boolean include(Frame frame) {
            return !super.include(frame);
        } 
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj instanceof TimeLocationReverse) {
                TimeLocationReverse other = (TimeLocationReverse)obj;
                return getIndex() == other.getIndex() && getMinMicroTime() == other.getMinMicroTime() && getMaxMicroTime() == other.getMaxMicroTime();
            }
            return false;
        }
        @Override
        public int hashCode() {
            return super.hashCode();
        }
        @Override
        public String toString() {
            StringBuilder buffer = new StringBuilder();
            return buffer.append("exclude : ")
                  .append(index)
                  .append(minMicroTime)
                  .append(maxMicroTime).toString();
        }
    }
}
