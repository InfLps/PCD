package com.inflps.pcd.WIDGET.CONTROL_VIEW;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.inflps.pcd.CORE.DRAWING_CORE.DrawingState;
import com.inflps.pcd.CORE.DRAWING_CORE.ShapeFactory;

public class ShapeSelectorView extends View {
    public enum ShapeType {
        CIRCLE, RECTANGLE, TRIANGLE, LINE,
        HEXAGON, PENTAGON, OCTAGON,
        STAR,
        COIL, CYLINDER, CUBE, PYRAMID, PRISM_TRIANGULAR, 
        TETRAHEDRON, OCTAHEDRON, ICOSAHEDRON, TREFOIL,
        CLOUD, THINKING_BUBBLE, SPEECH_BUBBLE, HEART
    }

    public enum ShapeStyle {
        FILL,
        STROKE
    }

    private ShapeType currentShape = ShapeType.CIRCLE;
    private ShapeStyle currentStyle = ShapeStyle.FILL;
    
    private int shapeColor = Color.WHITE;
    private int arrowColor = Color.LTGRAY;
    private float strokeWidthDp = 3f;
    
    private Paint shapePaint;
    private Paint arrowPaint;
    private RectF shapeBounds = new RectF();
    private GestureDetector gestureDetector;

    public interface OnShapeChangeListener {
        void onShapeChanged(ShapeType shape, ShapeStyle style);
    }

    private OnShapeChangeListener listener;

    public void setOnShapeChangeListener(OnShapeChangeListener listener) {
        this.listener = listener;
    }

    public ShapeSelectorView(Context context) { super(context); init(context); }
    public ShapeSelectorView(Context context, AttributeSet attrs) { super(context, attrs); init(context); }
    public ShapeSelectorView(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(context); }

    private void init(Context context) {
        shapePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shapePaint.setColor(shapeColor);
        shapePaint.setStrokeWidth(dpToPx(strokeWidthDp));
        shapePaint.setStrokeCap(Paint.Cap.ROUND);
        shapePaint.setStrokeJoin(Paint.Join.ROUND);
        shapePaint.setShadowLayer(dpToPx(3), 0, dpToPx(1), Color.parseColor("#60000000"));
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arrowPaint.setColor(arrowColor);
        arrowPaint.setStyle(Paint.Style.FILL);
        arrowPaint.setAlpha(150);

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                toggleStyle();
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                // Vertical Fling
                if (Math.abs(velocityY) > Math.abs(velocityX)) {
                    if (velocityY > 0) {
                        prevShape();
                    } else {
                        nextShape();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = (int) dpToPx(60);
        setMeasuredDimension(resolveSize(size, widthMeasureSpec), resolveSize(size, heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float padding = dpToPx(14);
        float size = Math.min(w, h);
        shapeBounds.set(padding, padding, size - padding, size - padding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float cx = getWidth() / 2f;
        canvas.drawCircle(cx, dpToPx(6), dpToPx(2), arrowPaint);
        canvas.drawCircle(cx, getHeight() - dpToPx(6), dpToPx(2), arrowPaint);

        if (currentStyle == ShapeStyle.FILL) {
            shapePaint.setStyle(Paint.Style.FILL);
        } else {
            shapePaint.setStyle(Paint.Style.STROKE);
        }
        shapePaint.setColor(shapeColor);

        DrawingState.ShapeType factoryType = mapLocalTypeToFactoryType(currentShape);
        
        if (factoryType != null) {
            Path iconPath = ShapeFactory.createShapePath(
                shapeBounds.left, 
                shapeBounds.top, 
                shapeBounds.right, 
                shapeBounds.bottom, 
                factoryType
            );
            canvas.drawPath(iconPath, shapePaint);
        }
    }

    private DrawingState.ShapeType mapLocalTypeToFactoryType(ShapeType localType) {
        try {
            return DrawingState.ShapeType.valueOf(localType.name());
        } catch (IllegalArgumentException e) {
            return DrawingState.ShapeType.CIRCLE;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    public void toggleStyle() {
        if (currentShape == ShapeType.LINE || currentShape == ShapeType.COIL || currentShape == ShapeType.TREFOIL) {
            return;
        }

        currentStyle = (currentStyle == ShapeStyle.FILL) ? ShapeStyle.STROKE : ShapeStyle.FILL;
        notifyListener();
        invalidate();
    }

    public void nextShape() {
        int nextOrdinal = (currentShape.ordinal() + 1) % ShapeType.values().length;
        setShapeByIndex(nextOrdinal);
    }

    public void prevShape() {
        int prevOrdinal = (currentShape.ordinal() - 1 + ShapeType.values().length) % ShapeType.values().length;
        setShapeByIndex(prevOrdinal);
    }
    
    private void setShapeByIndex(int index) {
        currentShape = ShapeType.values()[index];
        
        enforceShapeStyleConstraints();
        notifyListener();
        invalidate();
    }
    
    private void enforceShapeStyleConstraints() {
        switch (currentShape) {
            case COIL:
            case CYLINDER:
            case CUBE:
            case PYRAMID:
            case PRISM_TRIANGULAR:
            case TETRAHEDRON:
            case OCTAHEDRON:
            case ICOSAHEDRON:
            case TREFOIL:
            case LINE:
                if (currentStyle == ShapeStyle.FILL) {
                    currentStyle = ShapeStyle.STROKE;
                }
                break;
            case CLOUD:
            case THINKING_BUBBLE:
            case SPEECH_BUBBLE:
                if (currentStyle == ShapeStyle.STROKE) {
                    currentStyle = ShapeStyle.FILL;
                }
                break;
            default:
                break;
        }
    }
    
    private void notifyListener() {
        if (listener != null) {
            listener.onShapeChanged(currentShape, currentStyle);
        }
    }

    public ShapeType getShape() { return currentShape; }
    public ShapeStyle getStyle() { return currentStyle; }
    
    public void setColor(int color) {
        this.shapeColor = color;
        invalidate();
    }
}
