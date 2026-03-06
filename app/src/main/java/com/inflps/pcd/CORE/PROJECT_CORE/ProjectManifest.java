package com.inflps.pcd.CORE.PROJECT_CORE;

import java.util.ArrayList;
import java.util.List;

public class ProjectManifest {
    public int width;
    public int height;
    public int color;
    public int currentLayerId;
    public List<LayerMeta> layers = new ArrayList<>();

    public static class LayerMeta {
        public int id;
        public boolean isVisible;
        public boolean isLocked;
        public String blendMode;
    }
}