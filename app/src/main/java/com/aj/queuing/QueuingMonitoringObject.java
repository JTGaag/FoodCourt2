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
        //TODO: Check for length of 1
        //Loop over alle loststaande walkings en verander deze naar moving
        for(int i=0; i< blockArray.length; i++){
            if(blockArray[i].getBlockMacroState()== MacroBlockObject.MacroState.WALKING){
                if(i==0){
                    if(blockArray[i+1].getBlockMacroState()!= MacroBlockObject.MacroState.WALKING){
                        blockArray[i].setBlockMacroState(MacroBlockObject.MacroState.MOVING);
                    }
                }else if(i==(blockArray.length-1)){
                    if(blockArray[i-1].getBlockMacroState()!= MacroBlockObject.MacroState.WALKING){
                        blockArray[i].setBlockMacroState(MacroBlockObject.MacroState.MOVING);
                    }
                }else{
                    if(blockArray[i+1].getBlockMacroState()!= MacroBlockObject.MacroState.WALKING && blockArray[i+1].getBlockMacroState()!= MacroBlockObject.MacroState.WALKING){
                        blockArray[i].setBlockMacroState(MacroBlockObject.MacroState.MOVING);
                    }
                }
            }
            //ook voor enkelle queing activities
            if(blockArray[i].getBlockMacroState()== MacroBlockObject.MacroState.QUEUING){
                if(i==0){
                    if(blockArray[i+1].getBlockMacroState()== MacroBlockObject.MacroState.WALKING){
                        blockArray[i].setBlockMacroState(MacroBlockObject.MacroState.WALKING);
                    }
                }else if(i==(blockArray.length-1)){
                    if(blockArray[i-1].getBlockMacroState()== MacroBlockObject.MacroState.WALKING){
                        blockArray[i].setBlockMacroState(MacroBlockObject.MacroState.WALKING);
                    }
                }else{
                    if(blockArray[i+1].getBlockMacroState()== MacroBlockObject.MacroState.WALKING && blockArray[i+1].getBlockMacroState()== MacroBlockObject.MacroState.WALKING){
                        blockArray[i].setBlockMacroState(MacroBlockObject.MacroState.WALKING);
                    }
                }
            }
            //ook voor enkelle moving activities
            if(blockArray[i].getBlockMacroState()== MacroBlockObject.MacroState.MOVING){
                if(i==0){
                    if(blockArray[i+1].getBlockMacroState()== MacroBlockObject.MacroState.WALKING){
                        blockArray[i].setBlockMacroState(MacroBlockObject.MacroState.WALKING);
                    }
                }else if(i==(blockArray.length-1)){
                    if(blockArray[i-1].getBlockMacroState()== MacroBlockObject.MacroState.WALKING){
                        blockArray[i].setBlockMacroState(MacroBlockObject.MacroState.WALKING);
                    }
                }else{
                    if(blockArray[i+1].getBlockMacroState()== MacroBlockObject.MacroState.WALKING && blockArray[i+1].getBlockMacroState()== MacroBlockObject.MacroState.WALKING){
                        blockArray[i].setBlockMacroState(MacroBlockObject.MacroState.WALKING);
                    }
                }
            }
        }

        //Sla alle queues op
        boolean isQueue = false;
        int tempBeginIndex = 0;
        for(int i=0; i<blockArray.length; i++){
            if(isQueue){//If queue start look for stop
                if(blockArray[i].getBlockMacroState()== MacroBlockObject.MacroState.WALKING){
                    isQueue = false;
                    queueList.add(new Queue(tempBeginIndex, i-1));
                }
            }else{//If queue is stopped look for first queuing state
                if(blockArray[i].getBlockMacroState()== MacroBlockObject.MacroState.QUEUING){
                    isQueue = true;
                    tempBeginIndex = i;
                }
            }
        }
        //DONE: if at end queueing is stopped
        queueList.add(new Queue(tempBeginIndex, blockArray.length-1));

    }

    class Queue{
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
}
