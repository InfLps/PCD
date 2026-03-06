package com.inflps.pcd.CORE.LAYERS;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.Iterator;

import com.inflps.pcd.CORE.DRAWING_CORE.DrawingState;
import com.inflps.pcd.CORE.DRAWING_CORE.PathPaint;

public class LayerManager {
	private static final int MAX_LAYERS = 30;
	private final SparseArray<Bitmap> layerBuffers = new SparseArray<>();
	private static final SparseArray<PorterDuffXfermode> xfermodeCache = new SparseArray<>();
	private int logicalCanvasWidth; private int logicalCanvasHeight;

	private final Paint previewPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
	private final Matrix previewMatrix = new Matrix();

	public final ArrayList<DrawItem> drawHistory = new ArrayList<>();
	private final ArrayList<DrawItem> undoneHistory = new ArrayList<>();

	public final SparseArray<Boolean> layerVisibility = new SparseArray<>();
	private final SparseArray<Boolean> layerLocked = new SparseArray<>();
	private final SparseArray<PorterDuff.Mode> layerBlendModes = new SparseArray<>();
	private final ArrayList<Integer> layerDrawingOrder = new ArrayList<>();
	private int currentLayerId = 0;

	public interface DrawItem {
		int getLayerId();
		void draw(Canvas canvas);
		void scale(float scaleX, float scaleY);
		void recycle();
	}

	public interface LayerChangeListener { void onLayersChanged(); }

	private LayerChangeListener listener;

	public void setLayerChangeListener(LayerChangeListener l) { this.listener = l; }
	public void notifyLayerDataChanged() { if (listener != null) listener.onLayersChanged(); }

	public static class PathPaintWrapper implements DrawItem {
		private final PathPaint pp;
		public PathPaintWrapper(PathPaint pp) { this.pp = pp; }
		@Override public int getLayerId() { return pp.getLayerId(); }
		@Override public void draw(Canvas canvas) { canvas.drawPath(pp.getPath(), pp.getPaint()); }

		@Override
		public void scale(float scaleX, float scaleY) {
			if (Math.abs(scaleX - 1.0f) < 0.0001f && Math.abs(scaleY - 1.0f) < 0.0001f) return;
			Matrix scaleMatrix = new Matrix();
			scaleMatrix.setScale(scaleX, scaleY);
			pp.getPath().transform(scaleMatrix);
		}
		@Override public void recycle() { }
		public PathPaint getOriginal() { return pp; }
	}
	public static class FillItem implements DrawItem {
		private final int layerId;
		private Bitmap fillBitmap;
		private final Paint paint;
		final boolean isEraser;

