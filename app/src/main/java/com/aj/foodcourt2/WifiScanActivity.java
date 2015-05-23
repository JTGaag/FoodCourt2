package com.aj.foodcourt2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;


public class WifiScanActivity extends ActionBarActivity {

    TextView tvScanDetail;
    Button buttonScanWifi;
    WifiManager wifiManager;
    WifiReceiver wifiReceiver;
    List<ScanResult> wifiList;
    StringBuilder stringBuilder = new StringBuilder();
    long scanStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_scan);

        tvScanDetail = (TextView) findViewById(R.id.scan_detail_tv);
        buttonScanWifi = (Button) findViewById(R.id.button_scan_wifi);

        buttonScanWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanStartTime = System.currentTimeMillis();
                wifiManager.getWifiState();
                wifiManager.startScan();
                tvScanDetail.setText("\nStarting Scan...\n");
            }
        });

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiReceiver = new WifiReceiver();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        //wifiManager.startScan();
        tvScanDetail.setText("\nStarting Scan...\n");


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_wifi_scan, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        unregisterReceiver(wifiReceiver);
        super.onPause();
    }

    class WifiReceiver extends BroadcastReceiver {

        /**
         * This method is called when the BroadcastReceiver is receiving an Intent
         * broadcast.  During this time you can use the other methods on
         * BroadcastReceiver to view/modify the current result values.  This method
         * is always called within the main thread of its process, unless you
         * explicitly asked for it to be scheduled on a different thread using
         * {@link Context#registerReceiver(BroadcastReceiver,
         * IntentFilter, String, Handler)}. When it runs on the main
         * thread you should
         * never perform long-running operations in it (there is a timeout of
         * 10 seconds that the system allows before considering the receiver to
         * be blocked and a candidate to be killed). You cannot launch a popup dialog
         * in your implementation of onReceive().
         * <p/>
         * <p><b>If this BroadcastReceiver was launched through a &lt;receiver&gt; tag,
         * then the object is no longer alive after returning from this
         * function.</b>  This means you should not perform any operations that
         * return a result to you asynchronously -- in particular, for interacting
         * with services, you should use
         * {@link Context#startService(Intent)} instead of
         * {@link Context#bindService(Intent, ServiceConnection, int)}.  If you wish
         * to interact with a service that is already running, you can use
         * {@link #peekService}.
         * <p/>
         * <p>The Intent filters used in {@link Context#registerReceiver}
         * and in application manifests are <em>not</em> guaranteed to be exclusive. They
         * are hints to the operating system about how to find suitable recipients. It is
         * possible for senders to force delivery to specific recipients, bypassing filter
         * resolution.  For this reason, {@link #onReceive(Context, Intent) onReceive()}
         * implementations should respond only to known actions, ignoring any unexpected
         * Intents that they may receive.
         *
         * @param context The Context in which the receiver is running.
         * @param intent  The Intent being received.
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            stringBuilder = new StringBuilder();
            wifiList = wifiManager.getScanResults();
            stringBuilder.append("Scan time (sec): " + ((System.currentTimeMillis()-scanStartTime)/1000.0));
            stringBuilder.append("\n\n");
            for(int i=0; i<wifiList.size(); i++){
                stringBuilder.append(new Integer(i+1).toString() + ": ");
                stringBuilder.append(wifiList.get(i).SSID + " . " + WifiManager.calculateSignalLevel(wifiList.get(i).level, 255) + " . " + wifiList.get(i).frequency);
                //stringBuilder.append(wifiList.get(i).toString());
                stringBuilder.append("\n\n");
            }
            tvScanDetail.setText(stringBuilder);
        }
    }
}
