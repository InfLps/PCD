package com.inflps.pcd.CORE.BRUSH_CORE;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class BrushArchiveLoader {

    public interface BrushLoadCallback {
        void onSuccess(BrushSettings settings);
        void onError(Exception e);
    }

    public static void loadFromArchive(File archiveFile, BrushLoadCallback callback) {
        new Thread(() -> {
            try {
                BrushSettings settings = loadFromArchiveSync(archiveFile);
                if (settings != null) {
                    callback.onSuccess(settings);
                } else {
                    callback.onError(new Exception("Failed to load brush: texture missing."));
                }
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    public static BrushSettings loadFromArchiveSync(File archiveFile) {
        try (FileInputStream fis = new FileInputStream(archiveFile);
             ZipInputStream zis = new ZipInputStream(fis)) {
            
            BrushSettings settings = null;
            Bitmap texture = null;
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                
                if (name.endsWith("config.json")) {
                    settings = parseJson(zis);
                } else if (name.endsWith("texture.png")) {
                    texture = BitmapFactory.decodeStream(zis);
                }
                zis.closeEntry();
            }

            if (texture != null) {
                if (settings == null) settings = new BrushSettings();
                settings.texture = texture;
                return settings;
            }
        } catch (Exception e) {
            Log.e("BrushLoader", "Sync load failed for: " + archiveFile.getName(), e);
        }
        return null;
    }

    private static BrushSettings parseJson(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        
        JSONObject json = new JSONObject(sb.toString());
        BrushSettings s = new BrushSettings();
        s.name = json.optString("name", "Custom Brush");
        s.spacing = (float) json.optDouble("spacing", 0.1);
        s.stampAngle = (float) json.optDouble("stampAngle", 0.0);
        s.followPath = json.optBoolean("followPath", true);
        s.minOpacity = (float) json.optDouble("minOpacity", 1.0);
        s.fallout = (float) json.optDouble("fallout", 0.0);
        s.scatterJitter = (float) json.optDouble("scatterJitter", 0.0);
        s.angleJitter = (float) json.optDouble("angleJitter", 0.0);
        s.sizeJitter = (float) json.optDouble("sizeJitter", 0.0);
        s.colorJitter = (float) json.optDouble("colorJitter", 0.0);
        s.blendMode = json.optString("blendMode", "SRC_OVER");
        return s;
    }
}
