package com.aj.foodcourt2;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MagneticJoostActivity extends ActionBarActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor magneticSensor;

    TextView tvXaxis, tvYaxis, tvZaxis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magnetic_joost);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        tvXaxis = (TextView)findViewById(R.id.tv_x);
        tvYaxis = (TextView)findViewById(R.id.tv_y);
        tvZaxis = (TextView)findViewById(R.id.tv_z);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_magnetic_joost, menu);
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
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, magneticSensor, 10000);
    }


    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;

        switch(sensor.getType()){
            case Sensor.TYPE_ORIENTATION:
                tvXaxis.setText("X-axis: " + event.values[0]);
                tvYaxis.setText("Y-axis: " + event.values[1]);
                tvZaxis.setText("Z-axis: " + event.values[2]);
                break;
            default:
                break;
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
