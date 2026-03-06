package com.inflps.pcd.CORE.BRUSH_CORE;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class BrushPreviewView extends View {
    private BrushSettings settings;
    private BrushEngine engine = new BrushEngine();
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    
    private Bitmap drawingLayer;
    private Canvas drawingCanvas;
    private Path internalPath = new Path();
    
    private final Handler renderHandler = new Handler(Looper.getMainLooper());
    private final Runnable renderRunnable = this::performFullRender;

    public BrushPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.WHITE);
    }

    public void setSettings(BrushSettings s) { 
        this.settings = s; 
        renderPath(); 
    }

    public void renderPath() {
        renderHandler.removeCallbacks(renderRunnable);
        renderHandler.postDelayed(renderRunnable, 5);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0 || h <= 0) return;

        drawingLayer = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawingCanvas = new Canvas(drawingLayer);
        generateDefaultStartupPath(w, h);
        performFullRender();
    }

    private void generateDefaultStartupPath(int w, int h) {
        internalPath.reset();
        internalPath.moveTo(w * 0.1f, h * 0.6f);
        internalPath.cubicTo(
            w * 0.33f, h * 0.1f,
            w * 0.66f, h * 0.9f,
            w * 0.9f, h * 0.4f
        );
    }

    private void performFullRender() {
        if (drawingCanvas == null || settings == null || internalPath.isEmpty()) return;
        drawingCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        PathMeasure pm = new PathMeasure(internalPath, false);
        float length = pm.getLength();
        float[] pos = new float[2];
        
        pm.getPosTan(0, pos, null);
        engine.startStroke(pos[0], pos[1]);
        
        float step = 6f; 
        for (float distance = step; distance <= length; distance += step) {
            pm.getPosTan(distance, pos, null);
            engine.strokeTo(drawingCanvas, paint, settings, 60f, pos[0], pos[1]);
        }
        
        engine.endStroke(drawingCanvas, paint, settings, 60f);
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                internalPath.reset();
                internalPath.moveTo(x, y);
                drawingCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                engine.startStroke(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                internalPath.lineTo(x, y);
                engine.strokeTo(drawingCanvas, paint, settings, 60f, x, y);
                break;
            case MotionEvent.ACTION_UP:
                engine.endStroke(drawingCanvas, paint, settings, 60f);
                break;
        }
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (drawingLayer != null) {
            canvas.drawBitmap(drawingLayer, 0, 0, null);
        }
    }
}
