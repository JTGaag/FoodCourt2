package com.aj.particlefilter;

import android.content.Context;
import android.util.Log;

import com.aj.map.CollisionMap;
import com.aj.map.LineSegment;
import com.aj.map.RectangleMap;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Alexander on 12-5-2015.
 */
public class ParticleManager {

    int nParticles = 100; // nr. of particles
    Particle2 particleArray[];
    RectangleMap rectangleMap;
    CollisionMap collisionMap;
    double meanX = 0.0;
    double meanY = 0.0;
    Context context;

    private ArrayList<Particle2[]> savedData = new ArrayList<Particle2[]>();




    public static final double EPSILON = 0.0000001; //Real small value to check if crossproduct is very small (0 will not work do to rounding errors)

    /**
     *
     * @param nParticles
     * @param rectangleMap
     * @param collisionMap
     */

    public ParticleManager(int nParticles, RectangleMap rectangleMap, CollisionMap collisionMap, Context context) {
        this.nParticles = nParticles;
        particleArray = new Particle2[nParticles];
        this.rectangleMap = rectangleMap;
        this.collisionMap = collisionMap;
        this.context = context;
        if(!this.rectangleMap.isFinalized()){
            this.rectangleMap.assignWeights();
        }
        //initiateParticles();
        initateParticlesMap();
        saveParticleData();
    }

    /**
     *
     * @param meanDirection
     * @param directionStd
     * @param meanDistance
     * @param distanceStd
     */
    public void moveAndDistribute(double meanDirection, double directionStd, double meanDistance, double distanceStd){

        Particle2[] tempData = savedData.get(savedData.size()-1);
//        for(Particle2 par: tempData){
//            Log.d("Saved before move", "Valid: "+!par.isDestroyed());
//        }
        moveParticles(meanDirection, directionStd, meanDistance, distanceStd);

        Particle2[] tempData2 = savedData.get(savedData.size()-1);
//        for(Particle2 par: tempData2){
//            Log.d("Saved before dist", "Valid: "+!par.isDestroyed());
//        }
        redistribute();

        saveParticleData();

    }
    /**
     * resample and redistribute the particles to keep
     */
    public void redistribute(){
        int activeParticles = 0;
        int[] activeParticlesArray = new int[nParticles];
        int destroyedParticles = 0;
        int[] destroyedParticlesArray = new int[nParticles];
        int j = 0;
        int k =0;

        Particle2[] tempData = savedData.get(savedData.size()-1);

        //count the nr. of active and destroyed particles and find where they are
        for (int i=0; i<nParticles; i++){
            if (particleArray[i].isDestroyed()){
                destroyedParticles++;
                destroyedParticlesArray[j]= i;
                j++;
            }else{
                activeParticles++;
                activeParticlesArray[k] = i;
                k++;
            }
        }
        Log.d("Particle Manager", "Active particles: " + activeParticles + " ActiveParticleNumber: " + activeParticlesArray[0]);

//        //                destroyedParticles++;
////                destroyedParticlesArray[j]= i;
////                j++;
//        int randomLocation = (int)(Math.random()*nParticles);
//
//        //Log.d("Random", "x: " + randomLocation);
//        //Log.d("Valid random point", "valid: "+!tempData[randomLocation].isDestroyed());
//
//        particleArray[i].setX(tempData[randomLocation].getX());
//        particleArray[i].setY(tempData[randomLocation].getY());
//        particleArray[i].setParent(randomLocation);
//        particleArray[i].activate();

        //give a destroyed particle a random position of an active particle and activate it again.
        for (int i=0; i<destroyedParticles; i++){
            j = destroyedParticlesArray[i];

            int randomLocation = (int)(Math.random()*activeParticles);
            int l = activeParticlesArray[randomLocation];

            particleArray[j].setX(particleArray[l].getX());
            particleArray[j].setY(particleArray[l].getY());
            particleArray[j].setParent(l);
            particleArray[j].activate();
        }
    }

    /**
     *
     */
    public void saveParticleData(){
        Particle2 toSave[] = new Particle2[particleArray.length];
        for(int i=0; i<particleArray.length; i++){
            toSave[i] = new Particle2(particleArray[i]);
        }

        savedData.add(toSave);

//        for(Particle2 par: toSave){
//            Log.d("Save data", "Valid: "+!par.isDestroyed());
//        }

        /*
        int iets = (savedData.size()-1);
        Particle2[] tempArray = savedData.get(iets);
        tempArray[0].getX();
        savedData.get(0);
        */
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

            //Set destroyed particles
            if(!particleArray[i].isDestroyed()){
                for(LineSegment line: collisionMap.getLineSegments()){
                    if(doLinesIntersect(moveLine, line)){
                        particleArray[i].destroy();
                    }
                }
            }
        }
    }

    public Particle2[] getParticleArray() {
        return savedData.get(savedData.size()-1);
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



    /*
    Stuff for covergent detection

     */

    public void calculateMean(){
        double sumX = 0;
        double sumY = 0;
        double number = 0;
        for(Particle2 particle: particleArray){
            if(!particle.isDestroyed()) {
                sumX += particle.getX();
                sumY += particle.getY();
                number++;
            }
        }
        if(number==0){number = 1;}
        meanX = sumX / number;
        meanY = sumY / number;
    }


    public double getMeanX() {
        return meanX;
    }

    public double getMeanY() {
        return meanY;
    }
}