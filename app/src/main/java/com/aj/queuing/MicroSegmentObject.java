package com.aj.queuing;

/**
 * Created by Joost on 29/04/2015.
 */
public class MicroSegmentObject {
    public enum MicroState{
        STEP, NOSTEP
    }
    MicroState segmentMicroState;
    QueuingSensorData[] segmentData;
    long startTimestamp, endTimestamp;
    int pointsInSegment;

    public MicroSegmentObject(QueuingSensorData[] segmentData) {
        this.segmentData = segmentData;
        this.pointsInSegment = segmentData.length;
        startTimestamp = segmentData[0].getTimestamp();
        endTimestamp = segmentData[segmentData.length-1].getTimestamp();
        calculateState();
    }

    private void calculateState(){
        segmentMicroState = MicroState.NOSTEP;
        for(int i=0; i<segmentData.length; i++){
            if(segmentData[i].getStepIdentifier() == 2){
                segmentMicroState = MicroState.STEP;
            }
        }
    }

    public MicroState getSegmentMicroState() {
        return segmentMicroState;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public int getPointsInSegment() {
        return pointsInSegment;
    }
}
