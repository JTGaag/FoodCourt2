package com.aj.queuing;

/**
 * Created by Joost on 25/04/2015.
 * This object stores all the raw data gathered from the accelerometer, gravity and (light) sensors
 * This data object will be used in the QueuingDataHandler object to store the data and do calculations on it
 */
public class QueuingSensorData {

    //Raw Data
    private long timestamp;
    private double accelerationX, accelerationY, accelerationZ;
    private double gravityX, gravityY, gravityZ;
    private double lightLevel;
    private double proximityLevel;

    //Directly calculated data
    private double magnitudeGravity;
    private double gravityDotProduct;

    //Analysed data
    private int gravityShift = 1;
    private int maximumDetected = 0; //Placeholder for actual step calculating part
    private int lightLevelShift = 0; //Not implemented yet, but will be later

    //Disturption or noise booleans
    boolean pocketDisturbing = false;
    boolean noStepNoise = false;

    /*
    StepIdentifier is a variable that indicated if this datapoint is a special datapoint for the step analysis
    value:
        0: no special datapoint
        1: Datapoint is first peek in a step
        2: Datapoint is the maximum peek in a step
        3: Datapoint is the minimum valley in the step, after the maximum peek
     */
    private int stepIdentifier = 0;

    public QueuingSensorData(long timestamp, double accelerationX, double accelerationY, double accelerationZ, double gravityX, double gravityY, double gravityZ, double lightLevel, double proximityLevel) {
        this.timestamp = timestamp;
        this.accelerationX = accelerationX;
        this.accelerationY = accelerationY;
        this.accelerationZ = accelerationZ;
        this.gravityX = gravityX;
        this.gravityY = gravityY;
        this.gravityZ = gravityZ;
        this.lightLevel = lightLevel;
        magnitudeGravity = Math.sqrt(gravityX*gravityX + gravityY*gravityY + gravityZ*gravityZ);
        gravityDotProduct = (accelerationX*gravityX + accelerationY*gravityY + accelerationZ*gravityZ)/magnitudeGravity;
    }

    public QueuingSensorData(long timestamp, double accelerationX, double accelerationY, double accelerationZ, double gravityX, double gravityY, double gravityZ) {
        this.timestamp = timestamp;
        this.accelerationX = accelerationX;
        this.accelerationY = accelerationY;
        this.accelerationZ = accelerationZ;
        this.gravityX = gravityX;
        this.gravityY = gravityY;
        this.gravityZ = gravityZ;
        this.lightLevel = 0.0;magnitudeGravity = Math.sqrt(gravityX*gravityX + gravityY*gravityY + gravityZ*gravityZ);
        gravityDotProduct = (accelerationX*gravityX + accelerationY*gravityY + accelerationZ*gravityZ)/magnitudeGravity;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getAccelerationX() {
        return accelerationX;
    }

    public void setAccelerationX(double accelerationX) {
        this.accelerationX = accelerationX;
    }

    public double getAccelerationY() {
        return accelerationY;
    }

    public void setAccelerationY(double accelerationY) {
        this.accelerationY = accelerationY;
    }

    public double getAccelerationZ() {
        return accelerationZ;
    }

    public void setAccelerationZ(double accelerationZ) {
        this.accelerationZ = accelerationZ;
    }

    public double getGravityX() {
        return gravityX;
    }

    public void setGravityX(double gravityX) {
        this.gravityX = gravityX;
    }

    public double getGravityY() {
        return gravityY;
    }

    public void setGravityY(double gravityY) {
        this.gravityY = gravityY;
    }

    public double getGravityZ() {
        return gravityZ;
    }

    public void setGravityZ(double gravityZ) {
        this.gravityZ = gravityZ;
    }

    public double getLightLevel() {
        return lightLevel;
    }

    public void setLightLevel(double lightLevel) {
        this.lightLevel = lightLevel;
    }

    public double getMagnitudeGravity() {
        return magnitudeGravity;
    }

    public void setMagnitudeGravity(double magnitudeGravity) {
        this.magnitudeGravity = magnitudeGravity;
    }

    public double getGravityDotProduct() {
        return gravityDotProduct;
    }

    public void setGravityDotProduct(double gravityDotProduct) {
        this.gravityDotProduct = gravityDotProduct;
    }

    public int getGravityShift() {
        return gravityShift;
    }

    public void setGravityShift(int gravityShift) {
        this.gravityShift = gravityShift;
    }

    public int getMaximumDetected() {
        return maximumDetected;
    }

    public void setMaximumDetected(int maximumDetected) {
        this.maximumDetected = maximumDetected;
    }

    public int getLightLevelShift() {
        return lightLevelShift;
    }

    public void setLightLevelShift(int lightLevelShift) {
        this.lightLevelShift = lightLevelShift;
    }

    public int getStepIdentifier() {
        return stepIdentifier;
    }

    public void setStepIdentifier(int stepIdentifier) {
        this.stepIdentifier = stepIdentifier;
    }

    public double getProximityLevel() {
        return proximityLevel;
    }

    public void setProximityLevel(double proximityLevel) {
        this.proximityLevel = proximityLevel;
    }

    public void setPocketDisturbing(boolean pocketDisturbing) {
        this.pocketDisturbing = pocketDisturbing;
    }

    public void setNoStepNoise(boolean noStepNoise) {
        this.noStepNoise = noStepNoise;
    }

    public boolean isPocketDisturbing() {
        return pocketDisturbing;
    }

    public boolean isNoStepNoise() {
        return noStepNoise;
    }

    @Override
    public String toString() {
        return "QueuingSensorData{" +
                "timestamp=" + timestamp +
                ", accelerationX=" + accelerationX +
                ", accelerationY=" + accelerationY +
                ", accelerationZ=" + accelerationZ +
                ", gravityX=" + gravityX +
                ", gravityY=" + gravityY +
                ", gravityZ=" + gravityZ +
                ", lightLevel=" + lightLevel +
                ", magnitudeGravity=" + magnitudeGravity +
                ", gravityDotProduct=" + gravityDotProduct +
                ", gravityShift=" + gravityShift +
                ", maximumDetected=" + maximumDetected +
                ", lightLevelShift=" + lightLevelShift +
                ", stepIdentifier=" + stepIdentifier +
                '}';
    }
}
