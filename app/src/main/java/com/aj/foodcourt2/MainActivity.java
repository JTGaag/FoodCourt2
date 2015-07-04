package com.aj.foodcourt2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;


public class MainActivity extends AppCompatActivity {

    Button buttonAcelerometer, buttonGraphs, buttonDeleteFile, buttonGraphsAlexander, buttonGraphsJoost, buttonWifiScanActivity, buttonBluetoothScanActivity, buttonMapActivity, buttonCombinedActivity, buttonMagneticJoostActivity, buttonQueuingActivity, buttonActivityMonitoringActivity, buttonLocalization;
    Context context = this;

    private final static String PREF_NAME = "foodcourtPreferenceFile";
    private final static String STEP_MODE_NAME = "prefStepMode";
    private final static String DEBUG_MODE_NAME = "prefDebugMode";
    private final static String LOCATION_MODE_NAME = "prefLocationMode";
    private final static String LOCATION_MANUAL_NAME = "prefLocationManual";
    private final static String LOCATION_AUTO_NAME = "prefLocationAuto";
    SharedPreferences settings;

    Location locMe = new Location("");
    Location locEWI = new Location("");
    Location locRDW = new Location("");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        buttonAcelerometer = (Button)findViewById(R.id.button_accelerometer);
        buttonGraphs = (Button)findViewById(R.id.button_graphs);
        buttonGraphsAlexander = (Button)findViewById(R.id.button_graphs_alexander);
        buttonGraphsJoost = (Button)findViewById(R.id.button_graphs_joost);
        buttonDeleteFile = (Button) findViewById(R.id.button_delete_file);
        buttonWifiScanActivity = (Button) findViewById(R.id.button_wifi_scan_activity);
        buttonBluetoothScanActivity = (Button) findViewById(R.id.button_bluetooth_scan_activity);
        buttonMapActivity = (Button) findViewById(R.id.button_map_activity);
        buttonCombinedActivity = (Button) findViewById(R.id.button_combined_activity);
        buttonMagneticJoostActivity = (Button) findViewById(R.id.button_magnetic_joost_activity);
        buttonQueuingActivity = (Button) findViewById(R.id.button_queuing_activity);
        buttonActivityMonitoringActivity = (Button) findViewById(R.id.button_monitoring);
        buttonLocalization = (Button) findViewById(R.id.button_localization);


        buttonAcelerometer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventActivity = new Intent(MainActivity.this, AccelerometerActivity.class);
                startActivity(eventActivity);
            }
        });

        buttonGraphs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventActivity = new Intent(MainActivity.this, GraphActivity.class);
                startActivity(eventActivity);
            }
        });

        buttonGraphsAlexander.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventActivity = new Intent(MainActivity.this, GraphAlexanderActivity.class);
                startActivity(eventActivity);
            }
        });

        buttonGraphsJoost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventActivity = new Intent(MainActivity.this, GraphJoostActivity.class);
                startActivity(eventActivity);
            }
        });

        buttonDeleteFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = "accelerometerData.txt";
                File traceFile = new File(context.getExternalFilesDir(null), fileName);
                if(traceFile.exists()){
                    traceFile.delete();
                }
            }
        });

        buttonWifiScanActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventActivity = new Intent(MainActivity.this, WifiScanActivity.class);
                startActivity(eventActivity);
            }
        });

        buttonBluetoothScanActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventActivity = new Intent(MainActivity.this, BluetoothScanActivity.class);
                startActivity(eventActivity);
            }
        });

        buttonMapActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventActivity = new Intent(MainActivity.this, MapActivity.class);
                startActivity(eventActivity);
            }
        });

        buttonCombinedActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventActivity = new Intent(MainActivity.this, CombinedActivity.class);
                startActivity(eventActivity);
            }
        });

        buttonMagneticJoostActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventActivity = new Intent(MainActivity.this, MagneticJoostActivity.class);
                startActivity(eventActivity);
            }
        });

        buttonQueuingActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventActivity = new Intent(MainActivity.this, QueuingActivity.class);
                startActivity(eventActivity);
            }
        });

        buttonActivityMonitoringActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventActivity = new Intent(MainActivity.this, ActivityMonitoringActivity.class);
                startActivity(eventActivity);
            }
        });
        buttonLocalization.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventActivity = new Intent(MainActivity.this, LocalizationActivity.class);
                startActivity(eventActivity);
            }
        });


        //Location stuff
        //Localization
        locRDW.setLatitude(52.00069);
        locRDW.setLongitude(4.36907);
        locEWI.setLatitude(51.99885);
        locEWI.setLongitude(4.37395);

        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        locMe.setLatitude(location.getLatitude());
        locMe.setLongitude(location.getLongitude());

        double distanceEWI = locMe.distanceTo(locEWI);
        double distanceRDW = locMe.distanceTo(locRDW);

        SharedPreferences.Editor editor = settings.edit();
        if(distanceEWI<distanceRDW){//Closer to EWI
            editor.putInt(LOCATION_AUTO_NAME, 1);
            Toast.makeText(this, "Closer to EWI", Toast.LENGTH_SHORT).show();
        }else{//Closer to RDW
            editor.putInt(LOCATION_AUTO_NAME, 2);
            Toast.makeText(this, "Closer to RDW", Toast.LENGTH_SHORT).show();
        }
        editor.commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            Intent eventActivity = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(eventActivity);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();
        boolean debugMode = settings.getBoolean(DEBUG_MODE_NAME, false);
        int visibility = View.GONE;
        //set views
        if(debugMode){
            visibility = View.VISIBLE;
        }

        buttonQueuingActivity.setVisibility(visibility);
        buttonAcelerometer.setVisibility(visibility);
        buttonBluetoothScanActivity.setVisibility(visibility);
        buttonCombinedActivity.setVisibility(visibility);
        buttonDeleteFile.setVisibility(visibility);
        buttonGraphs.setVisibility(visibility);
        buttonGraphsAlexander.setVisibility(visibility);
        buttonGraphsJoost.setVisibility(visibility);
        buttonMagneticJoostActivity.setVisibility(visibility);
        buttonMapActivity.setVisibility(visibility);
        buttonWifiScanActivity.setVisibility(visibility);
    }
}
