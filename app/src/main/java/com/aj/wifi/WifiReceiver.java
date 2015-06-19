package com.aj.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.List;

/**
 * Created by Joost on 18/06/2015.
 */
public class WifiReceiver extends BroadcastReceiver {

    List<ScanResult> wifiList;
    WifiManager wifiManager;
    WifiListener wifiListener;

    public WifiReceiver(WifiListener wifiListener, WifiManager wifiManager){
        this.wifiListener = wifiListener;
        this.wifiManager = wifiManager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        wifiList = wifiManager.getScanResults();
        wifiListener.onWifiData(wifiList);
//
//        stringBuilder.append("Scan time (sec): " + ((System.currentTimeMillis()-scanStartTime)/1000.0));
//        stringBuilder.append("\n\n");
//        for(int i=0; i<wifiList.size(); i++){
//            stringBuilder.append(new Integer(i+1).toString() + ": ");
//            stringBuilder.append(wifiList.get(i).BSSID + " : " + wifiList.get(i).SSID + " . " + WifiManager.calculateSignalLevel(wifiList.get(i).level, 255) + " . " + wifiList.get(i).frequency);
//            //stringBuilder.append(wifiList.get(i).toString());
//            stringBuilder.append("\n\n");
//        }
//        tvScanDetail.setText(stringBuilder);
    }
}
