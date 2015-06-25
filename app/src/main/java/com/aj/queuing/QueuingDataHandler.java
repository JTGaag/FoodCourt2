package com.aj.queuing;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Joost on 25/04/2015.
 * This class will perform (possibly async) data handling for blocks of segments
 * It will call back to the handling activity when a block has been analysed and continues to store incomming data points
 */
public class QueuingDataHandler {

    //State enum
    public enum State {
        UNDEFINED, INITIATING, OPERATIONAL, STOPPING, STOPPED
    }

    private State currentState = State.UNDEFINED;

    //CallbackListener
    private QueuingListener queuingListener;

    //Sized of all kinds
    private int pointsPerSegment;
    private int segmentsPerBoundary;
    private int segmentsPerBody;
    private int rawDataSize;
    private int pointerStart;
    private int pointsPerBoundary;
    private int pointsPerBody;
    private int stepCount;

    private int queuingMode = 1;

    //Storage arrays
    private QueuingSensorData[] rawData;
    private QueuingSensorData[] rawDataBuffer;

    //Step time listArray
    private ArrayList<Long> timeOfSteps = new ArrayList<Long>();

    //Pointers
    private int rawDataPointer = 0;

    //Constants
    private final double TIME_TRESHOLD = 0.350;
    private final double ACCELERATION_TRESHOLD = 12;
    private final int GRAVITY_TIME_DIF = 20;
    private final int CHECK_DIF = 25;
    private final double GRAVITY_DIF_TRESHOLD = 7.0;

    //Step detection constants
    private double initialPeekThreshold = 8.0;
    private int timeDomainSteps = 50;
    private double accelerationDifferenceThresholdMin = 2.2;
    private double accelerationDifferenceThresholdMax = 20.0;

    //Step detection constants for Queueing
    private final double INITIAL_PEEK_THRESHOLD_QUEUEING = 8.0;
    private final int TIME_DOMAIN_STEPS_QUEUEING = 50;
    private final double ACCELERATION_DIFFERENCE_THRESHOLD_MIN_QUEUEING = 2.2;
    private final double ACCELERATION_DIFFERENCE_THRESHOLD_MAX_QUEUEING = 20.0;

    //Step detection constants for Localization
    private final double INITIAL_PEEK_THRESHOLD_LOCALIZATION = 9.0;
    private final int TIME_DOMAIN_STEPS_LOCALIZATION = 50;
    private final double ACCELERATION_DIFFERENCE_THRESHOLD_MIN_LOCALIZATION = 3.0;
    private final double ACCELERATION_DIFFERENCE_THRESHOLD_MAX_LOCALIZATION = 20.0;

    //For fourier analyisi
    private final int FOURIER_DOMAIN_RADIUS = 50; //Normal 150


    //Constructor for Handler also run initiation method

