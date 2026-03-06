package com.inflps.pcd.WIDGET.CONTROL_VIEW;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DragControlSmudgeView extends View {
	private float sensitivity = 0.5f;
	private int minValue = 0;
	private int maxValue = 100;
	private float currentValue = 50;
	private int viewSizeDp = 60;
	private int strokeWidthDp = 6;
	private int trackColor = Color.parseColor("#40000000");
	private int progressColor = Color.BLUE;
	private int textColor = Color.WHITE;
	private float textSizeDp = 16;
	private float lastTouchY;
	private boolean isDragging = false;
	private Paint trackPaint;
	private Paint progressPaint;
	private Paint textPaint;
	private RectF arcBounds;
	
	public interface OnValueChangeListener {
		void onValueChange(float newValue);
	}
	
	private OnValueChangeListener valueChangeListener;
	
	public void setOnValueChangeListener(OnValueChangeListener listener) {
		this.valueChangeListener = listener;
	}
	
	public DragControlSmudgeView(Context context) {
		super(context);
		init();
	}
	
	public DragControlSmudgeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public DragControlSmudgeView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
	
	private void init() {
		trackPaint = new Paint();
		trackPaint.setAntiAlias(true);
		trackPaint.setColor(trackColor);
		trackPaint.setStyle(Paint.Style.STROKE);
		trackPaint.setStrokeCap(Paint.Cap.ROUND);
		
		progressPaint = new Paint();
		progressPaint.setAntiAlias(true);
		progressPaint.setColor(progressColor);
		progressPaint.setStyle(Paint.Style.STROKE);
		progressPaint.setStrokeCap(Paint.Cap.ROUND);
		
		textPaint = new Paint();
		textPaint.setAntiAlias(true);
		textPaint.setColor(textColor);
		textPaint.setTextAlign(Paint.Align.CENTER);
		textPaint.setShadowLayer(3, 0, 0, Color.BLACK);
		
		arcBounds = new RectF();
		updatePaintSizes();
	}
	
	private void updatePaintSizes() {
		float strokePx = dpToPx(strokeWidthDp);
		trackPaint.setStrokeWidth(strokePx);
		progressPaint.setStrokeWidth(strokePx);
		textPaint.setTextSize(dpToPx((int)textSizeDp));
	}
	
	private float dpToPx(int dp) {
		return dp * getResources().getDisplayMetrics().density;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int desiredSize = (int) dpToPx(viewSizeDp);
		int width = resolveSize(desiredSize, widthMeasureSpec);
		int height = resolveSize(desiredSize, heightMeasureSpec);
		setMeasuredDimension(width, height);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		float strokePx = trackPaint.getStrokeWidth();
		float padding = strokePx / 2f + dpToPx(2);
		arcBounds.set(padding, padding, w - padding, h - padding);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		float centerX = getWidth() / 2f;
		float centerY = getHeight() / 2f;
		canvas.drawOval(arcBounds, trackPaint);
		float percentage = (currentValue - minValue) / (float) (maxValue - minValue);
		float sweepAngle = 360f * percentage;
		canvas.drawArc(arcBounds, -90, sweepAngle, false, progressPaint);
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
	
	public void setCurrentValue(float value) {
		this.currentValue = Math.max(minValue, Math.min(maxValue, value));
		invalidate();
		if (valueChangeListener != null) valueChangeListener.onValueChange(this.currentValue);
	}
	
	public void setProgressColor(int color) {
		this.progressColor = color;
		progressPaint.setColor(color);
		invalidate();
	}
	
	public void setTrackColor(int color) {
		this.trackColor = color;
		trackPaint.setColor(color);
		invalidate();
	}
	
	public void setStrokeWidthDp(int dp) {
		this.strokeWidthDp = dp;
		updatePaintSizes();
		requestLayout();
		invalidate();
	}
	
	public void setViewSizeDp(int sizeDp) {
		this.viewSizeDp = sizeDp;
		requestLayout();
	}
	
	public void setSensitivity(float sensitivity) {
		this.sensitivity = sensitivity;
	}
}
