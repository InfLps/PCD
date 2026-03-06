package com.inflps.pcd.CORE.BRUSH_CORE;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import java.util.Random;

public class BrushEngine {
    
    private float lastX, lastY;
    private long lastTime;
    private float distanceRemainder;
    private float smoothedVelocity;
    private boolean firstSegment;
    private float lastDx, lastDy;
    private final RectF stampRect = new RectF();
    private final Random random = new Random();
    private final float[] hsv = new float[3];
    private final Paint internalStampPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);

    public void startStroke(float x, float y) {
        lastX = x;
        lastY = y;
        lastTime = System.currentTimeMillis();
        distanceRemainder = 0f;
        lastDx = 0f;
        lastDy = 0f;
        smoothedVelocity = 0f;
        firstSegment = true;
        random.setSeed(42); 
    }

    public void strokeTo(Canvas canvas, Paint userPaint, BrushSettings brush, float baseSize, float x, float y) {
        if (brush == null || brush.texture == null) return;

        float dx = x - lastX;
        float dy = y - lastY;
        float dist = (float) Math.hypot(dx, dy);

        if (dist < 0.1f) return;

        long now = System.currentTimeMillis();
        long dt = Math.max(1, now - lastTime);
        lastDx = dx;
        lastDy = dy;

        float velocity = dist / dt;
        if (firstSegment) {
            smoothedVelocity = velocity;
            firstSegment = false;
        } else {
            smoothedVelocity = smoothedVelocity * 0.8f + velocity * 0.2f;
        }

        float currentSize = baseSize;
        if (brush.fallout > 0f) {
            currentSize = baseSize / (1f + smoothedVelocity * 0.05f);
            currentSize = Math.max(baseSize * 0.15f, currentSize);
        }

        float stepDenominator = (canvas.getWidth() > 2500) ? 1.5f : 1.0f;
        float step = Math.max(0.4f, (baseSize * brush.spacing) / stepDenominator);

        float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
        float traveled = step - distanceRemainder;
        internalStampPaint.setAlpha(userPaint.getAlpha());
        int baseColor = userPaint.getColor();

        while (traveled <= dist) {
            float t = traveled / dist;
            float sx = lastX + dx * t;
            float sy = lastY + dy * t;

            drawStamp(canvas, internalStampPaint, baseColor, brush, currentSize, sx, sy, angle);
            traveled += step;
        }

        distanceRemainder = Math.max(0, dist - (traveled - step));
        lastX = x;
        lastY = y;
        lastTime = now;
    }

    public void endStroke(Canvas canvas, Paint userPaint, BrushSettings brush, float baseSize) {
        if (brush == null || brush.fallout <= 0f) return;
        if (lastDx == 0f && lastDy == 0f) return;

        float dist = (float) Math.hypot(lastDx, lastDy);
        if (dist < 0.01f) return;

        float nx = lastDx / dist;
        float ny = lastDy / dist;
        float size = baseSize;
        float x = lastX;
        float y = lastY;

        internalStampPaint.setAlpha(userPaint.getAlpha());
        int baseColor = userPaint.getColor();

        for (int i = 0; i < 8; i++) {
            size *= (1f - brush.fallout);
            if (size < 0.5f) break;
            float step = size * 0.4f;
            x += nx * step;
            y += ny * step;
            drawStamp(canvas, internalStampPaint, baseColor, brush, size, x, y, 0f);
        }
    }

    private void drawStamp(Canvas canvas, Paint paint, int baseColor, BrushSettings brush, float size, float x, float y, float pathAngle) {
        float finalSize = size;
        if (brush.sizeJitter > 0f) {
            finalSize *= (1f - random.nextFloat() * brush.sizeJitter);
        }

        float sx = x;
        float sy = y;
        if (brush.scatterJitter > 0f) {
            float jitter = finalSize * brush.scatterJitter;
            sx += (random.nextFloat() - 0.5f) * jitter;
            sy += (random.nextFloat() - 0.5f) * jitter;
        }

        float rotation = brush.stampAngle;
        if (brush.followPath) rotation += pathAngle;
        if (brush.angleJitter > 0f) {
            rotation += (random.nextFloat() - 0.5f) * 360f * brush.angleJitter;
        }

        int opaqueColor = (baseColor | 0xFF000000);
        if (brush.colorJitter > 0f) {
            Color.colorToHSV(opaqueColor, hsv);
            hsv[2] *= (1f - random.nextFloat() * brush.colorJitter);
            int jittered = Color.HSVToColor(255, hsv);
            paint.setColorFilter(new PorterDuffColorFilter(jittered, PorterDuff.Mode.SRC_IN));
        } else {
            paint.setColorFilter(new PorterDuffColorFilter(opaqueColor, PorterDuff.Mode.SRC_IN));
        }

        canvas.save();
        canvas.translate(sx, sy);
        canvas.rotate(rotation);
        
        float halfSize = finalSize / 2f;
        stampRect.set(-halfSize, -halfSize, halfSize, halfSize);
        
        canvas.drawBitmap(brush.texture, null, stampRect, paint);
        
        canvas.restore();
    }
}
