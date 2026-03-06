package com.inflps.pcd.CORE.LISTENERS;

import android.graphics.PorterDuff;
import java.util.ArrayList;
import com.inflps.pcd.CORE.DRAWING_CORE.DrawingState;

public interface LayerInteractionListener {
    void onLayerSelected(int layerId);
    void onLayerVisibilityToggled(int layerId, boolean isVisible);
    void onLayerLockToggled(int layerId, boolean isLocked);
    void onLayerBlendModeSet(int layerId, PorterDuff.Mode mode);
    void onLayerDeleteRequested(int layerId, int positionInAdapter);
    void onLayerOrderChanged(ArrayList<Integer> newLayerOrder);
    void onShapeAdded(DrawingState.ShapeType shapeType);
    void requestAllLayerPreviewsUpdate();
}
