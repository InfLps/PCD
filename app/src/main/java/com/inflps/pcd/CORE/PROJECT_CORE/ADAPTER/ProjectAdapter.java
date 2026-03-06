package com.inflps.pcd.CORE.PROJECT_CORE.ADAPTER;

import android.content.Context;
import android.graphics.drawable.*;
import android.graphics.Color;
import android.graphics.Outline;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inflps.pcd.R;
import java.util.ArrayList;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {
	
	private static final int TYPE_NEW_BUTTON = 0;
	private static final int TYPE_PROJECT_ITEM = 1;
	
    private String highlight = "#353535"; 
    
	private List<ProjectItem> projectList;
	private final OnProjectActionListener actionListener;
	private final Context context;
	
	public interface OnProjectActionListener {
		void onProjectOpen(ProjectItem item);
		void onRename(ProjectItem item);
		void onExport(ProjectItem item);
		void onDelete(ProjectItem item);
	}
	
	public ProjectAdapter(Context context, List<ProjectItem> projectList, OnProjectActionListener actionListener) {
		this.context = context;
		this.projectList = projectList;
		this.actionListener = actionListener;
	}
	
	@Override
	public int getItemViewType(int position) {
		return projectList.get(position).isNewButton ? TYPE_NEW_BUTTON : TYPE_PROJECT_ITEM;
	}
	
	@NonNull
	@Override
	public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(context).inflate(R.layout.item_project_grid, parent, false);
		return new ProjectViewHolder(view);
	}
	
	@Override
	public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
		ProjectItem item = projectList.get(position);
		
		holder.itemView.setOnClickListener(v -> actionListener.onProjectOpen(item));
		applyHoverEffect(holder.itemView, highlight);
        
		if (getItemViewType(position) == TYPE_NEW_BUTTON) {
			holder.title.setText("New Project");
			holder.thumbnail.setImageResource(R.drawable.add_box_btn);
			holder.moreBtn.setVisibility(View.GONE);
			holder.thumbnail.setClipToOutline(false);
		} else {
			holder.title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
			holder.title.setMarqueeRepeatLimit(-1);
			holder.title.setHorizontallyScrolling(true);
			holder.title.setSelected(true);
			holder.title.setText(item.title);
			holder.moreBtn.setVisibility(View.VISIBLE);
            applyHoverEffect(holder.moreBtn, highlight);
			
			if (item.thumbnail != null) {
				holder.thumbnail.setImageBitmap(item.thumbnail);
				holder.thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
			} else {
				holder.thumbnail.setBackgroundColor(Color.LTGRAY);
			}
			
			holder.thumbnail.setClipToOutline(true);
			applyCustomBackground(holder.moreBtn, 15, 0xE6111111);
			holder.moreBtn.setOnClickListener(v -> showCustomMenu(v, item));
		}
	}
	
	private void showCustomMenu(View anchor, ProjectItem item) {
		View layout = LayoutInflater.from(context).inflate(R.layout.menu_project_item, null);
		
		PopupWindow popup = new PopupWindow(layout, 
		WindowManager.LayoutParams.WRAP_CONTENT, 
		WindowManager.LayoutParams.WRAP_CONTENT, 
		true);
		
		popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		popup.setElevation(10);
		
		LinearLayout background = layout.findViewById(R.id.background);
		LinearLayout rename = layout.findViewById(R.id.action_rename);
		LinearLayout export = layout.findViewById(R.id.action_export);
		LinearLayout delete = layout.findViewById(R.id.action_delete);
		
		background.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b, int c, int d) { this.setCornerRadius(a); this.setStroke(b, c); this.setColor(d); return this; } }.getIns((int)15, (int)1, 0xFF424242, 0xE9111111));
		applyHoverEffect(rename, highlight);
        applyHoverEffect(export, highlight);
        applyHoverEffect(delete, highlight);
        
		rename.setOnClickListener(v -> {
			actionListener.onRename(item);
			popup.dismiss();
		});
		
		export.setOnClickListener(v -> {
			actionListener.onExport(item);
			popup.dismiss();
		});
		
		delete.setOnClickListener(v -> {
			actionListener.onDelete(item);
			popup.dismiss();
		});

		popup.showAsDropDown(anchor);
	}
	
	private void applyCustomBackground(View view, int radius, int color) {
		GradientDrawable shape = new GradientDrawable();
		shape.setCornerRadius(radius);
		shape.setColor(color);
		view.setBackground(shape);
		view.setClipToOutline(true);
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

	@Override
	public int getItemCount() {
		return projectList.size();
	}
	
	public static class ProjectViewHolder extends RecyclerView.ViewHolder {
		public ImageView thumbnail;
		public TextView title;
		public ImageView moreBtn;
		
		public ProjectViewHolder(@NonNull View itemView) {
			super(itemView);
			thumbnail = itemView.findViewById(R.id.project_thumbnail);
			title = itemView.findViewById(R.id.project_title);
			moreBtn = itemView.findViewById(R.id.more_btn);
			
			thumbnail.setOutlineProvider(new ViewOutlineProvider() {
				@Override
				public void getOutline(View view, Outline outline) {
					outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 25f);
				}
			});
		}
	}
}