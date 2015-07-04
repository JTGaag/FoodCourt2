package com.aj.queuing;

import java.util.ArrayList;

/**
 * Created by Joost on 25/04/2015.
 * Interface to callback to main Activity when results are calculated from QueuingDataHandler
 */
public interface QueuingListener {
    //Need to define this more
    //Main activity needs to implement this so data can be send back from Handler to activity
    void onNewDataBlock(int count, QueuingSensorData[] dataArray, MacroBlockObject blockObject);

    void onStepCount(ArrayList<Long> timeOfSteps, long endTime);
}
