package com.aj.particlefilter;

import com.aj.map.CollisionMap;
import com.aj.map.LineSegment;
import com.aj.map.RectangleMap;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Alexander on 12-5-2015.
 */
public class ParticleManager {

    int nParticles = 10000; // nr. of particles
    Particle2[] particleArray;
    RectangleMap rectangleMap;
    CollisionMap collisionMap;

    public static final double EPSILON = 0.0000001; //Real small value to check if crossproduct is very small (0 will not work do to rounding errors)


    public ParticleManager(int nParticles, RectangleMap rectangleMap, CollisionMap collisionMap) {
        this.nParticles = nParticles;
        particleArray = new Particle2[nParticles];
        this.rectangleMap = rectangleMap;
        this.collisionMap = collisionMap;
        if(!this.rectangleMap.isFinalized()){
            this.rectangleMap.assignWeights();
        }
        //initiateParticles();
        initateParticlesMap();
    }

    /**
     * initiate particles
     */
    public void initiateParticles(){

        //random generation of particles
        double xval, yval;
        for (int i = 0; i<nParticles; i=i){
            //random x value between 0 and 20
            xval = Math.random()*20;
            //random y value between 0 and 14
            yval = Math.random()*14;
            //check if random location is valid
           if (checkvallidLocation(xval, yval)){
               particleArray[i].setX(xval);
               particleArray[i].setY(yval);
               i++;
           }
        }

        // initialise Nparticles
        for (int i=0 ; i<nParticles; i++){

        }
    }



    /**
     * resample and redistribute the particles to keep
     */
    public void redistribute(){

        int activeParticles = 0;
        int[] activeParticlesArray = new int[nParticles];
        int destroyedParticles = 0;
        int[] destroyedPariclesArray = new int[nParticles];
        int j = 0;
        int k =0;

        //count the nr. of active and destroyed particles and find where they are
        for (int i=0; i<nParticles;i++){
            if (particleArray[i].isDestroyed()){
                destroyedParticles++;
                destroyedPariclesArray[j]= i;
                j++;
            }else{
                activeParticles++;
                particleArray[i].setParent(i);
                activeParticlesArray[k] = i;
                k++;
            }
        }

        //give a destroyed particle a random position of an active particle and activate it again.
        for (int i=0; i<destroyedParticles; i++){
            j = destroyedPariclesArray[i];
            int randomLocation = (int)(Math.random()*activeParticles);

                int l = activeParticlesArray[randomLocation];
                particleArray[j].setX(particleArray[l].getX());
                particleArray[j].setY(particleArray[l].getY());
                particleArray[j].setParent(l);
                particleArray[j].activate();
            }
    }

    public void redistribute2(){
    }

    /**
     * detect if a particle went through a wall
     * source: http://ericleong.me/research/circle-line/#line-line-intersection
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param x3
     * @param y3
     * @param x4
     * @param y4
     * @return
     */
    public boolean checkLinesCollide(double x1, double y1, double x2, double y2,
                            double x3, double y3, double x4, double y4){
        double A1 = y2-y1;
        double B1 = x1-x2;
        double C1 = A1*x1 + B1*y1;
        double A2 = y4-y3;
        double B2 = x3-x4;
        double C2 = A2*x3 + B2*y3;
        double det = A1*B2-A2*B1;
        if(det > 0.00001 || det < 0.00001) {
            double x = (B2 * C1 - B1 * C2) / det;
            double y = (A1 * C2 - A2 * C1) / det;
            if (x >= Math.min(x1, x2) && x <= Math.max(x1, x2) && x >= Math.min(x3, x4)
                    && x <= Math.max(x3, x4) && y >= Math.min(y1, y2) && y <= Math.max(y1, y2) && y >= Math.min(y3, y4) && y <= Math.max(y3, y4)) {
                return true;
            }
            else {
                return false;
            }
        }
        else { // what to do when det=0?
            return false;
        }
    }


    public boolean DetectCollision(double particlex, double particley){
    //border segments

        for (int i=0;i<nParticles;i++){

        }

        return true;
    }


    /**
     * checks if the randomly generated location is vallid in the given map
     * @param currentx
     * @param currenty
     * @return
     */
    public boolean checkvallidLocation(double currentx, double currenty){

        if ((currentx<8&&currenty<8)
                ||((8<currentx&&currentx<12)&&currenty>6&&currenty<8)
                ||(currentx>12&&currentx<20&&currenty<8)
                ||(currentx>13.5&&currentx<14&&currenty>8&&currenty<11)
                ||(currentx>12&&currentx<16&&currenty>11&&currenty<15)){
            return true;
        }
        else{
            return false;
        }
    }

