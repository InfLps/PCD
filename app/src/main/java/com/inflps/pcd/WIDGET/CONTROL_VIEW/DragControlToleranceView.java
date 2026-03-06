package com.inflps.pcd.WIDGET.CONTROL_VIEW;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DragControlToleranceView extends View {
    private float sensitivity = 0.5f;
    private int minValue = 0;
    private int maxValue = 100;
    private float currentValue = 1;
    private int centerDotRadiusDp = 4;
    private int maxVisualRadiusDp = 35;
    private int minVisualRadiusDp = 8;
    private int primaryColor = Color.BLUE;
    private int toleranceAlpha = 100;
    private int textColor = Color.WHITE;
    private float textSizeDp = 16;
    private float lastTouchY;
    private boolean isDragging = false;
    private Paint centerPaint;
    private Paint tolerancePaint;
    private Paint textPaint;

    public interface OnValueChangeListener {
        void onValueChange(float newValue);
    }

    private OnValueChangeListener valueChangeListener;

    public void setOnValueChangeListener(OnValueChangeListener listener) {
        this.valueChangeListener = listener;
    }

    public DragControlToleranceView(Context context) {
        super(context);
        init();
    }

    public DragControlToleranceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DragControlToleranceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        centerPaint = new Paint();
        centerPaint.setAntiAlias(true);
        centerPaint.setColor(primaryColor);
        centerPaint.setStyle(Paint.Style.FILL);
        tolerancePaint = new Paint();
        tolerancePaint.setAntiAlias(true);
        tolerancePaint.setColor(primaryColor);
        tolerancePaint.setStyle(Paint.Style.FILL);
        tolerancePaint.setAlpha(toleranceAlpha);
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(textColor);
        textPaint.setTextSize(dpToPx((int) textSizeDp));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setShadowLayer(3, 0, 0, Color.BLACK);
    }

    private float dpToPx(int dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredSize = (int) dpToPx(maxVisualRadiusDp * 2 + 10);
        int width = resolveSize(desiredSize, widthMeasureSpec);
        int height = resolveSize(desiredSize, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float maxRadiusPx = dpToPx(maxVisualRadiusDp);
        float minRadiusPx = dpToPx(minVisualRadiusDp);
        float viewBoundRadius = Math.min(getWidth(), getHeight()) / 2f;
        if (maxRadiusPx > viewBoundRadius) maxRadiusPx = viewBoundRadius;
        float ratio = (currentValue - minValue) / (float) (maxValue - minValue);
        float currentRadius = minRadiusPx + (maxRadiusPx - minRadiusPx) * ratio;
        canvas.drawCircle(centerX, centerY, currentRadius, tolerancePaint);
        float centerDotPx = dpToPx(centerDotRadiusDp);
        if (centerDotPx < currentRadius) {
            canvas.drawCircle(centerX, centerY, centerDotPx, centerPaint);
        }
        String valueText = String.valueOf(Math.round(currentValue));
        float textY = centerY - ((textPaint.descent() + textPaint.ascent()) / 2);
        canvas.drawText(valueText, centerX, textY, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isDragging = true;
                lastTouchY = y;
                getParent().requestDisallowInterceptTouchEvent(true);
                requestFocus();
                return true;
            case MotionEvent.ACTION_MOVE:
                if (isDragging) {
                    float deltaY = lastTouchY - y;
                    float valueChange = deltaY * sensitivity;
                    float newValue = currentValue + valueChange;
                    currentValue = Math.max(minValue, Math.min(maxValue, newValue));
                    lastTouchY = y;
                    invalidate();
                    if (valueChangeListener != null) {
                        valueChangeListener.onValueChange(currentValue);
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragging = false;
                getParent().requestDisallowInterceptTouchEvent(false);
                clearFocus();
                return true;
        }
        return super.onTouchEvent(event);
    }

    public float getCurrentValue() { return currentValue; }
    public int getMinValue() { return minValue; }
    public int getMaxValue() { return maxValue; }
    public float getSensitivity() { return sensitivity; }
    
    public void setCurrentValue(float value) {
        this.currentValue = Math.max(minValue, Math.min(maxValue, value));
        invalidate();
        if (valueChangeListener != null) valueChangeListener.onValueChange(this.currentValue);
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
        if (currentValue < minValue) setCurrentValue(minValue);
        invalidate();
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
        if (currentValue > maxValue) setCurrentValue(maxValue);
        invalidate();
    }
    
    public void setSensitivity(float sensitivity) {
        this.sensitivity = sensitivity;
    }

    public void setColor(int color) {
        this.primaryColor = color;
        centerPaint.setColor(color);
        tolerancePaint.setColor(color);
        tolerancePaint.setAlpha(toleranceAlpha);
        invalidate();
    }
    
    public void setTextColor(int color) {
        this.textColor = color;
        textPaint.setColor(color);
        invalidate();
    }

    public void setToleranceAlpha(int alpha) {
        this.toleranceAlpha = Math.max(0, Math.min(255, alpha));
        tolerancePaint.setAlpha(this.toleranceAlpha);
        invalidate();
    }
}
