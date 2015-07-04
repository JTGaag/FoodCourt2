package com.aj.queuing;

/**
 * Created by Joost on 02/07/2015.
 */
public class QueuingDataObject {
    QueuingDisplayObject queuingDisplayObject;
    QueuingMonitoringObject queuingMonitoringObject;
    boolean finished = false;
    int position;

    public QueuingDataObject(int position, QueuingDisplayObject queuingDisplayObject) {
        this.position = position;
        this.queuingDisplayObject = queuingDisplayObject;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public QueuingMonitoringObject getQueuingMonitoringObject() {
        return queuingMonitoringObject;
    }

    public void setQueuingMonitoringObject(QueuingMonitoringObject queuingMonitoringObject) {
        this.queuingMonitoringObject = queuingMonitoringObject;
    }

    public QueuingDisplayObject getQueuingDisplayObject() {
        return queuingDisplayObject;
    }

    public void setQueuingDisplayObject(QueuingDisplayObject queuingDisplayObject) {
        this.queuingDisplayObject = queuingDisplayObject;
    }
}
