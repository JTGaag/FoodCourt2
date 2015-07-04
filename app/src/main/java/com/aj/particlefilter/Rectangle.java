package com.aj.particlefilter;

/**
 * Created by Alexander on 13-5-2015.
 * The map is divided into several rectangles to make particle distribution easier.
 */



public class Rectangle {

    double height, width;
    double x, y; //x to the left, y down
    double area;
    double areaWeight;
    String rectangleName = "Unkown";

    /**
     *
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public Rectangle(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.area = width * height;
    }

    public Rectangle(double x, double y, double width, double height, String rectangleName) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.rectangleName = rectangleName;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
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

    public double getArea() {
        return area;
    }

    public double getAreaWeight() {
        return areaWeight;
    }

    public void setAreaWeight(double areaWeight) {
        this.areaWeight = areaWeight;
    }

    public String getRectangleName() {
        return rectangleName;
    }

    public void setRectangleName(String rectangleName) {
        this.rectangleName = rectangleName;
    }
}
