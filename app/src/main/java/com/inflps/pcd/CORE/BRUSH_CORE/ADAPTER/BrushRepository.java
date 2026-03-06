package com.inflps.pcd.CORE.BRUSH_CORE.ADAPTER;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.Log;

import com.inflps.pcd.CORE.BRUSH_CORE.BrushArchiveLoader;
import com.inflps.pcd.CORE.BRUSH_CORE.BrushEngine;
import com.inflps.pcd.CORE.BRUSH_CORE.BrushSettings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class BrushRepository {
	
	private static final String TAG = "BrushRepository";
	private static final String BRUSH_FOLDER = "Brushes";
	
	private static final int THUMB_WIDTH = 150;
	private static final int THUMB_HEIGHT = 100;
	private static final float THUMB_STROKE_WIDTH = 12f;
	private static final float ENGINE_SIZE = 18f;
	
	private BrushRepository() { }
	
	public static List<BrushItem> loadBrushesFromStorage(Context context) {
		List<BrushItem> brushes = new ArrayList<>();
		brushes.add(new BrushItem("Import", null, null, true));
		Bitmap defaultThumb = generateDefaultThumbnail(Paint.Cap.ROUND);
		brushes.add(new BrushItem(
		"Default Pen",
		"internal_default",
		defaultThumb,
		false,
		true
		));
		
		File brushDir = resolveBrushDirectory(context);
		if (brushDir == null) return brushes;
		
		File[] files = brushDir.listFiles((dir, name) -> {
			String lower = name.toLowerCase();
			return lower.endsWith(".brush") || lower.endsWith(".png");
		});
		
		if (files == null) return brushes;
		
		for (File file : files) {
			try {
				addBrushFromFile(file, brushes);
			} catch (Exception e) {
				Log.e(TAG, "Failed to load brush: " + file.getName(), e);
			}
		}
		
		return brushes;
	}
	
	private static File resolveBrushDirectory(Context context) {
		File dir = new File(context.getExternalFilesDir(null), BRUSH_FOLDER);
		if (!dir.exists() && !dir.mkdirs()) {
			Log.e(TAG, "Could not create brush directory");
			return null;
		}
		return dir;
	}
	
	private static void addBrushFromFile(File file, List<BrushItem> list) {
		String fallbackName = stripExtension(file.getName());
		BrushSettings settings = loadBrushSettings(file, fallbackName);
		
		if (settings == null || settings.texture == null) return;
		
		String internalName = settings.name;
		if (internalName == null || internalName.isEmpty()) {
			internalName = fallbackName;
		}
		
		Bitmap thumbnail = generateBrushThumbnail(settings);
		list.add(new BrushItem(internalName, file.getAbsolutePath(), thumbnail, false));
	}
	
	private static BrushSettings loadBrushSettings(File file, String name) {
		if (file.getName().toLowerCase().endsWith(".brush")) {
			return BrushArchiveLoader.loadFromArchiveSync(file);
		}
		
		Bitmap texture = BitmapFactory.decodeFile(file.getAbsolutePath());
		if (texture == null) return null;
		
		BrushSettings settings = new BrushSettings();
		settings.name = name;
		settings.texture = texture;
		return settings;
	}
	
	private static String stripExtension(String name) {
		return name.replaceFirst("(?i)\\.(png|brush)$", "");
	}
	
	public static Bitmap generateDefaultThumbnail(Paint.Cap cap) {
		Bitmap bmp = createBlankThumbnail();
		Canvas canvas = new Canvas(bmp);
		
		Paint paint = createBasePaint(cap);
		canvas.drawPath(createThumbnailPath(), paint);
		
		return bmp;
	}
	
	public static Bitmap generateBrushThumbnail(BrushSettings settings) {
		Bitmap bmp = createBlankThumbnail();
		Canvas canvas = new Canvas(bmp);
		
		Paint paint = createBasePaint(Paint.Cap.ROUND);
		Path path = createThumbnailPath();
		
		renderPathWithBrushEngine(canvas, paint, path, settings);
		return bmp;
	}
	
	public static Bitmap createBlankThumbnail() {
		return Bitmap.createBitmap(
		THUMB_WIDTH,
		THUMB_HEIGHT,
		Bitmap.Config.ARGB_8888
		);
	}
	
	private static Paint createBasePaint(Paint.Cap cap) {
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(THUMB_STROKE_WIDTH);
		paint.setStrokeCap(cap);
		return paint;
	}
	
	private static Path createThumbnailPath() {
		Path path = new Path();
		path.moveTo(20, THUMB_HEIGHT / 2f + 10);
		path.cubicTo(
		THUMB_WIDTH / 3f, 10,
		2f * THUMB_WIDTH / 3f, THUMB_HEIGHT - 10,
		THUMB_WIDTH - 20, THUMB_HEIGHT / 2f - 10
		);
		return path;
	}
	
	private static void renderPathWithBrushEngine(
	Canvas canvas,
	Paint paint,
	Path path,
	BrushSettings settings
	) {
		BrushEngine engine = new BrushEngine();
		PathMeasure pm = new PathMeasure(path, false);
		
		float[] pos = new float[2];
		float step = 6f;
		
		pm.getPosTan(0, pos, null);
		engine.startStroke(pos[0], pos[1]);
		
		for (float d = step; d <= pm.getLength(); d += step) {
			pm.getPosTan(d, pos, null);
			engine.strokeTo(canvas, paint, settings, ENGINE_SIZE, pos[0], pos[1]);
		}
	}
	
	public static void installDefaultBrushes(Context context) {
		File targetDir = resolveBrushDirectory(context);
		if (targetDir == null) return;
		
		try {
			String[] assets = context.getAssets().list("Brushes");
			if (assets == null) return;
			
			for (String filename : assets) {
				File outFile = new File(targetDir, filename);
				
				if (!outFile.exists()) {
					copyAssetToFile(context, "Brushes/" + filename, outFile);
				}
			}
		} catch (java.io.IOException e) {
			Log.e("BrushRepository", "Failed to copy assets", e);
		}
	}
	
	private static void copyAssetToFile(Context context, String assetPath, File outFile) throws java.io.IOException {
		try (java.io.InputStream in = context.getAssets().open(assetPath);
		java.io.OutputStream out = new java.io.FileOutputStream(outFile)) {
			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
		}
	}
	
	@Deprecated
	public static Bitmap generateSineThumbnail(BrushSettings settings) {
		return generateBrushThumbnail(settings);
	}
}
