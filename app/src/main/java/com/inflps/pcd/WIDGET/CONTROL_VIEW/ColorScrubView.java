package com.inflps.pcd.WIDGET.CONTROL_VIEW;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class ColorScrubView extends View {

    private final float[] baseHsv = new float[3];
    private final float[] currentHsv = new float[3];
    private int currentColor;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float radius, centerX, centerY;
    private float defaultRadius;
    
    private GestureDetector gestureDetector;
    private OnColorScrubListener listener;
    
    private boolean hasTicked = false;
    private ValueAnimator scaleAnimator;

    public interface OnColorScrubListener {
        void onColorChanged(int color);
        void onColorClicked(int color);
    }

    public void setOnColorScrubListener(OnColorScrubListener listener) {
        this.listener = listener;
    }

    public ColorScrubView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCurrentColor(Color.parseColor("#3498db"));
        init();
    }

    private void init() {
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                animateScale(1.15f);
                hasTicked = false;
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (listener != null) listener.onColorClicked(currentColor);
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float dX, float dY) {
                float delta = dY / getHeight();
                updateColorValue(delta);
                return true;
            }
        });
    }

    public void setCurrentColor(int color) {
        Color.colorToHSV(color, baseHsv);
        System.arraycopy(baseHsv, 0, currentHsv, 0, 3);
        currentColor = color;
        invalidate();
    }

    private void updateColorValue(float delta) {
        float oldV = currentHsv[2];
        float oldS = currentHsv[1];

        if (delta > 0) {
            if (currentHsv[2] < 1.0f) {
                currentHsv[2] = Math.min(1.0f, currentHsv[2] + delta);
            } else {
                currentHsv[1] = Math.max(0.0f, currentHsv[1] - delta);
            }
        } else {
            if (currentHsv[1] < baseHsv[1]) {
                currentHsv[1] = Math.min(baseHsv[1], currentHsv[1] - delta);
            } else {
                currentHsv[2] = Math.max(0.0f, currentHsv[2] + delta);
            }
        }

        boolean crossedValue = (oldV < baseHsv[2] && currentHsv[2] >= baseHsv[2]) || 
                             (oldV > baseHsv[2] && currentHsv[2] <= baseHsv[2]);
        
        if (!hasTicked && crossedValue) {
            performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
            hasTicked = true;
        } else if (Math.abs(currentHsv[2] - baseHsv[2]) > 0.03f) {
            hasTicked = false; 
        }

        currentColor = Color.HSVToColor(currentHsv);
        if (listener != null) listener.onColorChanged(currentColor);
        invalidate();
    }

    private void animateScale(float scaleFactor) {
        if (scaleAnimator != null) scaleAnimator.cancel();
        
        float targetRadius = defaultRadius * scaleFactor;
        scaleAnimator = ValueAnimator.ofFloat(radius, targetRadius);
        scaleAnimator.setDuration(150);
        scaleAnimator.setInterpolator(new DecelerateInterpolator());
        scaleAnimator.addUpdateListener(anim -> {
            radius = (float) anim.getAnimatedValue();
            invalidate();
        });
        scaleAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(currentColor);
        canvas.drawCircle(centerX, centerY, radius, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dpToPx(2));
        paint.setColor(currentHsv[2] > 0.8f ? Color.parseColor("#20000000") : Color.parseColor("#40FFFFFF"));
        canvas.drawCircle(centerX, centerY, radius, paint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2f;
        centerY = h / 2f;
        defaultRadius = Math.min(w, h) * 0.40f;
        radius = defaultRadius;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            animateScale(1.0f);
        }
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    public int getCurrentColor() { return currentColor; }
}