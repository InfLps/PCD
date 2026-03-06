package com.inflps.pcd.CORE.DRAWING_CORE;

import android.graphics.Path;
import android.graphics.RectF;

public class ShapeFactory {
	public static Path createShapePath(float startX, float startY, float endX, float endY, DrawingState.ShapeType type) {
		Path shapePath = new Path();
		float left = Math.min(startX, endX);
		float top = Math.min(startY, endY);
		float right = Math.max(startX, endX);
		float bottom = Math.max(startY, endY);
		
		RectF bounds = new RectF(left, top, right, bottom);
		float width = bounds.width();
		float height = bounds.height();
		float cx = bounds.centerX();
		float cy = bounds.centerY();
		
		shapePath.setFillType(Path.FillType.EVEN_ODD);
		
		switch (type) {
			case RECTANGLE:
			shapePath.addRect(bounds, Path.Direction.CW);
			break;
			case CIRCLE:
			shapePath.addOval(bounds, Path.Direction.CW);
			break;
			case LINE:
			shapePath.moveTo(startX, startY);
			shapePath.lineTo(endX, endY);
			break;
			case CAPSULE:
			float capsuleRadius = Math.min(width, height) / 2f;
			shapePath.addRoundRect(bounds, capsuleRadius, capsuleRadius, Path.Direction.CW);
			break;
			case TRIANGLE:
			shapePath.moveTo(cx, top);
			shapePath.lineTo(right, bottom);
			shapePath.lineTo(left, bottom);
			shapePath.close();
			break;
			case RIGHT_TRIANGLE:
			shapePath.moveTo(left, top);
			shapePath.lineTo(left, bottom);
			shapePath.lineTo(right, bottom);
			shapePath.close();
			break;
			case PENTAGON:
			addRegularPolygon(shapePath, bounds, 5);
			break;
			case HEXAGON:
			addRegularPolygon(shapePath, bounds, 6);
			break;
			case HEPTAGON:
			addRegularPolygon(shapePath, bounds, 7);
			break;
			case OCTAGON:
			addRegularPolygon(shapePath, bounds, 8);
			break;
			case DECAGON:
			addRegularPolygon(shapePath, bounds, 10);
			break;
			case STAR_3:
			addStar(shapePath, bounds, 3, 0.4f);
			break;
			case STAR_4:
			addStar(shapePath, bounds, 4, 0.4f);
			break;
			case STAR:
			addStar(shapePath, bounds, 5, 0.4f);
			break;
			case STAR_6:
			addStar(shapePath, bounds, 6, 0.4f);
			break;
			case GEAR:
			addGear(shapePath, bounds, 8);
			break;
			case DIAMOND:
			shapePath.moveTo(cx, top);
			shapePath.lineTo(right, cy);
			shapePath.lineTo(cx, bottom);
			shapePath.lineTo(left, cy);
			shapePath.close();
			break;
			case TRAPEZOID:
			float trapInset = width * 0.2f;
			shapePath.moveTo(left + trapInset, top);
			shapePath.lineTo(right - trapInset, top);
			shapePath.lineTo(right, bottom);
			shapePath.lineTo(left, bottom);
			shapePath.close();
			break;
			case PARALLELOGRAM:
			float slant = width * 0.25f;
			shapePath.moveTo(left + slant, top);
			shapePath.lineTo(right, top);
			shapePath.lineTo(right - slant, bottom);
			shapePath.lineTo(left, bottom);
			shapePath.close();
			break;
			case KITE:
			shapePath.moveTo(cx, top);
			shapePath.lineTo(right, top + height * 0.35f);
			shapePath.lineTo(cx, bottom);
			shapePath.lineTo(left, top + height * 0.35f);
			shapePath.close();
			break;
			case CHEVRON:
			float dip = height * 0.3f;
			shapePath.moveTo(left, top);
			shapePath.lineTo(cx, top + dip);
			shapePath.lineTo(right, top);
			shapePath.lineTo(right, bottom);
			shapePath.lineTo(cx, bottom + dip);
			shapePath.lineTo(left, bottom);
			shapePath.close();
			break;
			case PLUS:
			addPlusSign(shapePath, bounds);
			break;
			case ARROW:
			addArrow(shapePath, bounds);
			break;
			case SPEECH_BUBBLE:
			addSpeechBubble(shapePath, bounds);
			break;
			case THINKING_BUBBLE:
			addThinkingBubble(shapePath, bounds);
			break;
			case HEART:
			addHeart(shapePath, bounds);
			break;
			case CLOUD:
				float section = width / 4f;
				shapePath.moveTo(left + section, bottom - height * 0.2f);
				shapePath.quadTo(bounds.centerX(), bottom, right - section, bottom - height * 0.2f);
				shapePath.arcTo(new RectF(right - section * 1.5f, bottom - height * 0.6f, right, bottom), 0, -180, false);
				shapePath.arcTo(new RectF(left + section, top, right - section, top + height * 0.8f), 0, -180, false);
				shapePath.arcTo(new RectF(left, bottom - height * 0.6f, left + section * 1.5f, bottom), 0, -180, false);
				shapePath.close();
			break;
			case SHIELD:
			addShield(shapePath, bounds);
			break;
			case LIGHTNING:
			addLightning(shapePath, bounds);
			break;
			case L_SHAPE:
			addLShape(shapePath, bounds);
			break;
			case TAG:
			addTag(shapePath, bounds);
			break;
			case CUBE:
			addCube(shapePath, bounds);
			break;
			case CYLINDER:
							float eH = height * 0.2f;
				shapePath.addOval(left, top, right, top + eH, Path.Direction.CW);
				shapePath.addArc(new RectF(left, bottom - eH, right, bottom), 0, 180);
				shapePath.moveTo(left, top + eH / 2f);
				shapePath.lineTo(left, bottom - eH / 2f);
				shapePath.moveTo(right, top + eH / 2f);
				shapePath.lineTo(right, bottom - eH / 2f);
			break;
			case CONE:
			addCone(shapePath, bounds);
			break;
			case PYRAMID:
			addPyramid(shapePath, bounds);
			break;
			case PRISM_TRIANGULAR:
			addTriangularPrism(shapePath, bounds);
			break;
			case TETRAHEDRON:
			addTetrahedron(shapePath, bounds);
			break;
			case OCTAHEDRON:
			addOctahedron(shapePath, bounds);
			break;
			case ICOSAHEDRON:
			addIcosahedron(shapePath, bounds);
			break;
			case SEMICIRCLE:
			shapePath.addArc(bounds, 180, 180);
			shapePath.close();
			break;
			case CRESCENT:
			addCrescent(shapePath, bounds);
			break;
			case PIE_SLICE:
			shapePath.moveTo(cx, cy);
			shapePath.arcTo(bounds, 30, 300, false);
			shapePath.close();
			break;
			case COIL:
			addCoil(shapePath, bounds);
			break;
			case TREFOIL:
			addTrefoil(shapePath, bounds);
			break;
		}
		return shapePath;
	}
	
