package com.inflps.pcd.WIDGET.COLOR_PICKER;

/* This class is created by InfLps on May 22, 2025 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class HueSVSquareColorPicker extends View {
	private float wheelPaddingPx;
	private float wheelThicknessPx;
	private float selectorRadiusPx;
	private Paint hueWheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint svSquarePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint selectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint innerWheelFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private float[] hsv = {0f, 1f, 1f};
	private OnColorChangedListener listener;
	private Path svSquarePath = new Path();
	private Region svSquareRegion = new Region();
	private PointF hueSelectorPos = new PointF();
	private PointF svSelectorPos = new PointF();
	private int outerWheelRadius;
	private int innerWheelRadius;
	private RectF hueWheelRect;
	private float svSquareSize;
	private RectF svSquareRect;
	private boolean touchingHue = false;
	private boolean touchingSV = false;
	
	public HueSVSquareColorPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
		Context ctx = getContext();
		wheelPaddingPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, ctx.getResources().getDisplayMetrics());
		wheelThicknessPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, ctx.getResources().getDisplayMetrics());
		selectorRadiusPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, ctx.getResources().getDisplayMetrics());
		hueWheelPaint.setStyle(Paint.Style.STROKE);
		hueWheelPaint.setStrokeWidth(wheelThicknessPx);
		hueWheelPaint.setDither(true);
		svSquarePaint.setStyle(Paint.Style.FILL);
		svSquarePaint.setDither(true);
		selectorPaint.setStyle(Paint.Style.STROKE);
		selectorPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, ctx.getResources().getDisplayMetrics()));
		selectorPaint.setColor(Color.WHITE);
		borderPaint.setStyle(Paint.Style.STROKE);
		borderPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, ctx.getResources().getDisplayMetrics()));
		borderPaint.setColor(Color.GRAY);
		innerWheelFillPaint.setStyle(Paint.Style.FILL);
		innerWheelFillPaint.setColor(Color.TRANSPARENT);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		int centerX = getWidth() / 2;
		int centerY = getHeight() / 2;
		outerWheelRadius = (int) (Math.min(centerX, centerY) - wheelPaddingPx);
		innerWheelRadius = (int) (outerWheelRadius - wheelThicknessPx);
		hueWheelRect = new RectF(
		centerX - outerWheelRadius + wheelThicknessPx / 2f,
		centerY - outerWheelRadius + wheelThicknessPx / 2f,
		centerX + outerWheelRadius - wheelThicknessPx / 2f,
		centerY + outerWheelRadius - wheelThicknessPx / 2f);
		svSquareSize = (float) (2 * innerWheelRadius / Math.sqrt(2) * 0.9);
		svSquareRect = new RectF(
		centerX - svSquareSize / 2f,
		centerY - svSquareSize / 2f,
		centerX + svSquareSize / 2f,
		centerY + svSquareSize / 2f);
		updateHueWheelShader(centerX, centerY);
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		int centerX = getWidth() / 2;
		int centerY = getHeight() / 2;
		canvas.drawOval(hueWheelRect, hueWheelPaint);
		canvas.drawCircle(centerX, centerY, innerWheelRadius, innerWheelFillPaint);
		updateHueSelectorPosition(centerX, centerY);
		drawHueSelector(canvas);
		drawSVSquare(canvas);
	}
	
	private void updateHueWheelShader(int centerX, int centerY) {
		int[] colors = new int[361];
		float[] hsvTemp = {0f, 1f, 1f};
		for (int i = 0; i <= 360; i++) {
			hsvTemp[0] = i;
			colors[i] = Color.HSVToColor(hsvTemp);
		}
		SweepGradient sweepGradient = new SweepGradient(centerX, centerY, colors, null);
		Matrix rotateMatrix = new Matrix();
		rotateMatrix.setRotate(-90, centerX, centerY);
		sweepGradient.setLocalMatrix(rotateMatrix);
		hueWheelPaint.setShader(sweepGradient);
	}
	
	private void updateHueSelectorPosition(int centerX, int centerY) {
		float radius = outerWheelRadius - wheelThicknessPx / 2f;
		float angle = (float) Math.toRadians(hsv[0] - 90);
		hueSelectorPos.x = centerX + radius * (float) Math.cos(angle);
		hueSelectorPos.y = centerY + radius * (float) Math.sin(angle);
	}
	
	private void drawHueSelector(Canvas canvas) {
		Paint colorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		colorPaint.setColor(Color.HSVToColor(new float[]{hsv[0], 1f, 1f}));
		canvas.drawCircle(hueSelectorPos.x, hueSelectorPos.y, selectorRadiusPx, selectorPaint);
		canvas.drawCircle(hueSelectorPos.x, hueSelectorPos.y, selectorRadiusPx - selectorPaint.getStrokeWidth(), colorPaint);
	}
	
	private void drawSVSquare(Canvas canvas) {
		svSquarePath.reset();
		svSquarePath.addRect(svSquareRect, Path.Direction.CW);
		svSquareRegion.setPath(svSquarePath, new Region(
		(int) svSquareRect.left, (int) svSquareRect.top,
		(int) svSquareRect.right, (int) svSquareRect.bottom));
		int baseColor = Color.HSVToColor(new float[]{hsv[0], 1f, 1f});
		Shader saturationShader = new LinearGradient(
		svSquareRect.left, svSquareRect.centerY(),
		svSquareRect.right, svSquareRect.centerY(),
		Color.WHITE, baseColor, Shader.TileMode.CLAMP);
		Shader valueShader = new LinearGradient(
		svSquareRect.centerX(), svSquareRect.top,
		svSquareRect.centerX(), svSquareRect.bottom,
		Color.WHITE, Color.BLACK, Shader.TileMode.CLAMP);
		ComposeShader combinedShader = new ComposeShader(saturationShader, valueShader, PorterDuff.Mode.MULTIPLY);
		svSquarePaint.setShader(combinedShader);
		canvas.drawPath(svSquarePath, svSquarePaint);
		canvas.drawPath(svSquarePath, borderPaint);
		updateSVSelectorPosition();
		drawSVSelector(canvas);
	}
	
	private void updateSVSelectorPosition() {
		float s = hsv[1];
		float v = hsv[2];
		float x = svSquareRect.left + (s * svSquareRect.width());
		float y = svSquareRect.top + ((1 - v) * svSquareRect.height());
		svSelectorPos.set(x, y);
	}
	
	private void drawSVSelector(Canvas canvas) {
		Paint colorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		colorPaint.setColor(Color.HSVToColor(hsv));
		canvas.drawCircle(svSelectorPos.x, svSelectorPos.y, selectorRadiusPx, selectorPaint);
		canvas.drawCircle(svSelectorPos.x, svSelectorPos.y, selectorRadiusPx - selectorPaint.getStrokeWidth(), colorPaint);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		int centerX = getWidth() / 2;
		int centerY = getHeight() / 2;
		float dx = x - centerX;
		float dy = y - centerY;
		float distance = (float) Math.sqrt(dx * dx + dy * dy);
		
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
			float minHueWheelRadius = outerWheelRadius - wheelThicknessPx - selectorRadiusPx;
			float maxHueWheelRadius = outerWheelRadius + selectorRadiusPx;
			if (distance >= minHueWheelRadius && distance <= maxHueWheelRadius) {
				touchingHue = true;
				updateHueFromPosition(x, y, centerX, centerY);
				return true;
			}
			if (svSquareRegion.contains((int) x, (int) y)) {
				touchingSV = true;
				updateSVFromTouch(x, y);
				return true;
			}
			break;
			case MotionEvent.ACTION_MOVE:
			if (touchingHue) {
				updateHueFromPosition(x, y, centerX, centerY);
			} else if (touchingSV) {
				updateSVFromTouch(x, y);
			}
			break;
			case MotionEvent.ACTION_UP:
			touchingHue = false;
			touchingSV = false;
			break;
		}
		return true;
	}
	
	private void updateHueFromPosition(float x, float y, int centerX, int centerY) {
		float dx = x - centerX;
		float dy = y - centerY;
		float angleRad = (float) Math.atan2(dy, dx);
		float angleDeg = (float) Math.toDegrees(angleRad);
		hsv[0] = (angleDeg + 360 + 90) % 360;
		notifyListener();
		invalidate();
	}
	
	private void updateSVFromTouch(float touchX, float touchY) {
		float clampedX = Math.max(svSquareRect.left, Math.min(svSquareRect.right, touchX));
		float clampedY = Math.max(svSquareRect.top, Math.min(svSquareRect.bottom, touchY));
		float s = (clampedX - svSquareRect.left) / svSquareRect.width();
		float v = 1f - ((clampedY - svSquareRect.top) / svSquareRect.height());
		hsv[1] = Math.max(0f, Math.min(1f, s));
		hsv[2] = Math.max(0f, Math.min(1f, v));
		notifyListener();
		invalidate();
	}
	
private void notifyListener() {
		if (listener != null) {
			listener.onColorChanged(getColor());
		}
	}

	public int getColor() {
		return Color.HSVToColor(hsv);
	}

	public void setColor(int color) {
		Color.colorToHSV(color, hsv);
		invalidate();
	}

	public void setColorFromHex(CharSequence hex) {
		try {
			String hexStr = hex.toString().replace("#", "");
			int color;
			if (hexStr.length() == 3) {
				hexStr = "" + hexStr.charAt(0) + hexStr.charAt(0)
				+ hexStr.charAt(1) + hexStr.charAt(1)
				+ hexStr.charAt(2) + hexStr.charAt(2);
			}

			if (hexStr.length() == 6) {
				color = Color.parseColor("#" + hexStr);
			} else if (hexStr.length() == 8) {
				color = Color.parseColor("#" + hexStr);
			} else {
				throw new IllegalArgumentException("Invalid hex color: " + hexStr);
			}

			setColor(color);
			notifyListener();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getColorAsHex() {
		return String.format("#%06X", (0xFFFFFF & getColor()));
	}

	public void setColorFromRGB(int r, int g, int b) {
		setColor(Color.rgb(r, g, b));
		notifyListener();
	}

	public int[] getColorAsRGB() {
		int color = getColor();
		return new int[]{
			Color.red(color),
			Color.green(color),
			Color.blue(color)
		};
	}

	public int getRed() {
		return Color.red(getColor());
	}

	public int getGreen() {
		return Color.green(getColor());
	}

	public int getBlue() {
		return Color.blue(getColor());
	}

	public void setOnColorChangedListener(OnColorChangedListener listener) {
		this.listener = listener;
	}

	public interface OnColorChangedListener {
		void onColorChanged(int color);
	}
}
