package com.inflps.pcd.CORE.TOOLS;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import java.util.ArrayList;

import com.inflps.pcd.DrawingView;
import com.inflps.pcd.CORE.LAYERS.LayerManager;
import com.inflps.pcd.CORE.DRAWING_CORE.DrawingState;

public class DrawingExportHelper {
	public static Bitmap exportBitmap(
	DrawingView view, 
	LayerManager layerManager, 
	DrawingState state, 
	//Deprecated! (No longer used) CanvasText currentText, 
	int logicalW, 
	int logicalH, 
	int targetW, 
	int targetH) 
	{
		if (targetW <= 0 || targetH <= 0) return null;
		
		Bitmap compositeBitmap = Bitmap.createBitmap(logicalW, logicalH, Bitmap.Config.ARGB_8888);
		Canvas compositeCanvas = new Canvas(compositeBitmap);
		if (state.canvasBackgroundColor != Color.TRANSPARENT) {
			compositeCanvas.drawColor(state.canvasBackgroundColor);
		} else {
			compositeCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		}
		
		layerManager.drawLayersToCanvas(compositeCanvas, logicalW, logicalH/*, currentText*/, state);
		
		if (targetW == logicalW && targetH == logicalH) {
			return compositeBitmap;
		}
		
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(compositeBitmap, targetW, targetH, true);
		compositeBitmap.recycle(); 
		return scaledBitmap;
	}
	
	public static Bitmap getLayerBitmap(
	DrawingView view, 
	LayerManager layerManager, 
	DrawingState state, 
	/*(Deprecated) CanvasText currentText,*/
	int logicalW, 
	int logicalH, 
	int layerId) 
	{
		if (layerManager.layerVisibility.get(layerId) == null) return null;
		
		Bitmap layerExportBitmap = Bitmap.createBitmap(logicalW, logicalH, Bitmap.Config.ARGB_8888);
		Canvas layerExportCanvas = new Canvas(layerExportBitmap);
		layerExportCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		
		boolean hasContent = false;
		
		for (LayerManager.DrawItem item : layerManager.drawHistory) {
			if (item.getLayerId() == layerId) {
				item.draw(layerExportCanvas); 
				hasContent = true;
			}
		}
		
		if (!hasContent) {
			layerExportBitmap.recycle(); 
			return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
		}
		return layerExportBitmap;
	}
}
