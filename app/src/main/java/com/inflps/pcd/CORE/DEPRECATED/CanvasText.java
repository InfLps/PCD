package com.inflps.pcd.CORE.DEPRECATED;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetrics;

public class CanvasText  {
	private String text;
	private float x, y;
	private int color;
	private float textSize;
	private Typeface typeface;
	private int layerId;
	private int textMinPixelSize;
	private RectF bounds = new RectF();
	private Paint highlightPaint;
	private static final float HANDLE_SIZE_DP = 12;
	private boolean isBold = false;
	private boolean isItalic = false;
    
	public CanvasText(String text, float x, float y, int color, float initialTextSize, Typeface typeface, int layerId, int minPixelSize)  {
		this.text = text;
		this.x = x;
		this.y = y;
		this.color = color;
		this.textSize = Math.max(minPixelSize * 2f, initialTextSize);
		this.typeface = typeface;
		this.layerId = layerId;
		this.textMinPixelSize = minPixelSize;
		highlightPaint = new Paint();
		highlightPaint.setColor(Color.parseColor("#804CAF50"));
		highlightPaint.setStyle(Paint.Style.STROKE);
		highlightPaint.setStrokeWidth(2f);
	}
    
	public String getText()  {
		return text;
	}
    
	public void setText(String text)  {
		this.text = text;
	}
    
	public float getX()  {
		return x;
	}
    
	public void setX(float x)  {
		this.x = x;
	}
    
	public float getY()  {
		return y;
	}
    
	public void setY(float y)  {
		this.y = y;
	}
    
	public int getColor()  {
		return color;
	}
    
	public void setColor(int color)  {
		this.color = color;
	}
    
	public float getTextSize()  {
		return textSize;
	}
    
	public void setTextSize(float textSize)  {
		this.textSize = Math.max(textMinPixelSize * 2f, textSize);
	}
    
	public Typeface getTypeface()  {
		int style = Typeface.NORMAL;
		if (isBold) style |= Typeface.BOLD;
		if (isItalic) style |= Typeface.ITALIC;
		if (typeface != null)  {
			return Typeface.create(typeface, style);
		}
		return Typeface.defaultFromStyle(style);
	}
    
	public void setTypeface(Typeface typeface)  {
		this.typeface = typeface;
	}
    
	public void setBold(boolean bold)  {
		isBold = bold;
	}
    
	public void setItalic(boolean italic)  {
		isItalic = italic;
	}
    
	public int getLayerId()  {
		return layerId;
	}
    
	public void draw(Canvas canvas, Paint paint, boolean snappingEnabled, int pixelSize, float scale)  {
		paint.setColor(color);
		paint.setTextSize(textSize);
		paint.setTypeface(getTypeface());
		paint.setAntiAlias(!snappingEnabled);
		paint.setSubpixelText(!snappingEnabled);
		paint.setTextAlign(Paint.Align.LEFT);
		paint.setDither(true);
		float drawX = snappingEnabled ? ((int)(x / pixelSize)) * pixelSize : x;
		float drawY = snappingEnabled ? ((int)(y / pixelSize)) * pixelSize : y;
		canvas.drawText(text, drawX, drawY, paint);
	}
    
	public void updateBounds(Paint textPaint, boolean snappingEnabled, int pixelSize)  {
		textPaint.setTextSize(textSize);
		textPaint.setTypeface(getTypeface());
		textPaint.setAntiAlias(!snappingEnabled);
		textPaint.setSubpixelText(!snappingEnabled);
		textPaint.setTextAlign(Paint.Align.LEFT);
		FontMetrics fm = textPaint.getFontMetrics();
		float textWidth = textPaint.measureText(text);
		float textHeight = fm.bottom - fm.top;
		float snappedX = snappingEnabled ? ((int)(x / pixelSize)) * pixelSize : x;
		float snappedY = snappingEnabled ? ((int)(y / pixelSize)) * pixelSize : y;
		bounds.set(snappedX, snappedY + fm.top, snappedX + textWidth, snappedY + fm.bottom);
	}
    
