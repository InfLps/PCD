package com.inflps.pcd.CORE.LISTENERS;

import android.graphics.Bitmap;
import android.graphics.RectF;
import com.inflps.pcd.CORE.DRAWING_CORE.DrawingState;

public interface DrawingViewListener {
	void onDrawingStarted();
	void onDrawingFinished();
	void onZoomStarted();
	void onZoomFinished();
    void onLayersChanged(); 
	void onShapeAdded(DrawingState.ShapeType shapeType);
	void onImageAdded(int width, int height);
	void onTextAdded(String text);
	void onTextEditStarted(String currentText);
    void onActiveObjectTransforming(RectF bounds, float rotation);
    void onActiveObjectSelected(boolean isSelected);
    void onObjectCommitted();
    void onCanvasResized(int width, int height);
    void onColorChanged(int newColor);
	void onOpacityChanged(int newOpacity);
	void onBrushSizeChanged(float newSize);
    void onLayerThumbnailUpdated(int layerId, Bitmap thumbnail);

//	@Deprecated
//	void onTextEditModeEntered(CanvasText textObject);
//	@Deprecated
//	void onTextEditModeExited();
//	@Deprecated
//	void onCurrentTextUpdated(String newText);
}