    /**
     *
     * @param queuingListener
     * @param pointsPerSegment
     * @param segmentsPerBoundary
     * @param segmentsPerBody
     */
    public QueuingDataHandler(QueuingListener queuingListener, int pointsPerSegment, int segmentsPerBoundary, int segmentsPerBody, int queueingMode) {
        this.queuingListener = queuingListener;
        this.pointsPerSegment = pointsPerSegment;
        this.segmentsPerBoundary = segmentsPerBoundary;
        this.segmentsPerBody = segmentsPerBody;
        this.queuingMode = queueingMode;

        switch(queueingMode){
            case 1: //Queuing first
                initialPeekThreshold = INITIAL_PEEK_THRESHOLD_QUEUEING;
                timeDomainSteps = TIME_DOMAIN_STEPS_QUEUEING;
                accelerationDifferenceThresholdMin = ACCELERATION_DIFFERENCE_THRESHOLD_MIN_QUEUEING;
                accelerationDifferenceThresholdMax = ACCELERATION_DIFFERENCE_THRESHOLD_MAX_QUEUEING;
                break;
            case 2: //Localization first
                initialPeekThreshold = INITIAL_PEEK_THRESHOLD_LOCALIZATION;
                timeDomainSteps = TIME_DOMAIN_STEPS_LOCALIZATION;
                accelerationDifferenceThresholdMin = ACCELERATION_DIFFERENCE_THRESHOLD_MIN_LOCALIZATION;
                accelerationDifferenceThresholdMax = ACCELERATION_DIFFERENCE_THRESHOLD_MAX_LOCALIZATION;
                break;
            case 3: //Step counting Joost
                initialPeekThreshold = 9.5; //9.8
                timeDomainSteps = TIME_DOMAIN_STEPS_LOCALIZATION;
                accelerationDifferenceThresholdMin = 3.2;
                accelerationDifferenceThresholdMax = ACCELERATION_DIFFERENCE_THRESHOLD_MAX_LOCALIZATION;
                break;
            case 4: //Step counting Jork
                initialPeekThreshold = 10.0;
                timeDomainSteps = TIME_DOMAIN_STEPS_LOCALIZATION;
                accelerationDifferenceThresholdMin = 3.0;
                accelerationDifferenceThresholdMax = ACCELERATION_DIFFERENCE_THRESHOLD_MAX_LOCALIZATION;
                break;
            case 5: //Step counting Willem
                initialPeekThreshold = 8.5;
                timeDomainSteps = TIME_DOMAIN_STEPS_LOCALIZATION;
                accelerationDifferenceThresholdMin = 2.5;
                accelerationDifferenceThresholdMax = ACCELERATION_DIFFERENCE_THRESHOLD_MAX_LOCALIZATION;
                break;
            case 6: //Step counting Alexander
                initialPeekThreshold = 9.0;
                timeDomainSteps = TIME_DOMAIN_STEPS_LOCALIZATION;
                accelerationDifferenceThresholdMin = 3.0;
                accelerationDifferenceThresholdMax = ACCELERATION_DIFFERENCE_THRESHOLD_MAX_LOCALIZATION;
                break;
            default:
                initialPeekThreshold = INITIAL_PEEK_THRESHOLD_LOCALIZATION;
                timeDomainSteps = TIME_DOMAIN_STEPS_LOCALIZATION;
                accelerationDifferenceThresholdMin = ACCELERATION_DIFFERENCE_THRESHOLD_MIN_LOCALIZATION;
                accelerationDifferenceThresholdMax = ACCELERATION_DIFFERENCE_THRESHOLD_MAX_LOCALIZATION;
                break;
        }

        initData();
    }

    private void initData() {
        //Create RweQueuingSensorData array.
        //Length is equal to the amount of body segments plus 2 times the number of boundary segments all multiplied by the amount of points per segment
        pointerStart = pointsPerSegment*(segmentsPerBoundary);
        pointsPerBoundary = pointsPerSegment*(segmentsPerBoundary);
        pointsPerBody = pointsPerSegment*(segmentsPerBody);

        //Checks
//        if(pointsPerBoundary<GRAVITY_TIME_DIF){
//            Log.e("DataBlock error","Not enough boundary points for gravity shift determination, resizing bounderies");
//            pointsPerBoundary = GRAVITY_TIME_DIF;
//        }
        if(pointsPerBody<2*pointsPerBoundary){
            Log.e("DataBlock error","Not enough body points, resizing body");
            pointsPerBody = 2*pointsPerBoundary;
        }


        //Setting array size
        rawDataSize = pointsPerBody + pointsPerBoundary*2;
        rawData = new QueuingSensorData[rawDataSize];
        currentState = State.INITIATING;
    }

