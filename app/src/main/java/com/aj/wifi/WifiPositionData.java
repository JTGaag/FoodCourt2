package com.aj.wifi;

import android.util.Log;

import com.aj.particlefilter.TimePositionData;

/**
 * Created by Joost on 21/06/2015.
 */
public class WifiPositionData {
    WifiData wifiData;
    TimePositionData timePositionData;

    public WifiPositionData(WifiData wifiData, TimePositionData timePositionData) {
        this.wifiData = wifiData;
        this.timePositionData = timePositionData;

        if(wifiData.getTimestamp()!=timePositionData.getTimestamp()){
            Log.e("Wifi position data", "Timestamps are not the same!");
        }
    }

    public WifiData getWifiData() {
        return wifiData;
    }

    public TimePositionData getTimePositionData() {
        return timePositionData;
    }
}
