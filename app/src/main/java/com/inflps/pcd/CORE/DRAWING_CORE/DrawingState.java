package com.inflps.pcd.CORE.DRAWING_CORE;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;

public class DrawingState {
	
	public enum DrawingMode {
		FREEHAND, 
		SHAPE, 
		TEXT, 
		TEXT_EDIT, 
		FILL, 
		SMUDGE,
        //SELECT
	}
	
	public enum ShapeType {
		RECTANGLE, 
		CIRCLE, 
		LINE, 
		TRIANGLE, 
		RIGHT_TRIANGLE,
		CAPSULE,
		KITE,
		PENTAGON,
		HEXAGON,
		HEPTAGON,
		OCTAGON,
		DECAGON,
		PLUS,
		DIAMOND,
		STAR_3,
		STAR_4,
		STAR,
		STAR_6,
		GEAR,
		LIGHTNING,
		SPEECH_BUBBLE,
        THINKING_BUBBLE,
		ARROW,
		SHIELD,
		L_SHAPE,
		COIL,
		CHEVRON,
		CYLINDER,
		CONE,
		CUBE,
		PYRAMID,
		PRISM_TRIANGULAR,
		HEART,
		CLOUD,
		SEMICIRCLE,
		CRESCENT,
		PIE_SLICE,
		TRAPEZOID,
		PARALLELOGRAM,
		TETRAHEDRON,
		OCTAHEDRON,
		ICOSAHEDRON,
		TREFOIL,
		TAG
	}
	
	public enum PaintStyle {
		FILL, 
		STROKE
	}
	
	public DrawingMode currentDrawingMode = DrawingMode.FREEHAND;
	public ShapeType currentShapeType = ShapeType.RECTANGLE;
	public PaintStyle currentPaintStyle = PaintStyle.STROKE;
	
	public float brushStrokeWidth = 10f;
	public boolean snappingEnabled = false;
	public int textGridSize = 10;
	public boolean showGrid = false;
	public int canvasBackgroundColor = Color.WHITE;
	
	public float smudgeBrushSize = 40f;
	public float smudgeStrength = 0.6f;
	
	public float[] currentDashEffectIntervals = null;
	public PorterDuff.Mode currentXferMode = null;
	
	public final Paint mPaint;
	
	public DrawingState() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(Color.BLACK);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(brushStrokeWidth);
	}
}
