package com.inflps.pcd.WIDGET.PANEL;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

public class BottomPanelHandler {
	
	private final View panel;
	private final View header;
	private final View content;
	
	private int expandedHeight;
	private int collapsedHeight;
	private boolean isExpanded = false;
	private boolean isSwipeEnabled = true;
	
	private ValueAnimator currentAnimator;
	private final int touchSlop;
	
	private boolean isDragging = false;
	private float startY;
	private int startHeight;
	
	public interface PanelStateListener { void onPanelStateChanged(boolean isExpanded);}
	private PanelStateListener stateListener;
	
	public BottomPanelHandler(View panel, View header, View content) {
		this.panel = panel;
		this.header = header;
		this.content = content;
		touchSlop = ViewConfiguration.get(panel.getContext()).getScaledTouchSlop();
		initializePanel();
	}
	
	private void initializePanel() {
		panel.post(() -> {
			expandedHeight = panel.getHeight();
			collapsedHeight = header.getHeight();
			
			isExpanded = false;
			setPanelHeight(collapsedHeight);
			
			content.setVisibility(View.GONE);
			content.setAlpha(0f);
			
			setupGestures();
		});
	}
	
	public void setPanelStateListener(PanelStateListener listener) {
		this.stateListener = listener;
	}
	
	public void setSwipeEnabled(boolean enabled) {
		this.isSwipeEnabled = enabled;
	}
	
	public void toggle() {
		if (isExpanded) collapse();
		else expand();
	}
	
	public void expand() {
		content.setVisibility(View.VISIBLE);
		animateHeight(panel.getHeight(), expandedHeight, true);
		content.animate()
		.alpha(1f)
		.setDuration(250)
		.setInterpolator(new DecelerateInterpolator())
		.start();
	}
	
	public void collapse() {
		animateHeight(panel.getHeight(), collapsedHeight, false);
		content.animate()
		.alpha(0f)
		.setDuration(200)
		.withEndAction(() -> {
			if (!isExpanded) content.setVisibility(View.GONE);
		})
		.start();
	}
	
	public boolean isExpanded() {
		return isExpanded;
	}
	
	public boolean isCollapsed() {
		return !isExpanded;
	}
	
	private void setupGestures() {
		header.setOnTouchListener((v, event) -> {
			if (!isSwipeEnabled) return false;
			
			switch (event.getActionMasked()) {
				case MotionEvent.ACTION_DOWN:
				if (currentAnimator != null && currentAnimator.isRunning()) {
					currentAnimator.cancel();
				}
				content.animate().cancel();
				
				startY = event.getRawY();
				startHeight = panel.getHeight();
				isDragging = false;
				
				if (content.getVisibility() != View.VISIBLE) {
					content.setVisibility(View.VISIBLE);
				}
				return true;
				
				case MotionEvent.ACTION_MOVE:
				float currentY = event.getRawY();
				float dy = startY - currentY; 
				if (!isDragging && Math.abs(dy) > touchSlop) {
					isDragging = true;
				}
				
				if (isDragging) {
					int newHeight = startHeight + (int) dy;
					if (newHeight < collapsedHeight) newHeight = collapsedHeight;
					if (newHeight > expandedHeight) newHeight = expandedHeight;
					
					setPanelHeight(newHeight);
					updateContentAlpha(newHeight);
				}
				return true;
				
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
				if (isDragging) {
					snapToNearestState();
				} else {
					toggle();
				}
				isDragging = false;
				return true;
			}
			return false;
		});
	}
	
	private void snapToNearestState() {
		int currentHeight = panel.getHeight();
		int threshold = collapsedHeight + (expandedHeight - collapsedHeight) / 2;
		
		if (currentHeight > threshold) {
			expand();
		} else {
			collapse();
		}
	}
	
	private void updateContentAlpha(int currentHeight) {
		float totalDragDistance = expandedHeight - collapsedHeight;
		if (totalDragDistance <= 0) return;
		
		float progress = (currentHeight - collapsedHeight) / totalDragDistance;
		progress = Math.max(0f, Math.min(1f, progress));
		
		content.setAlpha(progress);
	}
	
	private void setPanelHeight(int height) {
		ViewGroup.LayoutParams params = panel.getLayoutParams();
		params.height = height;
		panel.setLayoutParams(params);
	}
	
	private void animateHeight(int start, int end, boolean expanding) {
		if (currentAnimator != null && currentAnimator.isRunning()) {
			currentAnimator.cancel();
		}
		
		currentAnimator = ValueAnimator.ofInt(start, end);
		currentAnimator.setDuration(300);
		currentAnimator.setInterpolator(new DecelerateInterpolator());
		
		currentAnimator.addUpdateListener(animation -> {
			int val = (int) animation.getAnimatedValue();
			setPanelHeight(val);
		});
		
		currentAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				isExpanded = expanding;
				if (stateListener != null) stateListener.onPanelStateChanged(isExpanded);
			}
		});
		
		currentAnimator.start();
	}
}
