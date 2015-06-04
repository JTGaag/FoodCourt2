package com.aj.particlefilter;

/**
 * Created by Joost on 04/06/2015.
 */
public class GyroData {
    double zRot; //Rotation arround zz-axis in deg/s
    long timestamp; //Timestamp of datapoint

    /**
     * Constructor of gyro data [Radians as input]
     * @param zRot zRot in rad/s
     * @param timestamp timestamp of data
     */
    public GyroData(double zRot, long timestamp) {
        this.zRot = zRot *180 / Math.PI;
        this.timestamp = timestamp;
    }

    /**
     * Get rotation around z-axis in degree/s
     * @return Rotation around z-axis in degree/s
     */
    public double getZRot() {
        return zRot;
    }

    /**
     * Get Timestamp
     * @return Timestamp of data point
     */
    public long getTimestamp() {
        return timestamp;
    }
}
