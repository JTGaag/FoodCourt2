package com.aj.wifi;

import android.net.wifi.WifiManager;

/**
 * Created by Joost on 05/07/2015.
 */
public class AccessPointData {
    String bssid, ssid;
    int signalStrength;

    /**
     *
     * @param bssid
     * @param ssid
     * @param signalStrength in DB
     */
    public AccessPointData(String bssid, String ssid, int signalStrength) {
        this.bssid = bssid;
        this.ssid = ssid;
        this.signalStrength = WifiManager.calculateSignalLevel(signalStrength, 256);
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public int getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(int signalStrength) {
        this.signalStrength = signalStrength;
    }
}
