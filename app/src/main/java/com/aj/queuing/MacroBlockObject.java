package com.aj.queuing;

import android.util.Log;

/**
 * Created by Joost on 29/04/2015.
 */
public class MacroBlockObject {
    public enum MacroState{
        QUEUING, MOVING, WALKING, UNDEFINED
    }
    private MacroState blockMacroState;
    MicroSegmentObject[] blockSegments;
    int numberOfStepSegments = 0;
    int numberOfSegments;
    long startTimestamp, endTimestamp;
    boolean firstStep = false;
    boolean lastStep = false;
    double sampleFrequency;

    public MacroBlockObject(MicroSegmentObject[] blockSegments) {
        this.blockSegments = blockSegments;
        this.numberOfSegments = blockSegments.length;
        startTimestamp = blockSegments[0].getStartTimestamp();
        endTimestamp = blockSegments[blockSegments.length-1].getEndTimestamp();
        sampleFrequency = (this.numberOfSegments*blockSegments[0].getPointsInSegment()/((endTimestamp-startTimestamp)/1000000000.0));
        calculateState();
        Log.d("Block info","Number of points: "+this.numberOfSegments*blockSegments[0].getPointsInSegment());
        Log.d("Block info", "Time: " + ((endTimestamp - startTimestamp) / 1000000000.0));
        Log.d("Block info","Sampling freq: "+(this.numberOfSegments*blockSegments[0].getPointsInSegment()/((endTimestamp-startTimestamp)/1000000000.0)));
    }

    private void calculateState(){
        for(int i=0; i<blockSegments.length; i++){
            if(blockSegments[i].getSegmentMicroState() == MicroSegmentObject.MicroState.STEP){
              numberOfStepSegments++;
                if(i==1 || i==2){
                    firstStep = true;
                }
                if(i==(blockSegments.length-1) || i==(blockSegments.length-2)){
                    lastStep = true;
                }
            }
        }

        switch(numberOfStepSegments){
            case 0:
                blockMacroState = MacroState.QUEUING;
                break;
            case 1:
                blockMacroState = MacroState.QUEUING;
                break;
            case 2:
                blockMacroState = MacroState.QUEUING;
                break;
            case 3:
                blockMacroState = MacroState.WALKING;
                break;
            case 4:
                blockMacroState = MacroState.WALKING;
                break;
            case 5:
                blockMacroState = MacroState.WALKING;
                break;
            case 6:
                blockMacroState = MacroState.WALKING;
                break;
            case 7:
                blockMacroState = MacroState.WALKING;
                break;
            default:
                blockMacroState = MacroState.WALKING;
                break;
        }
    }


    public int getNumberOfStepSegments() {
        return numberOfStepSegments;
    }

    public int getNumberOfSegments() {
        return numberOfSegments;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public MacroState getBlockMacroState() {
        return blockMacroState;
    }

    public void setBlockMacroState(MacroState blockMacroState) {
        this.blockMacroState = blockMacroState;
    }

    public boolean isFirstStep() {
        return firstStep;
    }

    public boolean isLastStep() {
        return lastStep;
    }

    public double getSampleFrequency() {
        return sampleFrequency;
    }
}
