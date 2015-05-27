package com.aj.foodcourt2;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.aj.map.CollisionMap;
import com.aj.map.LineSegment;
import com.aj.map.RectangleMap;
import com.aj.map.TouchImageView;
import com.aj.particlefilter.Particle2;
import com.aj.particlefilter.ParticleManager;
import com.aj.particlefilter.Rectangle;

import java.util.ArrayList;


public class MapActivity extends ActionBarActivity {

    ArrayList<Rectangle> rectangleArrayList = new ArrayList<Rectangle>();
    ArrayList<LineSegment> lineSegmentArrayList = new ArrayList<LineSegment>();
    RectangleMap rectangleMap;
    CollisionMap collisionMap;
    LinearLayout li;
    ParticleManager particleManager;
    Particle2[] particleArray;

    TouchImageView mImage;

    EditText etDirection, etDistance;
    Button buttonMove;
    Bitmap bg;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        etDirection = (EditText)findViewById(R.id.editText_direction);
        etDistance = (EditText)findViewById(R.id.editText_distance);
        buttonMove = (Button)findViewById(R.id.button_move);


        //Line Segments
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

        //Make map
        rectangleArrayList.add(new Rectangle(0, 0, 8, 6.1));//conference room
        rectangleArrayList.add(new Rectangle(0, 6.1, 72, 2.1));//halway
        rectangleArrayList.add(new Rectangle(12, 0, 4, 6.1));//room1
        rectangleArrayList.add(new Rectangle(16, 0, 4, 6.1));//room2
        rectangleArrayList.add(new Rectangle(15, 8.2, 1, 3.1));//halway to coffeeroom
        rectangleArrayList.add(new Rectangle(12, 11.3, 4, 3));//coffeeroom
        rectangleArrayList.add(new Rectangle(56, 8.2, 4, 6.1));//room3
        rectangleArrayList.add(new Rectangle(60, 8.2, 4, 6.1));//room4

        bg = Bitmap.createBitmap(3700,1000, Bitmap.Config.ARGB_8888);

        rectangleMap = new RectangleMap(rectangleArrayList);
        rectangleMap.assignWeights();

        collisionMap = new CollisionMap(lineSegmentArrayList);

        particleManager = new ParticleManager(10000, rectangleMap, collisionMap);

        particleArray = particleManager.getParticleArray();

        li = (LinearLayout) findViewById(R.id.floor_map);

        mImage = (TouchImageView) findViewById(R.id.floor_map_zoom);
        mImage.setMaxZoom(8f);


        Paint paint = new Paint();
        paint.setColor(Color.rgb(0,0,0));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5.0f);

        Paint paintDot = new Paint();
        paintDot.setColor(Color.rgb(51,255,51));
        paintDot.setStyle(Paint.Style.FILL);
        paintDot.setStrokeWidth(2.0f);

        Paint paintDotDestroy = new Paint();
        paintDotDestroy.setColor(Color.rgb(255,51,51));
        paintDotDestroy.setStyle(Paint.Style.FILL);
        paintDotDestroy.setStrokeWidth(2.0f);

        Paint paintCollision = new Paint();
        paintCollision.setColor(Color.rgb(255,51,255));
        paintCollision.setStyle(Paint.Style.FILL);
        paintCollision.setStrokeWidth(5.0f);




        Canvas canvas = new Canvas(bg);
        for (Rectangle rec : rectangleMap.getRectangles()){
            canvas.drawRect((float)(rec.getX()*50), (float)(rec.getY()*50), (float)((rec.getX() + rec.getWidth())*50), (float)((rec.getY() + rec.getHeight())*50), paint);
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
        li.setBackgroundDrawable(new BitmapDrawable(bg));
        mImage.setImageBitmap(bg);


        buttonMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double direction = Double.parseDouble(etDirection.getText().toString());
                double distance = Double.parseDouble(etDistance.getText().toString());
                particleManager.moveAndDistribute(direction, 15, distance, (distance / 10));

                particleManager.calculateMean();


                Paint paint = new Paint();
                paint.setColor(Color.rgb(0, 0, 0));
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(5.0f);

                Paint paintDot = new Paint();
                paintDot.setColor(Color.rgb(51, 255, 51));
                paintDot.setStyle(Paint.Style.FILL);
                paintDot.setStrokeWidth(2.0f);

                Paint paintDotDestroy = new Paint();
                paintDotDestroy.setColor(Color.rgb(255, 51, 51));
                paintDotDestroy.setStyle(Paint.Style.FILL);
                paintDotDestroy.setStrokeWidth(2.0f);

                Paint paintCollision = new Paint();
                paintCollision.setColor(Color.rgb(255, 51, 255));
                paintCollision.setStyle(Paint.Style.FILL);
                paintCollision.setStrokeWidth(5.0f);

                Paint paintMove = new Paint();
                paintMove.setColor(Color.rgb(0, 51, 255));
                paintMove.setStyle(Paint.Style.FILL);
                paintMove.setStrokeWidth(2.0f);

                Paint paintMean = new Paint();
                paintMean.setColor(Color.rgb(0, 155, 155));
                paintMean.setStyle(Paint.Style.FILL);
                paintMean.setStrokeWidth(10.0f);


                bg.eraseColor(android.graphics.Color.TRANSPARENT);

                Canvas canvas = new Canvas(bg);
                for (Rectangle rec : rectangleMap.getRectangles()){
                    canvas.drawRect((float)(rec.getX()*50), (float)(rec.getY()*50), (float)((rec.getX() + rec.getWidth())*50), (float)((rec.getY() + rec.getHeight())*50), paint);
                }

                Particle2[] tmpParticleArray = particleManager.getParticleArray();

                for(Particle2 particle : tmpParticleArray){
                    //canvas.drawLine((float) particle.getOldX() * 50, (float) particle.getOldY() * 50, (float) particle.getX() * 50, (float) particle.getY() * 50, paintMove);
                    if(!particle.isDestroyed()) {
                        canvas.drawPoint((float) (particle.getX() * 50), (float) (particle.getY() * 50), paintDot);
                    }else{
                        //canvas.drawPoint((float) (particle.getX() * 50), (float) (particle.getY() * 50), paintDotDestroy);
                    }

                }

                for(LineSegment line: collisionMap.getLineSegments()){
                    canvas.drawLine((float) line.getX1() * 50, (float) line.getY1() * 50, (float) line.getX2() * 50, (float) line.getY2() * 50, paintCollision);
                }

                canvas.drawPoint((float)(particleManager.getMeanX()*50), (float)(particleManager.getMeanY()*50), paintMean);
                Log.d("Mean values","x: "+particleManager.getMeanX()+" y:"+particleManager.getMeanY());


                //noinspection deprecation
                li.setBackgroundDrawable(new BitmapDrawable(bg));
                mImage.setImageBitmap(bg);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
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
}