    /*
    TODO: all other things in the handler: -look especially to reordering the data points when everything is filled
     */
    public void addRawData(QueuingSensorData rawDataObject){
        //Switching over all states
        switch(currentState){
            case UNDEFINED:
                break;

            case INITIATING:
                //Add data during initiating period (Shifting might be a problem)
                if(rawDataPointer<rawDataSize) {
                    //Store data and add 1 to pointer
                    rawData[rawDataPointer] = rawDataObject;
                    rawDataPointer++;
                    if(rawDataPointer==rawDataSize){//Array is full, time to do analysis and shift array
                        copyDataBuffer();
                        //calculateGravityShift();
                        calculateSteps();

                        //Send steps
                        queuingListener.onStepCount(timeOfSteps, rawDataBuffer[rawDataSize-pointsPerBoundary-1].getTimestamp());


                        rawDataPointer = pointsPerBoundary*2;
                        currentState = State.OPERATIONAL;
                    }
                }else{
                    Log.e("Out of bound error", "Pointer of QueuingDataHandler is out of array size");
                }
                break;

            case OPERATIONAL:
                if(rawDataPointer<rawDataSize) {
                    //Store data and add 1 to pointer
                    rawData[rawDataPointer] = rawDataObject;
                    rawDataPointer++;
                    if(rawDataPointer==rawDataSize){//Array is full, time to do analysis and shift array
                        copyDataBuffer();
                        long startTime = System.currentTimeMillis();
                        //calculateGravityShift();
                        calculateSteps();

                        //Send steps
                        queuingListener.onStepCount(timeOfSteps, rawDataBuffer[rawDataSize-pointsPerBoundary-1].getTimestamp());

                        //Send block data
                        MacroBlockObject currentBlock = generateBlock();
                        QueuingSensorData[] analysedData = Arrays.copyOfRange(rawDataBuffer,pointsPerBoundary,(rawDataSize-pointsPerBoundary));
                        queuingListener.onNewDataBlock(stepCount, analysedData, currentBlock);
                        rawDataPointer = pointsPerBoundary*2;
                        currentState = State.OPERATIONAL;
                    }
                }else{
                    Log.e("Out of bound error", "Pointer of QueuingDataHandler is out of array size");
                }
                break;

            case STOPPING:
                break;

            case STOPPED:
                break;

            default:
                break;
        }
    }

    private void copyDataBuffer(){
        //TODO: maak deep copy
        rawDataBuffer = rawData.clone();
        for(int i=0; i<(pointsPerBoundary*2); i++){
            rawData[i] = rawDataBuffer[rawDataSize-(pointsPerBoundary*2)-1+i];
        }
    }

    private void calculateGravityShift(){
        for(int i=pointerStart; i<(rawDataSize-pointsPerBoundary); i++){
            QueuingSensorData currentDataPoint = rawDataBuffer[i];
            QueuingSensorData compareDataPoint = rawDataBuffer[i-GRAVITY_TIME_DIF];
            double xShift = currentDataPoint.getGravityX() - compareDataPoint.getGravityX();
            double yShift = currentDataPoint.getGravityY() - compareDataPoint.getGravityY();
            double zShift = currentDataPoint.getGravityZ() - compareDataPoint.getGravityZ();
            double totalShift = Math.sqrt(xShift*xShift + yShift*yShift + zShift*zShift);
            if(totalShift > GRAVITY_DIF_TRESHOLD){
                rawDataBuffer[i].setGravityShift(1);
            }else{
                rawDataBuffer[i].setGravityShift(0);
            }
        }
    }


