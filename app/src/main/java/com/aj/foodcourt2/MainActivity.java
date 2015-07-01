package com.aj.foodcourt2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.File;


public class MainActivity extends AppCompatActivity {

    Button buttonAcelerometer, buttonGraphs, buttonDeleteFile, buttonGraphsAlexander, buttonGraphsJoost, buttonWifiScanActivity, buttonBluetoothScanActivity, buttonMapActivity, buttonCombinedActivity, buttonMagneticJoostActivity, buttonQueuingActivity, buttonActivityMonitoringActivity;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
}
