package com.aj.particlefilter;

/**
 * Created by Joost on 04/06/2015.
 */
public class MagneticData {
    double azimut; //Rotation arround z-axis to magnetic-north [Deg]
    long timestamp;

    /**
     * Constructor for Magnetic data point
     * @param azimut azimut in [rad]
     * @param timestamp timestamp of data point
     */
    public MagneticData(double azimut, long timestamp) {
        this.azimut = azimut * 180 / Math.PI;
        this.timestamp = timestamp;
    }

    /**
     * Get Azimut [deg]
     * @return azimut [deg]
     */
    public double getAzimut() {
        return azimut;
    }

    /**
     * Get Timestamp
     * @return Timestamp of data point
     */
    public long getTimestamp() {
        return timestamp;
    }
}
