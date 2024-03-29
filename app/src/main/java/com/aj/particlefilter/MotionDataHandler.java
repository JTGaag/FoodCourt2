package com.aj.particlefilter;

import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Joost on 04/06/2015.
 */
public class MotionDataHandler {
    final String LOG_TAG = "Motion Data Handler";

    private final static String PREF_NAME = "foodcourtPreferenceFile";
    private final static String STEP_MODE_NAME = "prefStepMode";
    private final static String DEBUG_MODE_NAME = "prefDebugMode";
    private final static String LOCATION_MODE_NAME = "prefLocationMode";
    private final static String LOCATION_MANUAL_NAME = "prefLocationManual";
    private final static String LOCATION_AUTO_NAME = "prefLocationAuto";
    private final static String STRIDE_LENGTH_NAME = "prefStrideLength";
    SharedPreferences settings;

    //ArrayLists to save motion data
    ArrayList<GyroData> gyroDataArrayList = new ArrayList<GyroData>();
    ArrayList<MagneticData> magneticDataArrayList = new ArrayList<MagneticData>();
    ArrayList<Long> timeOfSteps = new ArrayList<Long>();

    //Variables and lists for calculation
    ArrayList<MagneticData> magneticBuffer = new ArrayList<MagneticData>();
    int numberOfStepsInMotion = 0;


    boolean calculationRequest = false;
    long timeForCalculations = 0;
    long lastCalculationTime = 0;
    long newCalculationTime;

    MotionListener motionListener;

    //Constants
    final double NS2S = 1.0/1000000000.0;
    final double TIME_LIMIT = 4.0; //time limit in seconds when new motion needs to ben calculated and updated.
    final double ROTATION_LIMIT = 60.0; // Rotation limit in degrees when motion needs to be updated
    double buildingRotationOffsetDeg = 246.8; //ROtation offset from true north to posiitve x-axis of map (EWI: 176.5; RDW:246.8 (220)) //Smaller = clockwise
    final double BUILDING_ROTATION_OFFSET_DEG_EWI = 175.0; //ROtation offset from true north to posiitve x-axis of map (EWI: 176.5; RDW:246.8 (220))
    final double BUILDING_ROTATION_OFFSET_DEG_RDW = 246.8; //ROtation offset from true north to posiitve x-axis of map (EWI: 176.5; RDW:246.8 (220))
    double distancePerStep = 0.64;
    final int ROTATION_POINTS = 100;

    public MotionDataHandler(MotionListener motionListener, SharedPreferences settings) {
        this.motionListener = motionListener;
        this.settings = settings;
        distancePerStep = settings.getInt(STRIDE_LENGTH_NAME,64)/100.0;
        Log.d("Motion handler", "Distance per Step: " + distancePerStep);

        if(settings.getBoolean(LOCATION_MODE_NAME, true)){//Manual mode
            switch(settings.getInt(LOCATION_MANUAL_NAME, 1)){
                case 1://EWI
                    buildingRotationOffsetDeg = BUILDING_ROTATION_OFFSET_DEG_EWI;
                    break;
                case 2://RDW
                    buildingRotationOffsetDeg = BUILDING_ROTATION_OFFSET_DEG_RDW;
                    break;
                default: //EWI
                    buildingRotationOffsetDeg = BUILDING_ROTATION_OFFSET_DEG_EWI;
                    break;
            }
        }else{//Automatic mode
            switch(settings.getInt(LOCATION_AUTO_NAME, 1)){
                case 1://EWI
                    buildingRotationOffsetDeg = BUILDING_ROTATION_OFFSET_DEG_EWI;
                    break;
                case 2://RDW
                    buildingRotationOffsetDeg = BUILDING_ROTATION_OFFSET_DEG_RDW;
                    break;
                default: //EWI
                    buildingRotationOffsetDeg = BUILDING_ROTATION_OFFSET_DEG_EWI;
                    break;
            }
        }
    }

    private void deleteOldData(long endTimestamp){
        int gyroStartId = gyroDataArrayList.size();
        int magneticStartId = magneticDataArrayList.size();
        int stepStartId = timeOfSteps.size();

        //Loop ove arraylist to see when
        for(int i=0; i<gyroDataArrayList.size(); i++){
            if(gyroDataArrayList.get(i).getTimestamp()>endTimestamp){
                gyroStartId = i;
                break;
            }
        }
        for(int i=0; i<magneticDataArrayList.size(); i++){
            if(magneticDataArrayList.get(i).getTimestamp()>endTimestamp){
                magneticStartId = i;
                break;
            }
        }
        for(int i=0; i<timeOfSteps.size(); i++){
            if(timeOfSteps.get(i)>endTimestamp){
                stepStartId = i;
                break;
            }
        }

        //Make new list
        if(gyroStartId == gyroDataArrayList.size()){// Totally clean list if all elements are old
            gyroDataArrayList.clear();
        }else{
            gyroDataArrayList = new ArrayList<GyroData>(gyroDataArrayList.subList(gyroStartId, gyroDataArrayList.size()));
        }
        //Also for magnetic data
        if(magneticStartId == magneticDataArrayList.size()){// Totally clean list if all elements are old
            magneticDataArrayList.clear();
        }else{
            magneticDataArrayList = new ArrayList<MagneticData>(magneticDataArrayList.subList(magneticStartId, magneticDataArrayList.size()));
        }
        //Also for step data
        if(stepStartId == timeOfSteps.size()){// Totally clean list if all elements are old
            timeOfSteps.clear();
        }else{
            timeOfSteps = new ArrayList<Long>(timeOfSteps.subList(stepStartId, timeOfSteps.size()));
        }
    }

