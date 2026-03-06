package com.inflps.pcd.CORE.BRUSH_CORE;

import android.graphics.Bitmap;
import android.graphics.PorterDuff;

public class BrushSettings {
    public String name = "Default Brush";// Default brush name (If config.json is missing)
    public float spacing = 0.1f;         // Percentage of size (0.1 = 10%)
    public float stampAngle = 0f;        // Initial rotation
    public boolean followPath = true;    // Rotates with stroke direction
    public float minOpacity = 1.0f;      // Base opacity
    public float fallout = 0.0f;         // How much it fades/shrinks at the end
    public String blendMode = "SRC_OVER";
    
    public float scatterJitter = 0.0f;   // Random displacement from the path
    public float angleJitter = 0.0f;     // Random rotation per stamp
    public float sizeJitter = 0.0f;      // Random size variation per stamp
    public float colorJitter = 0.0f;     // Random lightness/hue variation

    public transient Bitmap texture;     // The actual texture pattern (not stored in JSON, loaded from PNG - texture.png)

    public PorterDuff.Mode getBlendMode() {
        try { return PorterDuff.Mode.valueOf(blendMode); }
        catch (Exception e) { return PorterDuff.Mode.SRC_OVER; }
    }
}