	private static void addRegularPolygon(Path path, RectF bounds, int sides) {
		float cx = bounds.centerX();
		float cy = bounds.centerY();
		float radius = Math.min(bounds.width(), bounds.height()) / 2f;
		
		for (int i = 0; i < sides; i++) {
			float angle = (float) (i * 2 * Math.PI / sides - Math.PI / 2);
			float x = cx + radius * (float) Math.cos(angle);
			float y = cy + radius * (float) Math.sin(angle);
			if (i == 0) path.moveTo(x, y);
			else path.lineTo(x, y);
		}
		path.close();
	}
	
	private static void addStar(Path path, RectF bounds, int points, float innerRatio) {
		float cx = bounds.centerX();
		float cy = bounds.centerY();
		float outerR = Math.min(bounds.width(), bounds.height()) / 2f;
		float innerR = outerR * innerRatio;
		
		for (int i = 0; i < points * 2; i++) {
			float r = (i % 2 == 0) ? outerR : innerR;
			float angle = (float) (i * Math.PI / points - Math.PI / 2);
			float x = cx + r * (float) Math.cos(angle);
			float y = cy + r * (float) Math.sin(angle);
			if (i == 0) path.moveTo(x, y);
			else path.lineTo(x, y);
		}
		path.close();
	}
	