    private boolean isCalculationNeeded(){ //always after data has been saved
        if(lastCalculationTime == 0){ //If not initialized do it now
            lastCalculationTime = gyroDataArrayList.get(0).getTimestamp();
        }

        //Check time constrain
        if((gyroDataArrayList.get(gyroDataArrayList.size()-1).getTimestamp()-lastCalculationTime)*NS2S > TIME_LIMIT){
            Log.d(LOG_TAG, "Time triggered motion calculation");
            timeForCalculations = gyroDataArrayList.get(gyroDataArrayList.size()-1).getTimestamp();
            return true;
        }

        //Check for rotation threshold, counting downwards to first add the last added value, this will reduce the time needed to go through the loop because new values have a higher change of triggering this and then it will break out of the loop.
        double sumRotation = 0;
        for(int i = (gyroDataArrayList.size()-1); i>=0; i--){
            sumRotation += gyroDataArrayList.get(i).getZRot();
            if(Math.abs(sumRotation) > ROTATION_LIMIT){
                Log.d(LOG_TAG, "Rotation triggered motion calculation. Rotation: " + sumRotation);
                //NOTNEEDED: Block rotation calculation for a period of time
                if(gyroDataArrayList.size()-1>ROTATION_POINTS) {
                    timeForCalculations = Math.max(gyroDataArrayList.get(i).getTimestamp(), gyroDataArrayList.get(gyroDataArrayList.size()-1-ROTATION_POINTS).getTimestamp());
                }else{
                    timeForCalculations = gyroDataArrayList.get(i).getTimestamp();
                }
                return true;
            }
        }
        return false;
    }

    private void makeBuffers(long endTimestamp){
        int magneticStartId = magneticDataArrayList.size();
        numberOfStepsInMotion = timeOfSteps.size();


        for(int i=0; i<magneticDataArrayList.size(); i++){
            if(magneticDataArrayList.get(i).getTimestamp()>endTimestamp){
                magneticStartId = i;
                break;
            }
        }
        for(int i=0; i<timeOfSteps.size(); i++){
            if(timeOfSteps.get(i)>endTimestamp){
                numberOfStepsInMotion = i;
                break;
            }
        }

        magneticBuffer = new ArrayList<MagneticData>(magneticDataArrayList.subList(0,magneticStartId));
    }

    private double meanDirection(){
        double sumX = 0;
        double sumY = 0;
        int numberOfPoints = magneticBuffer.size();

        //Add all unit vectors
        for(MagneticData data: magneticBuffer){
            sumX += Math.cos(-(data.getAzimut() / 180 * Math.PI));
            sumY += Math.sin(-(data.getAzimut() / 180 * Math.PI));
        }

        if(sumX==0 && sumY==0) {
            Log.e(LOG_TAG, "Error in calculation average angle, possible that no values are received");
            return 0;
        }else {
            return Math.atan2(sumY / numberOfPoints, sumX / numberOfPoints) * 180 / Math.PI;
        }
    }


    private void doCalculation(){

        //Update buffers to use for calculations
        makeBuffers(timeForCalculations);

        //TODO: what to do with rotation (screwing up angles)
        //TODO: what if two calculations steps is needed between step gathering

        double direction = meanDirection() + buildingRotationOffsetDeg;
        double distance = numberOfStepsInMotion * distancePerStep;
        motionListener.onMotion(direction, distance, newCalculationTime);

        //Reset request boolean TODO: FIx with extra check for second calculation before getting step data
        calculationRequest = false;
        //Clear all used data after calculations
        deleteOldData(newCalculationTime);
    }


    public void addGyroData(GyroData gyroData){
        if(gyroDataArrayList.size()>0){
            gyroData.setZRot(((gyroData.getTimestamp()-gyroDataArrayList.get(gyroDataArrayList.size()-1).getTimestamp())*NS2S)*gyroData.getZRotRate());
        }else{
            gyroData.setZRot(0.0);
        }
        this.gyroDataArrayList.add(gyroData);
        //TODO: start check for sending data
        //Check
        if(!calculationRequest) {
            if(isCalculationNeeded()){
                calculationRequest = true;
                newCalculationTime = gyroData.getTimestamp();
                motionListener.onWifiCheck(newCalculationTime);
                lastCalculationTime = newCalculationTime;
            }
        }
    }

    public void addMagneticData(MagneticData magneticData){
        this.magneticDataArrayList.add(magneticData);
    }

    public void addSteps(ArrayList<Long> registeredSteps, long endTime){
        this.timeOfSteps.addAll(registeredSteps);
        if(calculationRequest && endTime >= newCalculationTime){
            //Do calculations
            doCalculation();
        }
    }


}
