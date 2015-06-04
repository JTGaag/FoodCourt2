package com.aj.foodcourt2;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.aj.queuing.MacroBlockObject;
import com.aj.queuing.QueuingDataHandler;
import com.aj.queuing.QueuingListener;
import com.aj.queuing.QueuingMonitoringObject;
import com.aj.queuing.QueuingSensorData;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.jtransforms.fft.DoubleFFT_1D;

import java.util.ArrayList;
import java.util.Date;


public class GraphJoostActivity extends ActionBarActivity implements SensorEventListener, QueuingListener{

    private GraphView graphX, graphY, graphZ, graphGravityDot, graphHorizontal, graphHandler, graphFourier, graphFourierNormalized;
    private TextView tvAccelX, tvAccelY, tvAccelZ, tvTimestamp, tvLight, tvStepCount, tvProximity, tvBlockStates;
    private TextView tvSum1, tvSum2, tvSum3, tvSum4, tvSum5, tvSum6;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gravitySensor;
    private Sensor lightSensor;
    private Sensor proximitySensor;
    private final int BUFFER_SIZE = 500;
    private final double GRAPH_DOMAIN = (BUFFER_SIZE * 20)/1000.0;
    private final double GRAPH_RANGE = 20;
    private final double TIME_TRESHOLD = 0.350;
    private final double ACCELERATION_TRESHOLD = 12;
    private final String FILENAME = "newData.txt";
    private int memoryPointer = 0;
    private long timestampArray[] = new long[BUFFER_SIZE];
    private double accelXarray[] = new double[BUFFER_SIZE];
    private double accelYarray[] = new double[BUFFER_SIZE];
    private double accelZarray[] = new double[BUFFER_SIZE];
    private double gravityXarray[] = new double[BUFFER_SIZE];
    private double gravityYarray[] = new double[BUFFER_SIZE];
    private double gravityZarray[] = new double[BUFFER_SIZE];

    private double lightArray[] = new double[BUFFER_SIZE];

    private double gravityDotArray[] = new double[BUFFER_SIZE];
    private double horizontalArray[] = new double[BUFFER_SIZE];

    private int maximumDetected[] = new int[BUFFER_SIZE];
    private int gravityShift[] = new int[BUFFER_SIZE];
    private int microActivity[] = new int[BUFFER_SIZE];

    private final int GRAVITY_TIME_DIF = 20;
    private final int CHECK_DIF = 25;
    private final double GRAVITY_DIF_TRESHOLD = 7.0;

    private final int ACTIVITY_STIL = 0;
    private final int ACTIVITY_WALKING = 1;

    private double currentGravityX = 0;
    private double currentGravityY = 0;
    private double currentGravityZ = 0;

    private double currentLightLevel = 0;
    private double currentProximityLevel = 0;

    private QueuingDataHandler queuingDataHandler;

    private ArrayList<MacroBlockObject> blockBuffer = new ArrayList<MacroBlockObject>();

    //For saving activity information
    private ArrayList<MacroBlockObject> blockToAnalyse = new ArrayList<MacroBlockObject>();
    Button buttonStartActivity, buttonStopActivity;
    long startActivityTime, stopActivityTime;
    int startActivityWait = 0;
    int stopActivityWait = 0;
    boolean started = false;
    boolean stopped = false;

