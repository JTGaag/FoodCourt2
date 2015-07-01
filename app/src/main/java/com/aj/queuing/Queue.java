package com.aj.queuing;

/**
 * Created by Joost on 01/07/2015.
 */
public class Queue{
    int beginIndex, endIndex;
    boolean validQueue = true;

    public Queue(int beginIndex, int endIndex) {
        this.beginIndex = beginIndex;
        this.endIndex = endIndex;
        if(endIndex<=beginIndex){
            validQueue = false;
        }
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
}
