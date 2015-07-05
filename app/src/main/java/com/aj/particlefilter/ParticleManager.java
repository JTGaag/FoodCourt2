package com.aj.particlefilter;

import android.content.Context;
import android.util.Log;

import com.aj.map.CollisionMap;
import com.aj.map.LineSegment;
import com.aj.map.RectangleMap;
import com.aj.wifi.ReturnedWifiPositionData;

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
    final int RADIUS = 4;
    final double PERCENTAGE = 0.80;
    final double DISTRIBUTE_PERCENTAGE = 0.70;
    long saveTimestamp = 0;
    final double DISTRIBUTE_RADIUS = 2.0;

    private ArrayList<Particle2[]> savedData = new ArrayList<Particle2[]>();
    private ArrayList<Long> savedTimestamps = new ArrayList<Long>();

    public ArrayList<double[]> trackedMeanData = new ArrayList<double[]>();




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
        initiateParticlesMap();
        saveParticleData();
    }

    /**
     *
     * @param meanDirection
     * @param directionStd
     * @param meanDistance
     * @param distanceStd
     */
    public void moveAndDistribute(long timestamp, double meanDirection, double directionStd, double meanDistance, double distanceStd){
        this.saveTimestamp = timestamp;
        moveParticles(meanDirection, directionStd, meanDistance, distanceStd);

        redistribute();

        saveParticleData();
        //Log.d("converging", "has Converged: " + hasConverged());
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

        //Particle2[] tempData = savedData.get(savedData.size()-1);

        //count the nr. of active and destroyed particles and find where they are
        for (int i=0; i<nParticles; i++){
            if (particleArray[i].isDestroyed()){
                destroyedParticles++;
                destroyedParticlesArray[j]= i;
                j++;
            }else{
                activeParticles++;
                activeParticlesArray[k] = i;
                particleArray[i].setParent(i);
                k++;
            }
        }
        Log.d("Particle Manager", "Active particles: " + activeParticles + " ActiveParticleNumber: " + activeParticlesArray[0]);


        //give a destroyed particle a random position of an active particle and activate it again.
        for (int i=0; i<destroyedParticles; i++){
            int n = destroyedParticlesArray[i];

            int randomLocation = (int)(Math.random()*activeParticles);
            int l = activeParticlesArray[randomLocation];

            particleArray[n].setX(particleArray[l].getX());
            particleArray[n].setY(particleArray[l].getY());
            particleArray[n].setParent(l);
            particleArray[n].activate();
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
        savedTimestamps.add(saveTimestamp);
    }

    public void initiateParticlesMap(){
        Log.d("Particle manager", "Initate map");
        Random random = new Random();
        ArrayList<Rectangle> rectangles = rectangleMap.getRectangles();
        for(int i=0; i<nParticles; i++){
            Rectangle tempRec = rectangles.get(rectangleMap.getRectangleIndex(random.nextDouble()));
            double tempX = tempRec.getX() + tempRec.getWidth() * random.nextDouble();
            double tempY = tempRec.getY() + tempRec.getHeight() * random.nextDouble();
            particleArray[i] = new Particle2(tempX, tempY);
            particleArray[i].setParent(i);
        }
    }

    public void moveParticles(double meanDirection, double directionStd, double meanDistance, double distanceStd){
        Random random = new Random();
        for(int i=0; i<particleArray.length; i++){
            double angle = random.nextGaussian()*directionStd + meanDirection;
            double distance = random.nextGaussian()*distanceStd + meanDistance;
            angle = angle/180*Math.PI;
            particleArray[i].setOldPosition();
            particleArray[i].setX(particleArray[i].getX() + Math.cos(angle)*distance);
            particleArray[i].setY(particleArray[i].getY() - Math.sin(angle)*distance);
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
        double r = crossProduct(aTmp.getX2(), aTmp.getY2(), x -line.getX1(), y-line.getY1());
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

    /**
     *
     * @return true if particles have converged enough to determine a valid location.
     */

    public boolean hasConverged(){

        calculateMean();

        int counter = 0;
        int[] particleLocations = new int[nParticles];
        double diffX, diffY, diffRadius,calcPercentage;
        Particle2[] convergedParticleArray = savedData.get(savedData.size()-1);
        double sumX = 0;
        double sumY = 0;
        int j = 0;

        for (int i=0 ; i<nParticles; i++){
            diffX = convergedParticleArray[i].getX() - meanX;
            diffY = convergedParticleArray[i].getY() - meanY;
            diffRadius = Math.sqrt(diffX*diffX + diffY*diffY);

            if (diffRadius <= RADIUS) {
                particleLocations[counter] = i;
                counter++;
            }
        }
        calcPercentage = counter/nParticles;

        if (calcPercentage >= PERCENTAGE){

            for (int i=0; i<counter; i++){
                j = particleLocations[i];
                sumX = sumX + (convergedParticleArray[j].getX()/counter);
                sumX = sumX + (convergedParticleArray[j].getX()/counter);
                meanX = sumX;
                meanY = sumY;
            }


            return true;
        }
        else {
            return false;
        }

    }



    public ArrayList<double[]> backTrack() {

        double trackMean[] = new double[2];
        boolean[] trueParentCopy = new boolean[nParticles];
        for (int i=0; i<nParticles; i++){
            trueParentCopy[i] = true;
        }

        //if (hasConverged()) {
            for (int i = 1; i < savedData.size() - 2; i++) { //the parent of the second iteration is the first iteration
                boolean[] trueParent = new boolean[nParticles];
                int nrTrue = 0;
                Particle2[] trackData = savedData.get(savedData.size()-i);
                trackMean[0] = 0;
                trackMean[1] = 0;
                for (int k=0; k<trackData.length; k++) {
                    Particle2 particle = trackData[k];
                    if (trueParentCopy[k]) {
                        trueParent[particle.getParent()] = true;
                    }
                }
                //deep copy
                for (int l=0;l<trueParentCopy.length; l++ ){
                    trueParentCopy[l] = new Boolean(trueParent[l]);
                }

                Particle2[] meanTrackData = savedData.get(savedData.size()-1-i);
                for (int j = 0; j < nParticles; j++) { //count nr of true particles
                    if (trueParent[j]) {
                        trackMean[0] = trackMean[0] + meanTrackData[j].getX();
                        trackMean[1] = trackMean[1] + meanTrackData[j].getY();
                        nrTrue++;
                    }
                }
                double trackMean2[] = new double[2];
                trackMean2[0] = trackMean[0] / nrTrue;
                trackMean2[1] = trackMean[1] / nrTrue;
                trackedMeanData.add(trackMean2);
                Log.d("nrParticles", "loop nr: " + i + " nrTrue: " + nrTrue);
                //}

            }
        //}
        return trackedMeanData;
    }

    public ArrayList<TimePositionData> backTrack2() {

        ArrayList<TimePositionData> trackedMeanData2 = new ArrayList<TimePositionData>();

        //Make everything true for first round (if particles are converged
        //TODO: change to only use particles that are in 90% of mean or something (when converged)
        boolean[] trueParentCopy = new boolean[nParticles]; //Array to be used to check for parents (will be persistent throughout loops
        for (int i=0; i<nParticles; i++){
            trueParentCopy[i] = true;
        }

        //if (hasConverged()) {
        for (int i = savedData.size()-1; i >= 0; i--) { //Start with last added datas and going back
            /*
            Start with getting all the parents of current step and getting the mean
             */
            boolean[] trueParent = new boolean[nParticles]; //New empty data array
            int nrTrue = 0;
            Particle2[] trackData = savedData.get(i); //Get data from
            double trackMean[] = new double[2];
            trackMean[0] = 0;
            trackMean[1] = 0;
            for (int k=0; k<trackData.length; k++) {
                Particle2 particle = trackData[k];
                if (trueParentCopy[k] && !particle.isDestroyed()) {
                    trueParent[particle.getParent()] = true; //Set parent to true
                    trackMean[0] += particle.getX(); //add x value
                    //Log.d("nrParticles", "loop nr: " + i + " Particle: " + k + " X-value: " + particle.getX() + " Y-value: " + particle.getY());
                    trackMean[1] += particle.getY(); //add y value
                    nrTrue++; //add number of points used
                }
            }

            /*
            Calculate mean and add it to list Array
             */
            double trackMean2[] = new double[2];
            trackMean2[0] = trackMean[0] / nrTrue;
            trackMean2[1] = trackMean[1] / nrTrue;
            TimePositionData dataObj = new TimePositionData(savedTimestamps.get(i), trackMean2[0], trackMean2[1]);
            trackedMeanData2.add(dataObj);

            //deep copy to be used for next itteration round
            for (int l=0;l<trueParentCopy.length; l++ ){
                trueParentCopy[l] = new Boolean(trueParent[l]);
            }

        }
        //}
        return trackedMeanData2;
    }

    public void weightedRedistribute(ArrayList<ReturnedWifiPositionData>  allPositionData ){

        //Clear data
        savedData.clear();
        savedTimestamps.clear();
        trackedMeanData.clear();

        Log.d("Particle manager", "Data size: "+ allPositionData.size());
        double totalWeight = 0;
        particleArray = new Particle2[nParticles];

        double availableParticles =  (nParticles * DISTRIBUTE_PERCENTAGE);
        Log.d("Particle manager", "Available particles: "+ availableParticles);
        double normalParticles = nParticles - availableParticles;
        int particleIndex = 0;
        Random random = new Random();

        for (ReturnedWifiPositionData  weight : allPositionData){
                totalWeight += weight.getCalcDifference();
        }

        for (ReturnedWifiPositionData  data : allPositionData){
            int particleAmount = (int)Math.floor(availableParticles * data.getCalcDifference()/totalWeight);
            //Log.d("Particle manager", "Particle amount: "+ particleAmount);
            for (int i=0;i<particleAmount; i=i ){

                double direction = random.nextDouble() * 2 * Math.PI;
                double radius = random.nextDouble() * DISTRIBUTE_RADIUS;

                double tempX = data.getxPosition() + Math.cos(direction) * radius;
                double tempY = data.getyPosition() + Math.sin(direction) * radius;

                if ( rectangleMap.isPointinRectangle(tempX, tempY) >=0){
                    //Log.d("particle Index", "index: "+particleIndex);
                    particleArray[particleIndex] = new Particle2(tempX, tempY);
                    particleArray[particleIndex].setParent(particleIndex);
                    particleIndex++;
                    i++;
                }

            }
        }

        Log.d("Particla Manager", "ParticleIndex: " + particleIndex);

        ArrayList<Rectangle> rectangles = rectangleMap.getRectangles();
        for(int i=particleIndex; i<nParticles; i++){
            Rectangle tempRec = rectangles.get(rectangleMap.getRectangleIndex(random.nextDouble()));
            double tempX = tempRec.getX() + tempRec.getWidth() * random.nextDouble();
            double tempY = tempRec.getY() + tempRec.getHeight() * random.nextDouble();
            particleArray[i] = new Particle2(tempX, tempY);
            particleArray[i].setParent(i);
        }

        saveParticleData();

    }

}