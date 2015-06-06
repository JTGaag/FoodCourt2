package com.aj.particlefilter;

/**
 * Created by Joost on 04/06/2015.
 */
public class GyroData {
    private double zRotRate; //Rotation arround zz-axis in deg/s
    private double zRot;
    private long timestamp; //Timestamp of datapoint

    /**
     * Constructor of gyro data [Radians as input]
     * @param zRotRate zRotRate in rad/s
     * @param timestamp timestamp of data
     */
    public GyroData(double zRotRate, long timestamp) {
        this.zRotRate = zRotRate *180 / Math.PI;
        this.timestamp = timestamp;
    }

    /**
     * Get rotation around z-axis in degree/s
     * @return Rotation around z-axis in degree/s
     */
    public double getZRotRate() {
        return zRotRate;
    }

    /**
     * Get Timestamp
     * @return Timestamp of data point
     */
    public long getTimestamp() {
        return timestamp;
    }

    public double getZRot() {
        return zRot;
    }

    public void setZRot(double zRot) {
        this.zRot = zRot;
    }
}
