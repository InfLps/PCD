package com.inflps.pcd.WIDGET.REFRESH_LAYOUT;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

public class ScaleRefreshLayout extends ViewGroup {

    private View mHeader;
    private View mTarget;
    private int mHeaderHeight;
    private boolean mIsRefreshing = false;
    private boolean mThresholdReached = false;
    private int mTouchSlop;
    private float mInitialDownY;
    private boolean mIsBeingDragged;
    private int mActivePointerId = -1;
    
    private final int REFRESH_STOP_POSITION = (int) (110 * getResources().getDisplayMetrics().density);
    
    private int mSafeOffset; 
    private ObjectAnimator mFlipAnimator;
    private OnRefreshListener mListener;

    public interface OnRefreshListener {
        void onRefresh();
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        this.mListener = listener;
    }

    public ScaleRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() >= 2) {
            mHeader = getChildAt(0);
            mTarget = getChildAt(1);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mHeader == null || mTarget == null) return;
        
        mHeaderHeight = mHeader.getMeasuredHeight();
        mSafeOffset = (int) (mHeaderHeight * 0.5f); 
        
        int width = getMeasuredWidth();
        int headerLeft = (width - mHeader.getMeasuredWidth()) / 2;
        
        mHeader.layout(headerLeft, -mHeaderHeight, headerLeft + mHeader.getMeasuredWidth(), 0);
        mHeader.setPivotX(mHeader.getMeasuredWidth() / 2f);
        mHeader.setPivotY(mHeader.getMeasuredHeight() / 2f);
mHeader.setTranslationY(-mHeaderHeight * 2);
        mTarget.layout(0, 0, width, getMeasuredHeight());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnabled() || mIsRefreshing || mTarget.canScrollVertically(-1)) return false;

        int action = ev.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                mInitialDownY = ev.getY();
                mIsBeingDragged = false;
                mThresholdReached = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == -1) return false;
                float y = ev.getY();
                if (y - mInitialDownY > mTouchSlop) mIsBeingDragged = true;
                break;
        }
        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mActivePointerId == -1) return super.onTouchEvent(ev);

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                float totalDiff = ev.getY() - mInitialDownY;
                float overscroll = (float) (Math.pow(Math.max(0, totalDiff), 0.8) * 2);

                if (overscroll > 0) {
                    moveViews(overscroll);
                    handleTactileFeedback(overscroll);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                float finalDiff = ev.getY() - mInitialDownY;
                float finalOverscroll = (float) (Math.pow(Math.max(0, finalDiff), 0.8) * 2);
                
                if (finalOverscroll >= mHeaderHeight) startRefreshing();
                else resetViews();
                
                mActivePointerId = -1;
                mIsBeingDragged = false;
                break;
        }
        return true;
    }

    private void moveViews(float y) {
        mTarget.setTranslationY(y);
        mHeader.setTranslationY((y / 2) - mSafeOffset);

        float scale = Math.min(1.2f, y / mHeaderHeight);
        mHeader.setScaleX(scale);
        mHeader.setScaleY(scale);
        mHeader.setAlpha(Math.min(1.0f, scale));
    }

    private void handleTactileFeedback(float overscroll) {
        if (overscroll >= mHeaderHeight && !mThresholdReached) {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            mThresholdReached = true;
        } else if (overscroll < mHeaderHeight) {
            mThresholdReached = false;
        }
    }

    private void startRefreshing() {
        if (mIsRefreshing) return;
        mIsRefreshing = true;
    mTarget.animate()
            .translationY(REFRESH_STOP_POSITION + 20)
            .setDuration(300)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    mHeader.animate()
            .translationY(REFRESH_STOP_POSITION) 
            .scaleX(1.0f)
            .scaleY(1.0f)
            .alpha(1.0f)
            .setDuration(300)
            .start();

        float distance = 8000 * getResources().getDisplayMetrics().density;
        mHeader.setCameraDistance(distance);

        mFlipAnimator = ObjectAnimator.ofFloat(mHeader, "rotationY", 0f, 360f);
        mFlipAnimator.setDuration(1200);
        mFlipAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mFlipAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mFlipAnimator.start();

        if (mListener != null) mListener.onRefresh();
    }

    public void setRefreshing(boolean refreshing) {
        if (mIsRefreshing == refreshing) return;
        if (!refreshing) {
            mIsRefreshing = false;
            if (mFlipAnimator != null) mFlipAnimator.cancel();
            resetViews();
        } else {
            startRefreshing();
        }
    }

    private void resetViews() {
    mTarget.animate()
            .translationY(0)
            .setDuration(400)
            .start();
    mHeader.animate()
            .translationY(-mHeaderHeight - mSafeOffset)
            .scaleY(0f)
            .alpha(0f)
            .rotationY(0f)
            .setDuration(400)
            .start();
    }
}
