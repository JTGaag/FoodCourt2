package com.aj.foodcourt2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.aj.map.CollisionMap;
import com.aj.map.LineSegment;
import com.aj.map.RectangleMap;
import com.aj.map.TouchImageView;
import com.aj.particlefilter.MotionDataHandler;
import com.aj.particlefilter.MotionListener;
import com.aj.particlefilter.Particle2;
import com.aj.particlefilter.ParticleManager;
import com.aj.particlefilter.Rectangle;
import com.aj.queuing.MacroBlockObject;
import com.aj.queuing.QueuingDataHandler;
import com.aj.queuing.QueuingListener;
import com.aj.queuing.QueuingSensorData;
import com.aj.server.ASyncServerReturn;
import com.aj.wifi.WifiData;
import com.aj.wifi.WifiListener;
import com.aj.wifi.WifiReceiver;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LocalizationActivity extends AppCompatActivity implements SensorEventListener, QueuingListener, MotionListener, WifiListener, ASyncServerReturn {

    final String LOG_TAG = "Localization activity";

    //sensors
    private SensorManager sensorManager;
    private Sensor gravitySensor;
    private Sensor magneticSensor;
    private Sensor accelerometerSensor;
    private Sensor gyroSensor;

    //Wifi sensors and managers
    WifiManager wifiManager;
    WifiReceiver wifiReceiver;
    long lastWifiScanTime;
    long scanStartTime;
    ArrayList<WifiData> wifiDataArrayList = new ArrayList<WifiData>();

    //magnetometer
    float[] mGravity;
    float[] mGeomagnetic;
    float azimut;
    float degrees;

    //Constants
    final double NS2S = 1.0/1000000000.0;
    private final static String PREF_NAME = "foodcourtPreferenceFile";
    private final static String STEP_MODE_NAME = "prefStepMode";
    private final static String DEBUG_MODE_NAME = "prefDebugMode";
    private final static int X_OFFSET = 100;
    private final static int Y_OFFSET = 100;


    //Settings
    SharedPreferences settings;

    //map
    ArrayList<Rectangle> rectangleArrayList = new ArrayList<Rectangle>();
    ArrayList<Rectangle> cellArrayList = new ArrayList<Rectangle>();
    ArrayList<LineSegment> lineSegmentArrayList = new ArrayList<LineSegment>();
    RectangleMap rectangleMap;
    RectangleMap cellMap;
    CollisionMap collisionMap;
    ParticleManager particleManager;
    Particle2[] particleArray;

    //Image to display floor map and particles
    TouchImageView touchImageMapView;
    Bitmap bg;

    //Stuff
    Button buttonMotionDetection, buttonReset, buttonBacktrack, buttonLocalize;
    ImageView ivPlay, ivStop, ivRecord;

    //Paints
    Paint paint = new Paint();
    Paint paintCell = new Paint();
    Paint paintCellStroke = new Paint();
    Paint paintDot = new Paint();
    Paint paintDotDestroy = new Paint();
    Paint paintCollision = new Paint();
    Paint paintMean = new Paint();
    Paint paintBacktrack = new Paint();

    //Variables to save gravity data
    private double currentGravityX = 0;
    private double currentGravityY = 0;
    private double currentGravityZ = -9;

    private final int ENLARGE_FACTOR = 100; //50 for EWI building 100 for RDW
    private final double DIRECTION_SD = 20; //20 for EWI, 25 for RDW

    boolean motionDetection = false;
    Context context = this;

    //QDH to be used to analyse acceleration data and output step information and quining information
    private QueuingDataHandler queuingDataHandler;
    private MotionDataHandler motionDataHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localization);

        //Initiate sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        //Wifi shizzle
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiReceiver = new WifiReceiver(this, wifiManager);

        //Imageviews
        ivPlay = (ImageView)findViewById(R.id.iv_play);
        ivStop = (ImageView)findViewById(R.id.iv_stop);
        ivRecord = (ImageView)findViewById(R.id.iv_record);

        //Button Shine
        buttonBacktrack = (Button)findViewById(R.id.button_backtrack);
        buttonLocalize = (Button)findViewById(R.id.button_localize);
        buttonMotionDetection = (Button)findViewById(R.id.button_motion_detection);
        buttonReset = (Button)findViewById(R.id.button_reset_localization);

        buttonBacktrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        buttonLocalize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        buttonMotionDetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                motionDetection = !motionDetection;
                changeImages();
                Toast.makeText(context, "Motiondeatection: " + motionDetection, Toast.LENGTH_SHORT).show();
            }
        });
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });

        //Do stuff with settings
        settings = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        /*
        ---------------------------------------------------------------
        Map and particle manager stuff
         */

        //Make maps to be used for distribution and collision
        //makeMaps();
        makeMaps2();

        //Init bitmap
        bg = Bitmap.createBitmap(3800,1000, Bitmap.Config.ARGB_8888);
        //Set paitns
        setPaints();
        touchImageMapView = (TouchImageView) findViewById(R.id.floor_map_zoom);
        touchImageMapView.setMaxZoom(8f);

        //Make rectangle map
        rectangleMap = new RectangleMap(rectangleArrayList);
        rectangleMap.assignWeights();
        // make cell map
        cellMap = new RectangleMap(cellArrayList);
        cellMap.assignWeights();
        //init COllison Map
        collisionMap = new CollisionMap(lineSegmentArrayList);

        //initialize particle manager
        particleManager = new ParticleManager(10000, rectangleMap, collisionMap, getApplicationContext());

        //Get the array of current particles
        particleArray = particleManager.getParticleArray();

        redrawMap();

    }


    @Override
    protected void onResume() {
        super.onResume();

        //Register all sensor listners
        sensorManager.registerListener(this, gravitySensor, 10000);
        sensorManager.registerListener(this, magneticSensor, 10000);
        sensorManager.registerListener(this, accelerometerSensor, 10000);
        sensorManager.registerListener(this, gyroSensor, 10000);

        /*
        Initialize all data handlers (in onResume to change settings
         */
        /*
        mode:
        1: normal queuing
        2: default localization
        3: Joost step counting
        4: Jork step counting
        5: Willem
        6: Alexander
         */
        queuingDataHandler = new QueuingDataHandler(this, 50, 1, 2, settings.getInt(STEP_MODE_NAME, 2), false);
        motionDataHandler = new MotionDataHandler(this);
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_localization, menu);
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
            Intent eventActivity = new Intent(LocalizationActivity.this, SettingsActivity.class);
            startActivity(eventActivity);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onJSONReturn(JSONObject json) {

    }

    /**
     * Called when straight motion has ended or specific time of time has passed.
     * This method will start the motion in the particle manager
     *
     * @param direction direction of detected motion
     * @param distance  distance of detected motion
     * @param timestamp
     */
    @Override
    public void onMotion(double direction, double distance, long timestamp) {

    }

    @Override
    public void onWifiCheck(long timestamp) {

    }

    @Override
    public void onNewDataBlock(int count, QueuingSensorData[] dataArray, MacroBlockObject blockObject) {

    }

    @Override
    public void onStepCount(ArrayList<Long> timeOfSteps, long entTime) {

    }


    @Override
    public void onSensorChanged(SensorEvent event) {

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onWifiData(List<ScanResult> wifiList) {

    }



    /*
    PAINTS and MAPS
     */

    private void redrawMap(){

        //reset bitmap
        bg.eraseColor(android.graphics.Color.TRANSPARENT);

        //Make canvas to draw on
        Canvas canvas = new Canvas(bg);

        //Get all the rectangle chapes (rooms)
        for (Rectangle rec : rectangleMap.getRectangles()) {
            canvas.drawRect((float) (rec.getX() * ENLARGE_FACTOR)+X_OFFSET, (float) (rec.getY() * ENLARGE_FACTOR)+Y_OFFSET, (float) ((rec.getX() + rec.getWidth()) * ENLARGE_FACTOR)+X_OFFSET, (float) ((rec.getY() + rec.getHeight()) * ENLARGE_FACTOR)+Y_OFFSET, paint);
        }

        //Draw cell when converged
        if(particleManager.hasConverged()) {
            particleManager.calculateMean();
            int r = cellMap.isPointinRectangle(particleManager.getMeanX(), particleManager.getMeanY());
            if ((r > 0) && (r < cellArrayList.size()) && (particleManager.hasConverged())) {
                Rectangle rec = cellArrayList.get(r);
                canvas.drawRect((float) (rec.getX() * ENLARGE_FACTOR)+X_OFFSET, (float) (rec.getY() * ENLARGE_FACTOR)+Y_OFFSET, (float) ((rec.getX() + rec.getWidth()) * ENLARGE_FACTOR)+X_OFFSET, (float) ((rec.getY() + rec.getHeight()) * ENLARGE_FACTOR)+Y_OFFSET, paintCell);
                canvas.drawRect((float) (rec.getX() * ENLARGE_FACTOR)+X_OFFSET, (float) (rec.getY() * ENLARGE_FACTOR)+Y_OFFSET, (float) ((rec.getX() + rec.getWidth()) * ENLARGE_FACTOR)+X_OFFSET, (float) ((rec.getY() + rec.getHeight()) * ENLARGE_FACTOR)+Y_OFFSET, paintCellStroke);
            }
        }


        //Get particles
        Particle2[] tmpParticleArray = particleManager.getParticleArray();

        //Draw particles
        for (Particle2 particle : tmpParticleArray) {
            //canvas.drawLine((float) particle.getOldX() * ENLARGE_FACTOR, (float) particle.getOldY() * ENLARGE_FACTOR, (float) particle.getX() * ENLARGE_FACTOR, (float) particle.getY() * ENLARGE_FACTOR, paintMove);
            if (!particle.isDestroyed()) {
                canvas.drawPoint((float) (particle.getX() * ENLARGE_FACTOR)+X_OFFSET, (float) (particle.getY() * ENLARGE_FACTOR)+Y_OFFSET, paintDot);
            } else {
                //canvas.drawPoint((float) (particle.getX() * ENLARGE_FACTOR), (float) (particle.getY() * ENLARGE_FACTOR), paintDotDestroy);
            }

        }

        //Draw collision map
        for (LineSegment line : collisionMap.getLineSegments()) {
            canvas.drawLine((float) (line.getX1() * ENLARGE_FACTOR)+X_OFFSET, (float) (line.getY1() * ENLARGE_FACTOR)+Y_OFFSET, (float) (line.getX2() * ENLARGE_FACTOR)+X_OFFSET, (float) (line.getY2() * ENLARGE_FACTOR)+Y_OFFSET, paintCollision);
        }

        particleManager.calculateMean();
        //Draw mean point DONE: calulate mean before hand here
        canvas.drawPoint((float) (particleManager.getMeanX() * ENLARGE_FACTOR)+X_OFFSET, (float) (particleManager.getMeanY() * ENLARGE_FACTOR)+Y_OFFSET, paintMean);
        //Log.d("Mean values", "x: " + particleManager.getMeanX() + " y:" + particleManager.getMeanY());


        //noinspection deprecation
        touchImageMapView.setImageBitmap(bg);
    }

    public void changeImages(){
        if(motionDetection){
            ivPlay.setImageResource(R.drawable.ic_play_arrow_red_24dp);
        }else{
            ivPlay.setImageResource(R.drawable.ic_play_arrow_white_24dp);
        }

        if(!motionDetection){
            ivStop.setImageResource(R.drawable.ic_stop_red_24dp);
        }else{
            ivStop.setImageResource(R.drawable.ic_stop_white_24dp);
        }

        if(motionDetection){
            ivRecord.setImageResource(R.drawable.ic_record_red_24dp);
        }else{
            ivRecord.setImageResource(R.drawable.ic_record_white_24dp);
        }
    }

    public void reset(){

        //initialize particle manager
        particleManager = new ParticleManager(10000, rectangleMap, collisionMap, getApplicationContext());
        //Get the array of current particles
        particleArray = particleManager.getParticleArray();
        redrawMap();
    }

    protected void setPaints(){
        //Set Paints
        //paint.setColor(Color.rgb(0, 0, 0));
        paint.setColor(getResources().getColor(R.color.foodcourt_cyan_300));
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(5.0f);

        paintCell.setColor(getResources().getColor(R.color.foodcourt_cyan_900));
        paintCell.setStyle(Paint.Style.FILL);

        paintCellStroke.setColor(Color.rgb(128, 0, 0));
        paintCellStroke.setStyle(Paint.Style.STROKE);
        paintCellStroke.setStrokeWidth(0.0f);

        //paintDot.setColor(Color.rgb(51, 128, 51));
        paintDot.setColor(getResources().getColor(R.color.foodcourt_pink_accent_900));
        paintDot.setStyle(Paint.Style.FILL);
        paintDot.setStrokeWidth(5.0f);

        paintDotDestroy.setColor(Color.rgb(255,51,51));
        paintDotDestroy.setStyle(Paint.Style.FILL);
        paintDotDestroy.setStrokeWidth(2.0f);

        //paintCollision.setColor(Color.rgb(255,51,255));
        paintCollision.setColor(getResources().getColor(R.color.foodcourt_yellow_accent_500));
        paintCollision.setStyle(Paint.Style.FILL);
        paintCollision.setStrokeWidth(10.0f);

        paintMean.setColor(getResources().getColor(R.color.foodcourt_pink_accent_500));
        paintMean.setStyle(Paint.Style.FILL);
        paintMean.setStrokeWidth(10.0f);

        paintBacktrack.setColor(getResources().getColor(R.color.foodcourt_pink_accent_500));
        paintBacktrack.setStyle(Paint.Style.FILL);
        paintBacktrack.setStrokeWidth(6.0f);
    }

    protected void makeMaps(){
        ////////////////////////////////////////////////////////////////////////
        //
        //Collision Map
        //
        ////////////////////////////////////////////////////////////////////////
        lineSegmentArrayList.add(new LineSegment(0, 0, 8, 0));//conference room 1
        lineSegmentArrayList.add(new LineSegment(0, 0, 0, 6.1));//conference room 2
        lineSegmentArrayList.add(new LineSegment(8, 0, 8, 6.1));//conference room 3
        lineSegmentArrayList.add(new LineSegment(0, 6.1, 1.5, 6.1));//conference room/hall 4
        lineSegmentArrayList.add(new LineSegment(2.5, 6.1, 5.5, 6.1));//conference room/hall 5
        lineSegmentArrayList.add(new LineSegment(6.5, 6.1, 8, 6.1));//conference room/hall 6

        lineSegmentArrayList.add(new LineSegment(12, 0, 16, 0));//room1 7
        lineSegmentArrayList.add(new LineSegment(12, 0, 12, 6.1));//room1 8
        lineSegmentArrayList.add(new LineSegment(16, 0, 16, 6.1));//room1/room2 9
        lineSegmentArrayList.add(new LineSegment(16, 0, 20, 0));//room2 10
        lineSegmentArrayList.add(new LineSegment(20, 0, 20, 6.1));//room2 11
        lineSegmentArrayList.add(new LineSegment(12, 6.1, 13.5, 6.1));//room1/hall 12
        lineSegmentArrayList.add(new LineSegment(14.5, 6.1, 16, 6.1));//room1/hall 13
        lineSegmentArrayList.add(new LineSegment(16, 6.1, 17.5, 6.1));//room2/hall 14
        lineSegmentArrayList.add(new LineSegment(18.5, 6.1, 20, 6.1));//room2/hall 15

        lineSegmentArrayList.add(new LineSegment(8, 6.1, 12, 6.1));//hall 16
        lineSegmentArrayList.add(new LineSegment(0, 6.1, 0, 8.2));//hall 17
        lineSegmentArrayList.add(new LineSegment(72, 6.1, 72, 8.2));//hall 18
        lineSegmentArrayList.add(new LineSegment(0, 8.2, 15, 8.2));//hall 19
        lineSegmentArrayList.add(new LineSegment(20, 6.1, 72, 6.1));//hall 20
        lineSegmentArrayList.add(new LineSegment(64, 8.2, 72, 8.2));//hall 21
        lineSegmentArrayList.add(new LineSegment(16, 8.2, 56, 8.2));//hall 22

        lineSegmentArrayList.add(new LineSegment(15, 8.2, 15, 11.3));//coffeeroom-corridor 23
        lineSegmentArrayList.add(new LineSegment(16, 8.2, 16, 11.3));//coffeeroom-corridor 24

        lineSegmentArrayList.add(new LineSegment(12, 11.3, 15, 11.3));//coffeeroom-corridor 25
        lineSegmentArrayList.add(new LineSegment(12, 11.3, 12, 14.3));//coffeeroom-corridor 26
        lineSegmentArrayList.add(new LineSegment(12, 14.3, 16, 14.3));//coffeeroom-corridor 27
        lineSegmentArrayList.add(new LineSegment(16, 11.3, 16, 14.3));//coffeeroom-corridor 28

        lineSegmentArrayList.add(new LineSegment(56, 8.2, 56, 14.3));//room3 29
        lineSegmentArrayList.add(new LineSegment(56, 14.3, 60, 14.3));//room3 30
        lineSegmentArrayList.add(new LineSegment(60, 8.2, 60, 14.3));//room3/room4 31
        lineSegmentArrayList.add(new LineSegment(60, 14.3, 64, 14.3));//room4 32
        lineSegmentArrayList.add(new LineSegment(64, 8.2, 64, 14.3));//room4 33
        lineSegmentArrayList.add(new LineSegment(56, 8.2, 57.5, 8.2));//room3/hall 34
        lineSegmentArrayList.add(new LineSegment(58.5, 8.2, 60, 8.2));//room3/hall 35
        lineSegmentArrayList.add(new LineSegment(60, 8.2, 61.5, 8.2));//room4/hall 36
        lineSegmentArrayList.add(new LineSegment(62.5, 8.2, 64, 8.2));//room4/hall 37

        ////////////////////////////////////////////////////////////////////////
        //
        //Rectangle (room) map
        //
        ////////////////////////////////////////////////////////////////////////
        rectangleArrayList.add(new Rectangle(0, 0, 8, 6.1));//conference room
        rectangleArrayList.add(new Rectangle(0, 6.1, 72, 2.1));//halway
        rectangleArrayList.add(new Rectangle(12, 0, 4, 6.1));//room1
        rectangleArrayList.add(new Rectangle(16, 0, 4, 6.1));//room2
        rectangleArrayList.add(new Rectangle(15, 8.2, 1, 3.1));//halway to coffeeroom
        rectangleArrayList.add(new Rectangle(12, 11.3, 4, 3));//coffeeroom
        rectangleArrayList.add(new Rectangle(56, 8.2, 4, 6.1));//room3
        rectangleArrayList.add(new Rectangle(60, 8.2, 4, 6.1));//room4

        ////////////////////////////////////////////////////////////////////////
        //
        //Cell map
        //
        ////////////////////////////////////////////////////////////////////////

        double difference = 0.8;
        cellArrayList.add(new Rectangle(2, 6.1, 4, 2.1));   //c19
        cellArrayList.add(new Rectangle(0, 0, 4, 6.1));     //c18
        cellArrayList.add(new Rectangle(4, 0, 4, 6.1));     //c17
        cellArrayList.add(new Rectangle(6, 6.1, 4, 2.1));   //c16
        cellArrayList.add(new Rectangle(10, 6.1, 4, 2.1));  //c15
        cellArrayList.add(new Rectangle(14, 6.1, 4, 2.1));  //c14
        cellArrayList.add(new Rectangle(12, 0, 4, 6.1));    //c13
        cellArrayList.add(new Rectangle(16, 0, 4, 6.1));    //c12
        cellArrayList.add(new Rectangle(12, 11.3, 4, 3));   //c11
        cellArrayList.add(new Rectangle(36-difference, 6.1, 4, 2.1));   //c9
        cellArrayList.add(new Rectangle(40-difference, 6.1, 4, 2.1));   //c8
        cellArrayList.add(new Rectangle(44-difference, 6.1, 4, 2.1));   //c7
        cellArrayList.add(new Rectangle(48-difference, 6.1, 4, 2.1));   //c6
        cellArrayList.add(new Rectangle(52-difference, 6.1, 4, 2.1));   //c5
        cellArrayList.add(new Rectangle(56-difference, 6.1, 4, 2.1));   //c4
        cellArrayList.add(new Rectangle(60-difference, 6.1, 4, 2.1));   //c3
        cellArrayList.add(new Rectangle(56, 8.2, 4, 6.1));   //c2
        cellArrayList.add(new Rectangle(60, 8.2, 4, 6.1));   //c1

    }

    protected void makeMaps2(){
        ////////////////////////////////////////////////////////////////////////
        //
        //Collision Map
        //
        ////////////////////////////////////////////////////////////////////////
        lineSegmentArrayList.add(new LineSegment(0, 0, 18.62, 0));//arround1
        lineSegmentArrayList.add(new LineSegment(0, 0, 0, 7.25));//arround2
        lineSegmentArrayList.add(new LineSegment(18.62, 0, 18.62, 7.25));//arround3
        lineSegmentArrayList.add(new LineSegment(0, 7.25, 18.62, 7.25));//arround4

        lineSegmentArrayList.add(new LineSegment(5.045, 0, 5.045, 3.36));//wall Willem/Victor
        lineSegmentArrayList.add(new LineSegment(9.325, 0, 9.325, 3.36));//wall Jerom/Victor
        lineSegmentArrayList.add(new LineSegment(13.61, 0, 13.61, 3.36));//wall Jerom/Jork

        lineSegmentArrayList.add(new LineSegment(12.95, 4.62, 12.95, 7.25));//wall Joost/Was
        lineSegmentArrayList.add(new LineSegment(12.95, 4.62, 15.24, 4.62));//wall Joost/hall
        lineSegmentArrayList.add(new LineSegment(11.75, 4.62, 11.75, 7.25));//wall meterkast/Was
        lineSegmentArrayList.add(new LineSegment(11.05, 4.62, 11.05, 7.25));//wall meterkast/hal
        lineSegmentArrayList.add(new LineSegment(11.05, 4.62, 11.75, 4.62));//wall meterkast/hal

        lineSegmentArrayList.add(new LineSegment(8.1, 4.62, 8.1, 7.25));//wall wc/Douche2
        lineSegmentArrayList.add(new LineSegment(8.1, 5.09, 9.55, 5.09));//wall wc/legeruimte
        lineSegmentArrayList.add(new LineSegment(8.1, 4.62, 9.55, 4.62));//wall hall/legeruimte
        lineSegmentArrayList.add(new LineSegment(9.55, 4.62, 9.55, 5.09));//wall hall/legeruimte

        lineSegmentArrayList.add(new LineSegment(8.1, 6.17, 9.55, 6.17));//wall wc/wc

        lineSegmentArrayList.add(new LineSegment(6.78, 4.94, 6.78, 7.25));//wall douche/douche

        lineSegmentArrayList.add(new LineSegment(5.43, 4.62, 5.43, 7.25));//wall douche/GR
        lineSegmentArrayList.add(new LineSegment(3.03, 4.62, 5.43, 4.62));//wall hall/GR

        lineSegmentArrayList.add(new LineSegment(0, 3.36, 3.79, 3.36));//wall kamer/hall1
        lineSegmentArrayList.add(new LineSegment(3.79, 3.36, 4.765, 3.36));//wall kamer/hall1 Deur willem
        lineSegmentArrayList.add(new LineSegment(4.765, 3.36, 5.325, 3.36));//wall kamer/hall1 W/V
        //lineSegmentArrayList.add(new LineSegment(5.325, 3.36, 6.3, 3.36));//wall kamer/hall1 Deur Victor
        lineSegmentArrayList.add(new LineSegment(6.3, 3.36, 12.355, 3.36));//wall kamer/hall1 J/V
        //lineSegmentArrayList.add(new LineSegment(12.355, 3.36, 13.33, 3.36));//wall kamer/hall1 Deur Jerom
        lineSegmentArrayList.add(new LineSegment(13.33, 3.36, 13.89, 3.36));//wall kamer/hall1 J/J
        lineSegmentArrayList.add(new LineSegment(13.89, 3.36, 14.865, 3.36));//wall kamer/hall1 Deur Jork
        lineSegmentArrayList.add(new LineSegment(14.865, 3.36, 18.62, 3.36));//wall kamer/hall1 J/J


        ////////////////////////////////////////////////////////////////////////
        //
        //Rectangle (room) map
        //
        ////////////////////////////////////////////////////////////////////////
        //rectangleArrayList.add(new Rectangle(0, 0, 5.045, 3.36));//Willems kamer
        rectangleArrayList.add(new Rectangle(5.045, 0, 4.28, 3.36));//Victors kamer
        rectangleArrayList.add(new Rectangle(9.325, 0, 4.285, 3.36));//Jeroms kamer
        //rectangleArrayList.add(new Rectangle(13.61, 0, 5.01, 3.36));//Jorks kamer
        rectangleArrayList.add(new Rectangle(0, 3.36, 3.03, 3.89));//GR1
        rectangleArrayList.add(new Rectangle(3.03, 4.62, 2.4, 2.63));//GR2
        rectangleArrayList.add(new Rectangle(12.95, 4.62, 2.29, 2.63));//Joost1
        rectangleArrayList.add(new Rectangle(15.24, 3.36, 3.38, 3.89));//Joost2

        rectangleArrayList.add(new Rectangle(3.03, 3.36, 12.21, 1.26));//Hall1
        rectangleArrayList.add(new Rectangle(5.43, 4.62, 2.67, .32));//Hall2
        rectangleArrayList.add(new Rectangle(9.55, 4.62, 1.5, 2.63));//Hall3

        rectangleArrayList.add(new Rectangle(11.75, 4.62, 1.2, 2.63));//wasHok

        rectangleArrayList.add(new Rectangle(5.43, 4.94, 1.35, 2.31));//Douche 1
        rectangleArrayList.add(new Rectangle(6.78, 4.94, 1.32, 2.31));//Douche 2

        rectangleArrayList.add(new Rectangle(8.1, 6.17, 1.45, 1.08));//wc1
        rectangleArrayList.add(new Rectangle(8.1, 5.09, 1.45, 1.08));//1c2

        ////////////////////////////////////////////////////////////////////////
        //
        //Cell map
        //
        ////////////////////////////////////////////////////////////////////////

        double difference = 0.8;
        cellArrayList.add(new Rectangle(2, 6.1, 4, 2.1));   //c19
        cellArrayList.add(new Rectangle(0, 0, 4, 6.1));     //c18
        cellArrayList.add(new Rectangle(4, 0, 4, 6.1));     //c17
        cellArrayList.add(new Rectangle(6, 6.1, 4, 2.1));   //c16
        cellArrayList.add(new Rectangle(10, 6.1, 4, 2.1));  //c15
        cellArrayList.add(new Rectangle(14, 6.1, 4, 2.1));  //c14
        cellArrayList.add(new Rectangle(12, 0, 4, 6.1));    //c13
        cellArrayList.add(new Rectangle(16, 0, 4, 6.1));    //c12
        cellArrayList.add(new Rectangle(12, 11.3, 4, 3));   //c11
        cellArrayList.add(new Rectangle(36-difference, 6.1, 4, 2.1));   //c9
        cellArrayList.add(new Rectangle(40-difference, 6.1, 4, 2.1));   //c8
        cellArrayList.add(new Rectangle(44-difference, 6.1, 4, 2.1));   //c7
        cellArrayList.add(new Rectangle(48-difference, 6.1, 4, 2.1));   //c6
        cellArrayList.add(new Rectangle(52-difference, 6.1, 4, 2.1));   //c5
        cellArrayList.add(new Rectangle(56-difference, 6.1, 4, 2.1));   //c4
        cellArrayList.add(new Rectangle(60-difference, 6.1, 4, 2.1));   //c3
        cellArrayList.add(new Rectangle(56, 8.2, 4, 6.1));   //c2
        cellArrayList.add(new Rectangle(60, 8.2, 4, 6.1));   //c1

    }

}
