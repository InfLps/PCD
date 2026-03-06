package com.inflps.pcd.WIDGET.CONTROL_VIEW;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;

import androidx.core.graphics.drawable.DrawableCompat;

public class ToolSwapView extends View {
	
	private Drawable primaryDrawable;
	private Drawable secondaryDrawable;
	private int shadowColor = Color.parseColor("#80000000");
	private RectF mainRect = new RectF();
	private RectF secondaryRect = new RectF();
	private Paint shadowPaint;
	private ValueAnimator swapAnimator;
	private float animProgress = 0f;
	private boolean isAnimating = false;
	private boolean internalToolIsActive = true; 
	private Drawable animFromMain;
	private Drawable animFromSecondary;
	
	public interface OnToolSwapListener {
		void onToolSwapped(boolean isPrimaryActive);
	}
	
	private OnToolSwapListener listener;
	
	public void setOnToolSwapListener(OnToolSwapListener listener) {
		this.listener = listener;
	}
	
	public ToolSwapView(Context context) { super(context); init(); }
	public ToolSwapView(Context context, AttributeSet attrs) { super(context, attrs); init(); }
	public ToolSwapView(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }
	
	private void init() {
		shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		shadowPaint.setColor(Color.TRANSPARENT);
		shadowPaint.setStyle(Paint.Style.FILL);
		shadowPaint.setShadowLayer(dpToPx(6), 0, dpToPx(3), shadowColor);
		setLayerType(LAYER_TYPE_SOFTWARE, null);
		setupAnimator();
	}
	
	private void setupAnimator() {
		swapAnimator = ValueAnimator.ofFloat(0f, 1f);
		swapAnimator.setDuration(400);
		swapAnimator.setInterpolator(new AnticipateOvershootInterpolator(0.8f));
		swapAnimator.addUpdateListener(animation -> {
			animProgress = (float) animation.getAnimatedValue();
			invalidate();
		});
		
		swapAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				isAnimating = false;
				animProgress = 0f;
				Drawable temp = primaryDrawable;
				primaryDrawable = secondaryDrawable;
				secondaryDrawable = temp;
				invalidate();
			}
		});
	}
	
	private float dpToPx(float dp) {
		return dp * getResources().getDisplayMetrics().density;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int desiredSize = (int) dpToPx(50); 
		int width = resolveSize(desiredSize, widthMeasureSpec);
		int height = resolveSize(desiredSize, heightMeasureSpec);
		setMeasuredDimension(width, height);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		float padding = dpToPx(4);
		
		float mainSize = Math.min(w, h) * 0.60f;
		float mainLeft = w - mainSize - padding;
		float mainTop = h - mainSize - padding;
		mainRect.set(mainLeft, mainTop, mainLeft + mainSize, mainTop + mainSize);
		
		float secSize = Math.min(w, h) * 0.40f;
		float secLeft = padding;
		float secTop = padding;
		secondaryRect.set(secLeft, secTop, secLeft + secSize, secTop + secSize);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if (isAnimating) {
			drawInterpolatedIcon(canvas, animFromMain, mainRect, secondaryRect, animProgress);
			drawInterpolatedIcon(canvas, animFromSecondary, secondaryRect, mainRect, animProgress);
		} else {
			drawIconAtRect(canvas, secondaryDrawable, secondaryRect, 0.7f);
			drawIconAtRect(canvas, primaryDrawable, mainRect, 1.0f);
		}
	}
	
	private void drawIconAtRect(Canvas canvas, Drawable icon, RectF rect, float alpha) {
		if (icon == null) return;
		
		float cx = rect.centerX();
		float cy = rect.centerY();
		float radius = rect.width() / 2f;
		canvas.drawCircle(cx, cy, radius * 0.8f, shadowPaint);
		int iconSize = (int) (rect.width());
		int half = iconSize / 2;
		icon.setBounds((int)cx - half, (int)cy - half, (int)cx + half, (int)cy + half);
		int prevAlpha = icon.getAlpha();
		icon.setAlpha((int)(255 * alpha));
		icon.draw(canvas);
		icon.setAlpha(prevAlpha);
	}
	
	private void drawInterpolatedIcon(Canvas canvas, Drawable icon, RectF startRect, RectF endRect, float progress) {
		if (icon == null) return;
		float curLeft = startRect.left + (endRect.left - startRect.left) * progress;
		float curTop = startRect.top + (endRect.top - startRect.top) * progress;
		float curSize = startRect.width() + (endRect.width() - startRect.width()) * progress;
		RectF currentRect = new RectF(curLeft, curTop, curLeft + curSize, curTop + curSize);
		float startAlpha = (startRect == mainRect) ? 1.0f : 0.7f;
		float endAlpha   = (endRect == mainRect)   ? 1.0f : 0.7f;
		float curAlpha   = startAlpha + (endAlpha - startAlpha) * progress;
		drawIconAtRect(canvas, icon, currentRect, curAlpha);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			triggerSwap();
			performClick();
			return true;
		}
		return true;
	}
	
	public void triggerSwap() {
		if (isAnimating) return;
		internalToolIsActive = !internalToolIsActive;
		animFromMain = primaryDrawable;
		animFromSecondary = secondaryDrawable;
		isAnimating = true;
		swapAnimator.start();
		
		if (listener != null) {
			listener.onToolSwapped(internalToolIsActive);
		}
	}
	
	public void setPrimaryDrawable(Drawable d) {
		this.primaryDrawable = d;
		tintDrawable(this.primaryDrawable);
		invalidate();
	}
	
	public void setSecondaryDrawable(Drawable d) {
		this.secondaryDrawable = d;
		tintDrawable(this.secondaryDrawable);
		invalidate();
	}
	
	private void tintDrawable(Drawable d) {
		if (d != null) {
			DrawableCompat.setTint(d, Color.WHITE);
		}
	}
}
