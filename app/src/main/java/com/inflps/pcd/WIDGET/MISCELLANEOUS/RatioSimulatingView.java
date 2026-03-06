package com.inflps.pcd.WIDGET.MISCELLANEOUS;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class RatioSimulatingView extends View {

    private int desiredWidth = 1;
    private int desiredHeight = 1;
    private float density;

    private Paint fillPaint;
    private Paint strokePaint;

    public RatioSimulatingView(Context context) {
        super(context);
        init(context);
    }

    public RatioSimulatingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RatioSimulatingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        density = context.getResources().
                  getDisplayMetrics().
                  density;
        fillPaint = new Paint();
        fillPaint.setColor(Color.WHITE);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setAntiAlias(true);
        strokePaint = new Paint();
        strokePaint.setColor(Color.GRAY);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(2 * density);
        strokePaint.setAntiAlias(true);
    }

    public void setDimensions(int width, int height) {
        if (width <= 0) {
            width = 1;
        }
        if (height <= 0) {
            height = 1;
        }
        this.desiredWidth = width;
        this.desiredHeight = height;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int suggestedWidth = View.getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int suggestedHeight = View.getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        int maxAllowedSizePx = Math.min(suggestedWidth, suggestedHeight);
        if (maxAllowedSizePx <= 0) {
            maxAllowedSizePx = (int) (50 * density);
        }
        float ratio = (float) desiredWidth / desiredHeight;
        int measuredWidth;
        int measuredHeight;
        if (desiredWidth >= desiredHeight) {
            measuredWidth = maxAllowedSizePx;
            measuredHeight = (int) (maxAllowedSizePx / ratio);
            if (measuredHeight > maxAllowedSizePx) {
                measuredHeight = maxAllowedSizePx;
                measuredWidth = (int) (maxAllowedSizePx * ratio);
            }
        } else {
            measuredHeight = maxAllowedSizePx;
            measuredWidth = (int) (maxAllowedSizePx * ratio);
            if (measuredWidth > maxAllowedSizePx) {
                measuredWidth = maxAllowedSizePx;
                measuredHeight = (int) (maxAllowedSizePx / ratio);
            }
        }
        if (measuredWidth <= 0) measuredWidth = 1;
        if (measuredHeight <= 0) measuredHeight = 1;
        setMeasuredDimension(
            MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float viewWidth = getMeasuredWidth();
        float viewHeight = getMeasuredHeight();
        canvas.drawRect(0, 0, viewWidth, viewHeight, fillPaint);
        canvas.drawRect(0, 0, viewWidth, viewHeight, strokePaint);
    }
}