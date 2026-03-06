package com.inflps.pcd.WIDGET.SLIDERS;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class RotaryJogSlider extends View {
    
    private float minValue = 0f;
    private float maxValue = 1.0f;
    private float currentValue = 0.5f;
    
    private Paint linePaint;
    private Paint centerMarkerPaint;
    private float zoomFactor = 1.0f; 
    private final float baseLineSpacing = dpToPx(20);
    
    private OnValueChangedListener listener;
    private float lastTouchX;
    
    private final Handler zoomHandler = new Handler(Looper.getMainLooper());
    private boolean isZoomed = false;
    private static final int ZOOM_DELAY = 300;
    
    public interface OnValueChangedListener {
        void onValueChanged(float value);
    }
    
    public RotaryJogSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        
        centerMarkerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerMarkerPaint.setColor(0xFFFFD700); 
        centerMarkerPaint.setStrokeWidth(dpToPx(3));
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        float width = getWidth();
        float height = getHeight();
        float midX = width / 2f;
        float radius = width / 2.2f; 
        
        float effectiveSpacing = baseLineSpacing * zoomFactor;
        float range = maxValue - minValue;
        float densityMultiplier = (range > 10) ? 1.0f : 10.0f;
        
        float totalOffset = (currentValue * effectiveSpacing * densityMultiplier);
        
        for (int i = -60; i <= 60; i++) {
            float toothValue = (Math.round(totalOffset / effectiveSpacing) + i) / densityMultiplier;
            float rawX = midX + (toothValue * densityMultiplier * effectiveSpacing) - totalOffset;
            float distanceRatio = (rawX - midX) / (width / 2f);
            
            if (Math.abs(distanceRatio) <= 1.0f) {
                float curvedX = midX + (float) Math.sin(distanceRatio * (Math.PI / 2)) * radius;
                float fade = (float) Math.cos(distanceRatio * (Math.PI / 2));
                
                if (toothValue < minValue || toothValue > maxValue) {
                    linePaint.setColor(Color.DKGRAY);
                    linePaint.setAlpha((int) (fade * 60));
                } else {
                    linePaint.setColor(Color.WHITE);
                    linePaint.setAlpha((int) (fade * 255));
                }
                
                boolean isMajor;
                if (range > 10) {
                    isMajor = Math.round(toothValue) % 10 == 0;
                } else {
                    isMajor = Math.abs(Math.round(toothValue * 10)) % 5 == 0;
                }
                
                float thickness = isMajor ? dpToPx(2.5f) : dpToPx(1.2f);
                float lineHeight = isMajor ? height * 0.5f : height * 0.25f;
                
                linePaint.setStrokeWidth(thickness);
                canvas.drawLine(curvedX, (height - lineHeight) / 2, curvedX, (height + lineHeight) / 2, linePaint);
            }
        }
        canvas.drawLine(midX, height * 0.1f, midX, height * 0.9f, centerMarkerPaint);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float range = maxValue - minValue;
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
                lastTouchX = event.getX();
                zoomHandler.postDelayed(() -> {
                    isZoomed = true;
                    animateZoom(2.5f);
                }, ZOOM_DELAY);
                return true;

            case MotionEvent.ACTION_MOVE:
                float deltaX = event.getX() - lastTouchX;
                float baseSens = isZoomed ? 0.0005f : 0.002f; 
                currentValue -= (deltaX * baseSens * range); 

                if (currentValue < minValue) currentValue = minValue;
                if (currentValue > maxValue) currentValue = maxValue;

                lastTouchX = event.getX();
                if (listener != null) listener.onValueChanged(currentValue);
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(false);
                zoomHandler.removeCallbacksAndMessages(null);
                if (isZoomed) {
                    isZoomed = false;
                    animateZoom(1.0f);
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void animateZoom(float target) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "zoomFactor", zoomFactor, target);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> invalidate());
        animator.start();
    }
    
    public void setZoomFactor(float zoom) { this.zoomFactor = zoom; }
    public void setConfig(float min, float max, float current) {
        this.minValue = min; this.maxValue = max; this.currentValue = current;
        invalidate();
    }
    public void setValue(float value) {
        this.currentValue = value;
        invalidate();
    }
    public void setOnValueChangedListener(OnValueChangedListener l) { this.listener = l; }

    private float dpToPx(float dp) { 
        return dp * getResources().getDisplayMetrics().density; 
    }
}
