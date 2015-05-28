package com.aj.foodcourt2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

import java.util.Arrays;
import java.util.List;


public class WifiScanActivity extends ActionBarActivity implements SensorEventListener{

    TextView tvScanDetail, tvZRotation, tvZMaxRotation, tvZMinRotation, tvRotation;
    Button buttonScanWifi;
    WifiManager wifiManager;
    WifiReceiver wifiReceiver;
    List<ScanResult> wifiList;
    StringBuilder stringBuilder = new StringBuilder();
    long scanStartTime;

    final double NS2S = 1.0/1000000000.0;

    SensorManager sensorManager;
    Sensor gyroSensor;

    int nBuffer = 100;
    double rotArray[] = new double[nBuffer];
    int bufferIndex = 0;

    long lastTime = 0;
    int nSum = 60;
    double sumRot = 0;
    int sumIndex = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_scan);

        tvScanDetail = (TextView) findViewById(R.id.scan_detail_tv);
        tvZRotation = (TextView) findViewById(R.id.z_rotation);
        tvZMaxRotation = (TextView) findViewById(R.id.max_z_rotation);
        tvZMinRotation = (TextView) findViewById(R.id.min_z_rotation);
        tvRotation = (TextView) findViewById(R.id.rotation);
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

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);




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
        super.onResume();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        sensorManager.registerListener(this, gyroSensor, 50000);

    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiReceiver);
        sensorManager.unregisterListener(this);
    }

    /**
     * Called when sensor values have changed.
     * <p>See {@link SensorManager SensorManager}
     * for details on possible sensor types.
     * <p>See also {@link SensorEvent SensorEvent}.
     * <p/>
     * <p><b>NOTE:</b> The application doesn't own the
     * {@link SensorEvent event}
     * object passed as a parameter and therefore cannot hold on to it.
     * The object may be part of an internal pool and may be reused by
     * the framework.
     *
     * @param event the {@link SensorEvent SensorEvent}.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        double xRot, yRot, zRot, zMax;
        long timestamp = event.timestamp;
        if(lastTime==0){
            lastTime = timestamp;
        }

        switch (sensor.getType()){
            case Sensor.TYPE_GYROSCOPE:
                long timeDif = timestamp - lastTime;
                lastTime = timestamp;
                double timeDifS = timeDif * NS2S;

                //Log.d("TimedifS", "TimedifS: "+timeDifS);

                zRot = event.values[2]; //Rotation arround the z axis
                zRot = zRot *180 / Math.PI;
                tvZRotation.setText("Rotation (z-axis): " + String.format("%-3.1f",zRot) + " [Degrees/s]");

                //Save in buffer
                rotArray[bufferIndex] = zRot;
                bufferIndex++;
                if(bufferIndex==nBuffer){
                    bufferIndex = 0;
                    Arrays.sort(rotArray);
                    zMax = rotArray[nBuffer-1];
                    tvZMaxRotation.setText("Max rotation (z-axis): " + String.format("%-3.1f",zMax) + " [Degrees/s]");
                    tvZMinRotation.setText("Min rotation (z-axis): " + String.format("%-3.1f",zMax) + " [Degrees/s]");
                }

                sumRot += (zRot * timeDifS);
                sumIndex++;
                if(sumIndex == nSum){
                    sumIndex = 0;
                    tvRotation.setText("Rotation last second: " + String.format("%-3.1f",sumRot) + " [Degrees]");

                    sumRot = 0;
                }

                break;
            default:
                break;
        }
    }

    /**
     * Called when the accuracy of the registered sensor has changed.
     * <p/>
     * <p>See the SENSOR_STATUS_* constants in
     * {@link SensorManager SensorManager} for details.
     *
     * @param sensor
     * @param accuracy The new accuracy of this sensor, one of
     *                 {@code SensorManager.SENSOR_STATUS_*}
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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
         * {@link Context#/bindService(Intent, /ServiceConnection, int)}.  If you wish
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
                stringBuilder.append(wifiList.get(i).BSSID + " : " + wifiList.get(i).SSID + " . " + WifiManager.calculateSignalLevel(wifiList.get(i).level, 255) + " . " + wifiList.get(i).frequency);
                //stringBuilder.append(wifiList.get(i).toString());
                stringBuilder.append("\n\n");
            }
            tvScanDetail.setText(stringBuilder);
        }
    }
}
