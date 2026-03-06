package com.inflps.pcd.CORE.FONTS.ADAPTER;

import android.graphics.Typeface;

public class FontItem {
    public String fontName;
    public String filePath;
    public boolean isImportButton;
    public transient Typeface typeface; 

    public FontItem(String name, String path, Typeface tf) {
        this.fontName = name;
        this.filePath = path;
        this.typeface = tf;
        this.isImportButton = false;
    }

    public FontItem(boolean isImportButton) {
        this.isImportButton = isImportButton;
    }
}
