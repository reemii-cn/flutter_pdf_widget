package com.github.barteksc.pdfviewer.model;

import android.graphics.PointF;

public class TwoPointF {
    public PointF p1;
    public PointF p2;

    public TwoPointF(float x1, float y1, float x2, float y2) {
        p1 = new PointF(x1, y1);
        p2 = new PointF(x2, y2);
    }
}
