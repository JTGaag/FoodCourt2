package com.aj.wifi;

import java.util.ArrayList;

/**
 * Created by Joost on 05/07/2015.
 */
public class ReturnedWifiPositionData {

    ArrayList<AccessPointData> accessPointDataArrayList;
    String phoneId;
    double xPosition, yPosition;
    double difference = 9999999999999.0;
    double calcDifference = 1;

    /**
     *
     * @param phoneId
     * @param xPosition
     * @param yPosition
     * @param accessPointDataArrayList
     */
    public ReturnedWifiPositionData(String phoneId, double xPosition, double yPosition, ArrayList<AccessPointData> accessPointDataArrayList) {
        this.phoneId = phoneId;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.accessPointDataArrayList = accessPointDataArrayList;
    }

    public double calculateDifference(ArrayList<AccessPointData> trueWifiData){
        double sumLevelDiffSquared = 0;

        for(AccessPointData accessPointComp : trueWifiData){
            String bssidComp = accessPointComp.getBssid();
            int levelComp = accessPointComp.getSignalStrength();
            //Log.d("SignalLevel", "BSSID: " + bssidComp + " Level: " + levelComp);
            int level = 0;
            for(AccessPointData accessPoint : accessPointDataArrayList){
                if(bssidComp.equalsIgnoreCase(accessPoint.getBssid())){
                    level = accessPoint.getSignalStrength();
                }
            }
            sumLevelDiffSquared += ((levelComp-level)*(levelComp-level));
        }

        difference = Math.sqrt(sumLevelDiffSquared);

        return difference;
    }

    public void calulateCalcDifference(double theoryMaxDifference){
        calcDifference = theoryMaxDifference - difference;
    }

    public double getCalcDifference() {
        return calcDifference;
    }

    public void setCalcDifference(double calcDifference) {
        this.calcDifference = calcDifference;
    }

    public double getDifference() {
        return difference;
    }

    public void setDifference(double difference) {
        this.difference = difference;
    }

    public ArrayList<AccessPointData> getAccessPointDataArrayList() {
        return accessPointDataArrayList;
    }

    public void setAccessPointDataArrayList(ArrayList<AccessPointData> accessPointDataArrayList) {
        this.accessPointDataArrayList = accessPointDataArrayList;
    }

    public String getPhoneId() {
        return phoneId;
    }

    public void setPhoneId(String phoneId) {
        this.phoneId = phoneId;
    }

    public double getxPosition() {
        return xPosition;
    }

    public void setxPosition(double xPosition) {
        this.xPosition = xPosition;
    }

    public double getyPosition() {
        return yPosition;
    }

    public void setyPosition(double yPosition) {
        this.yPosition = yPosition;
    }
}