	public void drawHighlight(Canvas canvas, float scale)  {
		highlightPaint.setStrokeWidth(2f / scale);
		canvas.drawRect(bounds, highlightPaint);
		float handlePx = HANDLE_SIZE_DP * (canvas.getDensity() / 160f) / scale;
		canvas.drawRect(bounds.left - handlePx/2, bounds.top - handlePx/2,
		bounds.left + handlePx/2, bounds.top + handlePx/2, highlightPaint);
		canvas.drawRect(bounds.right - handlePx/2, bounds.top - handlePx/2,
		bounds.right + handlePx/2, bounds.top + handlePx/2, highlightPaint);
		canvas.drawRect(bounds.right - handlePx/2, bounds.bottom - handlePx/2,
		bounds.right + handlePx/2, bounds.bottom + handlePx/2, highlightPaint);
		canvas.drawRect(bounds.left - handlePx/2, bounds.bottom - handlePx/2,
		bounds.left + handlePx/2, bounds.bottom + handlePx/2, highlightPaint);
	}
    
	public boolean contains(float touchX, float touchY)  {
		return bounds.contains(touchX, touchY);
	}
    
	public int getHandleAt(float touchX, float touchY, float handleTolerancePx)  {
		float handleArea = handleTolerancePx * (2.0f);
		if (touchX >= bounds.left - handleArea && touchX <= bounds.left + handleArea &&
		touchY >= bounds.top - handleArea && touchY <= bounds.top + handleArea)  {
			return 0;
		}
		if (touchX >= bounds.right - handleArea && touchX <= bounds.right + handleArea &&
		touchY >= bounds.top - handleArea && touchY <= bounds.top + handleArea)  {
			return 1;
		}
		if (touchX >= bounds.right - handleArea && touchX <= bounds.right + handleArea &&
		touchY >= bounds.bottom - handleArea && touchY <= bounds.bottom + handleArea)  {
			return 2;
		}
		if (touchX >= bounds.left - handleArea && touchX <= bounds.left + handleArea &&
		touchY >= bounds.bottom - handleArea && touchY <= bounds.bottom + handleArea)  {
			return 3;
		}
		return -1;
	}
    
	public void resize(float currentTouchX, float currentTouchY, PointF lastTouchPoint, int handleId)  {
		float dx = currentTouchX - lastTouchPoint.x;
		float dy = currentTouchY - lastTouchPoint.y;
		float oldWidth = bounds.width();
		float oldHeight = bounds.height();
		float newWidth = oldWidth;
		float newHeight = oldHeight;
		float originalX = x;
		float originalY = y;
		switch (handleId)  {
			case 0: 
			newWidth = oldWidth - dx;
			newHeight = oldHeight - dy;
			x += dx;
			y += dy;
			break;
			case 1: 
			newWidth = oldWidth + dx;
			newHeight = oldHeight - dy;
			y += dy;
			break;
			case 2: 
			newWidth = oldWidth + dx;
			newHeight = oldHeight + dy;
			break;
			case 3: 
			newWidth = oldWidth - dx;
			newHeight = oldHeight + dy;
			x += dx;
			break;
		}
		if (newWidth > 0 && newHeight > 0)  {
			float scaleFactor = Math.min(newWidth / oldWidth, newHeight / oldHeight);
			if (handleId == 0 || handleId == 2)  {
				scaleFactor = Math.max(newWidth / oldWidth, newHeight / oldHeight);
			}
			setTextSize(textSize * scaleFactor);
			float pivotX = originalX;
			float pivotY = originalY;
			if (handleId == 0)  {
				pivotX = bounds.right;
				pivotY = bounds.bottom;
			} else if (handleId == 1)  {
				pivotX = bounds.left;
				pivotY = bounds.bottom;
			} else if (handleId == 2)  {
				pivotX = bounds.left;
				pivotY = bounds.top;
			} else if (handleId == 3)  {
				pivotX = bounds.right;
				pivotY = bounds.top;
			}
		}
	}
    
	public RectF getBounds()  {
		return bounds;
	}
}