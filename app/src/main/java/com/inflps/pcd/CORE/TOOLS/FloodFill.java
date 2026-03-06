package com.inflps.pcd.CORE.TOOLS;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import java.util.LinkedList;
import java.util.Queue;

public class FloodFill {
	public static Bitmap perform(Bitmap source, int x, int y, int targetColor, int newColor, int tolerance) {
		if (targetColor == newColor) return null;
		int width = source.getWidth();
		int height = source.getHeight();
		Bitmap resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		int[] pixels = new int[width * height];
		source.getPixels(pixels, 0, width, 0, 0, width, height);
		int[] resultPixels = new int[width * height];
		int startPixelIndex = y * width + x;
		int startColor = pixels[startPixelIndex];
		int threshold = (int) (1020 * (tolerance / 100.0f));
		
		Queue<Point> queue = new LinkedList<>();
		queue.add(new Point(x, y));
		
		boolean[] visited = new boolean[width * height];
		
		while (!queue.isEmpty()) {
			Point p = queue.remove();
			int px = p.x;
			int py = p.y;
			int index = py * width + px;
			
			if (index < 0 || index >= pixels.length || visited[index]) continue;
			if (!checkPixel(pixels[index], startColor, threshold)) continue;
			
			int w = px;
			while (w > 0) {
				int leftIdx = py * width + (w - 1);
				if (!visited[leftIdx] && checkPixel(pixels[leftIdx], startColor, threshold)) {
					w--;
				} else {
					break;
				}
			}
			
			int e = px;
			while (e < width - 1) {
				int rightIdx = py * width + (e + 1);
				if (!visited[rightIdx] && checkPixel(pixels[rightIdx], startColor, threshold)) {
					e++;
				} else {
					break;
				}
			}
			
			for (int i = w; i <= e; i++) {
				int idx = py * width + i;
				
				visited[idx] = true;
				resultPixels[idx] = newColor;
				
				if (py > 0) {
					int upIdx = (py - 1) * width + i;
					if (!visited[upIdx] && checkPixel(pixels[upIdx], startColor, threshold)) {
						boolean prevWasMatch = (i > w) && !visited[(py - 1) * width + (i - 1)] && checkPixel(pixels[(py - 1) * width + (i - 1)], startColor, threshold);
						if (!prevWasMatch) queue.add(new Point(i, py - 1));
					}
				}
				
				if (py < height - 1) {
					int downIdx = (py + 1) * width + i;
					if (!visited[downIdx] && checkPixel(pixels[downIdx], startColor, threshold)) {
						boolean prevWasMatch = (i > w) && !visited[(py + 1) * width + (i - 1)] && checkPixel(pixels[(py + 1) * width + (i - 1)], startColor, threshold);
						if (!prevWasMatch) queue.add(new Point(i, py + 1));
					}
				}
			}
		}
		
		resultBitmap.setPixels(resultPixels, 0, width, 0, 0, width, height);
		return resultBitmap;
	}
	
	private static boolean checkPixel(int color1, int color2, int threshold) {
		if (threshold == 0) return color1 == color2;
		int a = Color.alpha(color1) - Color.alpha(color2);
		int r = Color.red(color1) - Color.red(color2);
		int g = Color.green(color1) - Color.green(color2);
		int b = Color.blue(color1) - Color.blue(color2);
		int diff = Math.abs(a) + Math.abs(r) + Math.abs(g) + Math.abs(b);
		return diff <= threshold;
	}
	
}
