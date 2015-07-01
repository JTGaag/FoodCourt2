package com.aj.foodcourt2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.aj.queuing.MacroBlockObject;
import com.aj.queuing.QueuingDataHandler;
import com.aj.queuing.QueuingDisplayObject;
import com.aj.queuing.QueuingListener;
import com.aj.queuing.QueuingSensorData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ActivityMonitoringActivity extends AppCompatActivity implements SensorEventListener, QueuingListener{

    /*
    UNDER THE HOOD
     */
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gravitySensor;

    private double currentGravityX = 0;
    private double currentGravityY = 0;
    private double currentGravityZ = 0;

    private QueuingDataHandler queuingDataHandler;

    private ArrayList<MacroBlockObject> blockBuffer = new ArrayList<>();
    private ArrayList<MacroBlockObject> blockToAnalyse = new ArrayList<>();

    QueuingDisplayObject bufferQueuingObject;

    long startActivityTime, stopActivityTime;
    int startActivityWait = 0;
    int stopActivityWait = 0;
    boolean started = false;
    boolean stopped = false;

    private final static String PREF_NAME = "foodcourtPreferenceFile";
    private final static String STEP_MODE_NAME = "prefStepMode";

    private long firstTimeMillis = 0;
    private long firstTimeNano = 0;


    SharedPreferences settings;

    //State enum
    public enum State {
        INITIATING, OPERATIONAL
    }

    private State currentState = State.INITIATING;

    /*
    INTERFACE STUFF
     */

    Button buttonStartMonitoring, buttonStopMonitoring;
    TextView tvStepMode, tvSteps, tvState;

    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    private List<QueuingDisplayObject> queuingDisplayObjects= new ArrayList<>();
    RVAdapter adapter;

    SimpleDateFormat sdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_monitoring);


        /*
        BACKGROUND Things
         */
        //Initiate sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        //Do stuff with settings
        settings = getSharedPreferences(PREF_NAME, MODE_PRIVATE);



        /*
        INTERFACE RELATED THINGS
         */

        recyclerView = (RecyclerView)findViewById(R.id.monitoring_recycler_view);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