	private static void addGear(Path path, RectF bounds, int teeth) {
		float cx = bounds.centerX();
		float cy = bounds.centerY();
		float outerR = Math.min(bounds.width(), bounds.height()) / 2f;
		float innerR = outerR * 0.8f;
		
		for (int i = 0; i < teeth * 2; i++) {
			float angle = (float) (i * Math.PI / teeth);
			float r = (i % 2 == 0) ? outerR : innerR;
			float x = cx + r * (float) Math.cos(angle);
			float y = cy + r * (float) Math.sin(angle);
			if (i == 0) path.moveTo(x, y);
			else path.lineTo(x, y);
		}
		path.close();
		path.addCircle(cx, cy, outerR * 0.3f, Path.Direction.CCW);
	}
	
	private static void addPlusSign(Path path, RectF bounds) {
		float cx = bounds.centerX();
		float cy = bounds.centerY();
		float thickness = Math.min(bounds.width(), bounds.height()) * 0.25f;
		
		path.moveTo(cx - thickness / 2, bounds.top);
		path.lineTo(cx + thickness / 2, bounds.top);
		path.lineTo(cx + thickness / 2, cy - thickness / 2);
		path.lineTo(bounds.right, cy - thickness / 2);
		path.lineTo(bounds.right, cy + thickness / 2);
		path.lineTo(cx + thickness / 2, cy + thickness / 2);
		path.lineTo(cx + thickness / 2, bounds.bottom);
		path.lineTo(cx - thickness / 2, bounds.bottom);
		path.lineTo(cx - thickness / 2, cy + thickness / 2);
		path.lineTo(bounds.left, cy + thickness / 2);
		path.lineTo(bounds.left, cy - thickness / 2);
		path.lineTo(cx - thickness / 2, cy - thickness / 2);
		path.close();
	}
	
	private static void addArrow(Path path, RectF bounds) {
		float shaftHeight = bounds.height() * 0.4f;
		float headWidth = bounds.width() * 0.4f;
		float cy = bounds.centerY();
		
		path.moveTo(bounds.left, cy - shaftHeight / 2f);
		path.lineTo(bounds.right - headWidth, cy - shaftHeight / 2f);
		path.lineTo(bounds.right - headWidth, bounds.top);
		path.lineTo(bounds.right, cy);
		path.lineTo(bounds.right - headWidth, bounds.bottom);
		path.lineTo(bounds.right - headWidth, cy + shaftHeight / 2f);
		path.lineTo(bounds.left, cy + shaftHeight / 2f);
		path.close();
	}
	
	private static void addSpeechBubble(Path path, RectF bounds) {
		float radius = bounds.width() * 0.15f;
		float tailHeight = bounds.height() * 0.2f;
		
		RectF body = new RectF(bounds.left, bounds.top, bounds.right, bounds.bottom - tailHeight);
		path.addRoundRect(body, radius, radius, Path.Direction.CW);
		
		path.moveTo(bounds.left + radius * 2, bounds.bottom - tailHeight);
		path.lineTo(bounds.left + bounds.width() * 0.2f, bounds.bottom);
		path.lineTo(bounds.left + bounds.width() * 0.4f, bounds.bottom - tailHeight);
		path.close();
	}
	
	private static void addThinkingBubble(Path path, RectF bounds) {
		float mainRadius = Math.min(bounds.width(), bounds.height()) * 0.35f;
		float cx = bounds.centerX();
		float cy = bounds.top + mainRadius + bounds.height() * 0.1f;
		
		path.addCircle(cx, cy, mainRadius, Path.Direction.CW);
		
		float bubble1Radius = mainRadius * 0.3f;
		float bubble2Radius = mainRadius * 0.18f;
		float bubble3Radius = mainRadius * 0.1f;
		
		float offsetX = bounds.width() * 0.25f;
		float offsetY = bounds.height() * 0.35f;
		
		path.addCircle(cx - offsetX * 0.6f, cy + offsetY * 0.7f, bubble1Radius, Path.Direction.CW);
		path.addCircle(cx - offsetX * 0.9f, cy + offsetY * 1.2f, bubble2Radius, Path.Direction.CW);
		path.addCircle(cx - offsetX * 1.1f, cy + offsetY * 1.6f, bubble3Radius, Path.Direction.CW);
	}
	
