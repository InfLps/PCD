package com.inflps.pcd.CORE.PROJECT_CORE.ADAPTER.DIFF;

import androidx.recyclerview.widget.DiffUtil;
import java.util.List;

import com.inflps.pcd.CORE.PROJECT_CORE.ADAPTER.ProjectItem; 

public class ProjectDiffCallback extends DiffUtil.Callback {

    private final List<ProjectItem> oldList;
    private final List<ProjectItem> newList;

    public ProjectDiffCallback(List<ProjectItem> oldList, List<ProjectItem> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).title.equals(newList.get(newItemPosition).title);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        ProjectItem oldItem = oldList.get(oldItemPosition);
        ProjectItem newItem = newList.get(newItemPosition);
        
        return oldItem.title.equals(newItem.title) && 
               oldItem.isNewButton == newItem.isNewButton &&
               oldItem.thumbnail == newItem.thumbnail;
    }
}
