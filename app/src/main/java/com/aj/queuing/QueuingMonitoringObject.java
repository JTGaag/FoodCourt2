package com.aj.queuing;

import java.util.ArrayList;

/**
 * Created by Joost on 05/05/2015.
 */
public class QueuingMonitoringObject {
    MacroBlockObject blockArray[];
    ArrayList<Queue> queueList = new ArrayList<Queue>();

    public QueuingMonitoringObject(MacroBlockObject[] blockArray) {
        this.blockArray = blockArray;
        analyseAll();
    }

    private void analyseAll(){
        if(blockArray.length>1) {
            //Loop over alle loststaande walkings en verander deze naar moving
            for (int i = 0; i < blockArray.length; i++) {
                if (blockArray[i].getBlockMacroState() == MacroBlockObject.MacroState.WALKING) {
                    if (i == 0) {
                        if (blockArray[i + 1].getBlockMacroState() != MacroBlockObject.MacroState.WALKING) {
                            blockArray[i].setBlockMacroState(MacroBlockObject.MacroState.MOVING);
                        }
                    } else if (i == (blockArray.length - 1)) {
                        if (blockArray[i - 1].getBlockMacroState() != MacroBlockObject.MacroState.WALKING) {
                            blockArray[i].setBlockMacroState(MacroBlockObject.MacroState.MOVING);
                        }
                    } else {
                        if (blockArray[i + 1].getBlockMacroState() != MacroBlockObject.MacroState.WALKING && blockArray[i - 1].getBlockMacroState() != MacroBlockObject.MacroState.WALKING) {
                            blockArray[i].setBlockMacroState(MacroBlockObject.MacroState.MOVING);
                        }
                    }
                }
                //ook voor enkelle queing activities
                if (blockArray[i].getBlockMacroState() == MacroBlockObject.MacroState.QUEUING) {
                    if (i == 0) {
                        if (blockArray[i + 1].getBlockMacroState() == MacroBlockObject.MacroState.WALKING) {
                            blockArray[i].setBlockMacroState(MacroBlockObject.MacroState.WALKING);
                        }
                    } else if (i == (blockArray.length - 1)) {
                        if (blockArray[i - 1].getBlockMacroState() == MacroBlockObject.MacroState.WALKING) {
                            blockArray[i].setBlockMacroState(MacroBlockObject.MacroState.WALKING);
                        }
                    } else {
                        if (blockArray[i + 1].getBlockMacroState() == MacroBlockObject.MacroState.WALKING && blockArray[i - 1].getBlockMacroState() == MacroBlockObject.MacroState.WALKING) {
                            blockArray[i].setBlockMacroState(MacroBlockObject.MacroState.WALKING);
                        }
                    }
                }
                //ook voor enkelle moving activities
                if (blockArray[i].getBlockMacroState() == MacroBlockObject.MacroState.MOVING) {
                    if (i == 0) {
                        if (blockArray[i + 1].getBlockMacroState() == MacroBlockObject.MacroState.WALKING) {
                            blockArray[i].setBlockMacroState(MacroBlockObject.MacroState.WALKING);
                        }
                    } else if (i == (blockArray.length - 1)) {
                        if (blockArray[i - 1].getBlockMacroState() == MacroBlockObject.MacroState.WALKING) {
                            blockArray[i].setBlockMacroState(MacroBlockObject.MacroState.WALKING);
                        }
                    } else {
                        if (blockArray[i + 1].getBlockMacroState() == MacroBlockObject.MacroState.WALKING && blockArray[i - 1].getBlockMacroState() == MacroBlockObject.MacroState.WALKING) {
                            blockArray[i].setBlockMacroState(MacroBlockObject.MacroState.WALKING);
                        }
                    }
                }
            }
        }

        //Sla alle queues op
        boolean isQueue = false;
        int tempBeginIndex = 0;
        int numberOfMoveBlocks = 0;
        int numberOfQueueBlocks = 0;
        int numberOfQueues = 0;
        for(int i=0; i<blockArray.length; i++){
            if(isQueue){//If queue start look for stop
                //Add queue and moves
                if(blockArray[i].getBlockMacroState()== MacroBlockObject.MacroState.QUEUING){
                    numberOfQueueBlocks++;
                }
                if(blockArray[i].getBlockMacroState()== MacroBlockObject.MacroState.MOVING){
                    numberOfMoveBlocks++;
                    if(blockArray[i-1].getBlockMacroState()!= MacroBlockObject.MacroState.MOVING){
                        numberOfQueues++;
                    }
                }

                //End condition
                if(blockArray[i].getBlockMacroState()== MacroBlockObject.MacroState.WALKING){
                    if(blockArray[i-1].getBlockMacroState()!= MacroBlockObject.MacroState.MOVING){
                        numberOfQueues++;
                    }
                    isQueue = false;
                    //TODO: fix weird shit with time
                    Queue tmpQueue = new Queue(tempBeginIndex, i-1);
                    tmpQueue.setBeginTime(blockArray[tempBeginIndex].getStartTimestamp());
                    tmpQueue.setEndTime(blockArray[i-1].getEndTimestamp());
                    tmpQueue.setNumberOfMoveBlocks(numberOfMoveBlocks);
                    tmpQueue.setNumberOfQueueBlocks(numberOfQueueBlocks);
                    tmpQueue.setNumberOfQueues(numberOfQueues);
                    queueList.add(tmpQueue);
                }
            }else{//If queue is stopped look for first queuing state
                if(blockArray[i].getBlockMacroState()== MacroBlockObject.MacroState.QUEUING){
                    isQueue = true;
                    tempBeginIndex = i;
                    numberOfMoveBlocks = 0;
                    numberOfQueueBlocks = 1;
                    numberOfQueues = 0;
                }
            }
        }
        //DONE: if at end queueing is stopped
        if(isQueue) {
            numberOfQueues++;
            Queue tmpQueue = new Queue(tempBeginIndex, blockArray.length - 1);
            tmpQueue.setBeginTime(blockArray[tempBeginIndex].getStartTimestamp());
            tmpQueue.setEndTime(blockArray[blockArray.length - 1].getEndTimestamp());
            tmpQueue.setNumberOfMoveBlocks(numberOfMoveBlocks);
            tmpQueue.setNumberOfQueueBlocks(numberOfQueueBlocks);
            tmpQueue.setNumberOfQueues(numberOfQueues);
            queueList.add(tmpQueue);
        }
        //queueList.add(new Queue(tempBeginIndex, blockArray.length-1));
    }

    public MacroBlockObject[] getBlockArray() {
        return blockArray;
    }

    public ArrayList<Queue> getQueueList() {
        return queueList;
    }
}
