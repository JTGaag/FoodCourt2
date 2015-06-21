package com.aj.particlefilter;

/**
 * Created by Joost on 21/06/2015.
 */
public class TimePositionData {
    double xPosition, yPosition;
    long timestamp;

    public TimePositionData(long timestamp, double xPosition, double yPosition) {
        this.timestamp = timestamp;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
    }

    public double getxPosition() {
        return xPosition;
    }

    public void setxPosition(double xPosition) {
        this.xPosition = xPosition;
    }

    public double getyPosition() {
        return yPosition;
    }

    public void setyPosition(double yPosition) {
        this.yPosition = yPosition;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
