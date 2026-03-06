package com.inflps.pcd.CORE.DRAWING_CORE.DRAWABLE;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import com.inflps.pcd.CORE.DRAWING_CORE.DrawingState;
import com.inflps.pcd.CORE.DRAWING_CORE.ShapeFactory;
import com.inflps.pcd.CORE.DRAWING_CORE.DrawingState.ShapeType;
import com.inflps.pcd.CORE.DRAWING_CORE.DrawingState.PaintStyle;

public class ShapeIconDrawable extends Drawable {
	
	private final Paint paint;
	private final Path path;
	private final ShapeType shapeType;
	private final float padding = 15f;
	
	public ShapeIconDrawable(ShapeType type, int color, PaintStyle requestedStyle) {
		this.shapeType = type;
		this.path = new Path();
		this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.paint.setColor(color);
		
		PaintStyle finalStyle = resolveStyle(type, requestedStyle);
		
		if (finalStyle == PaintStyle.STROKE) {
			this.paint.setStyle(Paint.Style.STROKE);
			this.paint.setStrokeWidth(4f);
			this.paint.setStrokeCap(Paint.Cap.ROUND);
			this.paint.setStrokeJoin(Paint.Join.ROUND);
		} else {
			this.paint.setStyle(Paint.Style.FILL);
		}
	}
	
	private PaintStyle resolveStyle(ShapeType type, PaintStyle requested) {
		switch (type) {
			case LINE:
			case COIL:
			case CYLINDER:
			case CONE:
			case CUBE:
			case PYRAMID:
			case PRISM_TRIANGULAR:
			case TETRAHEDRON:
			case OCTAHEDRON:
			case ICOSAHEDRON:
			case TREFOIL:
			return PaintStyle.STROKE;
			case CLOUD:
			return PaintStyle.FILL;
			default:
			return requested;
		}
	}
	
	@Override
	protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);
		path.reset();
		
		Path generatedPath = ShapeFactory.createShapePath(
		bounds.left + padding,
		bounds.top + padding,
		bounds.right - padding,
		bounds.bottom - padding,
		shapeType
		);
		
		path.addPath(generatedPath);
	}
	
	@Override
	public void draw(Canvas canvas) {
		canvas.drawPath(path, paint);
	}
	
	@Override
	public void setAlpha(int alpha) {
		paint.setAlpha(alpha);
	}
	
	@Override
	public void setColorFilter(ColorFilter colorFilter) {
		paint.setColorFilter(colorFilter);
	}
	
	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}
}
