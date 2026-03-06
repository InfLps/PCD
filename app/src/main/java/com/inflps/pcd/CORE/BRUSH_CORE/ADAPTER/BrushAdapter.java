package com.inflps.pcd.CORE.BRUSH_CORE.ADAPTER;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.drawable.*;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inflps.pcd.R;

import java.util.List;

public class BrushAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	
	private final List<BrushItem> items;
	private final OnBrushClickListener listener;
	
	private static int selectedPosition = -1; 
	private static String selectedFilePath = "";
	
	private String highlight = "#353535"; 
	
	
	public interface OnBrushClickListener {
		void onBrushSelected(String filePath);
		void onDefaultPenBrush();
		void onImportClicked();
		void onBrushLongClick(BrushItem item, View anchorView);
	}
	
	public BrushAdapter(List<BrushItem> items, OnBrushClickListener listener) {
		this.items = items;
		this.listener = listener;
		setHasStableIds(true);
		
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).type != BrushItem.Type.IMPORT) {
				selectedPosition = i;
				break;
			}
		}
	}
	
	@Override
	public int getItemViewType(int position) {
		return items.get(position).type.ordinal();
	}
	
	@Override
	public long getItemId(int position) {
		BrushItem item = items.get(position);
		return item.type == BrushItem.Type.BRUSH && item.filePath != null
		? item.filePath.hashCode()
		: position;
	}
	
	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(
	@NonNull ViewGroup parent,
	int viewType
	) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		BrushItem.Type type = BrushItem.Type.values()[viewType];
		
		switch (type) {
			case IMPORT:
			return new ImportViewHolder(
			inflater.inflate(R.layout.item_brush_import, parent, false)
			);
			case DEFAULT_BRUSH:
			return new BrushViewHolder(
			inflater.inflate(R.layout.item_default_brush, parent, false)
			);
			default:
			return new BrushViewHolder(
			inflater.inflate(R.layout.item_brush, parent, false)
			);
		}
	}
	
	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		boolean isSelected = (position == selectedPosition);
		BrushItem item = items.get(position);
		applyHoverEffect(holder.itemView, highlight);
		
		if (isSelected) {
			holder.itemView.setBackgroundResource(R.drawable.selected_item_grid);
		} else {
			holder.itemView.setBackground(null);
		}
		
		switch (item.type) {
			case IMPORT:
			bindImport(holder);
			break;
			
			case DEFAULT_BRUSH:
			bindDefaultBrush((BrushViewHolder) holder, item, position);
			break;
			
			case BRUSH:
			bindBrush((BrushViewHolder) holder, item, position);
			break;
		}
	}
	
	@Override
	public int getItemCount() {
		return items != null ? items.size() : 0;
	}
	
	private void bindImport(RecyclerView.ViewHolder holder) {
		holder.itemView.setOnClickListener(v -> listener.onImportClicked());
	}
	
	private void bindDefaultBrush(BrushViewHolder holder, BrushItem item, int position) {
		bindCommonBrushUI(holder, item);
		holder.itemView.setOnClickListener(v -> {
			updateSelection(position);
			listener.onDefaultPenBrush();
		});
	}
	
	private void bindBrush(BrushViewHolder holder, BrushItem item, int position) {
		bindCommonBrushUI(holder, item);
		holder.itemView.setOnClickListener(v -> {
			updateSelection(position);
			listener.onBrushSelected(item.filePath);
		});
		holder.itemView.setOnLongClickListener(v -> {
			listener.onBrushLongClick(item, v); 
			return true;
		});
	}
	
	private void bindCommonBrushUI(BrushViewHolder holder, BrushItem item) {
		holder.name.setText(item.name);
		holder.image.setImageBitmap(item.thumbnail);
		enableMarquee(holder.name);
	}
	
	private void enableMarquee(TextView tv) {
		tv.setEllipsize(TextUtils.TruncateAt.MARQUEE);
		tv.setMarqueeRepeatLimit(-1);
		tv.setHorizontallyScrolling(true);
		tv.setSelected(true);
	}
	
	private void updateSelection(int newPosition) {
		int oldPos = selectedPosition;
		selectedPosition = newPosition;
		selectedFilePath = items.get(newPosition).filePath;
		
		notifyItemChanged(oldPos);
		notifyItemChanged(selectedPosition);
	}
	
	public void setExternalSelection(int position) {
		if (position >= 0 && position < items.size()) {
			int oldPos = selectedPosition;
			selectedPosition = position;
			selectedFilePath = items.get(position).filePath;
			
			notifyItemChanged(oldPos);
			notifyItemChanged(selectedPosition);
		}
	}
	
	public String getActiveBrushPath() {
		return selectedFilePath;
	}
	
	public boolean hasSelection() {
		return selectedPosition != -1;
	}
	
	public int getSelectedPosition() {
		return selectedPosition;
	}
	
	
	public void applyHoverEffect(final View view, final String hexColor) {
		final int color = Color.parseColor(hexColor);
		view.setOnHoverListener(new View.OnHoverListener() {
			@Override
			public boolean onHover(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_HOVER_ENTER:
					triggerEffect(v, true, color);
					return true;
					case MotionEvent.ACTION_HOVER_EXIT:
					triggerEffect(v, false, color);
					return true;
				}
				return false;
			}
		});
		
		view.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
					triggerEffect(v, true, color);
					break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
					triggerEffect(v, false, color);
					break;
				}
				return false;
			}
		});
	}
	
	private void triggerEffect(View v, boolean active, int color) {
		float targetScale = active ? 0.95f : 1.0f;
		float targetAlpha = active ? 0.8f : 1.0f;
		
		v.animate()
		.scaleX(targetScale)
		.scaleY(targetScale)
		.alpha(targetAlpha)
		.setDuration(120)
		.start();
		
		if (active) {
			GradientDrawable shape = new GradientDrawable();
			shape.setShape(GradientDrawable.RECTANGLE);
			shape.setCornerRadius(15f); 
			shape.setColor(adjustAlpha(color, 0.35f));
			v.setForeground(shape);
		} else {
			v.setForeground(null);
		}
	}
	
	private int adjustAlpha(int color, float factor) {
		int alpha = Math.round(Color.alpha(color) * factor);
		int red = Color.red(color);
		int green = Color.green(color);
		int blue = Color.blue(color);
		return Color.argb(alpha, red, green, blue);
	}
	
	public void updateData(List<BrushItem> newItems) {
		this.items.clear();
		this.items.addAll(newItems);
		selectedPosition = -1;
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).filePath != null && items.get(i).filePath.equals(selectedFilePath)) {
				selectedPosition = i;
				break;
			}
		}
		notifyDataSetChanged();
	}
	
	static class BrushViewHolder extends RecyclerView.ViewHolder {
		final ImageView image;
		final TextView name;
		
		BrushViewHolder(View v) {
			super(v);
			image = v.findViewById(R.id.img_brush_preview);
			name = v.findViewById(R.id.txt_brush_name);
		}
	}
	
	static class ImportViewHolder extends RecyclerView.ViewHolder {
		ImportViewHolder(View v) {
			super(v);
		}
	}
}
