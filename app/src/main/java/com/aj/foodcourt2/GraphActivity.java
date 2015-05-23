package com.aj.foodcourt2;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


public class GraphActivity extends ActionBarActivity implements SensorEventListener{

    private GraphView graphX, graphY, graphZ, graphGravityDot, graphHorizontal;
    private TextView tvAccelX, tvAccelY, tvAccelZ, tvTimestamp;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gravitySensor;
    private final int BUFFER_SIZE = 500;
    private final double GRAPH_DOMAIN = (BUFFER_SIZE * 20)/1000.0;
    private final double GRAPH_RANGE = 20;
    private final double TIME_TRESHOLD = 0.350;
    private final double ACCELERATION_TRESHOLD = 12;
    private int memoryPointer = 0;
    private long timestampArray[] = new long[BUFFER_SIZE];
    private double accelXarray[] = new double[BUFFER_SIZE];
    private double accelYarray[] = new double[BUFFER_SIZE];
    private double accelZarray[] = new double[BUFFER_SIZE];
    private double gravityXarray[] = new double[BUFFER_SIZE];
    private double gravityYarray[] = new double[BUFFER_SIZE];
    private double gravityZarray[] = new double[BUFFER_SIZE];

    private double gravityDotArray[] = new double[BUFFER_SIZE];
    private double horizontalArray[] = new double[BUFFER_SIZE];

    private int maximumDetected[] = new int[BUFFER_SIZE];


    private double currentGravityX = 0;
    private double currentGravityY = 0;
    private double currentGravityZ = 0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        //Initiate graphinc shine
        graphX = (GraphView) findViewById(R.id.graph_x);
        graphY = (GraphView) findViewById(R.id.graph_y);
        graphZ = (GraphView) findViewById(R.id.graph_z);
        graphGravityDot = (GraphView) findViewById(R.id.graph_gravity_dot);
        graphHorizontal = (GraphView) findViewById(R.id.graph_horizontal);
        tvAccelX = (TextView) findViewById(R.id.accel_x_tv);
        tvAccelY = (TextView) findViewById(R.id.accel_y_tv);
        tvAccelZ = (TextView) findViewById(R.id.accel_z_tv);
        tvTimestamp = (TextView) findViewById(R.id.timestamp_tv);

