package com.aj.particlefilter;

/**
 * Created by Joost on 04/06/2015.
 */
public interface MotionListener {
    /**
     * Called when straight motion has ended or specific time of time has passed.
     * This method will start the motion in the particle manager
     * @param direction direction of detected motion
     * @param distance distance of detected motion
     */
    public void onMotion(double direction, double distance, long timestamp);

    public void onWifiCheck(long timestamp);

}
