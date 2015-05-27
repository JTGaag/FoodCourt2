package com.aj.foodcourt2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Joost on 26/05/2015.
 */
public class CustomDrawableView extends View {
    Paint paint = new Paint();
    Float azimut = 0.0f;

    public CustomDrawableView(Context context) {
        super(context);
        paint.setColor(0xff00ff00);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setAntiAlias(true);
    };

    public CustomDrawableView(Context context, AttributeSet attr) {
        super(context, attr);
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
        canvas.drawText("N", centerx + 5, centery - 10, paint);
        canvas.drawText("S", centerx - 10, centery + 15, paint);
        paint.setColor(0xff00ff00);
    }

    public void setAzimut(float azimut){
        this.azimut = azimut;
    }
}