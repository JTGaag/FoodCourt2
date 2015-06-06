package com.aj.particlefilter;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Joost on 04/06/2015.
 */
public class MotionDataHandler {
    //ArrayLists to save motion data
    ArrayList<GyroData> gyroDataArrayList = new ArrayList<GyroData>();
    ArrayList<MagneticData> magneticDataArrayList = new ArrayList<MagneticData>();
    ArrayList<Long> timeOfSteps = new ArrayList<Long>();


    boolean calculationRequest = false;
    long timeForCalculations = 0;
    long lastCalculationTime = 0;
    long newCalculationTime;

    MotionListener motionListener;

    //Constants
    final double NS2S = 1.0/1000000000.0;
    final double TIME_LIMIT = 10.0; //time limit in seconds when new motion needs to ben calculated and updated.
    final double ROTATION_LIMIT = 40.0; // Rotation limit in degrees when motion needs to be updated

    public MotionDataHandler(MotionListener motionListener) {
        this.motionListener = motionListener;
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
            Log.d("Motion Calculation", "Time triggered motion calculation");
            lastCalculationTime = gyroDataArrayList.get(gyroDataArrayList.size()-1).getTimestamp();
            return true;
        }

        //Check for rotation threshold, counting downwards to first add the last added value, this will reduce the time needed to go through the loop because new values have a higher change of triggering this and then it will break out of the loop.
        double sumRotation = 0;
        for(int i = (gyroDataArrayList.size()-1); i>=0; i--){
            sumRotation += gyroDataArrayList.get(i).getZRot();
            if(Math.abs(sumRotation) > ROTATION_LIMIT){
                Log.d("Motion Calculation", "Rotation triggered motion calculation. Rotation: " + sumRotation);
                lastCalculationTime = gyroDataArrayList.get(gyroDataArrayList.size()-1).getTimestamp();
                //TODO: Block rotation calculation for a period of time
                return true;
            }
        }
        return false;
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
                //Temp for test
                deleteOldData(gyroData.getTimestamp());
            }
        }
    }

    public void addMagneticData(MagneticData magneticData){
        this.magneticDataArrayList.add(magneticData);
    }

    public void addSteps(ArrayList<Long> registeredSteps){
        this.timeOfSteps.addAll(registeredSteps);
    }


}
