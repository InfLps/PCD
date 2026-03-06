package com.inflps.pcd.CORE.DRAWING_CORE;

import android.graphics.Path;
import android.graphics.Paint;
import android.graphics.Shader;

public class PathPaint {
	private Path path;
	private Paint paint;
	private int layerId;
	
	public PathPaint(Path path, Paint paint, int layerId) {
		this.path = path;
		this.paint = new Paint(paint);
		this.layerId = layerId;
	}
	
	public Path getPath() {return path;}
	public Paint getPaint() {return paint;}
	public int getLayerId() {return layerId;}
}