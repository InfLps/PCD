package com.inflps.pcd.WIDGET.CONTROL_VIEW;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DragControlOpacityView extends View {
	private float sensitivity = 0.5f;
	private int minValue = 0;
	private int maxValue = 255;
	private float currentValue = 128;
	private int minOpacityAlpha = 50;
	private int maxOpacityAlpha = 255;
	private int checkerSquareSizeDp = 20;
	private int checkerColor1 = Color.LTGRAY;
	private int checkerColor2 = Color.DKGRAY;
	private int indicatorColor = Color.BLUE;
	private int indicatorRadiusDp = 30;
	private int textColor = Color.WHITE;
	private float textSizeDp = 16;
	private float lastTouchY;
	private boolean isDragging = false;
	private Paint indicatorPaint;
	private Paint textPaint;
	private Paint checkerPaint1;
	private Paint checkerPaint2;
	private Path clipPath;
	
	public interface OnValueChangeListener {
		void onValueChange(float newValue);
	}
	
	private OnValueChangeListener valueChangeListener;
	
	public void setOnValueChangeListener(OnValueChangeListener listener) {
		this.valueChangeListener = listener;
	}
	
	public DragControlOpacityView(Context context) {
		super(context);
		init();
	}
	
	public DragControlOpacityView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public DragControlOpacityView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
	
	private void init() {
		indicatorPaint = new Paint();
		indicatorPaint.setAntiAlias(true);
		indicatorPaint.setColor(indicatorColor);
		indicatorPaint.setStyle(Paint.Style.FILL);
		textPaint = new Paint();
		textPaint.setAntiAlias(true);
		textPaint.setColor(textColor);
		textPaint.setTextSize(dpToPx((int)textSizeDp));
		textPaint.setTextAlign(Paint.Align.CENTER);
		checkerPaint1 = new Paint();
		checkerPaint1.setColor(checkerColor1);
		checkerPaint2 = new Paint();
		checkerPaint2.setColor(checkerColor2);
		clipPath = new Path();
		setCurrentValue(currentValue);
	}
	
	private float dpToPx(int dp) {
		return dp * getResources().getDisplayMetrics().density;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int desiredDiameterPx = (int) dpToPx(indicatorRadiusDp * 2);
		int paddingPx = (int) dpToPx(10);
		int desiredTotalSizePx = desiredDiameterPx + paddingPx;
		int width = resolveSize(desiredTotalSizePx, widthMeasureSpec);
		int height = resolveSize(desiredTotalSizePx, heightMeasureSpec);
		setMeasuredDimension(width, height);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		float centerX = getWidth() / 2f;
		float centerY = getHeight() / 2f;
		float desiredIndicatorRadiusPx = dpToPx(indicatorRadiusDp); 
		float maxAvailableRadiusPx = Math.min(getWidth(), getHeight()) / 2f;
		float actualIndicatorRadiusPx = Math.min(desiredIndicatorRadiusPx, maxAvailableRadiusPx);
		canvas.save();
		clipPath.reset();
		clipPath.addCircle(centerX, centerY, actualIndicatorRadiusPx, Path.Direction.CW);
		canvas.clipPath(clipPath);
		int squareSizePx = (int) dpToPx(checkerSquareSizeDp);
		int startX = (int) (centerX - actualIndicatorRadiusPx - squareSizePx);
		int startY = (int) (centerY - actualIndicatorRadiusPx - squareSizePx);
		int endX = (int) (centerX + actualIndicatorRadiusPx + squareSizePx);
		int endY = (int) (centerY + actualIndicatorRadiusPx + squareSizePx);
		for (int x = startX; x < endX; x += squareSizePx) {
			for (int y = startY; y < endY; y += squareSizePx) {
				Paint currentCheckerPaint = (((x / squareSizePx) + (y / squareSizePx)) % 2 == 0) ? checkerPaint1 : checkerPaint2;
				canvas.drawRect(x, y, x + squareSizePx, y + squareSizePx, currentCheckerPaint);
			}
		}
		canvas.restore();
		int alpha;
		if (maxValue == minValue) {
			alpha = maxOpacityAlpha;
		} else {
			alpha = (int) (minOpacityAlpha + (maxOpacityAlpha - minOpacityAlpha) *
			((currentValue - minValue) / (maxValue - minValue)));
		}
		alpha = Math.max(0, Math.min(255, alpha));
		indicatorPaint.setAlpha(alpha);
		canvas.drawCircle(centerX, centerY, actualIndicatorRadiusPx, indicatorPaint);
		String valueText = String.valueOf(Math.round(currentValue));
		float textY = centerY - ((textPaint.descent() + textPaint.ascent()) / 2);
		canvas.drawText(valueText, centerX, textY, textPaint);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event)  {
		float y = event.getY();
		switch (event.getAction())  {
			case MotionEvent.ACTION_DOWN:
			isDragging = true;
			lastTouchY = y;
			getParent().requestDisallowInterceptTouchEvent(true);
			requestFocus();
			return true;
			case MotionEvent.ACTION_MOVE:
			if (isDragging)  {
				float deltaY = lastTouchY - y;
				float valueChange = deltaY * sensitivity;
				float newValue = currentValue + valueChange;
				currentValue = Math.max(minValue, Math.min(maxValue, newValue));
				lastTouchY = y;
				invalidate();
				if (valueChangeListener != null)  {
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
    
	public float getCurrentValue()  {
		return currentValue;
	}
    
	public int getMinValue()  {
		return minValue;
	}
    
	public int getMaxValue()  {
		return maxValue;
	}
    
	public float getSensitivity()  {
		return sensitivity;
	}
    
	public int getMinOpacityAlpha()  {
		return minOpacityAlpha;
	}
    
	public int getMaxOpacityAlpha()  {
		return maxOpacityAlpha;
	}
    
	public int getCheckerSquareSizeDp()  {
		return checkerSquareSizeDp;
	}
    
	public int getCheckerColor1()  {
		return checkerColor1;
	}
    
	public int getCheckerColor2()  {
		return checkerColor2;
	}
    
	public int getIndicatorColor()  {
		return indicatorColor;
	}
    
	public int getIndicatorRadiusDp()  {
		return indicatorRadiusDp;
	}
    
	public int getTextColor()  {
		return textColor;
	}
    
	public float getTextSizeDp()  {
		return textSizeDp;
	}
    
	public void setCurrentValue(float value)  {
		this.currentValue = Math.max(minValue, Math.min(maxValue, value));
		invalidate();
		if (valueChangeListener != null)  {
			valueChangeListener.onValueChange(this.currentValue);
		}
	}
    
	public void setMinValue(int minValue)  {
		this.minValue = minValue;
		if (currentValue < minValue)  {
			currentValue = minValue;
			if (valueChangeListener != null) valueChangeListener.onValueChange(this.currentValue);
		}
		invalidate();
	}
    
	public void setMaxValue(int maxValue)  {
		this.maxValue = maxValue;
		if (currentValue > maxValue)  {
			currentValue = maxValue;
			if (valueChangeListener != null) valueChangeListener.onValueChange(this.currentValue);
		}
		invalidate();
	}
    
	public void setSensitivity(float sensitivity)  {
		this.sensitivity = sensitivity;
	}
    
	public void setMinOpacityAlpha(int minOpacityAlpha)  {
		this.minOpacityAlpha = Math.max(0, Math.min(255, minOpacityAlpha));
		invalidate();
	}
    
	public void setMaxOpacityAlpha(int maxOpacityAlpha)  {
		this.maxOpacityAlpha = Math.max(0, Math.min(255, maxOpacityAlpha));
		invalidate();
	}
    
	public void setCheckerSquareSizeDp(int checkerSquareSizeDp)  {
		this.checkerSquareSizeDp = checkerSquareSizeDp;
		invalidate();
	}
    
	public void setCheckerColor1(int checkerColor1)  {
		this.checkerColor1 = checkerColor1;
		checkerPaint1.setColor(checkerColor1);
		invalidate();
	}
    
	public void setCheckerColor2(int checkerColor2)  {
		this.checkerColor2 = checkerColor2;
		checkerPaint2.setColor(checkerColor2);
		invalidate();
	}
    
	public void setIndicatorColor(int indicatorColor)  {
		this.indicatorColor = indicatorColor;
		indicatorPaint.setColor(indicatorColor);
		invalidate();
	}
    
	public void setIndicatorRadiusDp(int indicatorRadiusDp)  {
		this.indicatorRadiusDp = indicatorRadiusDp;
		requestLayout();
		invalidate();
	}
    
	public void setTextColor(int textColor)  {
		this.textColor = textColor;
		textPaint.setColor(textColor);
		invalidate();
	}
    
	public void setTextSizeDp(float textSizeDp)  {
		this.textSizeDp = textSizeDp;
		textPaint.setTextSize(dpToPx((int)textSizeDp));
		invalidate();
	}
}