package com.aj.foodcourt2;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aj.map.CollisionMap;
import com.aj.map.LineSegment;
import com.aj.map.RectangleMap;
import com.aj.map.TouchImageView;
import com.aj.particlefilter.GyroData;
import com.aj.particlefilter.MagneticData;
import com.aj.particlefilter.MotionDataHandler;
import com.aj.particlefilter.MotionListener;
import com.aj.particlefilter.Particle2;
import com.aj.particlefilter.ParticleManager;
import com.aj.particlefilter.Rectangle;
import com.aj.particlefilter.TimePositionData;
import com.aj.queuing.MacroBlockObject;
import com.aj.queuing.QueuingDataHandler;
import com.aj.queuing.QueuingListener;
import com.aj.queuing.QueuingSensorData;
import com.aj.server.ASyncServerReturn;
import com.aj.server.AsyncHttpPost;
import com.aj.wifi.WifiData;
import com.aj.wifi.WifiListener;
import com.aj.wifi.WifiPositionData;
import com.aj.wifi.WifiReceiver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocalizationActivity extends AppCompatActivity implements SensorEventListener, QueuingListener, MotionListener, WifiListener, ASyncServerReturn{

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
    private final static String LOCATION_MODE_NAME = "prefLocationMode";
    private final static String LOCATION_MANUAL_NAME = "prefLocationManual";
    private final static String LOCATION_AUTO_NAME = "prefLocationAuto";
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
    TextView tvAzimutDegrees, tvSteps;
    TextView tvCurrentLocation;


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

    private int enlargeFactor = 100; //50 for EWI building 100 for RDW
    private final double DIRECTION_SD = 20; //20 for EWI, 25 for RDW

    boolean motionDetection = false;
    Context context = this;

    //QDH to be used to analyse acceleration data and output step information and quining information
    private QueuingDataHandler queuingDataHandler;
    private MotionDataHandler motionDataHandler;

    String deviceId = "Unknown";

    Location locRDW = new Location("");
    Location locEWI = new Location("");
    Location locMe = new Location("");
    //LocationManager locationManager;

    LocationManager locationManager;

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

        tvAzimutDegrees = (TextView)findViewById(R.id.tv_azimut_deg);
        tvSteps = (TextView)findViewById(R.id.tv_steps_localization);
        tvCurrentLocation = (TextView)findViewById(R.id.tv_current_location);

        //Button Shine
        buttonBacktrack = (Button)findViewById(R.id.button_backtrack);
        buttonLocalize = (Button)findViewById(R.id.button_localize);
        buttonMotionDetection = (Button)findViewById(R.id.button_motion_detection);
        buttonReset = (Button)findViewById(R.id.button_reset_localization);

        buttonBacktrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backTrack();
            }
        });
        buttonLocalize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNearestLocation();
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



        //Init bitmap
        bg = Bitmap.createBitmap(3800,1000, Bitmap.Config.ARGB_8888);
        //Set paitns
        setPaints();
        touchImageMapView = (TouchImageView) findViewById(R.id.floor_map_zoom);
        touchImageMapView.setMaxZoom(8f);

        //Get Id
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        deviceId = tm.getDeviceId();

        //Localization

        locRDW.setLatitude(52.00069);
        locRDW.setLongitude(4.36907);

        locEWI.setLatitude(51.99885);
        locEWI.setLongitude(4.37395);

        //Toast.makeText(context, "Distance: "+locRDW.distanceTo(locEWI), Toast.LENGTH_LONG).show();
        //Log.d(LOG_TAG, "Distance: "+locRDW.distanceTo(locEWI));

        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        locMe.setLatitude(location.getLatitude());
        locMe.setLongitude(location.getLongitude());
        Log.d(LOG_TAG, "Distance RDW-Me: "+locRDW.distanceTo(locMe));
        Toast.makeText(context, "Distance RDW-Me: "+locRDW.distanceTo(locMe), Toast.LENGTH_LONG).show();

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }


            @Override
            public void onProviderEnabled(String provider) {

            }


            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        //lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);

    }


    @Override
    protected void onResume() {
        super.onResume();

        //Register all sensor listners
        sensorManager.registerListener(this, gravitySensor, 10000);
        sensorManager.registerListener(this, magneticSensor, 10000);
        sensorManager.registerListener(this, accelerometerSensor, 10000);
        sensorManager.registerListener(this, gyroSensor, 10000);

        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
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

        //Map things
        initiateMapAndParsifalManager();
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiReceiver);
        //Unregister sensor listeners
        //TODO: if sensor acrivities are done in a service, look again at this. This may break the sensors in the service
        sensorManager.unregisterListener(this);
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
        if(json!=null) {
            //Log.d("Json Object from server", "JSON: " + json.toString());
            try {
                int response_code = json.getInt("response_code");
                Toast.makeText(getApplicationContext(), "Server response, code: "+response_code,Toast.LENGTH_SHORT).show();
                switch(response_code){
                    case 1:

                        break;
                    case 2:
                        /*
                        HOW WIFIDATA is Saved:

                        -BASE (JSONObject):
                            -response_code (integer)
                            -error (boolean)
                            -response (JSONArray):
                                -WifiAndPositionData (JSONObject):
                                    -x_position (double)
                                    -y_position (double)
                                    -wifi_list (JSONArray):
                                        -wifi information (JSONObject):
                                            -BSSID (string)
                                            -SSID (string)
                                            -level (integer)
                         */

                        JSONArray results = json.getJSONArray("response");
                        int numberOfResponse = results.length();
                        Log.d(LOG_TAG, "WIFI response list length: " + numberOfResponse);
                        for(int i=0; i<numberOfResponse; i++){
                            JSONObject jsonObject = results.getJSONObject(i);
                            //Log.d(LOG_TAG, "WIFI object: " + jsonObject.toString());
                            String wifiListString = jsonObject.getString("wifi_list");
                            //Remove ' " ' at the beginning and end to cast it in JSONArray
                            wifiListString = wifiListString.substring(1,wifiListString.length()-1);
                            //Log.d(LOG_TAG, "WIFI list string: " + wifiListString);
                            JSONArray wifiList = new JSONArray(wifiListString.toString());
                            Log.d(LOG_TAG, "WIFI list: " + wifiList.toString());
                        }

                        break;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            Log.d("Json Object from server", "NULL object");
        }
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
        particleManager.moveAndDistribute(timestamp, direction, DIRECTION_SD, distance, distance/10);
        redrawMap();
    }

    @Override
    public void onWifiCheck(long timestamp) {
        Log.d("WifiState", "State: "+wifiManager.getWifiState());
        wifiManager.getWifiState();
        lastWifiScanTime = timestamp;
        scanStartTime = System.currentTimeMillis();
        wifiManager.startScan();
    }

    /*
    Not used in localization
     */
    @Override
    public void onNewDataBlock(int count, QueuingSensorData[] dataArray, MacroBlockObject blockObject) {

    }

    @Override
    public void onStepCount(ArrayList<Long> timeOfSteps, long endTime) {
        //Log.d("Steps", "Number of steps: " + timeOfSteps.size());
        tvSteps.setText("number of steps: " + timeOfSteps.size());
        if(motionDetection) {
            motionDataHandler.addSteps(timeOfSteps, endTime);
        }
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //Variables used in this method
        Sensor sensor = sensorEvent.sensor;
        double x, y, z;
        long timestamp;


        //Switch over sensor type to do corresponding actions for the selected sensor type
        switch (sensor.getType()){

            case Sensor.TYPE_GRAVITY:
                //Save gravity values in variable to be used for step detection and for magnetic calculation
                mGravity = sensorEvent.values;
                currentGravityX = sensorEvent.values[0];
                currentGravityY = sensorEvent.values[1];
                currentGravityZ = sensorEvent.values[2];
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:

                //Calculate magnetic direction using gravity data and magnetic data
                mGeomagnetic = sensorEvent.values;
                if (mGravity != null && mGeomagnetic != null) {
                    float R[] = new float[9];
                    float I[] = new float[9];
                    boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                    if (success) {
                        float orientation[] = new float[3];
                        SensorManager.getOrientation(R, orientation);
                        azimut = orientation[0]; // orientation contains: azimut, pitch and roll
                        degrees = (float) azimut * 180 / (float) Math.PI;

                        //tvAzimut.setText("azimut: " + String.format("%-3.3f",azimut) + " [radians]");
                        tvAzimutDegrees.setText("degrees: " + String.format("%-3.1f",degrees) + "[degrees]");

                        if(motionDetection) {
                            motionDataHandler.addMagneticData(new MagneticData(azimut, sensorEvent.timestamp));
                        }
                    }
                }

                break;
            case Sensor.TYPE_ACCELEROMETER:

                //Use accelerometer data in combination with gravity data to detect steps. Sending it to the QDH
                timestamp = sensorEvent.timestamp;
                x = sensorEvent.values[0];
                y = sensorEvent.values[1];
                z = sensorEvent.values[2];
                queuingDataHandler.addRawData(new QueuingSensorData(timestamp, x, y,z, currentGravityX, currentGravityY, currentGravityZ));

                break;
            case Sensor.TYPE_GYROSCOPE:
                if(motionDetection) {
                    motionDataHandler.addGyroData(new GyroData(sensorEvent.values[2], sensorEvent.timestamp));
                }
                break;
            default:
                break;
        }
    }


    /*
    Not used for localization
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onWifiData(List<ScanResult> wifiList) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Scan time (sec): " + ((System.currentTimeMillis()-scanStartTime)/1000.0));
        Log.d("scantime", "scanStartTime: " + scanStartTime);
        stringBuilder.append("\n\n");
        for(int i=0; i<wifiList.size(); i++){
            stringBuilder.append(new Integer(i+1).toString() + ": ");
            stringBuilder.append(wifiList.get(i).BSSID + " : " + wifiList.get(i).SSID + " . " + WifiManager.calculateSignalLevel(wifiList.get(i).level, 255) + " . " + wifiList.get(i).frequency);
            //stringBuilder.append(wifiList.get(i).toString());
            stringBuilder.append("\n\n");
        }
//        tvWifi.setText(stringBuilder.toString());
        Toast.makeText(getApplicationContext(), "Wifi update", Toast.LENGTH_SHORT).show();

        //Check if timestamp is already in list else add wifi data to list (check is needed for random wifi upadtes by android od. if not doing this wifiData is saved without knowing where it is saved)
        boolean notInList = true;
        for(WifiData wifiData : wifiDataArrayList){
            if(wifiData.getTimestamp() == lastWifiScanTime){
                notInList = false;
            }
        }

        //If not already in List add wifi list to arraylist
        if(notInList){
            wifiDataArrayList.add(new WifiData(lastWifiScanTime, wifiList));
        }
    }



    /*
    PAINTS and MAPS
     */
    public void backTrack(){

        sendWifiDataToServer();

        ArrayList<TimePositionData> trackedMeanData;
        double[] beginCoordinates = new double[2];
        double[] endCoordinates = new double[2];

        //trackedMeanData = particleManager.backTrack();
        trackedMeanData = particleManager.backTrack2();

        bg.eraseColor(android.graphics.Color.TRANSPARENT);

        Canvas canvas = new Canvas(bg);
        for (Rectangle rec : rectangleMap.getRectangles()) {
            canvas.drawRect((float) (rec.getX() * enlargeFactor)+X_OFFSET, (float) (rec.getY() * enlargeFactor)+Y_OFFSET, (float) ((rec.getX() + rec.getWidth()) * enlargeFactor)+X_OFFSET, (float) ((rec.getY() + rec.getHeight()) * enlargeFactor)+Y_OFFSET, paint);
        }
        int r = cellMap.isPointinRectangle(particleManager.getMeanX(), particleManager.getMeanY());
        if ((r >0)&&(r<cellArrayList.size())&&(particleManager.hasConverged())){
            Rectangle rec = cellArrayList.get(r);
            canvas.drawRect((float) (rec.getX() * enlargeFactor)+X_OFFSET, (float) (rec.getY() * enlargeFactor)+Y_OFFSET, (float) ((rec.getX() + rec.getWidth()) * enlargeFactor)+X_OFFSET, (float) ((rec.getY() + rec.getHeight()) * enlargeFactor)+Y_OFFSET, paintCell);
            canvas.drawRect((float) (rec.getX() * enlargeFactor)+X_OFFSET, (float) (rec.getY() * enlargeFactor)+Y_OFFSET, (float) ((rec.getX() + rec.getWidth()) * enlargeFactor)+X_OFFSET, (float) ((rec.getY() + rec.getHeight()) * enlargeFactor)+Y_OFFSET, paintCellStroke);
        }


        Particle2[] tmpParticleArray = particleManager.getParticleArray();

        for (Particle2 particle : tmpParticleArray) {
            //canvas.drawLine((float) particle.getOldX() * enlargeFactor, (float) particle.getOldY() * enlargeFactor, (float) particle.getX() * enlargeFactor, (float) particle.getY() * enlargeFactor, paintMove);
            if (!particle.isDestroyed()) {
                canvas.drawPoint((float) (particle.getX() * enlargeFactor)+X_OFFSET, (float) (particle.getY() * enlargeFactor)+Y_OFFSET, paintDot);
            } else {
                //canvas.drawPoint((float) (particle.getX() * enlargeFactor), (float) (particle.getY() * enlargeFactor), paintDotDestroy);
            }

        }

        for (LineSegment line : collisionMap.getLineSegments()) {
            canvas.drawLine((float) (line.getX1() * enlargeFactor)+X_OFFSET, (float) (line.getY1() * enlargeFactor)+Y_OFFSET, (float) (line.getX2() * enlargeFactor)+X_OFFSET, (float) (line.getY2() * enlargeFactor)+Y_OFFSET, paintCollision);
        }

        particleManager.calculateMean();
        canvas.drawPoint((float) (particleManager.getMeanX() * enlargeFactor + X_OFFSET), (float) (particleManager.getMeanY() * enlargeFactor) + Y_OFFSET, paintMean);
        //Log.d("Mean values", "x: " + particleManager.getMeanX() + " y:" + particleManager.getMeanY());




        for (int i = 0; i < (trackedMeanData.size()-1); i++) {
            beginCoordinates[0] = trackedMeanData.get(i).getxPosition();
            beginCoordinates[1] = trackedMeanData.get(i).getyPosition();
            endCoordinates[0] = trackedMeanData.get(i+1).getxPosition();
            endCoordinates[1] = trackedMeanData.get(i+1).getyPosition();
            canvas.drawLine((float) (beginCoordinates[0] * enlargeFactor)+X_OFFSET, (float) (beginCoordinates[1] * enlargeFactor)+Y_OFFSET, (float) (endCoordinates[0] * enlargeFactor)+X_OFFSET, (float) (endCoordinates[1] * enlargeFactor)+Y_OFFSET, paintBacktrack);
            Log.d("trackedCoordinates", "xAxis: " + beginCoordinates[0] + " yAxis: " + beginCoordinates[1] + "xAxis: " + endCoordinates[0] + " yAxis: " + endCoordinates[1]);
            //Log.d("trackedEndCoordinates", "xAxis: " + endCoordinates[0] + " yAxis: " + endCoordinates[1]);
        }

        //noinspection deprecation
        touchImageMapView.setImageBitmap(bg);
    }

    private void sendWifiDataToServer(){
        /*
        BUILDING ID
        1: EWI
        2: RDW
        3: Others
         */

        //Variables needed
        ArrayList<WifiPositionData> wifiPositionToSend = new ArrayList<>();
        ArrayList<TimePositionData> timePositionFromBacktrack = particleManager.backTrack2();

        //Connect wifi data with position
        for(WifiData wifiData: wifiDataArrayList){//Loop over all wifi data to get
            for(TimePositionData timePositionData: timePositionFromBacktrack){
                if(wifiData.getTimestamp() == timePositionData.getTimestamp()){
                    wifiPositionToSend.add(new WifiPositionData(wifiData, timePositionData));
                }
            }
        }

        //Check if still a list with
        if(wifiPositionToSend.size()<1){
            Log.d(LOG_TAG, "WiFi list is empty no things to be saved");
            return;
        }

        JSONArray jsonWifiArray = new JSONArray();

        for(WifiPositionData wifiPositionData: wifiPositionToSend){
            JSONObject tempJSON = new JSONObject();
            try {
                tempJSON.put("x_pos", wifiPositionData.getTimePositionData().getxPosition());
                tempJSON.put("y_pos", wifiPositionData.getTimePositionData().getyPosition());
                tempJSON.put("wifi_list", wifiPositionData.getWifiData().getWifiListJSONArray().toString());
                Log.d("wifi_list", "wifi_list: " + wifiPositionData.getWifiData().getWifiListJSONArray().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonWifiArray.put(tempJSON);
        }
        Log.d("wifi_data_array", "wifi_data_array: " + jsonWifiArray.toString());


        int buildingId = 1;
        if(settings.getBoolean(LOCATION_MODE_NAME, true)){
            buildingId = settings.getInt(LOCATION_MANUAL_NAME, 1);
        }else{
            buildingId = settings.getInt(LOCATION_AUTO_NAME, 1);
        }
        //send to Server
        HashMap<String, String> postData = new HashMap<String, String>();
        postData.put("building_id", Integer.toString(buildingId));
        postData.put("phone_id", deviceId);
        //Add wifi array
        postData.put("wifi_data_array", jsonWifiArray.toString());

        AsyncHttpPost asyncHttpPost = new AsyncHttpPost(LocalizationActivity.this, postData);
        asyncHttpPost.execute("https://trimbl-registration.herokuapp.com/wifi/addwifidata");
    }

    private void initiateMapAndParsifalManager(){
        //Clear lists
        rectangleArrayList.clear();
        cellArrayList.clear();
        lineSegmentArrayList.clear();

        //TODO: edit cell map RDW
        //Make maps to be used for distribution and collision
        //makeMapsEWI();
        //makeMapsRDW();

        if(settings.getBoolean(LOCATION_MODE_NAME, true)){//Manual mode
            switch(settings.getInt(LOCATION_MANUAL_NAME, 1)){
                case 1://EWI
                    makeMapsEWI();
                    enlargeFactor = 50;
                    break;
                case 2://RDW
                    makeMapsRDW();
                    enlargeFactor = 100;
                    break;
                default: //EWI
                    makeMapsEWI();
                    enlargeFactor = 50;
                    break;
            }
        }else{//Automatic mode
            switch(settings.getInt(LOCATION_AUTO_NAME, 1)){
                case 1://EWI
                    makeMapsEWI();
                    enlargeFactor = 50;
                    break;
                case 2://RDW
                    makeMapsRDW();
                    enlargeFactor = 100;
                    break;
                default: //EWI
                    makeMapsEWI();
                    enlargeFactor = 50;
                    break;
            }
        }

        //Make rectangle map
        rectangleMap = new RectangleMap(rectangleArrayList);
        rectangleMap.assignWeights();
        // make cell map
        cellMap = new RectangleMap(cellArrayList);
        cellMap.assignWeights();

        //init Collison Map
        collisionMap = new CollisionMap(lineSegmentArrayList);

        //initialize particle manager
        particleManager = new ParticleManager(10000, rectangleMap, collisionMap, getApplicationContext());

        //Get the array of current particles
        particleArray = particleManager.getParticleArray();

        redrawMap();
    }

    private void redrawMap(){

        //reset bitmap
        bg.eraseColor(android.graphics.Color.TRANSPARENT);

        //Make canvas to draw on
        Canvas canvas = new Canvas(bg);

        //Get all the rectangle chapes (rooms)
        for (Rectangle rec : rectangleMap.getRectangles()) {
            canvas.drawRect((float) (rec.getX() * enlargeFactor)+X_OFFSET, (float) (rec.getY() * enlargeFactor)+Y_OFFSET, (float) ((rec.getX() + rec.getWidth()) * enlargeFactor)+X_OFFSET, (float) ((rec.getY() + rec.getHeight()) * enlargeFactor)+Y_OFFSET, paint);
        }

        //Draw cell when converged
        if(particleManager.hasConverged()) {
            particleManager.calculateMean();
            int r = cellMap.isPointinRectangle(particleManager.getMeanX(), particleManager.getMeanY());
            if ((r >= 0) && (r < cellArrayList.size()) && (particleManager.hasConverged())) {
                Rectangle rec = cellArrayList.get(r);
                canvas.drawRect((float) (rec.getX() * enlargeFactor)+X_OFFSET, (float) (rec.getY() * enlargeFactor)+Y_OFFSET, (float) ((rec.getX() + rec.getWidth()) * enlargeFactor)+X_OFFSET, (float) ((rec.getY() + rec.getHeight()) * enlargeFactor)+Y_OFFSET, paintCell);
                canvas.drawRect((float) (rec.getX() * enlargeFactor)+X_OFFSET, (float) (rec.getY() * enlargeFactor)+Y_OFFSET, (float) ((rec.getX() + rec.getWidth()) * enlargeFactor)+X_OFFSET, (float) ((rec.getY() + rec.getHeight()) * enlargeFactor)+Y_OFFSET, paintCellStroke);
            }
        }


        //Get particles
        Particle2[] tmpParticleArray = particleManager.getParticleArray();

        //Draw particles
        for (Particle2 particle : tmpParticleArray) {
            //canvas.drawLine((float) particle.getOldX() * enlargeFactor, (float) particle.getOldY() * enlargeFactor, (float) particle.getX() * enlargeFactor, (float) particle.getY() * enlargeFactor, paintMove);
            if (!particle.isDestroyed()) {
                canvas.drawPoint((float) (particle.getX() * enlargeFactor)+X_OFFSET, (float) (particle.getY() * enlargeFactor)+Y_OFFSET, paintDot);
            } else {
                //canvas.drawPoint((float) (particle.getX() * enlargeFactor), (float) (particle.getY() * enlargeFactor), paintDotDestroy);
            }

        }

        //Draw collision map
        for (LineSegment line : collisionMap.getLineSegments()) {
            canvas.drawLine((float) (line.getX1() * enlargeFactor)+X_OFFSET, (float) (line.getY1() * enlargeFactor)+Y_OFFSET, (float) (line.getX2() * enlargeFactor)+X_OFFSET, (float) (line.getY2() * enlargeFactor)+Y_OFFSET, paintCollision);
        }

        particleManager.calculateMean();
        //Draw mean point DONE: calulate mean before hand here
        canvas.drawPoint((float) (particleManager.getMeanX() * enlargeFactor)+X_OFFSET, (float) (particleManager.getMeanY() * enlargeFactor)+Y_OFFSET, paintMean);
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


    public void getNearestLocation(){

       // Canvas canvas = new Canvas(bg);
        particleManager.calculateMean();
        int r = cellMap.nearestRectangle(particleManager.getMeanX(), particleManager.getMeanY());
        if ((r >= 0) && (r < cellArrayList.size())) {
            Rectangle rec = cellArrayList.get(r);
           // canvas.drawRect((float) (rec.getX() * enlargeFactor)+X_OFFSET, (float) (rec.getY() * enlargeFactor)+Y_OFFSET, (float) ((rec.getX() + rec.getWidth()) * enlargeFactor)+X_OFFSET, (float) ((rec.getY() + rec.getHeight()) * enlargeFactor)+Y_OFFSET, paintCell);
          //  canvas.drawRect((float) (rec.getX() * enlargeFactor)+X_OFFSET, (float) (rec.getY() * enlargeFactor)+Y_OFFSET, (float) ((rec.getX() + rec.getWidth()) * enlargeFactor)+X_OFFSET, (float) ((rec.getY() + rec.getHeight()) * enlargeFactor)+Y_OFFSET, paintCellStroke);
            tvCurrentLocation.setText("x: " + particleManager.getMeanX() + " y: " + particleManager.getMeanY() + "\nnearest cell: " + rec.getRectangleName());

        }



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

    protected void makeMapsEWI(){
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
        cellArrayList.add(new Rectangle(2, 6.1, 4, 2.1, "Cell 19"));   //c19
        cellArrayList.add(new Rectangle(0, 0, 4, 6.1, "Cell 18"));     //c18
        cellArrayList.add(new Rectangle(4, 0, 4, 6.1, "Cell 17"));     //c17
        cellArrayList.add(new Rectangle(6, 6.1, 4, 2.1, "Cell 16"));   //c16
        cellArrayList.add(new Rectangle(10, 6.1, 4, 2.1, "Cell 15"));  //c15
        cellArrayList.add(new Rectangle(14, 6.1, 4, 2.1, "Cell 14"));  //c14
        cellArrayList.add(new Rectangle(12, 0, 4, 6.1, "Cell 13"));    //c13
        cellArrayList.add(new Rectangle(16, 0, 4, 6.1, "Cell 12"));    //c12
        cellArrayList.add(new Rectangle(12, 11.3, 4, 3, "Cell 11"));   //c11
        cellArrayList.add(new Rectangle(36-difference, 6.1, 4, 2.1, "Cell 09"));   //c9
        cellArrayList.add(new Rectangle(40-difference, 6.1, 4, 2.1, "Cell 08"));   //c8
        cellArrayList.add(new Rectangle(44-difference, 6.1, 4, 2.1, "Cell 07"));   //c7
        cellArrayList.add(new Rectangle(48-difference, 6.1, 4, 2.1, "Cell 06"));   //c6
        cellArrayList.add(new Rectangle(52-difference, 6.1, 4, 2.1, "Cell 05"));   //c5
        cellArrayList.add(new Rectangle(56-difference, 6.1, 4, 2.1, "Cell 04"));   //c4
        cellArrayList.add(new Rectangle(60-difference, 6.1, 4, 2.1, "Cell 03"));   //c3
        cellArrayList.add(new Rectangle(56, 8.2, 4, 6.1, "Cell 02"));   //c2
        cellArrayList.add(new Rectangle(60, 8.2, 4, 6.1, "Cell 01"));   //c1

    }

    protected void makeMapsRDW(){
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
        cellArrayList.add(new Rectangle(0, 0, 5.045, 3.36, "Willem's Room"));//Willems kamer
        cellArrayList.add(new Rectangle(5.045, 0, 4.28, 3.36, "Victor's Room"));//Victors kamer
        cellArrayList.add(new Rectangle(9.325, 0, 4.285, 3.36, "Jerom's Room"));//Jeroms kamer
        cellArrayList.add(new Rectangle(13.61, 0, 5.01, 3.36, "Jork's Room"));//Jorks kamer
        cellArrayList.add(new Rectangle(0, 3.36, 3.03, 3.89, "General Room 1"));//GR1
        cellArrayList.add(new Rectangle(3.03, 4.62, 2.4, 2.63, "General Room 2"));//GR2
        cellArrayList.add(new Rectangle(12.95, 4.62, 2.29, 2.63, "Joost's Room 1"));//Joost1
        cellArrayList.add(new Rectangle(15.24, 3.36, 3.38, 3.89, "Joost's Room 2"));//Joost2

        cellArrayList.add(new Rectangle(3.03, 3.36, 12.21, 1.26, "Hall 1"));//Hall1
        cellArrayList.add(new Rectangle(5.43, 4.62, 2.67, .32, "Hall 2"));//Hall2
        cellArrayList.add(new Rectangle(9.55, 4.62, 1.5, 2.63, "Hall 3"));//Hall3

        cellArrayList.add(new Rectangle(11.75, 4.62, 1.2, 2.63, "Laundry Room"));//wasHok

        cellArrayList.add(new Rectangle(5.43, 4.94, 1.35, 2.31, "Shower 1"));//Douche 1
        cellArrayList.add(new Rectangle(6.78, 4.94, 1.32, 2.31, "Shower 2"));//Douche 2

        cellArrayList.add(new Rectangle(8.1, 6.17, 1.45, 1.08, "Toilet 1"));//wc1
        cellArrayList.add(new Rectangle(8.1, 5.09, 1.45, 1.08, "Toilet 2"));//wc2

    }



}
