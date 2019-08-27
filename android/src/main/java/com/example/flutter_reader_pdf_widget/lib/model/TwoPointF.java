package com.example.flutter_reader_pdf_widget.lib.model;

import android.graphics.PointF;

public class TwoPointF {
    public PointF p1;
    public PointF p2;

    public TwoPointF(float x1, float y1, float x2, float y2) {
        p1 = new PointF(x1, y1);
        p2 = new PointF(x2, y2);
    }

    public TwoPointF(PointF p1, PointF p2) {
        this.p1 = p1;
        this.p2 = p2;
    }
}
