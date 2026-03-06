package com.inflps.pcd.WIDGET.CONTROL_VIEW;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class ColorSwapView extends View {

    private int primaryColor = Color.BLACK;
    private int secondaryColor = Color.WHITE;
    private int strokeColor = Color.WHITE;

    private final RectF topLeftRect = new RectF();
    private final RectF bottomRightRect = new RectF();
    private final Path path1 = new Path();
    private final Path path2 = new Path();

    private Paint colorPaint;
    private Paint strokePaint;
    private GestureDetector gestureDetector;
    private OnColorClickListener listener;

    private float animProgress = 0f;
    private ValueAnimator swapAnimator;
    private final ArgbEvaluator colorEvaluator = new ArgbEvaluator();

    private float cornerRadius;

    public interface OnColorClickListener {
        void onColorClick(boolean isBottomSquare);
        void onColorSwapped(int newPrimary, int newSecondary);
    }

    public void setOnColorClickListener(OnColorClickListener listener) {
        this.listener = listener;
    }

    public ColorSwapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        colorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        colorPaint.setStyle(Paint.Style.FILL);

        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(dpToPx(1.5f));
        strokePaint.setColor(strokeColor);
        strokePaint.setShadowLayer(dpToPx(0.5f), 0, 0, Color.parseColor("#40000000"));

        setLayerType(LAYER_TYPE_SOFTWARE, null);

        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) { return true; }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                handleTouch(e.getX(), e.getY());
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                swapColors();
                return true;
            }
        });
    }

    private void handleTouch(float x, float y) {
        if (listener == null) return;
        float distTop = (float) Math.hypot(x - topLeftRect.centerX(), y - topLeftRect.centerY());
        float distBottom = (float) Math.hypot(x - bottomRightRect.centerX(), y - bottomRightRect.centerY());
        
        if (animProgress < 0.5f) {
            listener.onColorClick(distBottom < distTop); 
        } else {
            listener.onColorClick(distBottom < distTop);
        }
    }

    public void swapColors() {
        if (swapAnimator != null && swapAnimator.isRunning()) return;

        swapAnimator = ValueAnimator.ofFloat(0f, 1f);
        swapAnimator.setDuration(300);
        swapAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        swapAnimator.addUpdateListener(anim -> {
            animProgress = (float) anim.getAnimatedValue();
            invalidate();
        });
        swapAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                int temp = primaryColor;
                primaryColor = secondaryColor;
                secondaryColor = temp;
                animProgress = 0f;
                invalidate();
                if (listener != null) listener.onColorSwapped(primaryColor, secondaryColor);
            }
        });
        swapAnimator.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float minDim = Math.min(w, h);
        float padding = minDim * 0.10f; 
        float size = minDim * 0.55f; 
        cornerRadius = size * 0.20f; 
        topLeftRect.set(padding, padding, padding + size, padding + size);
        bottomRightRect.set(w - padding - size, h - padding - size, w - padding, h - padding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF r1 = interpolateRect(topLeftRect, bottomRightRect, animProgress);
        RectF r2 = interpolateRect(bottomRightRect, topLeftRect, animProgress);

        float cr = cornerRadius;

        path1.reset();
        path1.addRoundRect(r1, new float[]{cr, cr, cr, cr, 0, 0, cr, cr}, Path.Direction.CW);
        path2.reset();
        path2.addRoundRect(r2, new float[]{0, 0, cr, cr, cr, cr, cr, cr}, Path.Direction.CW);
        colorPaint.setColor((int) colorEvaluator.evaluate(animProgress, secondaryColor, primaryColor));
        canvas.drawPath(path1, colorPaint);
        canvas.drawPath(path1, strokePaint);
        colorPaint.setColor((int) colorEvaluator.evaluate(animProgress, primaryColor, secondaryColor));
        canvas.drawPath(path2, colorPaint);
        canvas.drawPath(path2, strokePaint);
    }

    private RectF interpolateRect(RectF start, RectF end, float fraction) {
        return new RectF(
                start.left + (end.left - start.left) * fraction,
                start.top + (end.top - start.top) * fraction,
                start.right + (end.right - start.right) * fraction,
                start.bottom + (end.bottom - start.bottom) * fraction
        );
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    private float dpToPx(float dp) { 
        return dp * getResources().getDisplayMetrics().density; 
    }

    public int getPrimaryColor() { return primaryColor; }
    public int getSecondaryColor() { return secondaryColor; }
    
    public void setPrimaryColor(int color) { this.primaryColor = color; invalidate(); }
    public void setSecondaryColor(int color) { this.secondaryColor = color; invalidate(); }
}
