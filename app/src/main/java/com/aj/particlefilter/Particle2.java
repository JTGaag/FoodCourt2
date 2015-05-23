package com.aj.particlefilter;

/**
 * Created by Alexander on 12-5-2015.
 */
public class Particle2 {

    double x, y, oldX, oldY, direction, id, parent, distance;
    boolean destroyed = false;


    /**
     *
     * @param xvalue
     * @param yvalue
     */

    public Particle2(double xvalue, double yvalue) {

        this.x = xvalue;
        this.y = yvalue;
        this.oldX = xvalue;
        this.oldY = yvalue;

    }


    public Particle2(double xvalue, double yvalue, double direction, double id, double distance, double parent){

        this.x = xvalue;
        this.y = yvalue;
        this.direction = direction;
        this.id = id;

    }
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }
    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getDirection() {
        return direction;
    }

    public void setDirection(double direction) {
        this.direction = direction;
    }

    public double getId() {
        return id;
    }

    public void setId(double id) {
        this.id = id;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void destroy(){
        this.destroyed = true;
    }

    public void activate() { this.destroyed = false;}

    public boolean isDestroyed(){
        return destroyed;
    }

    public void setOldPosition(){
        oldX = x;
        oldY = y;
    }

    public double getOldX() {
        return oldX;
    }

    public double getOldY() {
        return oldY;
    }
}