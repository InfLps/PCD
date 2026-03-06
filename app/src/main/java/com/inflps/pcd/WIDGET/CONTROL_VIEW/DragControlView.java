package com.inflps.pcd.WIDGET.CONTROL_VIEW;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DragControlView extends View {
    private float sensitivity = 0.5f;
    private int minValue = 1;
    private int maxValue = 100;
    private float currentValue = 10;
    private int defaultViewSizeDp = 80;
    private int minVisualRadiusDp = 10;
    private float lastTouchY;
    private boolean isDragging = false;
    private Paint circlePaint;
    private Paint textPaint;

    public interface OnValueChangeListener {
        void onValueChange(float newValue);
    }

    private OnValueChangeListener valueChangeListener;

    public void setOnValueChangeListener(OnValueChangeListener listener) {
        this.valueChangeListener = listener;
    }

    public DragControlView(Context context) {
        super(context);
        init();
    }

    public DragControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DragControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.BLUE);
        circlePaint.setStyle(Paint.Style.FILL);
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(dpToPx(16));
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    private float dpToPx(int dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    public float getCurrentValue() {
        return currentValue;
    }

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public float getSensitivity() {
        return sensitivity;
    }

    public int getCircleColor() {
        return circlePaint.getColor();
    }

    public int getTextColor() {
        return textPaint.getColor();
    }

    public float getTextSize() {
        return textPaint.getTextSize();
    }

    public void setCurrentValue(float value) {
        this.currentValue = Math.max(minValue, Math.min(maxValue, value));
        invalidate();
        if (valueChangeListener != null) {
            valueChangeListener.onValueChange(this.currentValue);
        }
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
        if (currentValue < minValue) {
            currentValue = minValue;
            if (valueChangeListener != null) {
                valueChangeListener.onValueChange(this.currentValue);
            }
        }
        invalidate();
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
        if (currentValue > maxValue) {
            currentValue = maxValue;
            if (valueChangeListener != null) {
                valueChangeListener.onValueChange(this.currentValue);
            }
        }
        invalidate();
    }

    public void setSensitivity(float sensitivity) {
        this.sensitivity = sensitivity;
    }

    public void setCircleColor(int color) {
        circlePaint.setColor(color);
        invalidate();
    }

    public void setTextColor(int color) {
        textPaint.setColor(color);
        invalidate();
    }

    public void setTextSize(float textSizeDp) {
        textPaint.setTextSize(dpToPx((int)textSizeDp));
        invalidate();
    }

    public void setDefaultViewSizeDp(int defaultViewSizeDp) {
        this.defaultViewSizeDp = defaultViewSizeDp;
        requestLayout();
    }

    public void setMinVisualRadiusDp(int minVisualRadiusDp) {
        this.minVisualRadiusDp = minVisualRadiusDp;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = (int) dpToPx(defaultViewSizeDp);
        int desiredHeight = (int) dpToPx(defaultViewSizeDp);
        int width = resolveSize(desiredWidth, widthMeasureSpec);
        int height = resolveSize(desiredHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        float maxRadius = Math.min(getWidth(), getHeight()) / 2f;
        float minRadiusPx = dpToPx(minVisualRadiusDp);
        if (maxRadius < minRadiusPx) {
            maxRadius = minRadiusPx;
        }
        float radius = minRadiusPx + (maxRadius - minRadiusPx) * ((currentValue - minValue) / (maxValue - minValue));
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        canvas.drawCircle(centerX, centerY, radius, circlePaint);
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
}