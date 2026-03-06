package com.inflps.pcd;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import android.graphics.ColorFilter;
import android.graphics.Xfermode;
import android.graphics.PorterDuffColorFilter;

import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import android.graphics.BitmapFactory;
import com.google.gson.Gson;

import com.inflps.pcd.CORE.DRAWING_CORE.ShapeFactory;
import com.inflps.pcd.CORE.BRUSH_CORE.BrushArchiveLoader;
import com.inflps.pcd.CORE.BRUSH_CORE.BrushEngine;
import com.inflps.pcd.CORE.BRUSH_CORE.BrushSettings;
import com.inflps.pcd.CORE.DEPRECATED.CanvasText;
import com.inflps.pcd.CORE.DRAWING_CORE.DrawingState;
import com.inflps.pcd.CORE.DRAWING_CORE.DrawingState.ShapeType;
import com.inflps.pcd.CORE.DRAWING_CORE.PathPaint;
import com.inflps.pcd.CORE.LAYERS.LayerManager;
import com.inflps.pcd.CORE.LISTENERS.DrawingViewListener;
import com.inflps.pcd.CORE.LISTENERS.LayerInteractionListener;
import com.inflps.pcd.CORE.PROJECT_CORE.ProjectManifest;
import com.inflps.pcd.CORE.PROJECT_CORE.ProjectSerializer;
import com.inflps.pcd.CORE.TOOLS.*;

import java.util.ArrayList;

public class DrawingView extends View {
	private final DrawingState state = new DrawingState();
	private DrawingViewListener listener; private final LayerManager layerManager;
	private int logicalCanvasWidth = 1280; private int logicalCanvasHeight = 1280;
	private int exportWidth = 4096; private int exportHeight = 4096;
	private float proxyScale = 1.0f; private int maxProxyDimension = 2048;
	
	private boolean isProjectLoading = false;
	private boolean isHardwareAccelerated = true;
	private boolean isAntiAliasEnabled = true;
	
	private Bitmap mBitmap; private Canvas mCanvas;
	private final Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
	private final Paint canvasBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Matrix displayMatrix = new Matrix(); private final Matrix inverseMatrix = new Matrix();
	
	private final Path mPath = new Path(); private float mLastX, mLastY;
	
	private BrushSettings currentCustomBrush = null;
	private final BrushEngine brushEngine = new BrushEngine();
	
	private RectF activeShapeBounds = null;
	private final Path activeShapePath = new Path();
	private final Paint shapeEditBorderPaint = new Paint();
	private final Paint shapeHandlePaint = new Paint();
	private final Paint shapeHandleStrokePaint = new Paint();
	
	private String activeText = null; private Paint textPaint;
	private Typeface currentTypeface = Typeface.DEFAULT;
	
	private static final float BASE_TEXT_SIZE = 100f;
	private static final int TRANSFORM_NONE = 0;
	private static final int TRANSFORM_MOVE = 1;
	private static final int TRANSFORM_RESIZE_TL = 2; 
	private static final int TRANSFORM_RESIZE_TR = 3; 
	private static final int TRANSFORM_RESIZE_BL = 4; 
	private static final int TRANSFORM_RESIZE_BR = 5; 
	private static final int TRANSFORM_RESIZE_TOP    = 6;
	private static final int TRANSFORM_RESIZE_LEFT   = 7;
	private static final int TRANSFORM_RESIZE_RIGHT  = 8;
	private static final int TRANSFORM_RESIZE_BOTTOM = 9;
	private static final int TRANSFORM_ROTATE = 10;
	private static final float HANDLE_RADIUS = 20f;
	private static final float ROTATION_SNAP_INTERVAL = 45f;
	
	private int currentTransformMode = TRANSFORM_NONE;
	private final PointF lastTransformPoint = new PointF();
	private float shapeRotation = 0f; private float startRotation = 0f; private float startAngle = 0f;
	private final RectF transformStartBounds = new RectF();
	private float transformAspect = 1f;
	private Bitmap activeImageBitmap = null;
	private boolean isCreatingNewShape = false; private boolean isRotating = false;
	private final PointF shapeCreationStart = new PointF();
	
	private final Rect viewportSrcRect = new Rect();
	private final RectF viewportDstRect = new RectF();
	
	private float currentScale = 1f;
	private float currentTranslateX = 0f; private float currentTranslateY = 0f;
	private boolean hasPerformedZoom = false;
	private static final int MODE_NONE = 0;
	private static final int MODE_ZOOM = 1;
	private static final int MODE_DRAW = 2;
	private int currentZoomPanMode = MODE_NONE;
	private float currentRotation = 0f;
	private float initialRotationOnPinchStart = 0f;
	private float initialPinchAngle = 0f;
	private final PointF initialLogicalPinchCenter = new PointF();
	
	private float mDownX, mDownY;
	private int activePointerId = MotionEvent.INVALID_POINTER_ID;
	private float TOUCH_SLOP_DRAW;
	private final PointF initialPinchMidPoint = new PointF();
	private float initialPinchDistance;
	private float initialScaleOnPinchStart;
	private float initialTranslateXOnPinchStart, initialTranslateYOnPinchStart;
	private static final float ZOOM_SENSITIVITY_FACTOR = 1f;
	private GestureDetector gestureDetector;
	
	private boolean isSmudging = false;
	private float smudgeLastX, smudgeLastY;
	private final Paint smudgeSamplePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
	private final Paint smudgeDragPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
	private final Rect smudgeSrcRect = new Rect();
	private final Rect smudgeDstRect = new Rect();
	private Bitmap smudgeBuffer = null;
	private Canvas cachedLayerCanvas = null;
	private Bitmap lastUsedLayerBitmap = null;
	
	private boolean fillPotentialClick = false;
	private boolean drawingLocked = false;
	private final Handler drawingLockHandler = new Handler();
	private static final long DRAWING_LOCK_DURATION = 70;
	private int fillTolerance = 1;
	
	private final float[] tempCoords = new float[2];
	private final Matrix tempMatrix = new Matrix();
	
	private boolean isPickingColor = false;
	private final PointF pickerTouchPoint = new PointF();
	private int lastPickedColor = Color.TRANSPARENT;
	private final float LOUPE_RADIUS = 100f;
	private final float LOUPE_OFFSET = 180f;
	private float smoothOffsetX = -140f; private float smoothOffsetY = -140f;
	
