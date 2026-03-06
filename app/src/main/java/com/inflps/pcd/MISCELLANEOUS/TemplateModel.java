package com.inflps.pcd.MISCELLANEOUS;

public class TemplateModel {
    private String title;
    private int imageResId;
    private int width;
    private int height;

    public TemplateModel(String title, int imageResId, int width, int height) {
        this.title = title;
        this.imageResId = imageResId;
        this.width = width;
        this.height = height;
    }

    public String getTitle() { return title; }
    public int getImageResId() { return imageResId; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
