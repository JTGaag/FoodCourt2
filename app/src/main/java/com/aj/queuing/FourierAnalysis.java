package com.aj.queuing;

import org.jtransforms.fft.DoubleFFT_1D;

import java.util.Arrays;



/**
 * Created by Joost on 02/05/2015.
 */
public class FourierAnalysis {
    //raw data (everything)
    private QueuingSensorData timeData[];
    //Fourier data
    private double fft[];
    //Array with accel data
    private double dotArray[];
    //frequency for fourier data (ff)
    private double correspondingFrequencies[];
    private double samplingRate;
    private int numberOfPoints;

    //Data for step check
    private final int NUMBER_CHECK_POINTS = 6;
    private double checkPointsSum[] = new double[NUMBER_CHECK_POINTS];
    private double checkPointsMax[] = new double[NUMBER_CHECK_POINTS];
    private double checkPointsAvg[] = new double[NUMBER_CHECK_POINTS];
    private int checkPointsNumberOfDataPoints[] = new int[NUMBER_CHECK_POINTS];

    //Booleans for falase positive situations
    private boolean pocketDisturbing = false;
    private boolean noStepNoise = false;
        //Constants for checking
    private final double POCKET_DISTURPTION_TRESHOLD = 40.0;
    private final double NOISE_THRESHOLD = 100.0;



    public FourierAnalysis(QueuingSensorData[] timeData) {
        this.timeData = timeData;

        numberOfPoints = timeData.length;
        fft = new double[numberOfPoints*2]; //From: https://gist.github.com/jongukim/4037243
        correspondingFrequencies = new double[numberOfPoints*2];
        dotArray =  new double[numberOfPoints];
        samplingRate = timeData.length/((timeData[timeData.length-1].getTimestamp()-timeData[0].getTimestamp())/1000000000.0);
        //Calculate fourier shizzle
        doFourier();
        //Calculate noise
        calculateNoise();
    }

    private void doFourier(){
        //Set arrays to 0
        Arrays.fill(checkPointsSum, 0.0);
        Arrays.fill(checkPointsMax, 0.0);
        Arrays.fill(checkPointsAvg, 0.0);
        Arrays.fill(checkPointsNumberOfDataPoints, 0);

        //FFT object created
        DoubleFFT_1D fftDo = new DoubleFFT_1D(numberOfPoints);

        //put in data
        double sum = 0;
        for(int i=0; i<numberOfPoints; i++){
            dotArray[i] = timeData[i].getGravityDotProduct();
            sum += timeData[i].getGravityDotProduct();
        }
        double mean = sum / numberOfPoints;

        //Substract mean
        for(int i=0; i<numberOfPoints; i++){
            dotArray[i] -= mean;
        }

        //Dot array is now values minus the mean value (in order to eliminate high peak at 0 (because signal is always elivated)

        //Copy array. From: https://gist.github.com/jongukim/4037243
        System.arraycopy(dotArray, 0, fft, 0, numberOfPoints);

        //Do FFT shine
        fftDo.realForwardFull(fft);

        for(int i=0; i<numberOfPoints; i++){ //Only go over half of the frequencies (the rest will be mirrored)
            double frequency = samplingRate * (i) /(numberOfPoints*2);
            correspondingFrequencies[i] = frequency;

            //Fill check arrays
            if(frequency<=0){
                //Do not count 0
            }else if(frequency>0 && frequency<0.9){ //Low values mostly indicate that phone is taken out of pocket f<1Hz
                checkPointsSum[0] += Math.abs(fft[i]);
                checkPointsNumberOfDataPoints[0]++;
                if(Math.abs(fft[i])>checkPointsMax[0]){
                    checkPointsMax[0] = Math.abs(fft[i]);
                }

            }else if(frequency>1.2 && frequency<2.2){ //These frequencies indicate steps (steps are between 1.2 and 2.2 Hz
                checkPointsSum[1] += Math.abs(fft[i]);
                checkPointsNumberOfDataPoints[1]++;
                if(Math.abs(fft[i])>checkPointsMax[1]){
                    checkPointsMax[1] = Math.abs(fft[i]);
                }

            }else if(frequency>=2.2 && frequency<3.2){
                checkPointsSum[2] += Math.abs(fft[i]);
                checkPointsNumberOfDataPoints[2]++;
                if(Math.abs(fft[i])>checkPointsMax[2]){
                    checkPointsMax[2] = Math.abs(fft[i]);
                }

            }else if(frequency>=3.2 && frequency<4.2){
                checkPointsSum[3] += Math.abs(fft[i]);
                checkPointsNumberOfDataPoints[3]++;
                if(Math.abs(fft[i])>checkPointsMax[3]){
                    checkPointsMax[3] = Math.abs(fft[i]);
                }

            }else if(frequency>=4.2 && frequency<5.5){ //These frequencies can also indicate steps (freqency between the two peeks in steps)
                checkPointsSum[4] += Math.abs(fft[i]);
                checkPointsNumberOfDataPoints[4]++;
                if(Math.abs(fft[i])>checkPointsMax[4]){
                    checkPointsMax[4] = Math.abs(fft[i]);
                }

            }else if(frequency>=5.5){
                checkPointsSum[5] += Math.abs(fft[i]);
                checkPointsNumberOfDataPoints[5]++;
                if(Math.abs(fft[i])>checkPointsMax[5]){
                    checkPointsMax[5] = Math.abs(fft[i]);
                }

            }
        }

        for(int i=0; i< checkPointsAvg.length; i++){
            checkPointsAvg[i] = checkPointsSum[i]/checkPointsNumberOfDataPoints[i];
        }

    }

    private void calculateNoise(){
        //TODO: do the noise boolean checks
        if(checkPointsAvg[0]>POCKET_DISTURPTION_TRESHOLD){//Low frequencies give high result (meaning in and out of pocket)
            pocketDisturbing = true;
        }

        if(checkPointsAvg[2]>NOISE_THRESHOLD || checkPointsAvg[3]>NOISE_THRESHOLD){ //Medium frequencies noise (meaning that no step is taken, but a lot of acceleration is measured e.g. playing a game)
            noStepNoise = true;
        }

    }

    public double[] getCorrespondingFrequencies() {
        return correspondingFrequencies;
    }

    public double[] getFft() {
        return fft;
    }

    public double[] getCheckPointsSum() {
        return checkPointsSum;
    }

    public int[] getCheckPointsNumberOfDataPoints() {
        return checkPointsNumberOfDataPoints;
    }

    public double[] getCheckPointsMax() {
        return checkPointsMax;
    }

    public double[] getCheckPointsAvg() {
        return checkPointsAvg;
    }

    public boolean isPocketDisturbing() {
        return pocketDisturbing;
    }

    public boolean isNoStepNoise() {
        return noStepNoise;
    }
}
