package com.inflps.pcd.CORE.PROJECT_CORE;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import com.google.gson.Gson;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.inflps.pcd.CORE.LAYERS.LayerManager;

public class ProjectSerializer {
	public static void saveToPcdProj(Context context, String filePath, LayerManager lm, int width, int height, int color) throws Exception {
		ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(filePath)));
		ProjectManifest manifest = new ProjectManifest();
		manifest.width = width;
		manifest.height = height;
        manifest.color = color;
		manifest.currentLayerId = lm.getCurrentLayerId();
		
		int thumbSize = 512;
		float scale = Math.min((float) thumbSize / width, (float) thumbSize / height);
		int thumbW = (int) (width * scale);
		int thumbH = (int) (height * scale);
		
		Bitmap thumbnail = Bitmap.createBitmap(thumbW, thumbH, Bitmap.Config.ARGB_8888);
		Canvas thumbCanvas = new Canvas(thumbnail);
		
		thumbCanvas.drawColor(android.graphics.Color.WHITE); 
		
		for (int id : lm.getLayerDrawingOrder()) {
			Bitmap bmp = lm.getLayerBitmap(id);
			if (bmp == null) continue;
			
			zos.putNextEntry(new ZipEntry("layer_" + id + ".png"));
			bmp.compress(Bitmap.CompressFormat.PNG, 100, zos);
			zos.closeEntry();
	
			if (lm.isLayerVisible(id)) {
				Paint blendPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
				PorterDuff.Mode mode = lm.getLayerBlendMode(id);
				if (mode != PorterDuff.Mode.SRC_OVER) {
					blendPaint.setXfermode(new android.graphics.PorterDuffXfermode(mode));
				}
				
				Rect src = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
				RectF dst = new RectF(0, 0, thumbW, thumbH);
				thumbCanvas.drawBitmap(bmp, src, dst, blendPaint);
			}
			
			ProjectManifest.LayerMeta meta = new ProjectManifest.LayerMeta();
			meta.id = id;
			meta.isVisible = lm.isLayerVisible(id);
			meta.isLocked = lm.isLayerLocked(id);
			meta.blendMode = lm.getLayerBlendMode(id).name();
			manifest.layers.add(meta);
		}
		
		zos.putNextEntry(new ZipEntry("thumbnail.png"));
		thumbnail.compress(Bitmap.CompressFormat.JPEG, 85, zos);
		zos.closeEntry();
		thumbnail.recycle(); 
		zos.putNextEntry(new ZipEntry("manifest.json"));
		String json = new Gson().toJson(manifest);
		zos.write(json.getBytes());
		zos.closeEntry();
		zos.close();
	}
	
	public static ProjectManifest loadFromPcdProj(String filePath, LayerManager lm) throws Exception {
		ZipFile zipFile = new ZipFile(filePath);
		InputStream is = zipFile.getInputStream(zipFile.getEntry("manifest.json"));
		String json = new Scanner(is).useDelimiter("\\A").next();
		ProjectManifest manifest = new Gson().fromJson(json, ProjectManifest.class);
		lm.clearAllLayers(); 
		lm.setCanvasDimensions(manifest.width, manifest.height);
		
		ArrayList<Integer> newOrder = new ArrayList<>();
		for (ProjectManifest.LayerMeta meta : manifest.layers) {
			newOrder.add(meta.id);
		}
		lm.setLayerDrawingOrder(newOrder);
		
		for (ProjectManifest.LayerMeta meta : manifest.layers) {
			ZipEntry imgEntry = zipFile.getEntry("layer_" + meta.id + ".png");
			if (imgEntry != null) {
				InputStream imgStream = zipFile.getInputStream(imgEntry);
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inMutable = true; 
				Bitmap bmp = BitmapFactory.decodeStream(imgStream, null, options);
				
				lm.restoreLayer(meta.id, bmp, meta.isVisible, meta.isLocked, meta.blendMode);
			}
		}
		
		lm.setCurrentLayer(manifest.currentLayerId);
		lm.notifyLayerDataChanged(); 
		zipFile.close();
		return manifest;
	}
	
	public static void saveAutosaveBitmaps(Context context, String filePath, List<Bitmap> snapshots, List<Integer> order, int width, int height, int color, int currentLayerId) throws Exception {
		ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(filePath)));
		ProjectManifest manifest = new ProjectManifest();
		manifest.width = width;
		manifest.height = height;
        manifest.color = color;
		manifest.currentLayerId = currentLayerId;
		
		for (int i = 0; i < snapshots.size(); i++) {
			Bitmap bmp = snapshots.get(i);
			int layerId = order.get(i);
			zos.putNextEntry(new ZipEntry("layer_" + layerId + ".png"));
			bmp.compress(Bitmap.CompressFormat.PNG, 100, zos);
			zos.closeEntry();
			ProjectManifest.LayerMeta meta = new ProjectManifest.LayerMeta();
			meta.id = layerId;
			meta.isVisible = true;
			meta.isLocked = false;
			meta.blendMode = "SRC_OVER";
			manifest.layers.add(meta);
		}
		zos.putNextEntry(new ZipEntry("manifest.json"));
		String json = new Gson().toJson(manifest);
		zos.write(json.getBytes());
		zos.closeEntry();
		
		zos.close();
	}
	
	public interface LayerChangeListener {void onLayersChanged();}
	private LayerChangeListener listener;
	public void setLayerChangeListener(LayerChangeListener l) { this.listener = l; }
	private void notifyChange() {if (listener != null) listener.onLayersChanged();}
}