	private int previousColor = Color.TRANSPARENT; private float density = 1f;
	private Bitmap strokeBufferBitmap; private Canvas strokeBufferCanvas; private int storedUserAlpha = 255; 
	private final Paint transferPaint = new Paint(Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
	
	public DrawingView(Context c) { super(c); layerManager = new LayerManager(logicalCanvasWidth, logicalCanvasHeight); init(c); }
	public DrawingView(Context c, AttributeSet attrs) { super(c, attrs); layerManager = new LayerManager(logicalCanvasWidth, logicalCanvasHeight); init(c); }
	public DrawingView(Context c, AttributeSet attrs, int defStyleAttr) { super(c, attrs, defStyleAttr); layerManager = new LayerManager(logicalCanvasWidth, logicalCanvasHeight); init(c); }
	
	private void init(Context c) {
		density = c.getResources().getDisplayMetrics().density; 
		canvasBorderPaint.setColor(Color.DKGRAY);
		canvasBorderPaint.setStyle(Paint.Style.STROKE);
		canvasBorderPaint.setStrokeWidth(2f);
		shapeEditBorderPaint.setColor(Color.DKGRAY);
		shapeEditBorderPaint.setStyle(Paint.Style.STROKE);
		shapeEditBorderPaint.setStrokeWidth(3f);
		shapeEditBorderPaint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));
		shapeHandlePaint.setColor(Color.WHITE);
		shapeHandlePaint.setStyle(Paint.Style.FILL);
		shapeHandleStrokePaint.setColor(Color.DKGRAY);
		shapeHandleStrokePaint.setStyle(Paint.Style.STROKE);
		shapeHandleStrokePaint.setStrokeWidth(2f);
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setTextAlign(Paint.Align.CENTER);
		textPaint.setTextSize(BASE_TEXT_SIZE);
		ViewConfiguration vc = ViewConfiguration.get(c);
		TOUCH_SLOP_DRAW = vc.getScaledTouchSlop() * 0.5f;
		gestureDetector = new GestureDetector(c, new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onDoubleTap(MotionEvent e) { if (activeShapeBounds != null) {commitCurrentShape(); return true;} return false; }
			@Override
			public void onLongPress(MotionEvent e) {
				if (currentZoomPanMode == MODE_ZOOM) return; redrawCanvas(); 
				previousColor = state.mPaint.getColor(); isPickingColor = true;
				pickerTouchPoint.set(e.getX(), e.getY());
				updatePickedColor(e.getX(), e.getY());
				performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
				getParent().requestDisallowInterceptTouchEvent(true); invalidate();
			}
		});
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(0xFF303030); 
		displayMatrix.reset(); displayMatrix.postScale(currentScale, currentScale);
		displayMatrix.postRotate(currentRotation);
		displayMatrix.postTranslate(currentTranslateX, currentTranslateY);
		canvas.save(); canvas.concat(displayMatrix);
		RectF layerBounds = new RectF(0, 0, logicalCanvasWidth, logicalCanvasHeight);
		canvas.saveLayer(layerBounds, null); 
		Paint paperPaint = new Paint();
		paperPaint.setColor(state.canvasBackgroundColor);
		canvas.drawRect(layerBounds, paperPaint);
		mBitmapPaint.setFilterBitmap(false); mBitmapPaint.setAntiAlias(false); mBitmapPaint.setDither(false);
		if (mBitmap != null && !mBitmap.isRecycled()) { canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint); }
		if (strokeBufferBitmap != null && !strokeBufferBitmap.isRecycled()) {
			boolean isErasing = state.currentXferMode == PorterDuff.Mode.CLEAR || state.currentXferMode == PorterDuff.Mode.DST_OUT;
			Paint livePaint = new Paint();
			livePaint.setFilterBitmap(false); 
			livePaint.setAntiAlias(false);
			if (!isErasing) { livePaint.setAlpha(state.mPaint.getAlpha()); }
			if (isErasing) { livePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT)); }
			canvas.drawBitmap(strokeBufferBitmap, 0, 0, livePaint);
		}
		canvas.restore();
		canvasBorderPaint.setStrokeWidth(1.5f / currentScale);
		canvas.drawRect(layerBounds, canvasBorderPaint);
		if (activeShapeBounds != null) drawActiveShapePreview(canvas);
		canvas.restore();
		if (isPickingColor) drawColorPickerUI(canvas);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) { super.onSizeChanged(w, h, oldw, oldh); fitCanvasToScreen(); }
	
	private float dpToPx(float dp) {return dp * density;}
	private void drawLivePaths(Canvas canvas) {
		if (state.currentDrawingMode == DrawingState.DrawingMode.FREEHAND && !mPath.isEmpty()) {
			Paint previewPaint = new Paint(state.mPaint);
			previewPaint.setStrokeCap(Paint.Cap.ROUND);
			previewPaint.setStrokeJoin(Paint.Join.ROUND);
			previewPaint.setStyle(Paint.Style.STROKE);
			canvas.drawPath(mPath, previewPaint);
		}
	}
	private void drawActiveShapePreview(Canvas canvas) {
		canvas.save();
		float cx = activeShapeBounds.centerX(); float cy = activeShapeBounds.centerY();
		canvas.translate(cx, cy); canvas.rotate(shapeRotation);
		float w = activeShapeBounds.width(); float h = activeShapeBounds.height();
		float sx = w < 0 ? -1 : 1; float sy = h < 0 ? -1 : 1;
		canvas.scale(sx, sy);
		float halfW = Math.abs(w) / 2f; float halfH = Math.abs(h) / 2f;
		RectF localDrawRect = new RectF(-halfW, -halfH, halfW, halfH);
		if (activeImageBitmap != null) {
			Paint imgPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
			imgPaint.setAlpha(state.mPaint.getAlpha());
			canvas.drawBitmap(activeImageBitmap, null, localDrawRect, imgPaint);
		} else if (activeText != null) {
			textPaint.setColor(state.mPaint.getColor());
			textPaint.setAlpha(state.mPaint.getAlpha());
			textPaint.setTypeface(currentTypeface);
			textPaint.setStyle(Paint.Style.FILL); 
			Rect textBounds = new Rect();
			textPaint.getTextBounds(activeText, 0, activeText.length(), textBounds);
			Paint.FontMetrics metrics = textPaint.getFontMetrics();
			float yOffset = -(metrics.descent + metrics.ascent) / 2f;
			float textNaturalWidth = textBounds.width();
			if(textNaturalWidth < 1) textNaturalWidth = 1;
			float textNaturalHeight = textBounds.height(); 
			if(textNaturalHeight < 1) textNaturalHeight = 1;
			float scaleX = localDrawRect.width() / (textNaturalWidth * 1.2f); float scaleY = localDrawRect.height() / (textNaturalHeight * 1.2f);
			canvas.save(); canvas.scale(scaleX, scaleY);
			canvas.drawText(activeText, 0, yOffset, textPaint); canvas.restore();
		} else {
			Paint previewPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			previewPaint.setColor(state.mPaint.getColor());
			previewPaint.setAlpha(state.mPaint.getAlpha());
			previewPaint.setStyle(state.currentPaintStyle == DrawingState.PaintStyle.FILL ? Paint.Style.FILL : Paint.Style.STROKE);
			previewPaint.setStrokeWidth(state.currentPaintStyle == DrawingState.PaintStyle.FILL ? 0 : state.brushStrokeWidth);
			previewPaint.setStrokeCap(Paint.Cap.ROUND);
			previewPaint.setStrokeJoin(Paint.Join.ROUND);
			activeShapePath.reset();
			activeShapePath.addPath(ShapeFactory.createShapePath(
			localDrawRect.left, localDrawRect.top,
			localDrawRect.right, localDrawRect.bottom,
			state.currentShapeType));
			canvas.drawPath(activeShapePath, previewPaint);
		}
		canvas.scale(1/sx, 1/sy);
		float rawHalfW = w / 2f; float rawHalfH = h / 2f;
		RectF uiBounds = new RectF(-rawHalfW, -rawHalfH, rawHalfW, rawHalfH);
		RectF sortedUiBounds = new RectF(uiBounds);
		sortedUiBounds.sort();
		if (!isCreatingNewShape) {
			float uiScale = 1.0f / currentScale;
			shapeEditBorderPaint.setStrokeWidth(2f * uiScale);
			canvas.drawRect(sortedUiBounds, shapeEditBorderPaint);
			drawShapeHandles(canvas, uiBounds, uiScale); 
		} canvas.restore();
	}
	private void drawShapeHandles(Canvas canvas, RectF b, float uiScale) {
		float radius = HANDLE_RADIUS * uiScale;
		drawHandle(canvas, b.left, b.top, radius);      
		drawHandle(canvas, b.right, b.top, radius);     
		drawHandle(canvas, b.left, b.bottom, radius);     
		drawHandle(canvas, b.right, b.bottom, radius);    
		drawHandle(canvas, b.centerX(), b.top, radius);    
		drawHandle(canvas, b.centerX(), b.bottom, radius);     
		drawHandle(canvas, b.left, b.centerY(), radius);     
		drawHandle(canvas, b.right, b.centerY(), radius);     
		float handleY = b.top < b.bottom ? b.top : b.bottom;
		float rotOffset = 60f * uiScale;
		float rotY = b.top - (b.height() < 0 ? -rotOffset : rotOffset); 
		canvas.drawLine(b.centerX(), b.top, b.centerX(), rotY, shapeEditBorderPaint);
		drawHandle(canvas, b.centerX(), rotY, radius);
	}
	private void drawHandle(Canvas canvas, float x, float y, float r) {
		canvas.drawCircle(x, y, r, shapeHandlePaint); canvas.drawCircle(x, y, r, shapeHandleStrokePaint);
	}
	private void drawColorPickerUI(Canvas canvas) {
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		final float OUTER_RADIUS = dpToPx(45f);
		final float INNER_RADIUS = dpToPx(35f);
		final float OFFSET_DIST  = dpToPx(65f);
		float targetOffsetX = (pickerTouchPoint.x > getWidth() / 2f) ? -OFFSET_DIST : OFFSET_DIST;
		float targetOffsetY = (pickerTouchPoint.y > getHeight() / 2f) ? -OFFSET_DIST : OFFSET_DIST;
		smoothOffsetX += (targetOffsetX - smoothOffsetX) * 0.2f;
		smoothOffsetY += (targetOffsetY - smoothOffsetY) * 0.2f;
		float tipX = pickerTouchPoint.x;
		float tipY = pickerTouchPoint.y;
		float centerX = tipX + smoothOffsetX;
		float centerY = tipY + smoothOffsetY;
		centerX = Math.max(OUTER_RADIUS, Math.min(getWidth() - OUTER_RADIUS, centerX));
		centerY = Math.max(OUTER_RADIUS, Math.min(getHeight() - OUTER_RADIUS, centerY));
		Path path = new Path();
		path.moveTo(tipX, tipY); 
		double angleToTip = Math.atan2(tipY - centerY, tipX - centerX);
		float angleDegrees = (float) Math.toDegrees(angleToTip);
		RectF oval = new RectF(centerX - OUTER_RADIUS, centerY - OUTER_RADIUS, 
		centerX + OUTER_RADIUS, centerY + OUTER_RADIUS);
		path.arcTo(oval, angleDegrees + 45, 270, false);
		path.close();
		p.setStyle(Paint.Style.FILL);
		p.setColor(Color.WHITE);
		p.setShadowLayer(dpToPx(4f), 0, dpToPx(2f), 0x44000000);
		canvas.drawPath(path, p);
		p.clearShadowLayer();
		p.setColor(previousColor);
		RectF innerOval = new RectF(centerX - INNER_RADIUS, centerY - INNER_RADIUS, 
		centerX + INNER_RADIUS, centerY + INNER_RADIUS);
		canvas.drawArc(innerOval, 0, 180, true, p);
		p.setColor(lastPickedColor);
		canvas.drawArc(innerOval, 180, 180, true, p);
		p.setColor(Color.GRAY);
		p.setStrokeWidth(dpToPx(1.5f));
		canvas.drawLine(centerX - INNER_RADIUS, centerY, centerX + INNER_RADIUS, centerY, p);
		p.setStyle(Paint.Style.STROKE);
		p.setColor(Color.parseColor("#E0E0E0"));
		p.setStrokeWidth(dpToPx(2f));
		canvas.drawCircle(centerX, centerY, INNER_RADIUS, p);
		p.setColor(Color.WHITE);
		p.setStrokeWidth(dpToPx(1.5f));
		p.setShadowLayer(dpToPx(1f), 0, dpToPx(1f), 0x88000000);
		canvas.drawCircle(tipX, tipY, dpToPx(3f), p);
		canvas.drawLine(tipX - dpToPx(8f), tipY, tipX + dpToPx(8f), tipY, p);
		canvas.drawLine(tipX, tipY - dpToPx(8f), tipX, tipY + dpToPx(8f), p);
		p.clearShadowLayer(); invalidate();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (drawingLocked) return true;
		if (strokeBufferBitmap == null || strokeBufferBitmap.isRecycled()) { return false;  }
		if (gestureDetector.onTouchEvent(event)) return true;
		float[] pts = getCanvasCoords(event);
		float cx = pts[0]; float cy = pts[1];
		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
			handleActionDown(event, cx, cy);
			break;
			case MotionEvent.ACTION_POINTER_DOWN:
			handlePointerDown(event);
			break;
			case MotionEvent.ACTION_MOVE:
			handleActionMove(event, cx, cy);
			break;
			case MotionEvent.ACTION_UP:
			handleActionUp(event, cx, cy);
			break;
			case MotionEvent.ACTION_POINTER_UP:
			handlePointerUp(event);
			break;
			case MotionEvent.ACTION_CANCEL:
			resetTouchState();
			break;
		}
		invalidate(); return true;
	}
	
	private void handleActionDown(MotionEvent event, float cx, float cy) {
		if (currentZoomPanMode == MODE_ZOOM) return;
		mDownX = event.getX(); mDownY = event.getY();
		activePointerId = event.getPointerId(0);
		currentZoomPanMode = MODE_NONE;
		hasPerformedZoom = false;
		if (state.currentDrawingMode == DrawingState.DrawingMode.SHAPE) {handleShapeDown(cx, cy); return;}
		if (state.currentDrawingMode == DrawingState.DrawingMode.FILL) {
			fillPotentialClick = true;
		} else if (state.currentDrawingMode == DrawingState.DrawingMode.SMUDGE) {
			startSmudge(cx, cy); currentZoomPanMode = MODE_DRAW;
		}
	}
	private void handleShapeDown(float cx, float cy) {
		if (activeShapeBounds != null) {
			currentTransformMode = getTransformModeForTouch(cx, cy);
			if (currentTransformMode != TRANSFORM_NONE) {
				currentZoomPanMode = MODE_DRAW;
				lastTransformPoint.set(cx, cy);
				if (currentTransformMode == TRANSFORM_ROTATE) {
					isRotating = true;
					startRotation = shapeRotation;
					float centerX = activeShapeBounds.centerX(); float centerY = activeShapeBounds.centerY();
					startAngle = (float) Math.toDegrees(Math.atan2(cy - centerY, cx - centerX));
				} else {
					transformStartBounds.set(activeShapeBounds);
					if (Math.abs(transformStartBounds.height()) > 0) {
						transformAspect = Math.abs(transformStartBounds.width()) / Math.abs(transformStartBounds.height());
					}
				}
			} else {
				currentZoomPanMode = MODE_NONE;
			}
		} else {
			activeShapeBounds = new RectF(cx, cy, cx, cy);
			shapeCreationStart.set(cx, cy);
			isCreatingNewShape = true;
			currentTransformMode = TRANSFORM_NONE;
			currentZoomPanMode = MODE_DRAW;
			transformStartBounds.set(activeShapeBounds);
			transformAspect = 1f;
			if (listener != null) {
				listener.onShapeAdded(state.currentShapeType);
				listener.onActiveObjectSelected(true);
			}
		}
	}
	private void handleActionMove(MotionEvent event, float cx, float cy) {
		if (event.getPointerCount() > 1) { performZoom(event);  return; }
		float dx = event.getX() - mDownX; float dy = event.getY() - mDownY;
		if (isPickingColor) {
			pickerTouchPoint.set(event.getX(), event.getY());
			updatePickedColor(event.getX(), event.getY()); invalidate(); return;
		}
		if (currentZoomPanMode == MODE_ZOOM) { invalidate(); return; }
		if (currentZoomPanMode == MODE_NONE && Math.hypot(dx, dy) > TOUCH_SLOP_DRAW) { startDrawingMode(cx, cy); fillPotentialClick = false; }
		if (currentZoomPanMode == MODE_DRAW) {
			switch (state.currentDrawingMode) {
				case FREEHAND: touchMoveFreehand(cx, cy); break;
				case SMUDGE:   moveSmudge(cx, cy); break;
				case SHAPE:    handleShapeMove(cx, cy); break;
			}
		}
	}
	private void handleShapeMove(float cx, float cy) {
		if (activeShapeBounds == null) return;
		if (isCreatingNewShape) {
			activeShapeBounds.set(shapeCreationStart.x, shapeCreationStart.y, cx, cy);
		} else {
			updateShapeTransform(cx, cy);
		}
		if (listener != null) { listener.onActiveObjectTransforming(activeShapeBounds, shapeRotation); }
	}
	private void startDrawingMode(float cx, float cy) {
		if (state.currentDrawingMode == DrawingState.DrawingMode.FREEHAND) {
			float[] downPts = getCanvasCoordsRaw(mDownX, mDownY);
			currentZoomPanMode = MODE_DRAW;
			touchStartFreehand(downPts[0], downPts[1]);
			touchMoveFreehand(cx, cy);
			if (listener != null) listener.onDrawingStarted();
		}
	}
	private void handleActionUp(MotionEvent event, float cx, float cy) {
		if (hasPerformedZoom) { handleZoomEnd(); return; }
		if (isPickingColor) {
			isPickingColor = false;
			if (listener != null) listener.onColorChanged(lastPickedColor); 
			invalidate(); return; 
		}
		if (currentZoomPanMode == MODE_DRAW) {
			switch (state.currentDrawingMode) {
				case FREEHAND:
				touchUpFreehand();
				if (listener != null) listener.onDrawingFinished();
				break;
				case SMUDGE:
				endSmudge();
				if (listener != null) listener.onDrawingFinished();
				break;
				case SHAPE:
				isCreatingNewShape = false;
				currentTransformMode = TRANSFORM_NONE; break;
			}
		} else if (currentZoomPanMode == MODE_NONE && state.currentDrawingMode == DrawingState.DrawingMode.FREEHAND) {
			if (mPath.isEmpty()) {
				touchStartFreehand(cx, cy); touchUpFreehand();
				if (listener != null) listener.onDrawingFinished();
			}
		}
		if (state.currentDrawingMode == DrawingState.DrawingMode.FILL && fillPotentialClick) {
			performFill((int) cx, (int) cy);
			if (listener != null) listener.onDrawingFinished();
		}
		fillPotentialClick = false; resetTouchState();
	}
	private void handleZoomEnd() {
		if (currentZoomPanMode == MODE_ZOOM && listener != null) { listener.onZoomFinished(); }
		if (state.currentDrawingMode == DrawingState.DrawingMode.SHAPE && isCreatingNewShape) { activeShapeBounds = null; isCreatingNewShape = false; }
		fillPotentialClick = false; resetTouchState(); invalidate();
	}
	private int getTransformModeForTouch(float cx, float cy) {
		if (activeShapeBounds == null) return TRANSFORM_NONE;
		tempMatrix.reset();
		tempMatrix.setRotate(-shapeRotation, activeShapeBounds.centerX(), activeShapeBounds.centerY());
		float[] pts = {cx, cy};
		tempMatrix.mapPoints(pts);
		float tx = pts[0]; float ty = pts[1];
		RectF b = new RectF(activeShapeBounds); b.sort(); 
		int mode = TRANSFORM_NONE;
		float hitSlop = (HANDLE_RADIUS * 2.5f) / currentScale;
		float rotOffset = 60f / currentScale; float visualTopY = b.top; 
		if (dist(tx, ty, b.centerX(), visualTopY - rotOffset) < hitSlop) { return TRANSFORM_ROTATE; }
		if (dist(tx, ty, b.left, b.top) < hitSlop) mode = TRANSFORM_RESIZE_TL;
		else if (dist(tx, ty, b.right, b.top) < hitSlop) mode = TRANSFORM_RESIZE_TR;
		else if (dist(tx, ty, b.left, b.bottom) < hitSlop) mode = TRANSFORM_RESIZE_BL;
		else if (dist(tx, ty, b.right, b.bottom) < hitSlop) mode = TRANSFORM_RESIZE_BR;
		else if (dist(tx, ty, b.centerX(), b.top) < hitSlop) mode = TRANSFORM_RESIZE_TOP;
		else if (dist(tx, ty, b.centerX(), b.bottom) < hitSlop) mode = TRANSFORM_RESIZE_BOTTOM;
		else if (dist(tx, ty, b.left, b.centerY()) < hitSlop) mode = TRANSFORM_RESIZE_LEFT;
		else if (dist(tx, ty, b.right, b.centerY()) < hitSlop) mode = TRANSFORM_RESIZE_RIGHT;
		else if (b.contains(tx, ty)) { return TRANSFORM_MOVE; }
		boolean flipX = activeShapeBounds.width() < 0;
		boolean flipY = activeShapeBounds.height() < 0;
		return mapVisualToLogicalHandle(mode, flipX, flipY);
	}
	private void updateShapeTransform(float cx, float cy) {
		float totalDx = cx - lastTransformPoint.x; float totalDy = cy - lastTransformPoint.y;
		tempMatrix.reset(); tempMatrix.setRotate(-shapeRotation);
		float[] deltas = {totalDx, totalDy};
		tempMatrix.mapVectors(deltas);
		float dx = deltas[0]; float dy = deltas[1];
		RectF current = activeShapeBounds;
		switch (currentTransformMode) {
			case TRANSFORM_MOVE:
			activeShapeBounds.offset(totalDx, totalDy);
			lastTransformPoint.set(cx, cy);
			return;
			case TRANSFORM_ROTATE:
			float centerX = activeShapeBounds.centerX();
			float centerY = activeShapeBounds.centerY();
			float currentAngle = (float) Math.toDegrees(Math.atan2(cy - centerY, cx - centerX));
			float deltaAngle = currentAngle - startAngle;
			shapeRotation = startRotation + deltaAngle;
			
			float snapInterval = 15f; 
			if (Math.abs(shapeRotation % snapInterval) < 3f) {
				shapeRotation = Math.round(shapeRotation / snapInterval) * snapInterval;
			}
			return;
			case TRANSFORM_RESIZE_LEFT:   current.left += dx; break;
			case TRANSFORM_RESIZE_RIGHT:  current.right += dx; break;
			case TRANSFORM_RESIZE_TOP:    current.top += dy; break;
			case TRANSFORM_RESIZE_BOTTOM: current.bottom += dy; break;
			case TRANSFORM_RESIZE_TL: current.left += dx; current.top += dy; break;
			case TRANSFORM_RESIZE_TR: current.right += dx; current.top += dy; break;
			case TRANSFORM_RESIZE_BL: current.left += dx; current.bottom += dy; break;
			case TRANSFORM_RESIZE_BR: current.right += dx; current.bottom += dy; break;
		}
		if (activeImageBitmap != null && (
		currentTransformMode == TRANSFORM_RESIZE_TL || 
		currentTransformMode == TRANSFORM_RESIZE_TR || 
		currentTransformMode == TRANSFORM_RESIZE_BL || 
		currentTransformMode == TRANSFORM_RESIZE_BR)) {
		} lastTransformPoint.set(cx, cy);
	}
	private void applyCornerResize(float dx, float dy, boolean isLeft, boolean isTop) {
		RectF r = activeShapeBounds;
		if(isLeft) r.left += dx; else r.right += dx;
		if(isTop) r.top += dy; else r.bottom += dy;
		if (activeImageBitmap != null) {
			float w = Math.abs(r.width());
			float newH = w / transformAspect;
			if(isTop) { r.top = r.bottom + (r.top < r.bottom ? -newH : newH);
			} else { r.bottom = r.top + (r.top < r.bottom ? newH : -newH);}
		}
	}
	private int mapVisualToLogicalHandle(int mode, boolean flipX, boolean flipY) {
		int result = mode;
		if (flipX) {
			switch (result) {
				case TRANSFORM_RESIZE_LEFT:   result = TRANSFORM_RESIZE_RIGHT; break;
				case TRANSFORM_RESIZE_RIGHT:  result = TRANSFORM_RESIZE_LEFT; break;
				case TRANSFORM_RESIZE_TL:     result = TRANSFORM_RESIZE_TR; break;
				case TRANSFORM_RESIZE_TR:     result = TRANSFORM_RESIZE_TL; break;
				case TRANSFORM_RESIZE_BL:     result = TRANSFORM_RESIZE_BR; break;
				case TRANSFORM_RESIZE_BR:     result = TRANSFORM_RESIZE_BL; break;
			}
		}
		if (flipY) {
			switch (result) {
				case TRANSFORM_RESIZE_TOP:    result = TRANSFORM_RESIZE_BOTTOM; break;
				case TRANSFORM_RESIZE_BOTTOM: result = TRANSFORM_RESIZE_TOP; break;
				case TRANSFORM_RESIZE_TL:     result = TRANSFORM_RESIZE_BL; break;
				case TRANSFORM_RESIZE_TR:     result = TRANSFORM_RESIZE_BR; break;
				case TRANSFORM_RESIZE_BL:     result = TRANSFORM_RESIZE_TL; break;
				case TRANSFORM_RESIZE_BR:     result = TRANSFORM_RESIZE_TR; break;
			}
		} return result;
	}
	private void updatePickedColor(float screenX, float screenY) {
		float[] pts = getCanvasCoordsRaw(screenX, screenY);
		int x = (int) pts[0]; int y = (int) pts[1];
		if (x >= 0 && x < logicalCanvasWidth && y >= 0 && y < logicalCanvasHeight) {
			if (mBitmap != null && !mBitmap.isRecycled()) {
				lastPickedColor = mBitmap.getPixel(x, y);
				if (Color.alpha(lastPickedColor) < 255) { lastPickedColor = blendWithBackground(lastPickedColor, state.canvasBackgroundColor); }
				state.mPaint.setColor(lastPickedColor);
				if (listener != null) listener.onColorChanged(lastPickedColor);
			}
		}
	}
	private int blendWithBackground(int color, int background) {
		float alpha = Color.alpha(color) / 255f;
		int r = (int) ((Color.red(color) * alpha) + (Color.red(background) * (1 - alpha)));
		int g = (int) ((Color.green(color) * alpha) + (Color.green(background) * (1 - alpha)));
		int b = (int) ((Color.blue(color) * alpha) + (Color.blue(background) * (1 - alpha)));
		return Color.rgb(r, g, b);
	}
	
	public void commitCurrentShape() {
		if (activeShapeBounds == null) return;
		float cx = activeShapeBounds.centerX(); float cy = activeShapeBounds.centerY();
		float w = Math.abs(activeShapeBounds.width()); float h = Math.abs(activeShapeBounds.height());
		Matrix matrix = new Matrix();
		boolean flipX = activeShapeBounds.width() < 0; boolean flipY = activeShapeBounds.height() < 0;
		matrix.preScale(flipX ? -1 : 1, flipY ? -1 : 1);
		matrix.postRotate(shapeRotation);
		matrix.postTranslate(cx, cy);
		RectF localBounds = new RectF(-w / 2f, -h / 2f, w / 2f, h / 2f);
		if (activeImageBitmap != null) {
			Bitmap canvasBmp = Bitmap.createBitmap(logicalCanvasWidth, logicalCanvasHeight, Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(canvasBmp);
			c.setMatrix(matrix);
			Paint imgPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
			imgPaint.setAlpha(state.mPaint.getAlpha());
			c.drawBitmap(activeImageBitmap, null, localBounds, imgPaint);
			layerManager.addFill(canvasBmp, layerManager.getCurrentLayerId());
			activeImageBitmap = null;
		} else if (activeText != null) {
			Bitmap canvasBmp = Bitmap.createBitmap(logicalCanvasWidth, logicalCanvasHeight, Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(canvasBmp);
			c.setMatrix(matrix);
			textPaint.setColor(state.mPaint.getColor());
			textPaint.setAlpha(state.mPaint.getAlpha());
			textPaint.setTypeface(currentTypeface);
			textPaint.setStyle(Paint.Style.FILL);
			Rect textBounds = new Rect();
			textPaint.getTextBounds(activeText, 0, activeText.length(), textBounds);
			Paint.FontMetrics metrics = textPaint.getFontMetrics();
			float yOffset = -(metrics.descent + metrics.ascent) / 2f;
			float textNaturalWidth = textBounds.width();
			if(textNaturalWidth < 1) textNaturalWidth = 1;
			float textNaturalHeight = textBounds.height();
			if(textNaturalHeight < 1) textNaturalHeight = 1;
			float scaleX = localBounds.width() / (textNaturalWidth * 1.2f);
			float scaleY = localBounds.height() / (textNaturalHeight * 1.2f);
			
			c.scale(scaleX, scaleY);
			c.drawText(activeText, 0, yOffset, textPaint);
			layerManager.addFill(canvasBmp, layerManager.getCurrentLayerId());
			activeText = null;
		} else {
			Paint finalShapePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			finalShapePaint.setColor(state.mPaint.getColor());
			finalShapePaint.setAlpha(state.mPaint.getAlpha());
			
			boolean isFill = state.currentPaintStyle == DrawingState.PaintStyle.FILL;
			finalShapePaint.setStyle(isFill ? Paint.Style.FILL : Paint.Style.STROKE);
			finalShapePaint.setStrokeWidth(isFill ? 0 : state.brushStrokeWidth);
			finalShapePaint.setStrokeCap(Paint.Cap.ROUND);
			finalShapePaint.setStrokeJoin(Paint.Join.ROUND);
			
			Path p = ShapeFactory.createShapePath(
			localBounds.left, localBounds.top, 
			localBounds.right, localBounds.bottom, 
			state.currentShapeType);
			p.transform(matrix);
			layerManager.addPath(new PathPaint(p, finalShapePaint, layerManager.getCurrentLayerId()));
		}
		
		activeText = null; 
		activeImageBitmap = null;
		activeShapeBounds = null; 
		shapeRotation = 0f; 
		isCreatingNewShape = false;
		
		activeShapeBounds = null; shapeRotation = 0f; isCreatingNewShape = false; redrawCanvas();
		
		if (listener != null) {
			listener.onDrawingFinished();
			listener.onObjectCommitted();
		}
		
	}
	
	private void startSmudge(float cx, float cy) {
		if (layerManager.isCurrentLayerLocked()) return;
		isSmudging = true; 
		smudgeLastX = cx; smudgeLastY = cy;
		Bitmap lb = layerManager.getCurrentLayerBitmap();
		if (lb != null) { 
			if (cachedLayerCanvas == null || lastUsedLayerBitmap != lb) { 
				cachedLayerCanvas = new Canvas(lb); 
				lastUsedLayerBitmap = lb; 
			} 
		}
		int bs = Math.max(4, (int) state.smudgeBrushSize); int ss = bs + 10;
		if (smudgeBuffer == null || smudgeBuffer.getWidth() != ss) { 
			if (smudgeBuffer != null) smudgeBuffer.recycle(); 
			smudgeBuffer = Bitmap.createBitmap(ss, ss, Bitmap.Config.ARGB_8888); 
		} invalidate();
	}
	private void moveSmudge(float x, float y) {
		if (!isSmudging) return;
		Bitmap lb = layerManager.getCurrentLayerBitmap(); if (lb == null) return;
		float dx = x - smudgeLastX; float dy = y - smudgeLastY; 
		float d = (float) Math.hypot(dx, dy); 
		if (d < 0.4f) return;
		int bs = Math.max(4, (int) state.smudgeBrushSize); 
		int ss = bs + 10; int r = ss / 2;
		float step = Math.max(0.8f, bs * 0.05f); int steps = Math.max(1, (int) (d / step));
		smudgeDragPaint.setAlpha((int) (255 * state.smudgeStrength)); 
		smudgeDragPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
		for (int i = 1; i <= steps; i++) {
			float t = (float) i / steps; 
			float cx = smudgeLastX + (dx * t); float cy = smudgeLastY + (dy * t);
			float sx = cx - (dx / steps * 0.5f); float sy = cy - (dy / steps * 0.5f);
			int sl = (int) (sx - r); int st = (int) (sy - r);
			smudgeSrcRect.set(sl, st, sl + ss, st + ss); 
			smudgeDstRect.set(0, 0, ss, ss);
			if (smudgeSrcRect.left < 0) { smudgeDstRect.left -= smudgeSrcRect.left; smudgeSrcRect.left = 0; }
			if (smudgeSrcRect.top < 0) { smudgeDstRect.top -= smudgeSrcRect.top; smudgeSrcRect.top = 0; }
			if (smudgeSrcRect.right > lb.getWidth()) { smudgeDstRect.right -= (smudgeSrcRect.right - lb.getWidth()); smudgeSrcRect.right = lb.getWidth(); }
			if (smudgeSrcRect.bottom > lb.getHeight()) { smudgeDstRect.bottom -= (smudgeSrcRect.bottom - lb.getHeight()); smudgeSrcRect.bottom = lb.getHeight(); }
			if (smudgeSrcRect.width() > 0 && smudgeSrcRect.height() > 0) {
				smudgeBuffer.eraseColor(Color.TRANSPARENT); 
				Canvas bc = new Canvas(smudgeBuffer);
				bc.drawBitmap(lb, smudgeSrcRect, smudgeDstRect, smudgeSamplePaint);
				cachedLayerCanvas.drawBitmap(smudgeBuffer, cx - r, cy - r, smudgeDragPaint);
			}
		} smudgeLastX = x; smudgeLastY = y; redrawCanvas();
	}
	private void endSmudge() {
		if (!isSmudging) return; isSmudging = false;
		Bitmap lb = layerManager.getCurrentLayerBitmap();
		if (lb != null) layerManager.addFill(lb.copy(Bitmap.Config.ARGB_8888, false), layerManager.getCurrentLayerId());
		if (listener != null) listener.onDrawingFinished(); invalidate();
	}
	private float[] getCanvasCoords(MotionEvent e) {return getCanvasCoordsRaw(e.getX(), e.getY());}
	private float[] getCanvasCoordsRaw(float x, float y) {
		tempCoords[0] = x; tempCoords[1] = y;
		tempMatrix.reset();
		tempMatrix.postScale(currentScale, currentScale);
		tempMatrix.postRotate(currentRotation);
		tempMatrix.postTranslate(currentTranslateX, currentTranslateY);
		tempMatrix.invert(inverseMatrix);
		inverseMatrix.mapPoints(tempCoords); return tempCoords;
	}
	private float getPointerAngle(MotionEvent e) {
		if (e.getPointerCount() < 2) return 0f;
		float dx = e.getX(1) - e.getX(0);
		float dy = e.getY(1) - e.getY(0);
		return (float) Math.toDegrees(Math.atan2(dy, dx));
	}
	private void touchStartFreehand(float x, float y) {
		if (layerManager.isCurrentLayerLocked()) return;
		if (strokeBufferBitmap == null || strokeBufferBitmap.isRecycled()) { recreateBitmapAndCanvas(); }
		strokeBufferBitmap.eraseColor(Color.TRANSPARENT);
		if (currentCustomBrush != null) {
			brushEngine.startStroke(x, y);
		} else {
			mPath.reset(); mPath.moveTo(x, y);
		} mLastX = x; mLastY = y;
	}
	private void touchMoveFreehand(float x, float y) {
		float threshold = 2.0f / currentScale;
		if (Math.hypot(x - mLastX, y - mLastY) < threshold) return;
		if (layerManager.isCurrentLayerLocked()) return;
		float midX = (x + mLastX) / 2f; float midY = (y + mLastY) / 2f;
		boolean isErasing = state.currentXferMode == PorterDuff.Mode.CLEAR || 
		state.currentXferMode == PorterDuff.Mode.DST_OUT;
		if (currentCustomBrush != null) {
			int userAlpha = state.mPaint.getAlpha();
			int userColor = state.mPaint.getColor();
			PorterDuff.Mode userXfer = state.currentXferMode;
			state.mPaint.setAlpha(255);
			state.mPaint.setXfermode(null);
			if (isErasing) { state.mPaint.setColor(Color.BLACK); }
			float dynamicSize = state.brushStrokeWidth;
			brushEngine.strokeTo(strokeBufferCanvas, state.mPaint, currentCustomBrush, dynamicSize, midX, midY);
			state.mPaint.setAlpha(userAlpha);
			state.mPaint.setColor(userColor);
			if (userXfer != null) state.mPaint.setXfermode(new PorterDuffXfermode(userXfer));
		} else {
			mPath.quadTo(mLastX, mLastY, midX, midY);
			strokeBufferBitmap.eraseColor(Color.TRANSPARENT);
			Paint bufferPaint = new Paint(state.mPaint);
			bufferPaint.setXfermode(null); 
			bufferPaint.setAntiAlias(false);
			if (isErasing) {
				bufferPaint.setColor(Color.BLACK);
			} else {
				bufferPaint.setColor(state.mPaint.getColor());
			}
			bufferPaint.setAlpha(255); 
			bufferPaint.setStyle(Paint.Style.STROKE);
			bufferPaint.setStrokeWidth(state.brushStrokeWidth);
			bufferPaint.setStrokeCap(Paint.Cap.ROUND);
			bufferPaint.setStrokeJoin(Paint.Join.ROUND);
			strokeBufferCanvas.drawPath(mPath, bufferPaint);
		} mLastX = x; mLastY = y; invalidate();
	}
	private void touchUpFreehand() {
		if (layerManager.isCurrentLayerLocked()) return;
		Bitmap layerBitmap = layerManager.getCurrentLayerBitmap();
		if (layerBitmap == null) return;
		Canvas layerCanvas = new Canvas(layerBitmap);
		if (currentCustomBrush != null) {
			brushEngine.endStroke(strokeBufferCanvas, state.mPaint, currentCustomBrush, state.brushStrokeWidth);
			boolean isErasing = state.currentXferMode == PorterDuff.Mode.CLEAR ||
			state.currentXferMode == PorterDuff.Mode.DST_OUT;
			Bitmap historyStroke = Bitmap.createBitmap(logicalCanvasWidth, logicalCanvasHeight, Bitmap.Config.ARGB_8888);
			Canvas historyCanvas = new Canvas(historyStroke);
			Paint transferPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
			if (isErasing) {
				historyCanvas.drawBitmap(strokeBufferBitmap, 0, 0, null);
				layerManager.addBitmapToHistoryOnly(historyStroke, getCurrentLayerId(), true);
				transferPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
			} else {
				Paint historyPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
				historyPaint.setAlpha(state.mPaint.getAlpha());
				historyCanvas.drawBitmap(strokeBufferBitmap, 0, 0, historyPaint);
				layerManager.addBitmapToHistoryOnly(historyStroke, getCurrentLayerId(), false);
				transferPaint.setAlpha(state.mPaint.getAlpha());
			}
			layerCanvas.drawBitmap(strokeBufferBitmap, 0, 0, transferPaint);
		} else {
			if (!mPath.isEmpty()) {
				mPath.lineTo(mLastX, mLastY);
				strokeBufferBitmap.eraseColor(Color.TRANSPARENT);
				boolean isErasing = state.currentXferMode == PorterDuff.Mode.CLEAR || 
				state.currentXferMode == PorterDuff.Mode.DST_OUT;
				Paint bufferPaint = new Paint(state.mPaint);
				bufferPaint.setXfermode(null); 
				if (isErasing) {
					bufferPaint.setColor(Color.BLACK);
				} else {
					bufferPaint.setColor(state.mPaint.getColor());
				}
				strokeBufferCanvas.drawPath(mPath, bufferPaint);
				Bitmap historyStroke = Bitmap.createBitmap(logicalCanvasWidth, logicalCanvasHeight, Bitmap.Config.ARGB_8888);
				Canvas historyCanvas = new Canvas(historyStroke);
				Paint transferPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
				if (isErasing) {
					historyCanvas.drawBitmap(strokeBufferBitmap, 0, 0, null);
					layerManager.addBitmapToHistoryOnly(historyStroke, getCurrentLayerId(), true);
					transferPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
					layerCanvas.drawBitmap(strokeBufferBitmap, 0, 0, transferPaint);
				} else {
					transferPaint.setAlpha(state.mPaint.getAlpha());
					historyCanvas.drawBitmap(strokeBufferBitmap, 0, 0, transferPaint);
					layerManager.addBitmapToHistoryOnly(historyStroke, getCurrentLayerId(), false);
					layerCanvas.drawBitmap(strokeBufferBitmap, 0, 0, transferPaint);
				}
			}
		}
		mPath.reset();
		strokeBufferBitmap.eraseColor(Color.TRANSPARENT);
		redrawCanvas(); invalidate();
		final int layerId = getCurrentLayerId();
		new Thread(() -> {
			Bitmap updatedThumb = layerManager.getLayerThumbnail(layerId, 150, 150);
			post(() -> {
				if (listener != null) { listener.onLayerThumbnailUpdated(layerId, updatedThumb); }
			});
		}).start();
	}
	private void handlePointerDown(MotionEvent event) {
		if (isPickingColor) { isPickingColor = false; invalidate(); }
		if (event.getPointerCount() == 2) {
			fillPotentialClick = false; 
			hasPerformedZoom = true; 
			currentTransformMode = TRANSFORM_NONE;
			initialPinchDistance = getPointerDistance(event); 
			getMidPoint(initialPinchMidPoint, event);
			initialPinchAngle = getPointerAngle(event);
			initialScaleOnPinchStart = currentScale; 
			initialRotationOnPinchStart = currentRotation; 
			float[] pts = getCanvasCoordsRaw(initialPinchMidPoint.x, initialPinchMidPoint.y);
			initialLogicalPinchCenter.set(pts[0], pts[1]);
			currentZoomPanMode = MODE_ZOOM; 
			if (listener != null) listener.onZoomStarted(); 
			lockDrawingTemporarily();
		}
	}
	private void handlePointerUp(MotionEvent event) {
		int idx = event.getActionIndex(); int id = event.getPointerId(idx);
		if (id == activePointerId && event.getPointerCount() > 1) { int newIdx = idx == 0 ? 1 : 0; mDownX = event.getX(newIdx); mDownY = event.getY(newIdx); activePointerId = event.getPointerId(newIdx); }
		if (event.getPointerCount() < 2 && currentZoomPanMode == MODE_ZOOM) { handleZoomEnd(); lockDrawingTemporarily(); }
	}
	private void performZoom(MotionEvent event) {
		if (event.getPointerCount() < 2) return;
		float newDist = getPointerDistance(event); 
		PointF mid = new PointF(); 
		getMidPoint(mid, event);
		float newAngle = getPointerAngle(event);
		if (newDist > 10f) {
			float s = 1 + ((newDist / initialPinchDistance) - 1) * ZOOM_SENSITIVITY_FACTOR;
			currentScale = Math.max(0.1f, Math.min(initialScaleOnPinchStart * s, 60.0f));
			float rawRotation = initialRotationOnPinchStart + (newAngle - initialPinchAngle);
			float snapInterval = 90f;
			float tolerance = 10f;
			float mod = Math.abs(rawRotation % snapInterval);
			if (mod < tolerance) {
				currentRotation = rawRotation - (rawRotation % snapInterval);
			} else if (mod > snapInterval - tolerance) {
				currentRotation = rawRotation + (Math.signum(rawRotation) * (snapInterval - mod));
			} else {
				currentRotation = rawRotation;
			}
			tempMatrix.reset();
			tempMatrix.postScale(currentScale, currentScale);
			tempMatrix.postRotate(currentRotation);
			float[] logicalCenter = {initialLogicalPinchCenter.x, initialLogicalPinchCenter.y};
			tempMatrix.mapPoints(logicalCenter); 
			currentTranslateX = mid.x - logicalCenter[0];
			currentTranslateY = mid.y - logicalCenter[1];
		} invalidate();
	}
	private float getPointerDistance(MotionEvent e) { return (float) Math.hypot(e.getX(0) - e.getX(1), e.getY(0) - e.getY(1)); }
	private void getMidPoint(PointF p, MotionEvent e) { p.set((e.getX(0) + e.getX(1)) / 2f, (e.getY(0) + e.getY(1)) / 2f); }
	private void performFill(int x, int y) {
		if (layerManager.isCurrentLayerLocked()) return;
		if (x < 0 || y < 0 || x >= logicalCanvasWidth || y >= logicalCanvasHeight) return;
		Bitmap lb = layerManager.getCurrentLayerBitmap(); 
		if (lb == null) return;
		int tc = lb.getPixel(x, y); 
		int rc = state.mPaint.getColor(); 
		Bitmap f = FloodFill.perform(lb, x, y, tc, rc, fillTolerance);
		if (f != null) { 
			layerManager.addFill(f, layerManager.getCurrentLayerId()); refreshCanvas(); 
		}
	}
	private void resetTouchState() { currentZoomPanMode = MODE_NONE; activePointerId = MotionEvent.INVALID_POINTER_ID; }
	private float dist(float x1, float y1, float x2, float y2) { return (float) Math.hypot(x1 - x2, y1 - y2); }
	private void lockDrawingTemporarily() { drawingLocked = true; drawingLockHandler.postDelayed(() -> { drawingLocked = false; invalidate(); }, DRAWING_LOCK_DURATION); }
	private void recreateBitmapAndCanvas() {
		if (mBitmap != null) mBitmap.recycle();
		if (strokeBufferBitmap != null) strokeBufferBitmap.recycle();
		mBitmap = Bitmap.createBitmap(logicalCanvasWidth, logicalCanvasHeight, Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
		strokeBufferBitmap = Bitmap.createBitmap(logicalCanvasWidth, logicalCanvasHeight, Bitmap.Config.ARGB_8888);
		strokeBufferCanvas = new Canvas(strokeBufferBitmap);
		strokeBufferBitmap.eraseColor(Color.TRANSPARENT);
		setLayerType(LAYER_TYPE_HARDWARE, null);  redrawCanvas();
	}
	private void updatePaintColor() {
		if (state.currentDrawingMode == DrawingState.DrawingMode.FREEHAND) {
			state.mPaint.setStyle(Paint.Style.STROKE); state.mPaint.setStrokeWidth(state.brushStrokeWidth);
			state.mPaint.setStrokeCap(Paint.Cap.ROUND); state.mPaint.setStrokeJoin(Paint.Join.ROUND);
			state.mPaint.setPathEffect(state.currentDashEffectIntervals != null && state.currentXferMode == null ? new DashPathEffect(state.currentDashEffectIntervals, 0) : null);
		} invalidate();
	}
	private void resetXferMode() { state.currentXferMode = null; state.mPaint.setXfermode(null); }
	private void setupShapePaint(Paint p) { p.setStyle(state.currentPaintStyle == DrawingState.PaintStyle.FILL ? Paint.Style.FILL : Paint.Style.STROKE); p.setStrokeWidth(state.currentPaintStyle == DrawingState.PaintStyle.FILL ? 0 : state.brushStrokeWidth); p.setAntiAlias(true); p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeJoin(Paint.Join.ROUND); p.setShader(null); }
	
	public void fitCanvasToScreen() {
		if (getWidth() <= 0 || getHeight() <= 0) return;
		float sx = (float) getWidth() / logicalCanvasWidth;
		float sy = (float) getHeight() / logicalCanvasHeight;
		currentScale = Math.min(sx, sy) * 0.95f;
		currentRotation = 0f;
		currentTranslateX = (getWidth() - (logicalCanvasWidth * currentScale)) / 2f;
		currentTranslateY = (getHeight() - (logicalCanvasHeight * currentScale)) / 2f; invalidate();
	}
	public void redrawCanvas() { 
		if (mCanvas == null || mBitmap == null) return; 
		mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); 
		if (state.canvasBackgroundColor != Color.TRANSPARENT) { mCanvas.drawColor(state.canvasBackgroundColor); }
		layerManager.drawLayersToCanvas(mCanvas, logicalCanvasWidth, logicalCanvasHeight, state); postInvalidate(); 
	}
	public void importImage(Bitmap b) {
		if (b == null) return;
		if (activeShapeBounds != null) commitCurrentShape();
		activeImageBitmap = b; shapeRotation = 0f;
		float vw = getWidth() / currentScale; float vh = getHeight() / currentScale;
		float vcx = (-currentTranslateX / currentScale) + (vw / 2f); float vcy = (-currentTranslateY / currentScale) + (vh / 2f);
		float iw = b.getWidth(); float ih = b.getHeight();
		if (iw > logicalCanvasWidth * 0.8f || ih > logicalCanvasHeight * 0.8f) { float r = Math.min((logicalCanvasWidth * 0.8f) / iw, (logicalCanvasHeight * 0.8f) / ih); iw *= r; ih *= r; }
		float hw = iw / 2f; float hh = ih / 2f;
		activeShapeBounds = new RectF(vcx - hw, vcy - hh, vcx + hw, vcy + hh);
		state.currentDrawingMode = DrawingState.DrawingMode.SHAPE; isCreatingNewShape = false; currentTransformMode = TRANSFORM_NONE; 
		if (listener != null) {
			listener.onImageAdded(b.getWidth(), b.getHeight());
		} invalidate();
	}
	public void addText(String text) {
		if (text == null || text.isEmpty()) return;
		if (activeShapeBounds != null) commitCurrentShape();
		activeText = text;
		activeImageBitmap = null;
		state.currentDrawingMode = DrawingState.DrawingMode.SHAPE;
		isCreatingNewShape = false;
		currentTransformMode = TRANSFORM_NONE;
		Rect bounds = new Rect();
		textPaint.setTypeface(currentTypeface);
		textPaint.getTextBounds(activeText, 0, activeText.length(), bounds);
		float textWidth = bounds.width(); float textHeight = bounds.height();
		textWidth *= 1.2f; 
		textHeight *= 1.2f;
		float vw = getWidth() / currentScale;
		float vh = getHeight() / currentScale;
		float cx = (-currentTranslateX / currentScale) + (vw / 2f);
		float cy = (-currentTranslateY / currentScale) + (vh / 2f);
		activeShapeBounds = new RectF(
		cx - textWidth / 2f, cy - textHeight / 2f, cx + textWidth / 2f, cy + textHeight / 2f);
		shapeRotation = 0f;
		if (listener != null) {
			listener.onTextAdded(text); listener.onTextEditStarted(text); 
		} invalidate();
	}
	public void updateActiveText(String newText) {
		if (activeShapeBounds != null && activeText != null) { activeText = newText; invalidate(); }
	}
	public void setTypeface(Typeface tf) {
		this.currentTypeface = tf;
		if (activeShapeBounds != null && activeText != null) { invalidate(); }
	}
	public void setDrawingViewListener(DrawingViewListener l) { this.listener = l; }
	public void setStrokeWidth(float w) { 
		if (w >= 1f) { state.brushStrokeWidth = w; updatePaintColor(); } 
		if (listener != null) listener.onBrushSizeChanged(w);
	}
	public void setCanvasBackgroundColor(int c) { state.canvasBackgroundColor = c; redrawCanvas(); }
	public void setPaintStyle(DrawingState.PaintStyle s) { state.currentPaintStyle = s; updatePaintColor(); }
	public void setDashEffect(float[] i) { state.currentDashEffectIntervals = i; updatePaintColor(); }
	public void setAlpha(int a) {
		state.mPaint.setAlpha(a); 
		if (listener != null) listener.onOpacityChanged(a); invalidate(); 
	}
	public void setSmudgeBrushSize(float s) { if (s >= 1f) { state.smudgeBrushSize = s; invalidate(); } }
	public void setSmudgeStrength(float s) { state.smudgeStrength = Math.max(0f, Math.min(1f, s)); }
	public void undo() { if (activeShapeBounds != null) { activeShapeBounds = null; invalidate(); return; } layerManager.undo(); redrawCanvas(); }
	public void redo() { layerManager.redo(); redrawCanvas(); }
	public void refreshCanvas() { redrawCanvas(); }
	public void addLayer() { layerManager.addLayer(); redrawCanvas(); }
	public void removeCurrentLayer() { layerManager.removeCurrentLayer(); redrawCanvas(); }
	public void setLayerVisibility(int id, boolean v) { layerManager.setLayerVisibility(id, v); redrawCanvas(); }
	public void setEraserMode(boolean all) { 
		setDrawingMode(DrawingState.DrawingMode.FREEHAND); 
		PorterDuff.Mode mode = all ? PorterDuff.Mode.CLEAR : PorterDuff.Mode.DST_OUT;
		state.currentXferMode = mode;
		state.mPaint.setXfermode(new PorterDuffXfermode(mode)); 
		state.mPaint.setColor(Color.WHITE); 
	}
	public void setDrawingMode(DrawingState.DrawingMode m) { if (activeShapeBounds != null) commitCurrentShape(); state.currentDrawingMode = m; mPath.reset(); updatePaintColor(); invalidate(); }
	public void setShapeType(ShapeType t) {
		if (activeShapeBounds != null) 
		commitCurrentShape(); 
		state.currentShapeType = t; if (state.currentDrawingMode != DrawingState.DrawingMode.SHAPE) setDrawingMode(DrawingState.DrawingMode.SHAPE); 
		if (listener != null && t != null) { listener.onShapeAdded(t); }
	}
	public void setDrawingColor(String c) { 
		try { 
			int col = Color.parseColor(c); state.mPaint.setColor(col); resetXferMode(); updatePaintColor(); if (listener != null) listener.onColorChanged(col);
		} catch (IllegalArgumentException ignored) {} 
	}
	public void setLogicalCanvasSize(int w, int h) {
		if (w <= 0 || h <= 0) return;
		this.exportWidth = w; this.exportHeight = h;
		float ratio = 1.0f;
		if (w > maxProxyDimension || h > maxProxyDimension) { ratio = Math.min((float) maxProxyDimension / w, (float) maxProxyDimension / h); }
		int newLogicalW = Math.round(w * ratio);
		int newLogicalH = Math.round(h * ratio);
		this.proxyScale = 1.0f / ratio;
		if (isProjectLoading) {
			this.logicalCanvasWidth = newLogicalW;
			this.logicalCanvasHeight = newLogicalH;
			layerManager.setCanvasDimensionsNoScale(newLogicalW, newLogicalH);
		} else {
			this.logicalCanvasWidth = newLogicalW;
			this.logicalCanvasHeight = newLogicalH;
			layerManager.setCanvasDimensions(newLogicalW, newLogicalH);
		}
		recreateBitmapAndCanvas();
		post(() -> {
			fitCanvasToScreen(); redrawCanvas();
		});
	}
	public int getColor() { return state.mPaint.getColor(); }
	public String getColorHex() { return String.format("#%06X", (0xFFFFFF & state.mPaint.getColor())); }
	public int getCurrentLayerId() { return layerManager.getCurrentLayerId(); }
	public void setCurrentLayer(int id) { if (layerManager.isLayerVisible(id)) { layerManager.setCurrentLayer(id); invalidate(); } }
	public boolean isLayerVisible(int id) { return layerManager.isLayerVisible(id); }
	public boolean isLayerLocked(int id) { return layerManager.isLayerLocked(id); }
	public PorterDuff.Mode getLayerBlendMode(int id) { return layerManager.getLayerBlendMode(id); }
	public void setLayerLocked(int id, boolean l) { layerManager.setLayerLocked(id, l); invalidate(); }
	public void setLayerBlendMode(int id, PorterDuff.Mode m) { layerManager.setLayerBlendMode(id, m); redrawCanvas(); }
	public void setLayerDrawingOrder(ArrayList<Integer> o) { layerManager.setLayerDrawingOrder(o); redrawCanvas(); }
	public void clearCurrentLayer() { layerManager.clearCurrentLayer(); redrawCanvas(); }
	public Bitmap exportBitmap(int w, int h) { return DrawingExportHelper.exportBitmap(this, layerManager, state, logicalCanvasWidth, logicalCanvasHeight, w, h); }
	public Bitmap exportHighRes() {
		Bitmap finalExport = Bitmap.createBitmap(exportWidth, exportHeight, Bitmap.Config.ARGB_8888);
		Canvas exportCanvas = new Canvas(finalExport);
		exportCanvas.drawColor(state.canvasBackgroundColor);
		Paint layerPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
		for (int layerId : layerManager.getLayerDrawingOrder()) {
			if (!layerManager.isLayerVisible(layerId)) continue;
			PorterDuff.Mode mode = layerManager.getLayerBlendMode(layerId);
			layerPaint.setXfermode(new PorterDuffXfermode(mode));
			exportCanvas.saveLayer(0, 0, exportWidth, exportHeight, layerPaint); exportCanvas.save();
			exportCanvas.scale(proxyScale, proxyScale);
			for (LayerManager.DrawItem item : layerManager.drawHistory) {
				if (item.getLayerId() == layerId) { item.draw(exportCanvas);  }
			}
			exportCanvas.restore(); exportCanvas.restore();
		} return finalExport;
	}
	public Bitmap getLayerBitmap(int id) { return DrawingExportHelper.getLayerBitmap(this, layerManager, state, logicalCanvasWidth, logicalCanvasHeight, id); }
	public void setNeutralMode() { resetXferMode(); updatePaintColor(); }
	public void setExclusiveTool() { state.currentXferMode = PorterDuff.Mode.XOR; state.mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR)); updatePaintColor(); }
	public void setFillTolerance(int tolerance) {this.fillTolerance = Math.max(0, Math.min(100, tolerance));}
	public int getFillTolerance() {return fillTolerance;}
	public void useDefaultPen() {
		this.currentCustomBrush = null;
		state.mPaint.setXfermode(null); updatePaintColor(); invalidate();
	}
	public void useBrush(BrushSettings customSettings) {
		this.currentCustomBrush = customSettings;
		state.mPaint.setXfermode(new PorterDuffXfermode(customSettings.getBlendMode())); invalidate();
	}
	public LayerManager getLayerManager() {return layerManager;}
	public int getLogicalWidth() {return exportWidth;}
	public int getLogicalHeight() {return exportHeight;}
	public void notifyProjectLoaded() {
		if (mBitmap != null) { mCanvas = new Canvas(mBitmap); }
		post(() -> {
			fitCanvasToScreen(); redrawCanvas();
		});
	}
	public void setRenderingMode(boolean useHardware) {
		this.isHardwareAccelerated = useHardware;
		if (useHardware) {
			setLayerType(View.LAYER_TYPE_HARDWARE, null);
		} else {
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		} invalidate();
	}
	public void setAntiAliasEnabled(boolean enabled) {
		this.isAntiAliasEnabled = enabled;
		state.mPaint.setAntiAlias(enabled);
		mBitmapPaint.setFilterBitmap(enabled);
		mBitmapPaint.setAntiAlias(enabled); redrawCanvas();
	}
	public void setMaxProxyResolution(int dimension) {
		this.maxProxyDimension = Math.max(512, Math.min(dimension, 8192));
		setLogicalCanvasSize(exportWidth, exportHeight); 
	}
}
