package com.aj.map;

import com.aj.particlefilter.Rectangle;

import java.util.ArrayList;

/**
 * Created by Joost on 19/05/2015.
 */
public class RectangleMap {
    ArrayList<Rectangle> rectangles = new ArrayList<Rectangle>();
    boolean finalized = false;
    double weightSum[];

    public RectangleMap(ArrayList<Rectangle> rectangles) {
        this.rectangles = rectangles;
    }

    public RectangleMap() {
    }

    public void addRectangle(Rectangle rectangle){
        if(!finalized) {
            this.rectangles.add(rectangle);
        }
    }

    public double totalArea(){
        double totalArea = 0;
        for(Rectangle rec : rectangles){
            totalArea+=rec.getArea();
        }
        return totalArea;
    }

    public void assignWeights(){
        double totalArea = totalArea();
        weightSum = new double[rectangles.size()];

        for(int i=0; i<rectangles.size(); i++){
            Rectangle rec = rectangles.get(i);
            double weight = rec.getArea()/totalArea;
            rec.setAreaWeight(weight);
            rectangles.set(i, rec);

            //Set weightSUm
            if(i>0){
                weightSum[i] = weight + weightSum[i-1];
            }else{
                weightSum[i] = weight;
            }
            if(i==rectangles.size()-1){
                weightSum[i] = 1;
            }
        }
        finalized = true;
    }

    public int getRectangleIndex(double randomValue){
        int index = 0;
        if(!finalized){ return 0;}
        for(int i=0; i<weightSum.length; i++){
            if(randomValue<=weightSum[i]){
                index = i;
                break;
            }
        }
        return index;
    }

    public ArrayList<Rectangle> getRectangles() {
        return rectangles;
    }

    public boolean isFinalized() {
        return finalized;
    }
}