    /**
     * Method to calculate steps, starting to look in the analyse data segemnts, but editing data in the end boundary block, this to detect all steps over all the inserted data
     */
    private void calculateSteps(){

        //Reset step array List
        timeOfSteps.clear();

        stepCount = 0;
        for(int i=pointerStart; i<(rawDataSize-pointsPerBoundary); i++){//GO over all data points in analysing segments

            //first peek detection
            if(rawDataBuffer[i-1].getGravityDotProduct()<rawDataBuffer[i].getGravityDotProduct() && rawDataBuffer[i].getGravityDotProduct()>rawDataBuffer[i+1].getGravityDotProduct() && rawDataBuffer[i].getGravityDotProduct()> initialPeekThreshold){
                int tempMaxIndex = i; //Index of maximum
                boolean validPattern = true;

                //Loop over next datapoints to find new maximum
                for(int j=i; j<i+ timeDomainSteps; j++){
                    if(rawDataBuffer[j-1].getGravityDotProduct()<rawDataBuffer[j].getGravityDotProduct() && rawDataBuffer[j].getGravityDotProduct()>rawDataBuffer[j+1].getGravityDotProduct()) {
                        if (rawDataBuffer[j].getGravityDotProduct() > rawDataBuffer[tempMaxIndex].getGravityDotProduct()) {
                            tempMaxIndex = j;
                        }
                    }
                }

                int tempMinIndex = tempMaxIndex;
                //Loop to find local minimum
                for(int j=tempMaxIndex; j<i+ timeDomainSteps; j++){
                    if(rawDataBuffer[j-1].getGravityDotProduct()>rawDataBuffer[j].getGravityDotProduct() && rawDataBuffer[j].getGravityDotProduct()<rawDataBuffer[j+1].getGravityDotProduct()) {
                        if (rawDataBuffer[j].getGravityDotProduct() < rawDataBuffer[tempMinIndex].getGravityDotProduct()) {
                            tempMinIndex = j;
                        }
                    }
                }

                //Check for invalid patern
                if(tempMaxIndex==i || tempMinIndex==tempMaxIndex){
                    validPattern = false;
                }

                //Save data
                if(validPattern){
                    //diff in min and max between min and max threshold
                    if((rawDataBuffer[tempMaxIndex].getGravityDotProduct()-rawDataBuffer[tempMinIndex].getGravityDotProduct())> accelerationDifferenceThresholdMin && (rawDataBuffer[tempMaxIndex].getGravityDotProduct()-rawDataBuffer[tempMinIndex].getGravityDotProduct())< accelerationDifferenceThresholdMax){

                        //TODO: Do something here with fourier transform to see if possible step is not a distrubance such as random movement etc.

                        //Log.d("Array", "min index: " + (i-FOURIER_DOMAIN_RADIUS));
                        //Log.d("Array", "max index: " + (i + FOURIER_DOMAIN_RADIUS));
                        QueuingSensorData tempFourierData[] = Arrays.copyOfRange(rawDataBuffer, i-FOURIER_DOMAIN_RADIUS, i + FOURIER_DOMAIN_RADIUS);
                        FourierAnalysis tempFourierAnalysis = new FourierAnalysis(tempFourierData);

                        //Pocket disturption
                        if(tempFourierAnalysis.isPocketDisturbing()){
                            rawDataBuffer[i].setPocketDisturbing(true); //Start of step set disturbing
                            rawDataBuffer[tempMaxIndex].setPocketDisturbing(true); //Maximum in step set disturbing
                            rawDataBuffer[tempMinIndex].setPocketDisturbing(true); //Minimum in step set disturbing
                        }

                        //Game Noise
                        if(tempFourierAnalysis.isNoStepNoise()){
                            rawDataBuffer[i].setNoStepNoise(true); //Start of step set disturbing
                            rawDataBuffer[tempMaxIndex].setNoStepNoise(true); //Maximum in step set disturbing
                            rawDataBuffer[tempMinIndex].setNoStepNoise(true); //Minimum in step set disturbing
                        }

                        if(queuingMode == 1 || queuingMode == 0 || queuingMode == 4 || queuingMode < 10) {
                            if (!tempFourierAnalysis.isPocketDisturbing() && !tempFourierAnalysis.isNoStepNoise()) {
                                //Set dataPoints
                                rawDataBuffer[i].setStepIdentifier(1); //Start of step
                                rawDataBuffer[tempMaxIndex].setStepIdentifier(2); //Maximum in step
                                rawDataBuffer[tempMinIndex].setStepIdentifier(3); //Minimum in step
                                stepCount++;

                                //Add steps
                                timeOfSteps.add(new Long(rawDataBuffer[i].getTimestamp()));

                                i = i + timeDomainSteps;
                            }
                        }else{
                            //Set dataPoints
                            rawDataBuffer[i].setStepIdentifier(1); //Start of step
                            rawDataBuffer[tempMaxIndex].setStepIdentifier(2); //Maximum in step
                            rawDataBuffer[tempMinIndex].setStepIdentifier(3); //Minimum in step
                            stepCount++;

                            //Add steps
                            timeOfSteps.add(new Long(rawDataBuffer[i].getTimestamp()));

                            i = i + timeDomainSteps;
                        }
                    }
                }
            }

        }

    }



    private MacroBlockObject generateBlock(){
        MacroBlockObject currentBlock;
        MicroSegmentObject[] segmentObjects = new MicroSegmentObject[segmentsPerBody];

        for(int i=0; i<segmentsPerBody; i++){
            int start = (i*pointsPerSegment) + (pointsPerBoundary);
            int end = start + pointsPerSegment;
            QueuingSensorData[] tempSegmentData = Arrays.copyOfRange(rawDataBuffer,start,end);
            segmentObjects[i] = new MicroSegmentObject(tempSegmentData);
        }

        currentBlock = new MacroBlockObject(segmentObjects);

        return currentBlock;
    }

}
