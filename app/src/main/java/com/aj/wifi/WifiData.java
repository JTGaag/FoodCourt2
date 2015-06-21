package com.aj.wifi;

import android.net.wifi.ScanResult;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Joost on 19/06/2015.
 */
public class WifiData {
    long timestamp;
    List<ScanResult> wifiList;

    public WifiData(long timestamp, List<ScanResult> wifiList) {
        this.timestamp = timestamp;
        this.wifiList = wifiList;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<ScanResult> getWifiList() {
        return wifiList;
    }

    public JSONArray getWifiListJSONArray(){
        JSONArray jsonArray = new JSONArray();
        for(ScanResult scanResult: wifiList){
            JSONObject tempJSON = new JSONObject();
            try {
                tempJSON.put("BSSID",scanResult.BSSID);
                tempJSON.put("SSID",scanResult.SSID);
                tempJSON.put("level",scanResult.level);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("WiFiData", "Error parsing to jsonObject");
                break;
            }
            jsonArray.put(tempJSON);
        }
        return jsonArray;
    }
}
