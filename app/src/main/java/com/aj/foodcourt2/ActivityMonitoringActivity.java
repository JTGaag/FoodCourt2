package com.aj.foodcourt2;

import android.app.Dialog;
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
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.aj.queuing.MacroBlockObject;
import com.aj.queuing.Queue;
import com.aj.queuing.QueuingDataHandler;
import com.aj.queuing.QueuingDataObject;
import com.aj.queuing.QueuingDisplayObject;
import com.aj.queuing.QueuingListener;
import com.aj.queuing.QueuingMonitoringObject;
import com.aj.queuing.QueuingSensorData;
import com.aj.recyclerview.RVAdapter;
import com.aj.recyclerview.RecyclerViewClickListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ActivityMonitoringActivity extends AppCompatActivity implements SensorEventListener, QueuingListener, RecyclerViewClickListener{

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
    private ArrayList<QueuingDataObject> queuingDataObjects = new ArrayList<>();

    QueuingDisplayObject bufferQueuingObject;

    long startActivityTime, stopActivityTime;
    int startActivityWait = 0;
    int stopActivityWait = 0;
    boolean started = false;
    boolean stopped = false;
    boolean recording = false;

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
    ImageView ivPlay, ivStop, ivRecord;

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

        adapter = new RVAdapter(queuingDisplayObjects, this);
        recyclerView.setAdapter(adapter);

        tvStepMode = (TextView)findViewById(R.id.tv_step_mode);
        tvSteps = (TextView)findViewById(R.id.tv_steps);
        tvState = (TextView)findViewById(R.id.tv_state);

        tvState.setText("State: " + currentState);

        ivPlay = (ImageView)findViewById(R.id.iv_play);
        ivStop = (ImageView)findViewById(R.id.iv_stop);
        ivRecord = (ImageView)findViewById(R.id.iv_record);

        buttonStartMonitoring = (Button)findViewById(R.id.button_start_monitoring);
        buttonStopMonitoring = (Button)findViewById(R.id.button_stop_monitoring);

        buttonStartMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!started) {
                    started = true;
                    changeImages();
                    startActivityTime = System.currentTimeMillis();
                    Log.d("Activity logging", "Activity startTime: " + startActivityTime);
                    blockToAnalyse.clear();
                    Date now = new Date(System.currentTimeMillis());
                    adapter.addObject(new QueuingDisplayObject("Current Activity", "StartTime: " + sdf.format(now), "Log:\n"));
                    bufferQueuingObject = adapter.getLastObject();
                    linearLayoutManager.scrollToPosition(adapter.getItemCount()-1);
                    adapter.notifyDataSetChanged();
                    queuingDataObjects.add(new QueuingDataObject(adapter.getItemCount()-1, bufferQueuingObject));
                }
            }
        });

        buttonStopMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!stopped) {
                    stopped = true;
                    changeImages();
                    stopActivityTime = System.currentTimeMillis();
                    Date now = new Date(System.currentTimeMillis());
                    bufferQueuingObject.setTime(bufferQueuingObject.getTime() + "\nEndTime: " + sdf.format(now));
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
                    recording = true;
                    changeImages();
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
                    recording = false;
                    changeImages();
                    stopActivityWait = 0;
                    startActivityWait = 0;
                    Log.d("Activity logging", "Stopped logging");
                    bufferQueuingObject.setTitle("Activity #" + adapter.getItemCount());
                    adapter.notifyDataSetChanged();
                    QueuingMonitoringObject queuingMonitoringObject = new QueuingMonitoringObject(blockToAnalyse.toArray(new MacroBlockObject[blockToAnalyse.size()]));
                    //TODO: Change states in card view to final states from monitoring object
                    bufferQueuingObject.setInfo("Log: \n");
                    for(MacroBlockObject block : queuingMonitoringObject.getBlockArray()){
                        Date startTime = new Date(firstTimeMillis + (block.getStartTimestamp()-firstTimeNano)/1000000);
                        Date endTime = new Date(firstTimeMillis + (block.getEndTimestamp()-firstTimeNano)/1000000);
                        bufferQueuingObject.setInfo(bufferQueuingObject.getInfo() + sdf.format(startTime) + " - " + sdf.format(endTime) + " : " + block.getBlockMacroState() + "\n");
                    }
                    queuingDataObjects.get(queuingDataObjects.size()-1).setQueuingMonitoringObject(queuingMonitoringObject);
                    queuingDataObjects.get(queuingDataObjects.size()-1).setFinished(true);
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

    /**
     * Called when the view is clicked.
     *
     * @param v           view that is clicked
     * @param position    of the clicked item
     * @param isLongClick true if long click, false otherwise
     */
    @Override
    public void onClick(View v, int position, boolean isLongClick) {
        if(isLongClick){
            for(QueuingDataObject obj: queuingDataObjects){
                if(obj.getPosition() == position){

                    final Dialog dialog = new Dialog(this);
                    dialog.setContentView(R.layout.monitoring_dialog);
                    dialog.setTitle(obj.getQueuingDisplayObject().getTitle());
                    TextView dialogTime = (TextView)dialog.findViewById(R.id.dialog_time_info);
                    TextView dialogQueueNumber = (TextView)dialog.findViewById(R.id.dialog_queue_number);
                    TextView dialogQueueInfo = (TextView)dialog.findViewById(R.id.dialog_queue_info);

                    dialogTime.setText(obj.getQueuingDisplayObject().getTime());
                    //Log.d("RecyclerView Click", "LongPress, pos: " + position);
                    //Log.d("RecyclerView Click", "LongPress, finished: " + obj.isFinished());
                    if(obj.isFinished() && obj.getQueuingMonitoringObject() != null) {
                        //Number of Queues
                        dialogQueueNumber.setText("Number of Queues: " + obj.getQueuingMonitoringObject().getQueueList().size());

                        //Display info of Queues
                        StringBuilder stringBuilder = new StringBuilder();
                        String decimalFormat = "%.1f";
                        int nmb = 0;
                        for(Queue queue: obj.getQueuingMonitoringObject().getQueueList()){
                            nmb++;
                            Date startTime = new Date(firstTimeMillis + (queue.getBeginTime()-firstTimeNano)/1000000);
                            Date endTime = new Date(firstTimeMillis + (queue.getEndTime()-firstTimeNano)/1000000);
                            stringBuilder.append("<b>Queue #" + nmb + "</b><br />");
                            stringBuilder.append("Total blocks: " + queue.getNumberOfBlocks() + "<br />");
                            //stringBuilder.append("Index block 1: " + queue.getBeginIndex() + "<br />");
                            //stringBuilder.append("Index block end: " + queue.getEndIndex() + "<br />");
                            //stringBuilder.append("Begin Time: " + sdf.format(startTime) + "<br />");
                            //stringBuilder.append("End Time: " + sdf.format(endTime) + "<br />");
                            stringBuilder.append("Total time: " + String.format(decimalFormat, queue.totalTime()) + "<br />");
                            stringBuilder.append("Average serving Time: " + String.format(decimalFormat, queue.getAverageServingTime()) + "<br />");
                            stringBuilder.append("Number of Queue states: " + queue.getNumberOfQueues() + "<br />");
                            stringBuilder.append("Number of Queue blocks: " + queue.getNumberOfQueueBlocks() + "<br />");
                            stringBuilder.append("Number of Move blocks: " + queue.getNumberOfMoveBlocks() + "<br />");
                            stringBuilder.append("<br />------------------------------------<br />");

                        }

                        dialogQueueInfo.setText(Html.fromHtml(stringBuilder.toString()));


                        for (MacroBlockObject block : obj.getQueuingMonitoringObject().getBlockArray()) {
                            //Log.d("RecyclerView Click", "Block: " + block.getBlockMacroState());
                            //Get information from analysis
                        }
                    }

                    dialog.show();
                    break;
                }
            }

        }else{
            Log.d("RecyclerView Click", "Press, pos: " + position);
        }
    }

    public void changeImages(){
        if(started){
            ivPlay.setImageResource(R.drawable.ic_play_arrow_red_24dp);
        }else{
            ivPlay.setImageResource(R.drawable.ic_play_arrow_white_24dp);
        }

        if(stopped){
            ivStop.setImageResource(R.drawable.ic_stop_red_24dp);
        }else{
            ivStop.setImageResource(R.drawable.ic_stop_white_24dp);
        }

        if(recording){
            ivRecord.setImageResource(R.drawable.ic_record_red_24dp);
        }else{
            ivRecord.setImageResource(R.drawable.ic_record_white_24dp);
        }
    }
}