//        queuingDisplayObjects.add(new QueuingDisplayObject("Title 1", "Start: 12:05.05  End: 12:06.48", "Lorem ipsum dolor sit amet, blandit aliquam ante vitae, proin id mi aenean erat egestas felis. Condimentum erat sem turpis ullamcorper cursus. Penatibus eu congue at ullamcorper feugiat, tempus nec turpis quis, ut tincidunt lorem, ac ut id nostra. Massa tortor, et id tincidunt morbi, mattis platea elit aliquam pellentesque."));
//        queuingDisplayObjects.add(new QueuingDisplayObject("Title 2", "Start: 12:08.05  End: 12:10.48", "Lorem ipsum dolor sit amet, blandit aliquam ante vitae, proin id mi aenean erat egestas felis. Condimentum erat sem turpis ullamcorper cursus. Penatibus eu congue at ullamcorper feugiat, tempus nec turpis quis, ut tincidunt lorem, ac ut id nostra. Massa tortor, et id tincidunt morbi, mattis platea elit aliquam pellentesque."));
//        queuingDisplayObjects.add(new QueuingDisplayObject("Title 3", "Start: 12:23.05  End: 12:30.48", "Lorem ipsum dolor sit amet, blandit aliquam ante vitae, proin id mi aenean erat egestas felis. Condimentum erat sem turpis ullamcorper cursus. Penatibus eu congue at ullamcorper feugiat, tempus nec turpis quis, ut tincidunt lorem, ac ut id nostra. Massa tortor, et id tincidunt morbi, mattis platea elit aliquam pellentesque."));

        adapter = new RVAdapter(queuingDisplayObjects);
        recyclerView.setAdapter(adapter);

        tvStepMode = (TextView)findViewById(R.id.tv_step_mode);
        tvSteps = (TextView)findViewById(R.id.tv_steps);
        tvState = (TextView)findViewById(R.id.tv_state);

        tvState.setText("State: " + currentState);

        buttonStartMonitoring = (Button)findViewById(R.id.button_start_monitoring);
        buttonStopMonitoring = (Button)findViewById(R.id.button_stop_monitoring);

        buttonStartMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!started) {
                    started = true;
                    startActivityTime = System.currentTimeMillis();
                    Log.d("Activity logging", "Activity startTime: " + startActivityTime);
                    blockToAnalyse.clear();
                    Date now = new Date(System.currentTimeMillis());
                    adapter.addObject(new QueuingDisplayObject("Current Activity", "StartTime: " + sdf.format(now), "Log:\n"));
                    bufferQueuingObject = adapter.getLastObject();
                    linearLayoutManager.scrollToPosition(adapter.getItemCount()-1);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        buttonStopMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!stopped) {
                    stopped = true;
                    stopActivityTime = System.currentTimeMillis();
                    Date now = new Date(System.currentTimeMillis());
                    bufferQueuingObject.setTime(bufferQueuingObject.getTime() + " | EndTime: " + sdf.format(now));
                    adapter.notifyDataSetChanged();
                }
            }
        });

        sdf = new SimpleDateFormat("HH:mm:ss.S");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_monitoring, menu);
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener(this, accelerometer, 10000);
        sensorManager.registerListener(this, gravitySensor, 10000);

        //Initate dataHandler (Boundaries must be more equal or more than 150/points per segemnts)
        queuingDataHandler = new QueuingDataHandler(this, 50, 3, 7, settings.getInt(STEP_MODE_NAME, 2), true);

        tvStepMode.setText("Step mode: "+settings.getInt(STEP_MODE_NAME, 2));
    }


    @Override
    protected void onPause() {
        super.onPause();

        sensorManager.unregisterListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent eventActivity = new Intent(ActivityMonitoringActivity.this, SettingsActivity.class);
            startActivity(eventActivity);
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onNewDataBlock(int count, QueuingSensorData[] dataArray, MacroBlockObject blockObject) {
        tvSteps.setText("Steps: " + count);

        //Do block analysis
        blockBuffer.add(blockObject);

        //Set state
        if(currentState!=State.OPERATIONAL && blockBuffer.size()>2){
            currentState = State.OPERATIONAL;
            tvState.setText("State: " + currentState);
        }

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


            }

            if(blockBuffer.get(2).getNumberOfStepSegments()==1 && blockBuffer.get(3).getNumberOfStepSegments()==1 && blockBuffer.get(2).isLastStep() && blockBuffer.get(3).isFirstStep()){
                MacroBlockObject tmpObj = blockBuffer.get(2);
                tmpObj.setBlockMacroState(MacroBlockObject.MacroState.MOVING);
                blockBuffer.remove(2);
                blockBuffer.add(2,tmpObj);
            }

//            //Get block information and display it in textview
//            String tempString = (String)tvBlockStates.getText();
//            StringBuilder stringBuilder = new StringBuilder();
//            stringBuilder.append(tempString);
//            stringBuilder.append("\n");
//            stringBuilder.append(blockBuffer.get(2).getBlockMacroState() + " : " + blockBuffer.get(2).getNumberOfStepSegments() + " firstStep: " + blockBuffer.get(2).isFirstStep() + " LastStep: " +blockBuffer.get(2).isLastStep());
//            tvBlockStates.setText(stringBuilder.toString());
//            Log.d("blockState", blockBuffer.get(2).getBlockMacroState() + " : " + blockBuffer.get(2).getNumberOfStepSegments() + " firstStep: " + blockBuffer.get(2).isFirstStep() + " LastStep: " + blockBuffer.get(2).isLastStep());

            //Saving blocks to array to review later (only when things are full)
            //and if button start is pressed
            Date blockStartTime = new Date(firstTimeMillis + (blockBuffer.get(2).getStartTimestamp()-firstTimeNano)/1000000);
            Date blockEndTime = new Date(firstTimeMillis + (blockBuffer.get(2).getEndTimestamp()-firstTimeNano)/1000000);
            if(started){
                startActivityWait++;
                if(startActivityWait>2) {
                    blockToAnalyse.add(blockBuffer.get(2));
                    bufferQueuingObject.setInfo(bufferQueuingObject.getInfo() + sdf.format(blockStartTime) + " - " + sdf.format(blockEndTime) + " : " + blockBuffer.get(2).getBlockMacroState() + "\n");
                    adapter.notifyDataSetChanged();
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
                    bufferQueuingObject.setTitle("Activity #" + adapter.getItemCount());
                    adapter.notifyDataSetChanged();
                    //QueuingMonitoringObject queuingMonitoringObject = new QueuingMonitoringObject(blockToAnalyse.toArray(new MacroBlockObject[blockToAnalyse.size()]));

                    //showMonitoringDialog();
                }
            }
        }

    }

    @Override
    public void onStepCount(ArrayList<Long> timeOfSteps, long entTime) {

    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        double x, y, z;
        long timestamp;

        if(firstTimeMillis == 0 || firstTimeNano == 0){
            firstTimeMillis = System.currentTimeMillis();
            firstTimeNano = event.timestamp;
        }

        switch (sensor.getType()){

            case Sensor.TYPE_ACCELEROMETER:
                timestamp = event.timestamp;
                x = event.values[0];
                y = event.values[1];
                z = event.values[2];

                //Add data to handler
                queuingDataHandler.addRawData(new QueuingSensorData(event.timestamp, event.values[0], event.values[1], event.values[2], currentGravityX, currentGravityY, currentGravityZ, 0, 0));

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
}
