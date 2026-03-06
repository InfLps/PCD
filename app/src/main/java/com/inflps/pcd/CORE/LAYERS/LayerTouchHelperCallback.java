package com.inflps.pcd.CORE.LAYERS;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import com.inflps.pcd.MainActivity;
import com.inflps.pcd.DrawingView;
import com.inflps.pcd.MainActivity.LayerAdapter;
import com.inflps.pcd.MainActivity.LayerItem;

public class LayerTouchHelperCallback extends ItemTouchHelper.Callback {
	
	private final LayerAdapter mAdapter;
	private final DrawingView mPdv;
	
	public LayerTouchHelperCallback(LayerAdapter adapter, DrawingView pdv) {
		mAdapter = adapter;
		mPdv = pdv;
	}
	
	@Override
	public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
		final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
		final int swipeFlags = 0;
		return makeMovementFlags(dragFlags, swipeFlags);
	}
	
	@Override
	public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
		mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
		return true;
	}
	
	@Override
	public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) { }
	
	@Override
	public boolean isLongPressDragEnabled() { return true; }
	
	@Override
	public boolean isItemViewSwipeEnabled() { return false; }
	
	@Override
	public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) { 
		super.onSelectedChanged(viewHolder, actionState);
	}
	
	@Override
	public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
		super.clearView(recyclerView, viewHolder);
		ArrayList<Integer> newLayerOrder = new ArrayList<>();
		for (LayerItem item : mAdapter.getLayers()) {
			newLayerOrder.add(item.getLayerId());
		}
		mPdv.setLayerDrawingOrder(newLayerOrder);
	}
}