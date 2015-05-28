package com.aj.foodcourt2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

// http://www.codingforandroid.com/2011/01/using-orientation-sensors-simple.html
public class GraphAlexanderActivity extends ActionBarActivity implements SensorEventListener{

    private GraphView graphX, graphY, graphZ, graphGravityDot, graphHorizontal;
    private TextView tvAccelX, tvAccelY, tvAccelZ, tvTimestamp;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gravitySensor;
    private Sensor magneticSensor;

    float[] mGravity;
    float[] mGeomagnetic;

    Float azimut;
    Float degrees;

    float averageMag=0;
    int averageSize=100;
    float[] averageMagArray = new float[averageSize];
    int teller=0;


    EditText etOffset;

    //CustomDrawableView mCustomDrawableView;

    public class CustomDrawableView extends View {
        Paint paint = new Paint();
        public CustomDrawableView(Context context) {
            super(context);
            paint.setColor(0xff00ff00);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setAntiAlias(true);
        };

        protected void onDraw(Canvas canvas) {
            int width = 50;
            int height = 50;
            int centerx = width/2;
            int centery = height/2;
           // canvas.drawLine(centerx, 0, centerx, height, paint);
           // canvas.drawLine(0, centery, width, centery, paint);
            // Rotate the canvas with the azimut
            if (azimut != null)
                canvas.rotate(-azimut*180/3.14158f, centerx, centery);
            paint.setColor(0xff0000ff);
            canvas.drawLine(centerx, -25, centerx, +25, paint);
            //canvas.drawLine(-25, centery, 25, centery, paint);
            canvas.drawText("N", centerx+5, centery-10, paint);
            canvas.drawText("S", centerx-10, centery+15, paint);
            paint.setColor(0xff00ff00);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_alexander);
        tvAccelX = (TextView) findViewById(R.id.accel_x_tv);
        tvAccelY = (TextView) findViewById(R.id.accel_y_tv);
        etOffset = (EditText) findViewById(R.id.editText_offset);

        //  mCustomDrawableView = new CustomDrawableView(this);
        //setContentView(mCustomDrawableView);    // Register the sensor listeners
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //Initiate sensors
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        for (int i = 0; i < averageSize; i++) {
            averageMagArray[i] = 0;
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, gravitySensor, 10000);
        sensorManager.registerListener(this, magneticSensor, 10000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_graph, menu);
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
    public void onSensorChanged(SensorEvent event) {




        Sensor sensor = event.sensor;
        double x, y, z;
        long timestamp;

/*
        switch (sensor.getType()){
            case Sensor.TYPE_LINEAR_ACCELERATION:


                break;
            case Sensor.TYPE_ACCELEROMETER:


                break;
            case Sensor.TYPE_GRAVITY:

                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                timestamp = event.timestamp;
                x = event.values[0];
                y = event.values[1];
                z = event.values[2];

                tvTimestamp.setText("Timestamp: " + timestamp);
                tvAccelX.setText("X : "+ String.format("%-3.6f",x) +" [muT]");
                tvAccelY.setText("Y : "+ String.format("%-3.6f",y) +" [muT]");
                tvAccelZ.setText("Z : "+ String.format("%-3.6f",z) +" [muT]");

                break;
            default:
                break;

        }*/


        if (event.sensor.getType() == Sensor.TYPE_GRAVITY)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values;

            if (mGravity != null && mGeomagnetic != null) {
                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                if (success) {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    azimut = orientation[0]; // orientation contains: azimut, pitch and roll
                    //mCustomDrawableView.setAzimut(azimut);
                    //mCustomDrawableView.invalidate();

                    degrees = (float) azimut * 180 / (float) Math.PI;

                    tvAccelX.setText("angle : " + String.format("%-3.6f", averageMag) + " [radians]");
                    tvAccelY.setText("angle :" + String.format("%-3.6f", degrees) + "[degrees]" + "offset:" + etOffset.getText().toString());

                    averageMagArray[teller] = azimut;
                    if (teller < (averageSize - 1)) {
                        teller++;
                    } else {
                        teller = 0;

                        averageMag = 0;
                        for (int j = 0; j < averageSize; j++) {
                            averageMag = averageMag + (averageMagArray[j] / averageSize);
                        }
//                        degrees = (float) averageMag * 180 / (float) Math.PI;
//
//                        tvAccelX.setText("angle : " + String.format("%-3.6f", averageMag) + " [radians]");
//                        tvAccelY.setText("angle :" + String.format("%-3.6f", degrees) + "[degrees]" + "offset:" + etOffset.getText().toString());


                    }

                    //
                    // tvAccelY.setText("angle : "+ String.format("%-3.6f",azimut) +" [radians]"));
                    //  tvAccelZ.setText("Z : "+ String.format("%-3.6f",azimut*180/3.14159f) +" [degrees]"));
                }
                else {
                    teller=0;
                }
                averageMag=0;
                for (int j=0; j<averageSize;j++){
                    averageMag = averageMag + (averageMagArray[j]/averageSize);
                }
                degrees = (float)averageMag*180/3.14159f;
               //
               // tvAccelY.setText("angle : "+ String.format("%-3.6f",azimut) +" [radians]"));
              //  tvAccelZ.setText("Z : "+ String.format("%-3.6f",azimut*180/3.14159f) +" [degrees]"));
            }
        }

        tvAccelX.setText("angle : "+ String.format("%-3.6f",averageMag) +" [radians]");
       tvAccelY.setText("angle :" + String.format("%-3.6f", degrees)+ "[degrees]" + "offset:" + etOffset.getText().toString());

       //mCustomDrawableView.invalidate();
    }





    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }







}
