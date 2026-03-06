package com.inflps.pcd.CORE.PROJECT_CORE.ADAPTER;

import android.graphics.Bitmap;

public class ProjectItem {
    public String title;
    public String path;
    public Bitmap thumbnail;
    public boolean isNewButton;

    public ProjectItem(String title, String path, Bitmap thumbnail) {
        this.title = title;
        this.path = path;
        this.thumbnail = thumbnail;
        this.isNewButton = false;
    }

    public ProjectItem() {
        this.isNewButton = true;
    }
}