    final Context context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_joost);

        //Initiate graphinc shine
        graphX = (GraphView) findViewById(R.id.graph_x);
        graphY = (GraphView) findViewById(R.id.graph_y);
        graphZ = (GraphView) findViewById(R.id.graph_z);
        graphGravityDot = (GraphView) findViewById(R.id.graph_gravity_dot);
        graphHorizontal = (GraphView) findViewById(R.id.graph_horizontal);
        graphHandler = (GraphView) findViewById(R.id.graph_handler);
        graphFourier = (GraphView) findViewById(R.id.graph_fourier);
        graphFourierNormalized = (GraphView) findViewById(R.id.graph_fourier_normalized);
        tvAccelX = (TextView) findViewById(R.id.accel_x_tv);
        tvAccelY = (TextView) findViewById(R.id.accel_y_tv);
        tvAccelZ = (TextView) findViewById(R.id.accel_z_tv);
        tvTimestamp = (TextView) findViewById(R.id.timestamp_tv);
        tvLight = (TextView) findViewById(R.id.light_tv);
        tvProximity = (TextView) findViewById(R.id.proximity_tv);
        tvStepCount = (TextView) findViewById(R.id.step_count_tv);
        tvBlockStates = (TextView) findViewById(R.id.block_states_tv);

        tvSum1 = (TextView) findViewById(R.id.sum1_tv);
        tvSum2 = (TextView) findViewById(R.id.sum2_tv);
        tvSum3 = (TextView) findViewById(R.id.sum3_tv);
        tvSum4 = (TextView) findViewById(R.id.sum4_tv);
        tvSum5 = (TextView) findViewById(R.id.sum5_tv);
        tvSum6 = (TextView) findViewById(R.id.sum6_tv);

        //Initiate sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        //Initate dataHandler (Bandaries must be more equal or more than 150/points per segemnts)
        queuingDataHandler = new QueuingDataHandler(this, 50, 3, 7);

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
        graphHandler.addSeries(series);
        graphFourier.addSeries(series);
        graphFourierNormalized.addSeries(series);

        //Set axis for the graphs
        setAxesOfGraphs();

        //set first parts of gravityshift
        for(int i=0; i<GRAVITY_TIME_DIF; i++){
            gravityShift[i] = 1;
        }

        //Things for activity monitoring
        buttonStartActivity = (Button)findViewById(R.id.button_start_activity);
        buttonStopActivity = (Button)findViewById(R.id.button_stop_activity);

        buttonStartActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                started = true;
                startActivityTime = System.currentTimeMillis();
                Log.d("Activity logging", "Activity startTime: " + startActivityTime);
                blockToAnalyse.clear();
            }
        });

        buttonStopActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopped = true;
                stopActivityTime = System.currentTimeMillis();
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
//        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
//        sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_GAME);
//        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_GAME);
//        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_GAME);

        sensorManager.registerListener(this, accelerometer, 10000);
        sensorManager.registerListener(this, gravitySensor, 10000);
        sensorManager.registerListener(this, lightSensor, 10000);
        sensorManager.registerListener(this, proximitySensor, 10000);
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

                //addDataToMemory(timestamp, x, y, z);

                tvTimestamp.setText("Timestamp: " + timestamp);
                tvAccelX.setText("X acceleration: "+ String.format("%-3.6f",x) +" [m/s^2]");
                tvAccelY.setText("Y acceleration: "+ String.format("%-3.6f",y) +" [m/s^2]");
                tvAccelZ.setText("Z acceleration: "+ String.format("%-3.6f",z) +" [m/s^2]");

                //Add data to handler
                queuingDataHandler.addRawData(new QueuingSensorData(event.timestamp, event.values[0], event.values[1], event.values[2], currentGravityX, currentGravityY, currentGravityZ, currentLightLevel, currentProximityLevel));

                break;
            case Sensor.TYPE_GRAVITY:
                currentGravityX = event.values[0];
                currentGravityY = event.values[1];
                currentGravityZ = event.values[2];
                break;
            case Sensor.TYPE_LIGHT:
                tvLight.setText("Light: "+ event.values[0] +" [lux]");
                currentLightLevel = event.values[0];
                break;
            case Sensor.TYPE_PROXIMITY:
                tvProximity.setText("Proximity: "+ event.values[0] +" [cm]");
                currentProximityLevel = event.values[0];
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

            lightArray[memoryPointer] = currentLightLevel;

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
                DataPoint[] microActivityDataPoints = new DataPoint[BUFFER_SIZE];
                DataPoint[] gravityShiftDataPoints = new DataPoint[BUFFER_SIZE];
                DataPoint[] lightLevelDataPoints = new DataPoint[BUFFER_SIZE];

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

                    lightLevelDataPoints[i] = new DataPoint(time, lightArray[i]/10);

                }

                //Look for gravity shifts
                for(int i=GRAVITY_TIME_DIF; i<BUFFER_SIZE; i++){
                    double xShift = gravityXarray[i]-gravityXarray[i-GRAVITY_TIME_DIF];
                    double yShift = gravityYarray[i]-gravityYarray[i-GRAVITY_TIME_DIF];
                    double zShift = gravityZarray[i]-gravityZarray[i-GRAVITY_TIME_DIF];
                    double totalShift = Math.sqrt(xShift*xShift + yShift*yShift + zShift*zShift);
                    if (totalShift>GRAVITY_DIF_TRESHOLD){
                        gravityShift[i] = 1;
                    }else{
                        gravityShift[i] = 0;
                    }
                }
                //Set points in dataPoint array
                for(int i=0; i<BUFFER_SIZE; i++){
                    double time = (timestampArray[i]-startTime)/1000000000.0;
                    gravityShiftDataPoints[i] = new DataPoint(time, gravityShift[i]*20);
                }

                //Get all the peeks (this will be improved by Alexander
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
                        //Go over all values arround this point so see if there is agravity shift
                        boolean gravShift = false;
                        for(int j=Math.max(0,i-CHECK_DIF); j<Math.min(BUFFER_SIZE,i+CHECK_DIF); j++){
                            if(gravityShift[j]==1){
                                gravShift = true;
                                break;
                            }
                        }

                        //If no gravity shift is detected then a maximumis correct else it is set back
                        if(!gravShift){
                            peekDataPoints[i] = new DataPoint(time, 20);
                        }else{
                            peekDataPoints[i] = new DataPoint(time, 0);
                            maximumDetected[i] = 0;
                        }

                    }else{
                        peekDataPoints[i] = new DataPoint(time,0);
                    }
                }




                for(int i=0; i<BUFFER_SIZE; i+=25){
                    int lengthLeft = BUFFER_SIZE - i;
                    boolean walking = false;
                    for(int j=0; j<(Math.min(25, lengthLeft)); j++){
                        if(maximumDetected[i+j]==1){
                            walking = true;
                            break;
                        }
                    }
                    for(int j=0; j<(Math.min(25, lengthLeft)); j++){
                        double time = (timestampArray[i+j]-startTime)/1000000000.0;
                        if(walking){
                            microActivity[i+j] = 1;
                            microActivityDataPoints[i+j] = new DataPoint(time, 20);
                        }else{
                            microActivity[i+j] = 0;
                            microActivityDataPoints[i+j] = new DataPoint(time, 0);
                        }
                    }
                }

                //save data
                //saveToFile(timestampArray, accelXarray, accelYarray, accelZarray);

                LineGraphSeries<DataPoint> xSeries = new LineGraphSeries<DataPoint>(xData);
                LineGraphSeries<DataPoint> ySeries = new LineGraphSeries<DataPoint>(yData);
                LineGraphSeries<DataPoint> zSeries = new LineGraphSeries<DataPoint>(zData);

                LineGraphSeries<DataPoint> gravityXSeries = new LineGraphSeries<DataPoint>(gravityXData);
                LineGraphSeries<DataPoint> gravityYSeries = new LineGraphSeries<DataPoint>(gravityYData);
                LineGraphSeries<DataPoint> gravityZSeries = new LineGraphSeries<DataPoint>(gravityZData);

                LineGraphSeries<DataPoint> gravityDotSeries = new LineGraphSeries<DataPoint>(gravityDotProduct);
                LineGraphSeries<DataPoint> horizontalSeries = new LineGraphSeries<DataPoint>(horizontalAccel);

                LineGraphSeries<DataPoint> peekSeries = new LineGraphSeries<DataPoint>(peekDataPoints);
                LineGraphSeries<DataPoint> microActivitySeries = new LineGraphSeries<DataPoint>(microActivityDataPoints);
                LineGraphSeries<DataPoint> gravityShiftSeries = new LineGraphSeries<DataPoint>(gravityShiftDataPoints);

                LineGraphSeries<DataPoint> lightLevelSeries = new LineGraphSeries<DataPoint>(lightLevelDataPoints);

                gravityXSeries.setColor(Color.CYAN);
                gravityYSeries.setColor(Color.CYAN);
                gravityZSeries.setColor(Color.CYAN);

                peekSeries.setColor(Color.GREEN);
                microActivitySeries.setColor(Color.YELLOW);
                gravityShiftSeries.setColor(Color.rgb(255, 102, 0));

                lightLevelSeries.setColor(Color.YELLOW);

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
                //graphGravityDot.addSeries(microActivitySeries);
                graphGravityDot.addSeries(gravityShiftSeries);
                graphGravityDot.addSeries(lightLevelSeries);

                //graphY.addSeries(fftSeries);

                //setAxesOfGraphs();
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
        graphHandler.getViewport().setXAxisBoundsManual(true);
        graphFourier.getViewport().setXAxisBoundsManual(true);
        graphFourierNormalized.getViewport().setXAxisBoundsManual(true);

        graphX.getViewport().setYAxisBoundsManual(true);
        graphY.getViewport().setYAxisBoundsManual(true);
        graphZ.getViewport().setYAxisBoundsManual(true);
        graphGravityDot.getViewport().setYAxisBoundsManual(true);
        graphHorizontal.getViewport().setYAxisBoundsManual(true);
        graphHandler.getViewport().setYAxisBoundsManual(true);
        graphFourier.getViewport().setYAxisBoundsManual(true);
        graphFourierNormalized.getViewport().setYAxisBoundsManual(true);

        graphX.getViewport().setMinX(0);
        graphY.getViewport().setMinX(0);
        graphZ.getViewport().setMinX(0);
        graphGravityDot.getViewport().setMinX(0);
        graphHorizontal.getViewport().setMinX(0);
        graphHandler.getViewport().setMinX(0);
        graphFourier.getViewport().setMinX(0);
        graphFourierNormalized.getViewport().setMinX(0);

        graphX.getViewport().setMaxX(GRAPH_DOMAIN);
        graphY.getViewport().setMaxX(GRAPH_DOMAIN);
        graphZ.getViewport().setMaxX(GRAPH_DOMAIN);
        graphGravityDot.getViewport().setMaxX(GRAPH_DOMAIN);
        graphHorizontal.getViewport().setMaxX(GRAPH_DOMAIN);
        graphHandler.getViewport().setMaxX(8);
        graphFourier.getViewport().setMaxX(6);
        graphFourierNormalized.getViewport().setMaxX(6);

        graphX.getViewport().setMinY(-GRAPH_RANGE);
        graphY.getViewport().setMinY(-GRAPH_RANGE);
        graphZ.getViewport().setMinY(-GRAPH_RANGE);
        graphGravityDot.getViewport().setMinY(0);
        graphHorizontal.getViewport().setMinY(0);
        graphHandler.getViewport().setMinY(0);
        graphFourier.getViewport().setMinY(0);
        graphFourierNormalized.getViewport().setMinY(0);

        graphX.getViewport().setMaxY(GRAPH_RANGE);
        graphY.getViewport().setMaxY(GRAPH_RANGE);
        graphZ.getViewport().setMaxY(GRAPH_RANGE);
        graphGravityDot.getViewport().setMaxY(GRAPH_RANGE);
        graphHorizontal.getViewport().setMaxY(GRAPH_RANGE);
        graphHandler.getViewport().setMaxY(GRAPH_RANGE);
        graphFourier.getViewport().setMaxY(200);
        graphFourierNormalized.getViewport().setMaxY(0.15);
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

    private void saveToFile(QueuingSensorData[] data){
        StringBuilder stringBuilder = new StringBuilder();
        for(int i=0; i<data.length; i++){
            stringBuilder.append(data[i].getTimestamp()+","
                    +String.format("%-3.4f",data[i].getAccelerationX())+","
                    +String.format("%-3.4f",data[i].getAccelerationY())+","
                    +String.format("%-3.4f",data[i].getAccelerationZ())+","
                    +String.format("%-3.4f",data[i].getGravityX())+","
                    +String.format("%-3.4f",data[i].getGravityY())+","
                    +String.format("%-3.4f",data[i].getGravityZ())+","
                    +String.format("%-3.4f",data[i].getGravityDotProduct())+","
                    +String.format("%-3.4f",data[i].getProximityLevel())+","
                    +Integer.toString(data[i].getStepIdentifier()));
            stringBuilder.append("\n");
        }
        SaveToFileTask saveToFileTask = new SaveToFileTask(this, FILENAME);
        saveToFileTask.execute(stringBuilder.toString());
    }

    @Override
    public void onNewDataBlock(int count, QueuingSensorData[] dataArray, MacroBlockObject blockObject) {

        //Saving files
        saveToFile(dataArray);

        tvStepCount.setText("StepCount: "+count);
        long startTime = dataArray[0].getTimestamp();
        DataPoint[] accelData = new DataPoint[dataArray.length];
        DataPoint[] maximumInStepDetection = new DataPoint[dataArray.length];
        DataPoint[] pocketDetection = new DataPoint[dataArray.length];
        DataPoint[] noStepNoise = new DataPoint[dataArray.length];
        for(int i=0; i<dataArray.length; i++){
            //Log.d("test","Datapoint: "+dataArray[i].getGravityDotProduct());
            accelData[i] = new DataPoint(((dataArray[i].getTimestamp()-startTime)/1000000000.0), dataArray[i].getGravityDotProduct());
            if(dataArray[i].getStepIdentifier()==2){
                maximumInStepDetection[i] = new DataPoint(((dataArray[i].getTimestamp()-startTime)/1000000000.0),20);
            }else{
                maximumInStepDetection[i] = new DataPoint(((dataArray[i].getTimestamp()-startTime)/1000000000.0),0);
            }

            //Pocket distrubing
            if(dataArray[i].isPocketDisturbing()){
                pocketDetection[i] = new DataPoint(((dataArray[i].getTimestamp()-startTime)/1000000000.0),12);
            }else{
                pocketDetection[i] = new DataPoint(((dataArray[i].getTimestamp()-startTime)/1000000000.0),0);
            }

            //No Step Noise
            if(dataArray[i].isNoStepNoise()){
                noStepNoise[i] = new DataPoint(((dataArray[i].getTimestamp()-startTime)/1000000000.0),12);
            }else{
                noStepNoise[i] = new DataPoint(((dataArray[i].getTimestamp()-startTime)/1000000000.0),0);
            }
        }
        LineGraphSeries<DataPoint> accelSeries = new LineGraphSeries<DataPoint>(accelData);
        LineGraphSeries<DataPoint> stepSeries = new LineGraphSeries<DataPoint>(maximumInStepDetection);
        LineGraphSeries<DataPoint> pocketSeries = new LineGraphSeries<DataPoint>(pocketDetection);
        LineGraphSeries<DataPoint> noStepNoiseSeries = new LineGraphSeries<DataPoint>(noStepNoise);

        accelSeries.setColor(Color.BLUE);
        stepSeries.setColor(Color.GREEN);
        pocketSeries.setColor(Color.RED);
        noStepNoiseSeries.setColor(Color.YELLOW);

        graphHandler.removeAllSeries();
        graphHandler.addSeries(accelSeries);
        graphHandler.addSeries(stepSeries);
        graphHandler.addSeries(pocketSeries);
        graphHandler.addSeries(noStepNoiseSeries);

        //Do block analysis
        blockBuffer.add(blockObject);
        if(blockBuffer.size()>5){
            blockBuffer.remove(0);
        }
        if(blockBuffer.size()==5){
            if(blockBuffer.get(2).getNumberOfStepSegments()>1 && blockBuffer.get(2).getNumberOfStepSegments()<8){//Kijken of er gemoved wordt
                int tempQueuingNumber = 0;
                for(int i=0; i<5; i++){
                    if((blockBuffer.get(i).getBlockMacroState()==MacroBlockObject.MacroState.QUEUING || blockBuffer.get(i).getBlockMacroState()==MacroBlockObject.MacroState.MOVING)&& i!=2){
                        tempQueuingNumber++;
                    }
                }
                if(tempQueuingNumber>2){
                    MacroBlockObject tmpObj = blockBuffer.get(2);
                    tmpObj.setBlockMacroState(MacroBlockObject.MacroState.MOVING);
                    blockBuffer.remove(2);
                    blockBuffer.add(2,tmpObj);
                }

//                if(blockBuffer.get(0).getBlockMacroState() == MacroBlockObject.MacroState.QUEUING && blockBuffer.get(2).getBlockMacroState() == MacroBlockObject.MacroState.QUEUING){
//                    MacroBlockObject tmpObj = blockBuffer.get(1);
//                    tmpObj.setBlockMacroState(MacroBlockObject.MacroState.MOVING);
//                    blockBuffer.remove(1);
//                    blockBuffer.add(1,tmpObj);
//                }
            }

            if(blockBuffer.get(2).getNumberOfStepSegments()==1 && blockBuffer.get(3).getNumberOfStepSegments()==1 && blockBuffer.get(2).isLastStep() && blockBuffer.get(3).isFirstStep()){
                MacroBlockObject tmpObj = blockBuffer.get(2);
                tmpObj.setBlockMacroState(MacroBlockObject.MacroState.MOVING);
                blockBuffer.remove(2);
                blockBuffer.add(2,tmpObj);
            }

            //Get block information and display it in textview
            String tempString = (String)tvBlockStates.getText();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(tempString);
            stringBuilder.append("\n");
            stringBuilder.append(blockBuffer.get(2).getBlockMacroState() + " : " + blockBuffer.get(2).getNumberOfStepSegments() + " firstStep: " + blockBuffer.get(2).isFirstStep() + " LastStep: " +blockBuffer.get(2).isLastStep());
            tvBlockStates.setText(stringBuilder.toString());
            Log.d("blockState", blockBuffer.get(2).getBlockMacroState() + " : " + blockBuffer.get(2).getNumberOfStepSegments() + " firstStep: " + blockBuffer.get(2).isFirstStep() + " LastStep: " +blockBuffer.get(2).isLastStep());

            //Saving blocks to array to review later (only when things are full)
            //and if button start is pressed
            long blockStartTime = (new Date()).getTime() +((blockBuffer.get(2).getStartTimestamp() - System.nanoTime())/1000000L);
            long blockEndTime = (new Date()).getTime() +((blockBuffer.get(2).getEndTimestamp() - System.nanoTime())/1000000L);
            if(started){
                startActivityWait++;
                if(startActivityWait>2) {
                    blockToAnalyse.add(blockBuffer.get(2));
                    Log.d("Activity logging", "Added block");
                }
            }

            if(stopped){
                stopActivityWait++;
                if(stopActivityWait>2) {
                    started = false;
                    stopped = false;
                    stopActivityWait = 0;
                    startActivityWait = 0;
                    Log.d("Activity logging", "Stopped logging");

                    QueuingMonitoringObject queuingMonitoringObject = new QueuingMonitoringObject(blockToAnalyse.toArray(new MacroBlockObject[blockToAnalyse.size()]));

                    showMonitoringDialog();
                }
            }
        }

        /*
        //Fourier shizzle
        More information:
        https://gist.github.com/jongukim/4037243
        http://stackoverflow.com/questions/7649003/jtransforms-fft-in-android-from-pcm-data
         */
        //Array with accel data
        int numberOfPoints = dataArray.length;
        double samplingRate = blockObject.getSampleFrequency(); //50.0; //HZ
        double dotArray[] =  new double[numberOfPoints];
        //FFT object created
        DoubleFFT_1D fftDo = new DoubleFFT_1D(numberOfPoints);
        double fft[] = new double[numberOfPoints*2]; //From: https://gist.github.com/jongukim/4037243
        //put in data
        double sum = 0;
        for(int i=0; i<numberOfPoints; i++){
            dotArray[i] = dataArray[i].getGravityDotProduct();
            sum += dataArray[i].getGravityDotProduct();
        }
        double mean = sum / numberOfPoints;

        //Substract mean
        for(int i=0; i<numberOfPoints; i++){
            dotArray[i] -= mean;
        }

        //Copy array. From: https://gist.github.com/jongukim/4037243
        System.arraycopy(dotArray, 0, fft, 0, numberOfPoints);

        //Do FFT shine
        fftDo.realForwardFull(fft);

        //used for normalizing
        double totalFFT = 0;
        double sum1, sum2, sum3, sum4, sum5, sum6;
        int points1, points2, points3, points4, points5, points6;
        sum1 = sum2 = sum3 = sum4 = sum5 = sum6 = 0;
        points1 = points2 = points3 = points4 = points5 = points6 = 0;
        for(int i=0; i<numberOfPoints; i++){//Loop over half off fft to extract data
            totalFFT += Math.abs(fft[i]);
            double frequency = samplingRate * (i) /fft.length;

            if(frequency<0){
            }else if(frequency < 1.2){
                sum1 += Math.abs(fft[i]); //Geen step
                points1++;
            }else if(frequency < 2.0){
                sum2 += Math.abs(fft[i]);  //interesting step possibility
                points2++;
            }else if(frequency < 3.0){
                sum3 += Math.abs(fft[i]); //Nothing
                points3++;
            }else if(frequency < 4.0){
                sum4 += Math.abs(fft[i]); //nothing
                points4++;
            }else if(frequency < 5.5){
                sum5 += Math.abs(fft[i]); //interesting
                points5++;
            }else if(frequency >= 5.5) {
                sum6 += Math.abs(fft[i]); //nothing
                points6++;
            }
        }
        for(int i=0; i<numberOfPoints; i++){//Loop over half off fft to extract data

        }

        tvSum1.setText(String.format("%-3.1f", sum1/Math.max(1,points1)));
        tvSum2.setText(String.format("%-3.1f", sum2/Math.max(1,points2)));
        tvSum3.setText(String.format("%-3.1f", sum3/Math.max(1,points3)));
        tvSum4.setText(String.format("%-3.1f", sum4/Math.max(1,points4)));
        tvSum5.setText(String.format("%-3.1f", sum5/Math.max(1,points5)));
        tvSum6.setText(String.format("%-3.1f", sum6/Math.max(1,points6)));

        //Log.d("totalFFT", "sum: " + totalFFT);

        //DataPoint array for graph
        DataPoint fourierDataPoints[] = new DataPoint[numberOfPoints];
        DataPoint fourierNormalizedDataPoints[] = new DataPoint[numberOfPoints];
        for(int i=0; i<numberOfPoints; i++){//Loop over half off fft to extract data
            double frequency = samplingRate * (i) /fft.length; //http://stackoverflow.com/questions/7649003/jtransforms-fft-in-android-from-pcm-data
            fourierDataPoints[i] = new DataPoint(frequency,Math.abs(fft[i]));
            fourierNormalizedDataPoints[i] = new DataPoint(frequency,(Math.abs(fft[i])/totalFFT));
            if(i==(numberOfPoints-1)){
                Log.d("max freqency", "freq: "+frequency);
            }
        }

        BarGraphSeries<DataPoint> fourierSeries = new BarGraphSeries<DataPoint>(fourierDataPoints);
        BarGraphSeries<DataPoint> fourierNormalizedSeries = new BarGraphSeries<DataPoint>(fourierNormalizedDataPoints);
        fourierSeries.setSpacing(50);
        fourierNormalizedSeries.setSpacing(50);

        graphFourier.removeAllSeries();
        graphFourier.addSeries(fourierSeries);

        graphFourierNormalized.removeAllSeries();
        graphFourierNormalized.addSeries(fourierNormalizedSeries);


    }

    @Override
    public void onStepCount(ArrayList<Long> timeOfSteps) {

    }

    public void showMonitoringDialog(){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.activity_monitoring_dialog);
        dialog.setTitle("Monitoring Dialog");

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("States:");
        stringBuilder.append("\n");
        stringBuilder.append("\n");

        //Loop over arraylist for info on blocks
        for(int i=0; i<blockToAnalyse.size(); i++){
            MacroBlockObject obj = blockToAnalyse.get(i);
            if(obj.getBlockMacroState()== MacroBlockObject.MacroState.WALKING){
                stringBuilder.append(Html.fromHtml("<font color=#ff0000>"+ obj.getBlockMacroState() + " : " + obj.getNumberOfStepSegments() +"</font>"));
            }else if(obj.getBlockMacroState() == MacroBlockObject.MacroState.MOVING){
                stringBuilder.append(Html.fromHtml("<font color=#ffff00>"+ obj.getBlockMacroState() + " : " + obj.getNumberOfStepSegments() +"</font>"));
            }else{
                stringBuilder.append(Html.fromHtml("<font color=#00ff00>"+ obj.getBlockMacroState() + " : " + obj.getNumberOfStepSegments() +"</font>"));
            }
            stringBuilder.append("\n");
        }

        TextView text = (TextView)dialog.findViewById(R.id.block_states_dialog_tv);
        text.setText(stringBuilder.toString());

        dialog.show();
    }

}
