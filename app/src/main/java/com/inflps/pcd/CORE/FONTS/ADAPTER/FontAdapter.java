package com.inflps.pcd.CORE.FONTS.ADAPTER;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.graphics.Typeface;
import android.text.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.MotionEvent;
import android.text.style.*;
import android.util.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inflps.pcd.R;
import java.util.List;

public class FontAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private static final int TYPE_IMPORT = 0;
	private static final int TYPE_FONT = 1;
	
	private int selectedPosition = RecyclerView.NO_POSITION;
	
	private String highlight = "#353535"; 
	
	private List<FontItem> fontList;
	private OnFontClickListener listener;
	
	public interface OnFontClickListener {
		void onFontClick(FontItem item);
		void onImportClick();
		void onFontLongClick(FontItem item, int position);
	}
	
	
	public FontAdapter(List<FontItem> list, OnFontClickListener listener) {
		this.fontList = list;
		this.listener = listener;
	}
	
	@Override
	public int getItemViewType(int position) {
		return fontList.get(position).isImportButton ? TYPE_IMPORT : TYPE_FONT;
	}
	
	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		if (viewType == TYPE_IMPORT) {
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_import_button, parent, false);
			return new ImportViewHolder(v);
		} else {
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_font_preview, parent, false);
			return new FontViewHolder(v);
		}
	}
	
	public void setSelectedPosition(int position) {
		int previousPosition = selectedPosition;
		selectedPosition = position;
		notifyItemChanged(previousPosition);
		notifyItemChanged(selectedPosition);
	}
	
	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		FontItem item = fontList.get(position);
		
		applyHoverEffect(holder.itemView, highlight);
		
		if (holder instanceof FontViewHolder) {
			FontViewHolder fv = (FontViewHolder) holder;
			if (position == selectedPosition) {
				fv.itemView.setBackgroundResource(R.drawable.selected_item_grid);
			} else {
				fv.itemView.setBackgroundResource(0); 
			}
			fv.fileName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
			fv.fileName.setMarqueeRepeatLimit(-1);
			fv.fileName.setHorizontallyScrolling(true);
			fv.fileName.setSelected(true);
			
			fv.fileName.setText(item.fontName);
			
			fv.textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
			fv.textView.setMarqueeRepeatLimit(-1);
			fv.textView.setHorizontallyScrolling(true);
			fv.textView.setSelected(true);
			fv.textView.setTypeface(item.typeface);
			
			fv.itemView.setOnClickListener(v -> {
				setSelectedPosition(holder.getAdapterPosition());
				listener.onFontClick(item);
			});
			
			fv.itemView.setOnLongClickListener(v -> {
				if (item.filePath != null) {
					listener.onFontLongClick(item, holder.getAdapterPosition());
					return true;
				}
				return false;
			});
		} else {
			holder.itemView.setBackgroundResource(0);
			holder.itemView.setOnClickListener(v -> listener.onImportClick());
		}
	}
	
	
	@Override
	public int getItemCount() { return fontList.size(); }
	
	static class FontViewHolder extends RecyclerView.ViewHolder {
		TextView textView;
		TextView fileName;
		FontViewHolder(View v) { super(v); 
			textView = v.findViewById(R.id.fontPreviewText);
			fileName = v.findViewById(R.id.fontNameText); 
		}
	}
	
	static class ImportViewHolder extends RecyclerView.ViewHolder {
		ImportViewHolder(View v) { super(v); }
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
}