	private static void addHeart(Path path, RectF bounds) {
		float cx = bounds.centerX();
		
		path.moveTo(cx, bounds.bottom);
		path.cubicTo(
		bounds.left, bounds.bottom - bounds.height() * 0.4f,
		bounds.left, bounds.top,
		cx, bounds.top + bounds.height() * 0.3f
		);
		path.cubicTo(
		bounds.right, bounds.top,
		bounds.right, bounds.bottom - bounds.height() * 0.4f,
		cx, bounds.bottom
		);
		path.close();
	}
	
	private static void addShield(Path path, RectF bounds) {
		float cx = bounds.centerX();
		
		path.moveTo(bounds.left, bounds.top);
		path.lineTo(bounds.right, bounds.top);
		path.lineTo(bounds.right, bounds.top + bounds.height() * 0.6f);
		path.quadTo(bounds.right, bounds.bottom, cx, bounds.bottom);
		path.quadTo(bounds.left, bounds.bottom, bounds.left, bounds.top + bounds.height() * 0.6f);
		path.close();
	}
	
	private static void addLightning(Path path, RectF bounds) {
		path.moveTo(bounds.left + bounds.width() * 0.6f, bounds.top);
		path.lineTo(bounds.left, bounds.top + bounds.height() * 0.5f);
		path.lineTo(bounds.left + bounds.width() * 0.4f, bounds.top + bounds.height() * 0.5f);
		path.lineTo(bounds.left + bounds.width() * 0.3f, bounds.bottom);
		path.lineTo(bounds.right, bounds.top + bounds.height() * 0.4f);
		path.lineTo(bounds.left + bounds.width() * 0.6f, bounds.top + bounds.height() * 0.4f);
		path.close();
	}
	
	private static void addLShape(Path path, RectF bounds) {
		float thickX = bounds.width() * 0.35f;
		float thickY = bounds.height() * 0.35f;
		
		path.moveTo(bounds.left, bounds.top);
		path.lineTo(bounds.left + thickX, bounds.top);
		path.lineTo(bounds.left + thickX, bounds.bottom - thickY);
		path.lineTo(bounds.right, bounds.bottom - thickY);
		path.lineTo(bounds.right, bounds.bottom);
		path.lineTo(bounds.left, bounds.bottom);
		path.close();
	}
	
	private static void addTag(Path path, RectF bounds) {
		float notch = bounds.width() * 0.2f;
		float cy = bounds.centerY();
		
		path.moveTo(bounds.left, bounds.top);
		path.lineTo(bounds.right - notch, bounds.top);
		path.lineTo(bounds.right, cy);
		path.lineTo(bounds.right - notch, bounds.bottom);
		path.lineTo(bounds.left, bounds.bottom);
		path.close();
	}
	
	private static void addCube(Path path, RectF bounds) {
		float offset = Math.min(bounds.width(), bounds.height()) * 0.25f;
		
		path.addRect(bounds.left, bounds.top + offset, bounds.right - offset, bounds.bottom, Path.Direction.CW);
		
		path.moveTo(bounds.left, bounds.top + offset);
		path.lineTo(bounds.left + offset, bounds.top);
		path.lineTo(bounds.right, bounds.top);
		path.lineTo(bounds.right - offset, bounds.top + offset);
		path.close();
		
		path.moveTo(bounds.right - offset, bounds.top + offset);
		path.lineTo(bounds.right, bounds.top);
		path.lineTo(bounds.right, bounds.bottom - offset);
		path.lineTo(bounds.right - offset, bounds.bottom);
		path.close();
	}
	
