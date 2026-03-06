package com.inflps.pcd.CORE.BRUSH_CORE.ADAPTER;

import android.graphics.Bitmap;

public class BrushItem {
	
	public enum Type {
		IMPORT,
		DEFAULT_BRUSH,
		BRUSH
	}
	
	public final String name;
	public final String filePath;
	public final Bitmap thumbnail;
	public final Type type;
	
	private BrushItem(String name, String filePath, Bitmap thumbnail, Type type) {
		this.name = name;
		this.filePath = filePath;
		this.thumbnail = thumbnail;
		this.type = type;
	}
	
	public static BrushItem importItem() {
		return new BrushItem("Import", null, null, Type.IMPORT);
	}
	
	public static BrushItem defaultBrush(String name, Bitmap thumbnail) {
		return new BrushItem(name, "internal_default", thumbnail, Type.DEFAULT_BRUSH);
	}
	
	public static BrushItem brush(String name, String filePath, Bitmap thumbnail) {
		return new BrushItem(name, filePath, thumbnail, Type.BRUSH);
	}
	
	public BrushItem(String name, String filePath, Bitmap thumbnail, boolean isImport) {
		this(
		name,
		filePath,
		thumbnail,
		isImport ? Type.IMPORT : Type.BRUSH
		);
	}
	
	public BrushItem(String name, String filePath, Bitmap thumbnail, boolean isImport, boolean isDefault) {
		this(
		name,
		filePath,
		thumbnail,
		isDefault ? Type.DEFAULT_BRUSH :
		isImport ? Type.IMPORT : Type.BRUSH
		);
	}
}
