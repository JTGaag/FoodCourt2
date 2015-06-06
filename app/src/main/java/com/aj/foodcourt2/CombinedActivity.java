package com.aj.foodcourt2;


import android.graphics.Bitmap;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.aj.map.CollisionMap;
import com.aj.map.LineSegment;
import com.aj.map.RectangleMap;
import com.aj.map.TouchImageView;
import com.aj.particlefilter.GyroData;
import com.aj.particlefilter.MotionDataHandler;
import com.aj.particlefilter.MotionListener;
import com.aj.particlefilter.Particle2;
import com.aj.particlefilter.ParticleManager;
import com.aj.particlefilter.Rectangle;
import com.aj.queuing.MacroBlockObject;
import com.aj.queuing.QueuingDataHandler;
import com.aj.queuing.QueuingListener;
import com.aj.queuing.QueuingSensorData;

import java.util.ArrayList;

public class CombinedActivity extends ActionBarActivity implements SensorEventListener, QueuingListener, MotionListener {

    //sensors
    private SensorManager sensorManager;
    private Sensor gravitySensor;
    private Sensor magneticSensor;
    private Sensor accelerometerSensor;
    private Sensor gyroSensor;

    //magnetometer
    float[] mGravity;
    float[] mGeomagnetic;
    float azimut;
    float degrees;
    private TextView tvAzimut, tvAzimutDegrees, tvSteps;

    //Constants
    final double NS2S = 1.0/1000000000.0;

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
    TouchImageView mImage;

    //Stuff
    EditText etDirection, etDistance;
    Button buttonMove;
    Bitmap bg;

    //Variables to save gravity data
    private double currentGravityX = 0;
    private double currentGravityY = 0;
    private double currentGravityZ = -9;

    //QDH to be used to analyse acceleration data and output step information and quining information
    private QueuingDataHandler queuingDataHandler;
    private MotionDataHandler motionDataHandler;


