package com.inflps.pcd.CORE.TOOLS;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import java.util.LinkedList;
import java.util.Queue;

public class SelectionManager {
	public enum SelectionType { RECTANGLE, LASSO, MAGIC_WAND }
	
	private SelectionType currentType = SelectionType.RECTANGLE;
	private final Path selectionPath = new Path();
	private final Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private boolean isActive = false;
	private float animationPhase = 0;
	
	public SelectionManager() {
		outlinePaint.setStyle(Paint.Style.STROKE);
	}
	
	public void setSelectionType(SelectionType type) { this.currentType = type; }
	
	public SelectionType getSelectionType() {
		return currentType;
	}
	
	public void updateRect(float x1, float y1, float x2, float y2) {
		isActive = true;
		selectionPath.reset();
		selectionPath.addRect(Math.min(x1, x2), Math.min(y1, y2), 
		Math.max(x1, x2), Math.max(y1, y2), Path.Direction.CW);
	}
	
	public void startLasso(float x, float y) {
		isActive = true;
		selectionPath.reset();
		selectionPath.moveTo(x, y);
	}
	
	public void updateLasso(float x, float y) {
		selectionPath.lineTo(x, y);
	}
	
	public void endLasso() {
		selectionPath.close();
	}
	
	public void performMagicWand(Bitmap source, int startX, int startY, int tolerance) {
		if (source == null) return;
		isActive = true;
		selectionPath.reset(); 
	}
	
	public void draw(Canvas canvas, float currentScale) {
		if (!isActive || selectionPath.isEmpty()) return;
		
		animationPhase = (animationPhase + 1) % 20;
		outlinePaint.setStrokeWidth(2f / currentScale);
		
		outlinePaint.setColor(Color.BLACK);
		outlinePaint.setPathEffect(null);
		canvas.drawPath(selectionPath, outlinePaint);
		
		outlinePaint.setColor(Color.WHITE);
		outlinePaint.setPathEffect(new DashPathEffect(new float[]{10/currentScale, 10/currentScale}, animationPhase));
		canvas.drawPath(selectionPath, outlinePaint);
	}
	
	public void clear() {
		isActive = false;
		selectionPath.reset();
	}
	
	public void offsetSelection(float dx, float dy) {
		if (this.isActive && this.selectionPath != null) {
			this.selectionPath.offset(dx, dy);
		}
	}
	
	public Path getPath() { return selectionPath; }
	public boolean isActive() { return isActive; }
}
