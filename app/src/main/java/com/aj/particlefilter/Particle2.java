package com.aj.particlefilter;

/**
 * Created by Alexander on 12-5-2015.
 */
public class Particle2 {

    double x, y, oldX, oldY, direction, distance;
    boolean destroyed = false;
    int parent, id;


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
        this.parent = -1;

    }

    public Particle2(Particle2 old){
        this.x = old.getX();
        this.y = old.getY();
        this.parent = old.getParent();
        this.destroyed = old.isDestroyed();
    }


    public Particle2(double xvalue, double yvalue, double direction, int id, double distance, double parent){

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public int getParent() {
        return parent;
    }

    public void setParent(int parent) {
        this.parent = parent;
    }
}