    //Paints
    Paint paint = new Paint();
    Paint paintCell = new Paint();
    Paint paintCellStroke = new Paint();
    Paint paintDot = new Paint();
    Paint paintDotDestroy = new Paint();
    Paint paintCollision = new Paint();
    Paint paintMean = new Paint();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combined);

        //Initiate sensors
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        //TextViews
        tvAzimut = (TextView) findViewById(R.id.tv_azimut);
        tvAzimutDegrees = (TextView) findViewById(R.id.tv_azimut_degrees);
        tvSteps = (TextView) findViewById(R.id.tv_steps);

        //Input objects
        etDirection = (EditText)findViewById(R.id.editText_direction_combined);
        etDistance = (EditText)findViewById(R.id.editText_distance_combined);
        buttonMove = (Button)findViewById(R.id.button_move_combined);

        //Make maps to be used for distribution and collision
        makeMaps();

        //Init bitmap
        bg = Bitmap.createBitmap(3700,1000, Bitmap.Config.ARGB_8888);

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

        //Set paitns
        setPaints();

        mImage = (TouchImageView) findViewById(R.id.floor_map_zoom);
        mImage.setMaxZoom(8f);


        Canvas canvas = new Canvas(bg);
        for (Rectangle rec : rectangleMap.getRectangles()){
            canvas.drawRect((float)(rec.getX()*50), (float)(rec.getY()*50), (float)((rec.getX() + rec.getWidth())*50), (float)((rec.getY() + rec.getHeight())*50), paint);
        }

        int r = cellMap.isPointinRectangle(particleManager.getMeanX(), particleManager.getMeanY());
        if ((r >0)&&(r<cellArrayList.size()&&(particleManager.hasConverged()))){
            Rectangle rec = cellArrayList.get(r);
            canvas.drawRect((float)(rec.getX()*50), (float)(rec.getY()*50), (float)((rec.getX() + rec.getWidth())*50), (float)((rec.getY() + rec.getHeight())*50), paintCell);
            canvas.drawRect((float)(rec.getX()*50), (float)(rec.getY()*50), (float)((rec.getX() + rec.getWidth())*50), (float)((rec.getY() + rec.getHeight())*50), paintCellStroke);
        }


        for(Particle2 particle : particleArray){
            if(!particle.isDestroyed()) {
                canvas.drawPoint((float) (particle.getX() * 50), (float) (particle.getY() * 50), paintDot);
            }else{
                //canvas.drawPoint((float) (particle.getX() * 50), (float) (particle.getY() * 50), paintDotDestroy);
            }
        }

        for(LineSegment line: collisionMap.getLineSegments()){
            canvas.drawLine((float)line.getX1()*50,(float)line.getY1()*50, (float)line.getX2()*50, (float)line.getY2()*50, paintCollision);
        }


        //noinspection deprecation
        mImage.setImageBitmap(bg);


        //Move particle button (manual particle movement)
        buttonMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double direction = Double.parseDouble(etDirection.getText().toString());
                double distance = Double.parseDouble(etDistance.getText().toString());
                particleManager.moveAndDistribute(direction, 15, distance, (distance / 10));

                particleManager.calculateMean();

                bg.eraseColor(android.graphics.Color.TRANSPARENT);

                Canvas canvas = new Canvas(bg);
                for (Rectangle rec : rectangleMap.getRectangles()) {
                    canvas.drawRect((float) (rec.getX() * 50), (float) (rec.getY() * 50), (float) ((rec.getX() + rec.getWidth()) * 50), (float) ((rec.getY() + rec.getHeight()) * 50), paint);
                }
                int r = cellMap.isPointinRectangle(particleManager.getMeanX(), particleManager.getMeanY());
                if ((r >0)&&(r<cellArrayList.size())&&(particleManager.hasConverged())){
                    Rectangle rec = cellArrayList.get(r);
                    canvas.drawRect((float)(rec.getX()*50), (float)(rec.getY()*50), (float)((rec.getX() + rec.getWidth())*50), (float)((rec.getY() + rec.getHeight())*50), paintCell);
                    canvas.drawRect((float)(rec.getX()*50), (float)(rec.getY()*50), (float)((rec.getX() + rec.getWidth())*50), (float)((rec.getY() + rec.getHeight())*50), paintCellStroke);
                }


                Particle2[] tmpParticleArray = particleManager.getParticleArray();

                for (Particle2 particle : tmpParticleArray) {
                    //canvas.drawLine((float) particle.getOldX() * 50, (float) particle.getOldY() * 50, (float) particle.getX() * 50, (float) particle.getY() * 50, paintMove);
                    if (!particle.isDestroyed()) {
                        canvas.drawPoint((float) (particle.getX() * 50), (float) (particle.getY() * 50), paintDot);
                    } else {
                        //canvas.drawPoint((float) (particle.getX() * 50), (float) (particle.getY() * 50), paintDotDestroy);
                    }

                }

                for (LineSegment line : collisionMap.getLineSegments()) {
                    canvas.drawLine((float) line.getX1() * 50, (float) line.getY1() * 50, (float) line.getX2() * 50, (float) line.getY2() * 50, paintCollision);
                }

                canvas.drawPoint((float) (particleManager.getMeanX() * 50), (float) (particleManager.getMeanY() * 50), paintMean);
                //Log.d("Mean values", "x: " + particleManager.getMeanX() + " y:" + particleManager.getMeanY());


                //noinspection deprecation
                mImage.setImageBitmap(bg);
            }
        });

        /*
        Initialize all data handlers
         */
        queuingDataHandler = new QueuingDataHandler(this, 50, 3, 7);
        motionDataHandler = new MotionDataHandler(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Register all sensor listners
        sensorManager.registerListener(this, gravitySensor, 10000);
        sensorManager.registerListener(this, magneticSensor, 10000);
        sensorManager.registerListener(this, accelerometerSensor, 10000);
        sensorManager.registerListener(this, gyroSensor, 10000);
    }

    @Override
    protected void onPause() {
        super.onPause();

        //Unregister sensor listeners
        //TODO: if sensor acrivities are done in a service, look again at this. This may break the sensors in the service
        sensorManager.unregisterListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_combined, menu);
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

                        tvAzimut.setText("azimut: " + String.format("%-3.3f",azimut) + " [radians]");
                        tvAzimutDegrees.setText("degrees: " + String.format("%-3.1f",degrees) + "[degrees]");

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

                motionDataHandler.addGyroData(new GyroData(sensorEvent.values[2], sensorEvent.timestamp));

                break;
            default:
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onNewDataBlock(int count, QueuingSensorData[] dataArray, MacroBlockObject blockObject) {
        tvSteps.setText("number of steps: " + count);
    }

    @Override
    public void onStepCount(ArrayList<Long> timeOfSteps) {
        Log.d("Steps", "Number of steps: " + timeOfSteps.size());
        for(long time: timeOfSteps){
        }
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

    protected void setPaints(){
        //Set Paints
        paint.setColor(Color.rgb(0, 0, 0));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5.0f);

        paintCell.setColor(Color.argb(128, 255, 0, 0));
        paintCell.setStyle(Paint.Style.FILL);

        paintCellStroke.setColor(Color.rgb(128, 0, 0));
        paintCellStroke.setStyle(Paint.Style.STROKE);
        paintCellStroke.setStrokeWidth(5.0f);

        paintDot.setColor(Color.rgb(51, 128, 51));
        paintDot.setStyle(Paint.Style.FILL);
        paintDot.setStrokeWidth(5.0f);

        paintDotDestroy.setColor(Color.rgb(255,51,51));
        paintDotDestroy.setStyle(Paint.Style.FILL);
        paintDotDestroy.setStrokeWidth(2.0f);

        paintCollision.setColor(Color.rgb(255,51,255));
        paintCollision.setStyle(Paint.Style.FILL);
        paintCollision.setStrokeWidth(5.0f);

        paintMean.setColor(Color.rgb(255, 0, 0));
        paintMean.setStyle(Paint.Style.FILL);
        paintMean.setStrokeWidth(10.0f);
    }

    /**
     * Called when straight motion has ended or specific time of time has passed.
     * This method will start the motion in the particle manager
     *
     * @param direction direction of detected motion
     * @param distance  distance of detected motion
     */
    @Override
    public void onMotion(double direction, double distance) {

    }
}
