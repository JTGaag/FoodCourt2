package com.aj.queuing;

/**
 * Created by Joost on 01/07/2015.
 */
public class Queue{
    final double MOVING_TIME = 2.0;
    int beginIndex, endIndex, numberOfBlocks, numberOfQueueBlocks, numberOfQueues;
    int numberOfMoveBlocks = -1;
    long beginTime, endTime = 0;
    boolean validQueue = true;

    public Queue(int beginIndex, int endIndex) {
        this.beginIndex = beginIndex;
        this.endIndex = endIndex;
        this.numberOfBlocks = this.endIndex - this.beginIndex + 1;
        if(endIndex<=beginIndex){
            validQueue = false;
        }
    }

    public double totalTime(){
        double time = 0;
        if(beginTime!=0 && endTime!=0){
            time = (endTime-beginTime)/1000000000.0;
        }
        return time;
    }

    public double getAverageServingTime(){
        double servingTime = 0;

        if(beginTime!=0 && endTime!=0 && numberOfMoveBlocks!= -1 && numberOfQueues!=0){
            servingTime = (totalTime()-numberOfMoveBlocks*MOVING_TIME)/numberOfQueues;
        }

        return servingTime;
    }

    public int getBeginIndex() {
        return beginIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public boolean isValidQueue() {
        return validQueue;
    }

    public int getNumberOfBlocks() {
        return numberOfBlocks;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getNumberOfMoveBlocks() {
        return numberOfMoveBlocks;
    }

    public void setNumberOfMoveBlocks(int numberOfMoveBlocks) {
        this.numberOfMoveBlocks = numberOfMoveBlocks;
    }

    public int getNumberOfQueueBlocks() {
        return numberOfQueueBlocks;
    }

    public void setNumberOfQueueBlocks(int numberOfQueueBlocks) {
        this.numberOfQueueBlocks = numberOfQueueBlocks;
    }

    public int getNumberOfQueues() {
        return numberOfQueues;
    }

    public void setNumberOfQueues(int numberOfQueues) {
        this.numberOfQueues = numberOfQueues;
    }
}
