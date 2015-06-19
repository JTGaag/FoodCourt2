package com.aj.wifi;

import android.net.wifi.ScanResult;

import java.util.List;

/**
 * Created by Joost on 18/06/2015.
 */
public interface WifiListener {
    void onWifiData(List<ScanResult> wifiList);
}
