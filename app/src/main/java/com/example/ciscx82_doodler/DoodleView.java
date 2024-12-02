package com.example.ciscx82_doodler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class DoodleView extends View {
    private Paint paint;
    private List<Point> points;

    public DoodleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(0xFF000000); // Default to black
        paint.setStrokeWidth(10);

        points = new ArrayList<>();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Point point : points) {
            paint.setStrokeWidth(point.size);
            paint.setColor(point.color);
            canvas.drawCircle(point.x, point.y, point.size / 2, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
            points.add(new Point(event.getX(), event.getY(), paint.getColor(), paint.getStrokeWidth()));
            invalidate();
        }
        return true;
    }

    public void setBrushSize(float size) {
        paint.setStrokeWidth(size);
    }

    public void setBrushColor(int color) {
        paint.setColor(color);
    }

    public void clearCanvas() {
        points.clear();
        invalidate();
    }

    private static class Point {
        float x, y;
        int color;
        float size;

        Point(float x, float y, int color, float size) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.size = size;
        }
    }
}
