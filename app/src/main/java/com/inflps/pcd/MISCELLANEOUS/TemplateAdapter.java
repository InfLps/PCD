package com.inflps.pcd.MISCELLANEOUS;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.inflps.pcd.R;
import java.util.List;
import android.graphics.Color;
import android.graphics.drawable.*;
import android.view.MotionEvent;

public class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.ViewHolder> {

    private List<TemplateModel> list;
    private OnTemplateClickListener listener;
    private String highlight = "#353535"; 
    
    private int selectedPosition = 1; 

    public interface OnTemplateClickListener {
        void onTemplateClick(TemplateModel template);
    }

    public TemplateAdapter(List<TemplateModel> list, OnTemplateClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_template, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TemplateModel item = list.get(position);
        
        holder.title.setText(item.getTitle() != null ? item.getTitle() : "Unknown");
        
        int resId = item.getImageResId();
        if (resId != 0) {
            try {
                holder.icon.setImageResource(resId);
            } catch (Exception e) {
                holder.icon.setImageResource(R.drawable.app_ic); 
            }
        } else {
            holder.icon.setImageResource(R.drawable.app_ic);
        }
        
        if (position == selectedPosition) {
            holder.itemView.setBackgroundResource(R.drawable.selected_item_grid);
        } else {
            holder.itemView.setBackgroundResource(android.R.color.transparent);
        }
        
        applyHoverEffect(holder.itemView, highlight);
        
        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onTemplateClick(item);
            }
        });
    }

    @Override
    public int getItemCount() { 
        return list.size(); 
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;

        public ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.img_template);
            title = itemView.findViewById(R.id.txt_template_name);
        }
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
