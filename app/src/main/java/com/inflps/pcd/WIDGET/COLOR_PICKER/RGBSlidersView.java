package com.inflps.pcd.WIDGET.COLOR_PICKER;

/* This class is created by InfLps on May 25, 2025*/

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.util.Log;

public class RGBSlidersView extends View  {
	private static final String TAG = "RGBSlidersView";
	private Paint sliderBackgroundPaint;
	private Paint sliderFillPaint;
	private Paint selectorPaint;
	private Paint borderPaint;
	private RectF redSliderRect;
	private RectF greenSliderRect;
	private RectF blueSliderRect;
	private PointF redSelectorPos;
	private PointF greenSelectorPos;
	private PointF blueSelectorPos;
	private int currentR = 0;
	private int currentG = 0;
	private int currentB = 0;
	private float sliderHeightPx;
	private float sliderPaddingPx;
	private float selectorRadiusPx;
	private float borderWidthPx;
	private boolean touchingRed = false;
	private boolean touchingGreen = false;
	private boolean touchingBlue = false;
	private OnRGBChangedListener listener;
	
    public RGBSlidersView(Context context)  {
		super(context);
		init();
	}
	
    public RGBSlidersView(Context context, AttributeSet attrs)  {
		super(context, attrs);
		init();
	}
	
    public RGBSlidersView(Context context, AttributeSet attrs, int defStyleAttr)  {
		super(context, attrs, defStyleAttr);
		init();
	}
	