        //Initiate sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[]{
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        graphX.addSeries(series);
        graphY.addSeries(series);
        graphZ.addSeries(series);
        graphGravityDot.addSeries(series);
        graphHorizontal.addSeries(series);

    }


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_GAME);
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








        switch (sensor.getType()){
            case Sensor.TYPE_LINEAR_ACCELERATION:

                timestamp = event.timestamp;
                x = event.values[0];
                y = event.values[1];
                z = event.values[2];

                addDataToMemory(timestamp, x, y, z);

                tvTimestamp.setText("Timestamp: " + timestamp);
                tvAccelX.setText("X acceleration: "+ String.format("%-3.6f",x) +" [m/s^2]");
                tvAccelY.setText("Y acceleration: "+ String.format("%-3.6f",y) +" [m/s^2]");
                tvAccelZ.setText("Z acceleration: "+ String.format("%-3.6f",z) +" [m/s^2]");

                break;
            case Sensor.TYPE_ACCELEROMETER:
                timestamp = event.timestamp;
                x = event.values[0];
                y = event.values[1];
                z = event.values[2];

                addDataToMemory(timestamp, x, y, z);

                tvTimestamp.setText("Timestamp: " + timestamp);
                tvAccelX.setText("X acceleration: "+ String.format("%-3.6f",x) +" [m/s^2]");
                tvAccelY.setText("Y acceleration: "+ String.format("%-3.6f",y) +" [m/s^2]");
                tvAccelZ.setText("Z acceleration: "+ String.format("%-3.6f",z) +" [m/s^2]");

                break;
            case Sensor.TYPE_GRAVITY:
                currentGravityX = event.values[0];
                currentGravityY = event.values[1];
                currentGravityZ = event.values[2];
                break;
            default:
                break;

        }


    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void addDataToMemory(long timestamp, double accelX, double accelY, double accelZ){
        if(memoryPointer<BUFFER_SIZE) { //Only do things when it can be stored in array
            timestampArray[memoryPointer] = timestamp;
            accelXarray[memoryPointer] = accelX;
            accelYarray[memoryPointer] = accelY;
            accelZarray[memoryPointer] = accelZ;
            gravityXarray[memoryPointer] = currentGravityX;
            gravityYarray[memoryPointer] = currentGravityY;
            gravityZarray[memoryPointer] = currentGravityZ;

            //increate pointer
            memoryPointer++;

            //if pointer is at end
            if(memoryPointer==BUFFER_SIZE){


                //Display in graph
                long startTime = timestampArray[0];
                DataPoint[] xData = new DataPoint[BUFFER_SIZE];
                DataPoint[] yData = new DataPoint[BUFFER_SIZE];
                DataPoint[] zData = new DataPoint[BUFFER_SIZE];

                DataPoint[] gravityXData = new DataPoint[BUFFER_SIZE];
                DataPoint[] gravityYData = new DataPoint[BUFFER_SIZE];
                DataPoint[] gravityZData = new DataPoint[BUFFER_SIZE];

                DataPoint[] gravityDotProduct = new DataPoint[BUFFER_SIZE];
                DataPoint[] horizontalAccel = new DataPoint[BUFFER_SIZE];
                DataPoint[] peekDataPoints = new DataPoint[BUFFER_SIZE];

                for(int i = 0; i<BUFFER_SIZE; i++){
                    double time = (timestampArray[i]-startTime)/1000000000.0;
                    xData[i] = new DataPoint(time, accelXarray[i]);
                    yData[i] = new DataPoint(time, accelYarray[i]);
                    zData[i] = new DataPoint(time, accelZarray[i]);

                    gravityXData[i] = new DataPoint(time, gravityXarray[i]);
                    gravityYData[i] = new DataPoint(time, gravityYarray[i]);
                    gravityZData[i] = new DataPoint(time, gravityZarray[i]);

                    gravityDotArray[i] = (accelXarray[i]*gravityXarray[i] + accelYarray[i]*gravityYarray[i] + accelZarray[i]*gravityZarray[i])/(Math.sqrt(gravityXarray[i]*gravityXarray[i] + gravityYarray[i]*gravityYarray[i] + gravityZarray[i]*gravityZarray[i]));
                    gravityDotProduct[i] = new DataPoint(time, (accelXarray[i]*gravityXarray[i] + accelYarray[i]*gravityYarray[i] + accelZarray[i]*gravityZarray[i])/(Math.sqrt(gravityXarray[i]*gravityXarray[i] + gravityYarray[i]*gravityYarray[i] + gravityZarray[i]*gravityZarray[i])));
                    double magGravity = Math.sqrt(gravityXarray[i]*gravityXarray[i] + gravityYarray[i]*gravityYarray[i] + gravityZarray[i]*gravityZarray[i]);
                    double verticalComp = (accelXarray[i]*gravityXarray[i] + accelYarray[i]*gravityYarray[i] + accelZarray[i]*gravityZarray[i])/ magGravity;
                    double xg = gravityXarray[i]*verticalComp/magGravity;
                    double yg = gravityYarray[i]*verticalComp/magGravity;
                    double zg = gravityZarray[i]*verticalComp/magGravity;

                    double xx = accelXarray[i]-xg;
                    double yy = accelYarray[i]-yg;
                    double zz = accelZarray[i]-zg;
                    horizontalAccel[i] = new DataPoint(time, Math.sqrt(xx*xx + yy*yy + zz*zz));
                    horizontalArray[i] = Math.sqrt(xx*xx + yy*yy + zz*zz);

                }

                double lastPeekTime = 0.0;
                for(int i = 0; i<BUFFER_SIZE; i++){
                    double time = (timestampArray[i]-startTime)/1000000000.0;
                    //Determine peeks
                    if(i>0 && i < BUFFER_SIZE-1){

                        if(gravityDotArray[i] > gravityDotArray[i-1] && gravityDotArray[i] > gravityDotArray[i+1] && gravityDotArray[i] > ACCELERATION_TRESHOLD){
                            double diffTime = time-lastPeekTime;


                            if(diffTime>TIME_TRESHOLD) {
                                maximumDetected[i] = 1;
                                lastPeekTime = time;
                                Log.d("time","Time: "+diffTime+ "  TRUE");
                            }else{
                                maximumDetected[i] = 0;
                                Log.d("time","Time: "+diffTime+ " FALSE");
                            }

                        }else{
                            maximumDetected[i] = 0;
                        }

//                        //No peeks between 250ms
//                        if(time-lastPeekTime>0.250) {
//                            maximumDetected[i] = ;
//                            lastPeekTime = time;
//                        }else{
//                            maximumDetected[i] = false;
//                        }
                    }else{
                        maximumDetected[i] = 0;
                    }

                    if(maximumDetected[i]==1){
                        peekDataPoints[i] = new DataPoint(time, 20);
                    }else{
                        peekDataPoints[i] = new DataPoint(time,0);
                    }
                }

                //save data
                saveToFile(timestampArray, accelXarray, accelYarray, accelZarray);

                LineGraphSeries<DataPoint> xSeries = new LineGraphSeries<DataPoint>(xData);
                LineGraphSeries<DataPoint> ySeries = new LineGraphSeries<DataPoint>(yData);
                LineGraphSeries<DataPoint> zSeries = new LineGraphSeries<DataPoint>(zData);

                LineGraphSeries<DataPoint> gravityXSeries = new LineGraphSeries<DataPoint>(gravityXData);
                LineGraphSeries<DataPoint> gravityYSeries = new LineGraphSeries<DataPoint>(gravityYData);
                LineGraphSeries<DataPoint> gravityZSeries = new LineGraphSeries<DataPoint>(gravityZData);

                LineGraphSeries<DataPoint> gravityDotSeries = new LineGraphSeries<DataPoint>(gravityDotProduct);
                LineGraphSeries<DataPoint> horizontalSeries = new LineGraphSeries<DataPoint>(horizontalAccel);

                LineGraphSeries<DataPoint> peekSeries = new LineGraphSeries<DataPoint>(peekDataPoints);

                gravityXSeries.setColor(Color.CYAN);
                gravityYSeries.setColor(Color.CYAN);
                gravityZSeries.setColor(Color.CYAN);

                peekSeries.setColor(Color.MAGENTA);

                ySeries.setColor(Color.RED);
                zSeries.setColor(Color.GREEN);
                graphX.removeAllSeries();
                graphY.removeAllSeries();
                graphZ.removeAllSeries();
                graphGravityDot.removeAllSeries();
                graphHorizontal.removeAllSeries();

                graphX.addSeries(xSeries);
                graphY.addSeries(ySeries);
                graphZ.addSeries(zSeries);

                graphX.addSeries(gravityXSeries);
                graphY.addSeries(gravityYSeries);
                graphZ.addSeries(gravityZSeries);

                graphGravityDot.addSeries(gravityDotSeries);
                graphHorizontal.addSeries(horizontalSeries);

                graphGravityDot.addSeries(peekSeries);

                //graphY.addSeries(fftSeries);

                setAxesOfGraphs();
                memoryPointer = 0;
            }
        }else{ //Display error message and reset everything
            Log.d("Storage error", "memoryPointer is out of array bounds");
            memoryPointer = 0;
        }
    }

    private void setAxesOfGraphs(){
        //set axes
        graphX.getViewport().setXAxisBoundsManual(true);
        graphY.getViewport().setXAxisBoundsManual(true);
        graphZ.getViewport().setXAxisBoundsManual(true);
        graphGravityDot.getViewport().setXAxisBoundsManual(true);
        graphHorizontal.getViewport().setXAxisBoundsManual(true);

        graphX.getViewport().setYAxisBoundsManual(true);
        graphY.getViewport().setYAxisBoundsManual(true);
        graphZ.getViewport().setYAxisBoundsManual(true);
        graphGravityDot.getViewport().setYAxisBoundsManual(true);
        graphHorizontal.getViewport().setYAxisBoundsManual(true);

        graphX.getViewport().setMinX(0);
        graphY.getViewport().setMinX(0);
        graphZ.getViewport().setMinX(0);
        graphGravityDot.getViewport().setMinX(0);
        graphHorizontal.getViewport().setMinX(0);

        graphX.getViewport().setMaxX(GRAPH_DOMAIN);
        graphY.getViewport().setMaxX(GRAPH_DOMAIN);
        graphZ.getViewport().setMaxX(GRAPH_DOMAIN);
        graphGravityDot.getViewport().setMaxX(GRAPH_DOMAIN);
        graphHorizontal.getViewport().setMaxX(GRAPH_DOMAIN);

        graphX.getViewport().setMinY(-GRAPH_RANGE);
        graphY.getViewport().setMinY(-GRAPH_RANGE);
        graphZ.getViewport().setMinY(-GRAPH_RANGE);
        graphGravityDot.getViewport().setMinY(-GRAPH_RANGE);
        graphHorizontal.getViewport().setMinY(-GRAPH_RANGE);

        graphX.getViewport().setMaxY(GRAPH_RANGE);
        graphY.getViewport().setMaxY(GRAPH_RANGE);
        graphZ.getViewport().setMaxY(GRAPH_RANGE);
        graphGravityDot.getViewport().setMaxY(GRAPH_RANGE);
        graphHorizontal.getViewport().setMaxY(GRAPH_RANGE);
    }

    private void saveToFile(long[] timestamps, double[] accelXarray, double[] accelYarray, double[] accelZarray){
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < timestamps.length; i++){
//            try {
//                JSONObject jsonObject = new JSONObject();
//                jsonObject.put("timestamp",timestamps[i]);
//                jsonObject.put("accelX",accelXarray[i]);
//                jsonObject.put("accelY",accelYarray[i]);
//                jsonObject.put("accelZ",accelZarray[i]);
//                stringBuilder.append(jsonObject.toString());
//                stringBuilder.append("\n");
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
            stringBuilder.append(timestamps[i]+","
                    +String.format("%-3.4f",accelXarray[i])+","
                    +String.format("%-3.4f",accelYarray[i])+","
                    +String.format("%-3.4f",accelZarray[i])+","
                    +String.format("%-3.4f",gravityXarray[i])+","
                    +String.format("%-3.4f",gravityYarray[i])+","
                    +String.format("%-3.4f",gravityZarray[i])+","
                    +String.format("%-3.4f",gravityDotArray[i])+","
                    +String.format("%-3.4f",horizontalArray[i])+","
                    +Integer.toString(maximumDetected[i]));
            stringBuilder.append("\n");
        }
        SaveToFileTask saveToFileTask = new SaveToFileTask(this);
        saveToFileTask.execute(stringBuilder.toString());
    }
}
