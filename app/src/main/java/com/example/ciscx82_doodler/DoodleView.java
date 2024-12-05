package com.example.ciscx82_doodler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class DoodleView extends View {
    private Paint paint;
    private List<Point> points;
    private Bitmap backgroundBitmap;

    public DoodleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(0xFF000000); // Default to black
        paint.setStrokeWidth(10);
        paint.setAlpha(255); // Default fully opaque

        points = new ArrayList<>();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the background bitmap if available
        if (backgroundBitmap != null) {
            canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        }

        for (Point point : points) {
            paint.setStrokeWidth(point.size);
            paint.setColor(point.color);
            paint.setAlpha(point.opacity);
            canvas.drawCircle(point.x, point.y, point.size / 2, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
            points.add(new Point(event.getX(), event.getY(), paint.getColor(), paint.getAlpha(), paint.getStrokeWidth()));
            invalidate();
        }
        return true;
    }

    public void setBrushSize(float size) {
        paint.setStrokeWidth(size);
    }

    public void setBrushColor(int color) {
        int currentOpacity = paint.getAlpha(); // Get the current opacity
        paint.setColor(color);
        paint.setAlpha(currentOpacity); // Restore the opacity
    }

    public void setBrushOpacity(int opacity) {
        paint.setAlpha(opacity);
    }

    public void clearCanvas() {
        points.clear();
        backgroundBitmap = null;
        invalidate();
    }

    private static class Point {
        float x, y;
        int color;
        int opacity;
        float size;

        Point(float x, float y, int color, int opacity, float size) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.opacity = opacity;
            this.size = size;
        }
    }

    // Method to get the bitmap of the current doodle
    public Bitmap getBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas); // Draw the view onto the canvas
        return bitmap;
    }

    // Method to load a bitmap as background
    public void loadBitmap(Bitmap bitmap) {
        backgroundBitmap = Bitmap.createScaledBitmap(bitmap, getWidth(), getHeight(), true);
        invalidate();
    }
}