    private void init()  {
		Context ctx = getContext();
		sliderHeightPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, ctx.getResources().getDisplayMetrics());
		sliderPaddingPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, ctx.getResources().getDisplayMetrics());
		selectorRadiusPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, ctx.getResources().getDisplayMetrics());
		borderWidthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, ctx.getResources().getDisplayMetrics());
		sliderBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		sliderBackgroundPaint.setColor(Color.LTGRAY);
		sliderBackgroundPaint.setStyle(Paint.Style.FILL);
		sliderFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		sliderFillPaint.setStyle(Paint.Style.FILL);
		sliderFillPaint.setDither(true);
		selectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		selectorPaint.setStyle(Paint.Style.STROKE);
		selectorPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, ctx.getResources().getDisplayMetrics()));
		selectorPaint.setColor(Color.WHITE);
		borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		borderPaint.setStyle(Paint.Style.STROKE);
		borderPaint.setStrokeWidth(borderWidthPx);
		borderPaint.setColor(Color.GRAY);
		redSliderRect = new RectF();
		greenSliderRect = new RectF();
		blueSliderRect = new RectF();
		redSelectorPos = new PointF();
		greenSelectorPos = new PointF();
		blueSelectorPos = new PointF();
	}
	
    @Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)  {
		int desiredHeight = (int) (3 * sliderHeightPx + 2 * sliderPaddingPx + 2 * (selectorRadiusPx + borderWidthPx));
		setMeasuredDimension(View.getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
		resolveSize(desiredHeight, heightMeasureSpec));
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)  {
		super.onSizeChanged(w, h, oldw, oldh);
		Log.d(TAG, "onSizeChanged: w=" + w + ", h=" + h);
		float left = selectorRadiusPx + borderWidthPx;
		float right = w - (selectorRadiusPx + borderWidthPx);
		float currentTop = selectorRadiusPx + borderWidthPx;
		redSliderRect.set(left, currentTop, right, currentTop + sliderHeightPx);
		currentTop += sliderHeightPx + sliderPaddingPx;
		greenSliderRect.set(left, currentTop, right, currentTop + sliderHeightPx);
		currentTop += sliderHeightPx + sliderPaddingPx;
		blueSliderRect.set(left, currentTop, right, currentTop + sliderHeightPx);
		updateSliderShaders();
		updateSelectorPositions();
	}

	private void updateSliderShaders()  {
		Shader redShader = new LinearGradient(
		redSliderRect.left, redSliderRect.centerY(),
		redSliderRect.right, redSliderRect.centerY(),
		Color.rgb(0, currentG, currentB), 
		Color.rgb(255, currentG, currentB), 
		Shader.TileMode.CLAMP
		);
		Shader greenShader = new LinearGradient(
		greenSliderRect.left, greenSliderRect.centerY(),
		greenSliderRect.right, greenSliderRect.centerY(),
		Color.rgb(currentR, 0, currentB), 
		Color.rgb(currentR, 255, currentB), 
		Shader.TileMode.CLAMP
		);
		Shader blueShader = new LinearGradient(
		blueSliderRect.left, blueSliderRect.centerY(),
		blueSliderRect.right, blueSliderRect.centerY(),
		Color.rgb(currentR, currentG, 0), 
		Color.rgb(currentR, currentG, 255), 
		Shader.TileMode.CLAMP
		);
	}

	private void updateSelectorPositions()  {
		redSelectorPos.x = redSliderRect.left + (currentR / 255f) * redSliderRect.width();
		redSelectorPos.y = redSliderRect.centerY();
		greenSelectorPos.x = greenSliderRect.left + (currentG / 255f) * greenSliderRect.width();
		greenSelectorPos.y = greenSliderRect.centerY();
		blueSelectorPos.x = blueSliderRect.left + (currentB / 255f) * blueSliderRect.width();
		blueSelectorPos.y = blueSliderRect.centerY();
		Log.d(TAG, "Selector positions updated: R=" + redSelectorPos.x + ", G=" + greenSelectorPos.x + ", B=" + blueSelectorPos.x);
	}
	
    @Override
	protected void onDraw(Canvas canvas)  {
		super.onDraw(canvas);
		canvas.drawRect(redSliderRect, sliderBackgroundPaint);
		Shader redShader = new LinearGradient(
		redSliderRect.left, redSliderRect.centerY(),
		redSliderRect.right, redSliderRect.centerY(),
		Color.rgb(0, currentG, currentB), 
		Color.rgb(255, currentG, currentB), 
		Shader.TileMode.CLAMP
		);
		sliderFillPaint.setShader(redShader);
		canvas.drawRect(redSliderRect, sliderFillPaint);
		canvas.drawRect(redSliderRect, borderPaint);
		drawSelector(canvas, redSelectorPos, Color.rgb(currentR, currentG, currentB));
		canvas.drawRect(greenSliderRect, sliderBackgroundPaint);
		Shader greenShader = new LinearGradient(
		greenSliderRect.left, greenSliderRect.centerY(),
		greenSliderRect.right, greenSliderRect.centerY(),
		Color.rgb(currentR, 0, currentB),
		Color.rgb(currentR, 255, currentB),
		Shader.TileMode.CLAMP
		);
		sliderFillPaint.setShader(greenShader);
		canvas.drawRect(greenSliderRect, sliderFillPaint);
		canvas.drawRect(greenSliderRect, borderPaint);
		drawSelector(canvas, greenSelectorPos, Color.rgb(currentR, currentG, currentB));
		canvas.drawRect(blueSliderRect, sliderBackgroundPaint);
		Shader blueShader = new LinearGradient(
		blueSliderRect.left, blueSliderRect.centerY(),
		blueSliderRect.right, blueSliderRect.centerY(),
		Color.rgb(currentR, currentG, 0),
		Color.rgb(currentR, currentG, 255),
		Shader.TileMode.CLAMP);
		sliderFillPaint.setShader(blueShader);
		canvas.drawRect(blueSliderRect, sliderFillPaint);
		canvas.drawRect(blueSliderRect, borderPaint);
		drawSelector(canvas, blueSelectorPos, Color.rgb(currentR, currentG, currentB));
	}
	
    private void drawSelector(Canvas canvas, PointF position, int color)  {
		Paint actualColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		actualColorPaint.setColor(color);
		canvas.drawCircle(position.x, position.y, selectorRadiusPx, selectorPaint);
		canvas.drawCircle(position.x, position.y, selectorRadiusPx - selectorPaint.getStrokeWidth(), actualColorPaint);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)  {
		float x = event.getX();
		float y = event.getY();
		switch (event.getAction())  {
			case MotionEvent.ACTION_DOWN:
			if (redSliderRect.contains(x, y))  {
				touchingRed = true;
				updateRGBFromTouch(x, redSliderRect, 0);
				return true;
			} else if (greenSliderRect.contains(x, y))  {
				touchingGreen = true;
				updateRGBFromTouch(x, greenSliderRect, 1);
				return true;
			} else if (blueSliderRect.contains(x, y))  {
				touchingBlue = true;
				updateRGBFromTouch(x, blueSliderRect, 2);
				return true;
			}
			break;
			case MotionEvent.ACTION_MOVE:
			if (touchingRed)  {
				updateRGBFromTouch(x, redSliderRect, 0);
			} else if (touchingGreen)  {
				updateRGBFromTouch(x, greenSliderRect, 1);
			} else if (touchingBlue)  {
				updateRGBFromTouch(x, blueSliderRect, 2);
			}
			break;
			case MotionEvent.ACTION_UP:
			touchingRed = false;
			touchingGreen = false;
			touchingBlue = false;
			break;
		}
		return super.onTouchEvent(event);
	}
	
    private void updateRGBFromTouch(float touchX, RectF sliderRect, int channel)  {
		float clampedX = Math.max(sliderRect.left, Math.min(sliderRect.right, touchX));
		float normalizedValue = (clampedX - sliderRect.left) / sliderRect.width();
		int value = (int) (normalizedValue * 255);
		value = Math.max(0, Math.min(255, value));
		boolean changed = false;
	
    	if (channel == 0 && currentR != value)  {
			currentR = value;
			changed = true;
		} else if (channel == 1 && currentG != value)  {
			currentG = value;
			changed = true;
		} else if (channel == 2 && currentB != value)  {
			currentB = value;
			changed = true;
		}
        
		if (changed)  {
			updateSelectorPositions();
			invalidate();
			if (listener != null)  {
				listener.onRGBChanged(currentR, currentG, currentB);
			}
		}
	}

	public void setColor(int color)  {
		currentR = Color.red(color);
		currentG = Color.green(color);
		currentB = Color.blue(color);
		updateSelectorPositions();
		invalidate();
	}

	public int getColor()  {
		return Color.rgb(currentR, currentG, currentB);
	}

	public int getRed()  {
		return currentR;
	}

	public int getGreen()  {
		return currentG;
	}

	public int getBlue()  {
		return currentB;
	}

	public void setOnRGBChangedListener(OnRGBChangedListener listener)  {
		this.listener = listener;
	}

	public interface OnRGBChangedListener  {
		void onRGBChanged(int r, int g, int b);
	}
}