    public void initateParticlesMap(){
        Random random = new Random();
        ArrayList<Rectangle> rectangles = rectangleMap.getRectangles();
        for(int i=0; i<nParticles; i++){
            Rectangle tempRec = rectangles.get(rectangleMap.getRectangleIndex(random.nextDouble()));
            double tempX = tempRec.getX() + tempRec.getWidth() * random.nextDouble();
            double tempY = tempRec.getY() + tempRec.getHeight() * random.nextDouble();
            particleArray[i] = new Particle2(tempX, tempY);
        }
    }

    public void moveParticles(double meanDirection, double directionStd, double meanDistance, double distanceStd){
        Random random = new Random();
        for(int i=0; i<particleArray.length; i++){
            double angle = random.nextGaussian()*directionStd + meanDirection;
            double distance = random.nextGaussian()*distanceStd + meanDistance;
            angle = angle/180*Math.PI;
            particleArray[i].setOldPosition();
            particleArray[i].setX(particleArray[i].getX() + Math.sin(angle)*distance);
            particleArray[i].setY(particleArray[i].getY() - Math.cos(angle)*distance);
            double x1 = particleArray[i].getOldX();
            double y1 = particleArray[i].getOldY();
            double x2 = particleArray[i].getX();
            double y2 = particleArray[i].getY();
            LineSegment moveLine = new LineSegment(x1, y1, x2, y2);

            //Set destroyed partcles
            if(!particleArray[i].isDestroyed()){
                for(LineSegment line: collisionMap.getLineSegments()){
//                    if(checkLinesCollide(x1, y1, x2, y2, line.getX1(), line.getY1(), line.getX2(), line.getY2())){
//                        particleArray[i].destroy();
//                        //break;
//
//                    }
                    if(doLinesIntersect(moveLine, line)){
                        particleArray[i].destroy();
                    }
                }
            }
        }
    }

    public Particle2[] getParticleArray() {
        return particleArray;
    }



    /*
    Following code is to check for intersection  from: http://martin-thoma.com/how-to-check-if-two-line-segments-intersect/

     */
    public boolean doBoundingBoxesIntersect(LineSegment line1, LineSegment line2){
        return Math.min(line1.getX1(), line1.getX2()) <= Math.max(line2.getX1(), line2.getX2())
                && Math.max(line1.getX1(), line1.getX2()) >= Math.min(line2.getX1(), line2.getX2())
                && Math.min(line1.getY1(), line1.getY2()) <= Math.max(line2.getY1(), line2.getY2())
                && Math.max(line1.getY1(), line1.getY2()) >= Math.min(line2.getY1(), line2.getY2());
    }

    public double crossProduct(double x1, double y1, double x2, double y2){
        return (x1*y2 -x2*y1);
    }

    public boolean isPointOnLine(LineSegment line, double x, double y){
        //Move to origin
        LineSegment aTmp = new LineSegment(0, 0, line.getX2()-line.getX1(), line.getY2()-line.getY1());
        double r = crossProduct(aTmp.getX2(), aTmp.getY2(), x-line.getX1(), y-line.getY1());
        return Math.abs(r) < EPSILON;
    }

    public boolean isPointRightOfLine(LineSegment line, double x, double y){
        //Move to origin
        LineSegment aTmp = new LineSegment(0, 0, line.getX2()-line.getX1(), line.getY2()-line.getY1());
        return crossProduct(aTmp.getX2(), aTmp.getY2(), x - line.getX1(), y - line.getY1()) < 0;
    }

    public boolean linesSegmentTouchesOrCrossesLine(LineSegment a, LineSegment b){
        return isPointOnLine(a, b.getX1(), b.getY1())
                || isPointOnLine(a, b.getX2(), b.getY2())
                || (isPointRightOfLine(a, b.getX1(), b.getY1()) ^ isPointRightOfLine(a, b.getX2(), b.getY2()));
    }

    public boolean doLinesIntersect(LineSegment a, LineSegment b){
        return doBoundingBoxesIntersect(a, b)
                && linesSegmentTouchesOrCrossesLine(a, b)
                && linesSegmentTouchesOrCrossesLine(b, a);
    }
}