	private static void addCylinder(Path path, RectF bounds) {
		float ellipseHeight = bounds.height() * 0.2f;
		
		path.addOval(bounds.left, bounds.top, bounds.right, bounds.top + ellipseHeight, Path.Direction.CW);
		
		path.moveTo(bounds.left, bounds.top + ellipseHeight / 2f);
		path.lineTo(bounds.left, bounds.bottom - ellipseHeight / 2f);
		path.arcTo(new RectF(bounds.left, bounds.bottom - ellipseHeight, bounds.right, bounds.bottom), 180, 180);
		path.lineTo(bounds.right, bounds.top + ellipseHeight / 2f);
		path.close();
	}
	
	private static void addCone(Path path, RectF bounds) {
		float baseHeight = bounds.height() * 0.2f;
		float cx = bounds.centerX();
		
		path.addOval(bounds.left, bounds.bottom - baseHeight, bounds.right, bounds.bottom, Path.Direction.CW);
		
		path.moveTo(bounds.left, bounds.bottom - baseHeight / 2f);
		path.lineTo(cx, bounds.top);
		path.lineTo(bounds.right, bounds.bottom - baseHeight / 2f);
	}
	
	private static void addPyramid(Path path, RectF bounds) {
		float baseHeight = bounds.height() * 0.25f;
		float slant = bounds.width() * 0.15f;
		float cx = bounds.centerX();
		
		path.moveTo(bounds.left + slant, bounds.bottom - baseHeight);
		path.lineTo(bounds.right, bounds.bottom - baseHeight);
		path.lineTo(bounds.right - slant, bounds.bottom);
		path.lineTo(bounds.left, bounds.bottom);
		path.close();
		
		path.moveTo(bounds.left, bounds.bottom);
		path.lineTo(cx, bounds.top);
		path.lineTo(bounds.left + slant, bounds.bottom - baseHeight);
		path.close();
		
		path.moveTo(bounds.right - slant, bounds.bottom);
		path.lineTo(cx, bounds.top);
		path.lineTo(bounds.right, bounds.bottom - baseHeight);
		path.close();
	}
	
	private static void addTriangularPrism(Path path, RectF bounds) {
		float depth = bounds.width() * 0.3f;
		path.moveTo(bounds.left + depth, bounds.bottom);
		path.lineTo(bounds.right, bounds.bottom);
		path.lineTo(bounds.right - (bounds.width() - depth) / 2, bounds.top);
		path.close();
		path.moveTo(bounds.left + depth, bounds.bottom);
		path.lineTo(bounds.left, bounds.bottom);
		path.lineTo(bounds.left + (bounds.width() - depth) / 2, bounds.top);
		path.lineTo(bounds.right - (bounds.width() - depth) / 2, bounds.top);
		path.close();
	}
	
	private static void addTetrahedron(Path path, RectF bounds) {
		float cx = bounds.centerX();
		float cy = bounds.centerY();
		float r = Math.min(bounds.width(), bounds.height()) / 2f;
		
		float x1 = cx + r * (float) Math.cos(-Math.PI / 2);
		float y1 = cy + r * (float) Math.sin(-Math.PI / 2);
		float x2 = cx + r * (float) Math.cos(Math.PI / 6);
		float y2 = cy + r * (float) Math.sin(Math.PI / 6);
		float x3 = cx + r * (float) Math.cos(5 * Math.PI / 6);
		float y3 = cy + r * (float) Math.sin(5 * Math.PI / 6);
		
		float apexX = cx;
		float apexY = cy - r * 0.3f;
		
		path.moveTo(x1, y1);
		path.lineTo(x2, y2);
		path.lineTo(x3, y3);
		path.close();
		
		path.moveTo(x1, y1);
		path.lineTo(apexX, apexY);
		path.lineTo(x2, y2);
		path.close();
		
		path.moveTo(x2, y2);
		path.lineTo(apexX, apexY);
		path.lineTo(x3, y3);
		path.close();
	}
	