		public FillItem(int layerId, Bitmap bitmap, boolean isEraser) {
			this.layerId = layerId;
			this.fillBitmap = bitmap;
			this.isEraser = isEraser;
			this.paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
			if (isEraser) { this.paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT)); }
		}
		public FillItem(int layerId, Bitmap bitmap) { this(layerId, bitmap, false); }

		@Override public int getLayerId() { return layerId; }
		@Override public void draw(Canvas canvas) { if (fillBitmap != null && !fillBitmap.isRecycled()) { canvas.drawBitmap(fillBitmap, 0, 0, paint); } }
		@Override
		public void scale(float scaleX, float scaleY) {
			if (Math.abs(scaleX - 1.0f) < 0.0001f && Math.abs(scaleY - 1.0f) < 0.0001f) return;
			if (fillBitmap != null && !fillBitmap.isRecycled()) {
				int newW = Math.round(fillBitmap.getWidth() * scaleX); int newH = Math.round(fillBitmap.getHeight() * scaleY);
				if (newW > 0 && newH > 0 && (newW != fillBitmap.getWidth() || newH != fillBitmap.getHeight())) {
					Bitmap scaled = Bitmap.createScaledBitmap(fillBitmap, newW, newH, true);
					fillBitmap.recycle(); fillBitmap = scaled;
				}
			}
		}
		@Override
		public void recycle() {
			if (fillBitmap != null && !fillBitmap.isRecycled()) { fillBitmap.recycle(); fillBitmap = null; }
		}
	}
	public static class ClearItem implements DrawItem {
		private final int layerId;
		public ClearItem(int layerId) { this.layerId = layerId; }
		@Override public int getLayerId() { return layerId; }
		@Override public void draw(Canvas canvas) { canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); }
		@Override public void scale(float scaleX, float scaleY) {}
		@Override public void recycle() {}
	}

	public LayerManager(int width, int height) {
		this.logicalCanvasWidth = width; this.logicalCanvasHeight = height; initializeNewLayerData(0);
	}

	private void initializeNewLayerData(int layerId) {
		layerVisibility.put(layerId, true);
		layerLocked.put(layerId, false);
		layerBlendModes.put(layerId, PorterDuff.Mode.SRC_OVER);
		if (!layerDrawingOrder.contains(layerId)) { layerDrawingOrder.add(layerId); }
		initializeLayerBuffer(layerId);
	}
	private void initializeLayerBuffer(int layerId) {
		Bitmap buffer = layerBuffers.get(layerId);
		if (buffer != null && !buffer.isRecycled()) buffer.recycle();
		Bitmap newBuffer = Bitmap.createBitmap(logicalCanvasWidth, logicalCanvasHeight, Bitmap.Config.ARGB_8888);
		layerBuffers.put(layerId, newBuffer);
	}

	public void addPath(PathPaint path) {
		drawHistory.add(new PathPaintWrapper(path));
		Bitmap layerBuffer = layerBuffers.get(path.getLayerId());
		if (layerBuffer != null) { new Canvas(layerBuffer).drawPath(path.getPath(), path.getPaint()); }
		clearUndoneHistory();
	}
	public void addFill(Bitmap filledBitmap, int layerId) {
		Bitmap historyCopy = filledBitmap.copy(Bitmap.Config.ARGB_8888, false);
		drawHistory.add(new FillItem(layerId, historyCopy));
		Bitmap layerBuffer = layerBuffers.get(layerId);
		if (layerBuffer != null) {
			new Canvas(layerBuffer).drawBitmap(filledBitmap, 0, 0, null);
		}
		clearUndoneHistory();
	}
	public void addBitmapToHistoryOnly(Bitmap bitmap, int layerId, boolean isEraser) {
		Bitmap historyCopy = bitmap.copy(Bitmap.Config.ARGB_8888, false);
		drawHistory.add(new FillItem(layerId, historyCopy, isEraser));
		clearUndoneHistory();
	}
	public void addBitmapToHistoryOnly(Bitmap bitmap, int layerId) { addBitmapToHistoryOnly(bitmap, layerId, false); }
	public void undo() {
		if (drawHistory.isEmpty()) return;
		DrawItem item = drawHistory.remove(drawHistory.size() - 1);
		if (item instanceof LayerStateItem) {
			LayerStateItem state = (LayerStateItem) item;
			int id = state.getLayerId();
			Bitmap existingBuffer = layerBuffers.get(id);
			if (existingBuffer != null && !existingBuffer.isRecycled()) {
				existingBuffer.recycle();
			}
			Bitmap restoredBuffer = state.getSnapshot().copy(Bitmap.Config.ARGB_8888, true);
			layerBuffers.put(id, restoredBuffer);
			layerVisibility.put(id, state.isVisible());
			layerLocked.put(id, state.isLocked());
			layerBlendModes.put(id, state.getBlendMode());
			if (!layerDrawingOrder.contains(id)) {
				int insertAt = Math.min(state.getOriginalIndex(), layerDrawingOrder.size());
				layerDrawingOrder.add(insertAt, id);
			}
			currentLayerId = id;
		} else {
			undoneHistory.add(item);
			reconstructLayerBuffer(item.getLayerId());
		}
		notifyLayerDataChanged();
	}
	public void redo() {
		if (undoneHistory.isEmpty()) return;
		DrawItem itemToRestore = undoneHistory.remove(undoneHistory.size() - 1);
		drawHistory.add(itemToRestore);
		reconstructLayerBuffer(itemToRestore.getLayerId());
	}

	private void clearUndoneHistory() { for (DrawItem item : undoneHistory) item.recycle(); undoneHistory.clear(); }

	public int addLayer() {
		if (layerVisibility.size() < MAX_LAYERS) {
			int newId = 0;
			while (layerVisibility.get(newId) != null) newId++;
			initializeNewLayerData(newId);
			currentLayerId = newId; return newId;
		} return -1;
	}
	public void removeCurrentLayer() {
		if (layerDrawingOrder.size() <= 1) return;
		final int idToRemove = currentLayerId;
		final int currentIndex = layerDrawingOrder.indexOf(idToRemove);
		Bitmap currentBmp = layerBuffers.get(idToRemove);
		Bitmap backup = (currentBmp != null && !currentBmp.isRecycled())
				? currentBmp.copy(Bitmap.Config.ARGB_8888, true)
				: Bitmap.createBitmap(logicalCanvasWidth, logicalCanvasHeight, Bitmap.Config.ARGB_8888);
		LayerStateItem deleteAction = new LayerStateItem(
				idToRemove, backup,
				layerVisibility.get(idToRemove, true),
				layerLocked.get(idToRemove, false),
				layerBlendModes.get(idToRemove, PorterDuff.Mode.SRC_OVER),
				currentIndex);
		drawHistory.add(deleteAction);
		layerVisibility.remove(idToRemove);
		layerLocked.remove(idToRemove);
		layerBlendModes.remove(idToRemove);
		layerDrawingOrder.remove(Integer.valueOf(idToRemove));
		if (currentBmp != null) currentBmp.recycle();
		layerBuffers.remove(idToRemove);
		int nextIndex = Math.max(0, currentIndex - 1);
		currentLayerId = layerDrawingOrder.get(nextIndex);
		notifyLayerDataChanged();
	}
	public void clearCurrentLayer() {
		if (isCurrentLayerLocked()) return;
		int idToClear = currentLayerId;
		drawHistory.add(new ClearItem(idToClear));
		Bitmap buffer = layerBuffers.get(idToClear);
		if (buffer != null && !buffer.isRecycled()) {
			buffer.eraseColor(Color.TRANSPARENT);
		}
		clearUndoneHistory(); notifyLayerDataChanged();
	}
	public void clearAllLayers() {
		for (DrawItem item : drawHistory) item.recycle();
		drawHistory.clear(); clearUndoneHistory();
		for (int i = 0; i < layerBuffers.size(); i++) {
			Bitmap b = layerBuffers.valueAt(i);
			if (b != null && !b.isRecycled()) b.recycle();
		}
		layerBuffers.clear();
		layerVisibility.clear();
		layerLocked.clear();
		layerBlendModes.clear();
		layerDrawingOrder.clear();
		currentLayerId = 0;
	}
	public int getCurrentLayerId() { return currentLayerId; }
	public void setCurrentLayer(int layerId) { if (layerVisibility.get(layerId) != null) this.currentLayerId = layerId; }
	public boolean isLayerLocked(int layerId) { return layerLocked.get(layerId, false); }
	public boolean isCurrentLayerLocked() { return isLayerLocked(currentLayerId); }
	public boolean isLayerVisible(int layerId) { return layerVisibility.get(layerId, false); }
	public Bitmap getCurrentLayerBitmap() { return layerBuffers.get(currentLayerId); }
	public Canvas getCurrentLayerCanvas() {
		Bitmap buffer = layerBuffers.get(currentLayerId);
		return (buffer != null) ? new Canvas(buffer) : null;
	}
	public void setCanvasDimensions(int newWidth, int newHeight) {
		if (this.logicalCanvasWidth == newWidth && this.logicalCanvasHeight == newHeight) { return; }
		float scaleX = (float) newWidth / (logicalCanvasWidth > 0 ? logicalCanvasWidth : newWidth);
		float scaleY = (float) newHeight / (logicalCanvasHeight > 0 ? logicalCanvasHeight : newHeight);
		if (Math.abs(newWidth - logicalCanvasWidth) < 1 && Math.abs(newHeight - logicalCanvasHeight) < 1) {
			this.logicalCanvasWidth = newWidth; this.logicalCanvasHeight = newHeight; return;
		}
		for (int i = 0; i < layerBuffers.size(); i++) {
			int key = layerBuffers.keyAt(i);
			Bitmap oldBuffer = layerBuffers.get(key);
			if (oldBuffer != null && !oldBuffer.isRecycled()) {
				Bitmap newBuffer = Bitmap.createScaledBitmap(oldBuffer, newWidth, newHeight, true);
				layerBuffers.put(key, newBuffer); oldBuffer.recycle();
			}
		}
		for (DrawItem item : drawHistory) item.scale(scaleX, scaleY);
		for (DrawItem item : undoneHistory) item.scale(scaleX, scaleY);
		this.logicalCanvasWidth = newWidth; this.logicalCanvasHeight = newHeight;
	}
	public void setLayerVisibility(int id, boolean v) { if (layerVisibility.get(id) != null) layerVisibility.put(id, v); }
	public void setLayerLocked(int id, boolean l) { if (layerLocked.get(id) != null) layerLocked.put(id, l); }
	public void setLayerBlendMode(int id, PorterDuff.Mode m) { if (layerBlendModes.get(id) != null) layerBlendModes.put(id, m); }
	public PorterDuff.Mode getLayerBlendMode(int id) { return layerBlendModes.get(id, PorterDuff.Mode.SRC_OVER); }
	public ArrayList<Integer> getLayerDrawingOrder() { return new ArrayList<>(layerDrawingOrder); }
	public void setLayerDrawingOrder(ArrayList<Integer> order) { layerDrawingOrder.clear(); layerDrawingOrder.addAll(order); }
	public void drawLayersToCanvas(Canvas canvas, int width, int height, DrawingState state) {
		Paint blendPaint = new Paint(Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
		for (int layerId : layerDrawingOrder) {
			if (!layerVisibility.get(layerId, true)) continue;
			Bitmap layerBmp = layerBuffers.get(layerId);
			if (layerBmp == null || layerBmp.isRecycled()) continue;
			PorterDuff.Mode blendMode = layerBlendModes.get(layerId, PorterDuff.Mode.SRC_OVER);
			blendPaint.setXfermode(getCachedXfermode(blendMode));
			canvas.drawBitmap(layerBmp, 0, 0, blendPaint);
		}
	}
	public void flattenLayerToBitmap(int layerId, Bitmap newSnapshot) {
		Iterator<DrawItem> it = drawHistory.iterator();
		while (it.hasNext()) {
			DrawItem item = it.next();
			if (item.getLayerId() == layerId) {
				item.recycle();
				it.remove();
			}
		}
		Bitmap historyCopy = newSnapshot.copy(Bitmap.Config.ARGB_8888, false);
		drawHistory.add(new FillItem(layerId, historyCopy));
		Bitmap buffer = layerBuffers.get(layerId);
		if (buffer != null) {
			buffer.eraseColor(Color.TRANSPARENT);
			new Canvas(buffer).drawBitmap(newSnapshot, 0, 0, null);
		}
		clearUndoneHistory();
	}
	public Bitmap getLayerBitmap(int id) { return layerBuffers.get(id); }
	public Bitmap getLayerBuffer(int id) { return layerBuffers.get(id); }
	public void restoreLayer(int id, Bitmap loadedBmp, boolean visible, boolean locked, String blendModeName) {
		Iterator<DrawItem> it = drawHistory.iterator();
		while (it.hasNext()) {
			DrawItem item = it.next();
			if (item.getLayerId() == id) {
				item.recycle(); it.remove();
			}
		}
		Bitmap targetBmp;
		if (loadedBmp.getWidth() != logicalCanvasWidth || loadedBmp.getHeight() != logicalCanvasHeight) {
			targetBmp = Bitmap.createScaledBitmap(loadedBmp, logicalCanvasWidth, logicalCanvasHeight, true);
			loadedBmp.recycle();
		} else {
			targetBmp = loadedBmp;
		}
		Bitmap liveBuffer = targetBmp.copy(Bitmap.Config.ARGB_8888, true);
		layerBuffers.put(id, liveBuffer);
		Bitmap snapshot = targetBmp.copy(Bitmap.Config.ARGB_8888, false);
		drawHistory.add(new FillItem(id, snapshot));
		layerVisibility.put(id, visible);
		layerLocked.put(id, locked);
		try {
			layerBlendModes.put(id, PorterDuff.Mode.valueOf(blendModeName));
		} catch (Exception e) {
			layerBlendModes.put(id, PorterDuff.Mode.SRC_OVER);
		}
		if (!layerDrawingOrder.contains(id)) { layerDrawingOrder.add(id); }
	}
	public Bitmap getLayerThumbnail(int layerId, int thumbW, int thumbH) {
		Bitmap layerBmp = layerBuffers.get(layerId);
		if (layerBmp == null || layerBmp.isRecycled()) return null;
		Bitmap thumb = Bitmap.createBitmap(thumbW, thumbH, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(thumb);
		float scaleX = (float) thumbW / layerBmp.getWidth();
		float scaleY = (float) thumbH / layerBmp.getHeight();
		previewMatrix.reset();
		previewMatrix.setScale(scaleX, scaleY);
		canvas.drawBitmap(layerBmp, previewMatrix, previewPaint); return thumb;
	}

	private void reconstructLayerBuffer(int layerId) {
		Bitmap buffer = layerBuffers.get(layerId);
		if (buffer == null || buffer.isRecycled() ||
				buffer.getWidth() != logicalCanvasWidth ||
				buffer.getHeight() != logicalCanvasHeight) {
			if (buffer != null) buffer.recycle();
			buffer = Bitmap.createBitmap(logicalCanvasWidth, logicalCanvasHeight, Bitmap.Config.ARGB_8888);
			layerBuffers.put(layerId, buffer);
		}
		buffer.eraseColor(Color.TRANSPARENT);
		Canvas bufferCanvas = new Canvas(buffer);
		for (DrawItem item : drawHistory) {
			if (item.getLayerId() == layerId) { item.draw(bufferCanvas); }
		}
	}
	private PorterDuffXfermode getCachedXfermode(PorterDuff.Mode mode) {
		int key = mode.ordinal();
		PorterDuffXfermode xfermode = xfermodeCache.get(key);
		if (xfermode == null) {
			xfermode = new PorterDuffXfermode(mode); xfermodeCache.put(key, xfermode);
		} return xfermode;
	}
	private void ensureLayerBuffer(int layerId) {
		Bitmap buffer = layerBuffers.get(layerId);
		if (buffer == null || buffer.isRecycled()) {
			initializeLayerBuffer(layerId);
			reconstructLayerBuffer(layerId);
		}
	}

	public void setCanvasDimensionsNoScale(int newWidth, int newHeight) {
		this.logicalCanvasWidth = newWidth; this.logicalCanvasHeight = newHeight;
	}

	public static class LayerStateItem implements DrawItem {
		private final int layerId;
		private final Bitmap snapshot;
		private final boolean visible;
		private final boolean locked;
		private final PorterDuff.Mode blendMode;
		private final int originalIndex;

		public LayerStateItem(int id, Bitmap bmp, boolean vis, boolean lock, PorterDuff.Mode mode, int index) {
			this.layerId = id;
			this.snapshot = bmp;
			this.visible = vis;
			this.locked = lock;
			this.blendMode = mode;
			this.originalIndex = index;
		}

		@Override public int getLayerId() { return layerId; }
		@Override
		public void draw(Canvas canvas) {
			if (snapshot != null && !snapshot.isRecycled()) {
				canvas.drawBitmap(snapshot, 0, 0, null);
			}
		}
		@Override public void scale(float sx, float sy) { }
		@Override public void recycle() { if (snapshot != null) snapshot.recycle(); }

		public Bitmap getSnapshot() { return snapshot; }
		public boolean isVisible() { return visible; }
		public boolean isLocked() { return locked; }
		public PorterDuff.Mode getBlendMode() { return blendMode; }
		public int getOriginalIndex() { return originalIndex; }
	}
}
