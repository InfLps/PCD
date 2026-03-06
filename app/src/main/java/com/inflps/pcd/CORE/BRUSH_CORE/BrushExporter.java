package com.inflps.pcd.CORE.BRUSH_CORE;

import android.content.Context;
import android.graphics.Bitmap;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BrushExporter {
    public static void saveBrush(Context ctx, BrushSettings s, Bitmap tex, File out) {
        try (FileOutputStream fos = new FileOutputStream(out)) {
            saveBrushToStream(s, tex, fos);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void saveBrushToStream(BrushSettings s, Bitmap tex, java.io.OutputStream os) {
        try (ZipOutputStream zos = new ZipOutputStream(os)) {
            zos.putNextEntry(new ZipEntry("config.json"));
            JSONObject json = new JSONObject();
            json.put("name", s.name);
            json.put("spacing", s.spacing);
            json.put("stampAngle", s.stampAngle);
            json.put("followPath", s.followPath);
            json.put("scatterJitter", s.scatterJitter);
            json.put("angleJitter", s.angleJitter);
            json.put("sizeJitter", s.sizeJitter);
            zos.write(json.toString().getBytes());
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("texture.png"));
            tex.compress(Bitmap.CompressFormat.PNG, 100, zos);
            zos.closeEntry();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