	private static void addOctahedron(Path p, RectF b) {
		float cx = b.centerX();
		float cy = b.centerY();
		float r = Math.min(b.width(), b.height()) / 2f;
		
		p.moveTo(cx, b.top);
		p.lineTo(b.right, cy);
		p.lineTo(cx, b.bottom);
		p.lineTo(b.left, cy);
		p.close();
		
		p.moveTo(b.left, cy);
		p.lineTo(cx, cy + (r * 0.3f));
		p.lineTo(b.right, cy);
		
		p.moveTo(cx, b.top);
		p.lineTo(cx, cy + (r * 0.3f));
		p.lineTo(cx, b.bottom);
	}
	
	private static void addIcosahedron(Path path, RectF bounds) {
		float cx = bounds.centerX();
		float cy = bounds.centerY();
		float r = Math.min(bounds.width(), bounds.height()) / 2f;
		float innerR = r * 0.45f;
		
		float[] ox = new float[6];
		float[] oy = new float[6];
		float[] ix = new float[3];
		float[] iy = new float[3];
		for (int i = 0; i < 6; i++) {
			float angle = (float) (Math.toRadians(-90 + (i * 60)));
			ox[i] = cx + r * (float) Math.cos(angle);
			oy[i] = cy + r * (float) Math.sin(angle);
		}
		for (int i = 0; i < 3; i++) {
			float angle = (float) (Math.toRadians(90 + (i * 120)));
			ix[i] = cx + innerR * (float) Math.cos(angle);
			iy[i] = cy + innerR * (float) Math.sin(angle);
		}
		
		path.moveTo(ox[0], oy[0]);
		for (int i = 1; i < 6; i++) {
			path.lineTo(ox[i], oy[i]);
		}
		path.close();
		path.moveTo(ix[0], iy[0]);
		for (int i = 1; i < 3; i++) {
			path.lineTo(ix[i], iy[i]);
		}
		path.close();
		for (int i = 0; i < 3; i++) {
			int mid = (2 * i + 3) % 6; 
			int prev = (mid + 5) % 6;
			int next = (mid + 1) % 6;
			
			path.moveTo(ix[i], iy[i]); path.lineTo(ox[mid], oy[mid]);
			path.moveTo(ix[i], iy[i]); path.lineTo(ox[prev], oy[prev]);
			path.moveTo(ix[i], iy[i]); path.lineTo(ox[next], oy[next]);
		}
	}
	
	private static void addCrescent(Path path, RectF bounds) {
		float r = Math.min(bounds.width(), bounds.height()) / 2f;
		float cx = bounds.centerX();
		float cy = bounds.centerY();
		path.addCircle(cx, cy, r, Path.Direction.CW);
		
		Path cutout = new Path();
		cutout.addCircle(cx + r * 0.5f, cy - r * 0.2f, r, Path.Direction.CW);
		path.op(cutout, Path.Op.DIFFERENCE);
	}
	
	private static void addCoil(Path path, RectF bounds) {
		float cx = bounds.centerX();
		float cy = bounds.centerY();
		int loops = 5;
		float radiusStep = (Math.min(bounds.width(), bounds.height()) / 2f) / (loops * 10);
		
		path.moveTo(cx, cy);
		for (int i = 0; i < loops * 40; i++) {
			float angle = 0.1f * i;
			float r = radiusStep * i;
			float x = cx + r * (float) Math.cos(angle);
			float y = cy + r * (float) Math.sin(angle);
			path.lineTo(x, y);
		}
	}
	
	private static void addTrefoil(Path path, RectF bounds) {
		float cx = bounds.centerX();
		float cy = bounds.centerY();
		float scale = Math.min(bounds.width(), bounds.height()) / 4f;
		
		for (int i = 0; i <= 100; i++) {
			double t = (i / 100.0) * 2 * Math.PI;
			float x = cx + scale * (float) (Math.sin(t) + 2 * Math.sin(2 * t));
			float y = cy - scale * (float) (Math.cos(t) - 2 * Math.cos(2 * t));
			if (i == 0) path.moveTo(x, y);
			else path.lineTo(x, y);
		}
		path.close();
	}
}
