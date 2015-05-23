package com.aj.foodcourt2;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.aj.bluetooth.BeaconObject;
import com.aj.enums.SettingsConstants;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;


public class BluetoothScanActivity extends ActionBarActivity implements BeaconConsumer{
    protected static final String TAG_ALTBEACON = "AltBeacon";

    Button buttonBtStart, buttonBtStop;
    TextView tvbleutoothDevices;
    org.altbeacon.beacon.BeaconManager beaconManagerAlt;
    BeaconConsumer beaconConsumer = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_scan);

        //Connect layout with code
        tvbleutoothDevices = (TextView) findViewById(R.id.bluetooth_devices_tv);
        buttonBtStart = (Button) findViewById(R.id.bt_start);
        buttonBtStop = (Button) findViewById(R.id.bt_stop);

        beaconManagerAlt = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
        beaconManagerAlt.getBeaconParsers().add(new BeaconParser().setBeaconLayout(SettingsConstants.BEACON_LAYOUT));

        buttonBtStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beaconManagerAlt.bind(beaconConsumer);
            }
        });

        buttonBtStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAltBeacon();
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAltBeacon();
    }

    private void stopAltBeacon(){
        if (beaconManagerAlt.isBound(this)) {
            beaconManagerAlt.unbind(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bluetooth_scan, menu);
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

    @Override
    public void onBeaconServiceConnect() {
        beaconManagerAlt.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    for (Beacon beacon : beacons) {
                        final BeaconObject beaconObject = new BeaconObject(beacon);
                        Log.i(TAG_ALTBEACON, beaconObject.toString());
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                /updateScanResults(beaconObject);
//                            }
//                        });

                    }
                }
            }
        });

        try {
            beaconManagerAlt.startRangingBeaconsInRegion(
                    new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {    }
    }
}
