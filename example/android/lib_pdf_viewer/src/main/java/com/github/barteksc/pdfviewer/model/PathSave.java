package com.github.barteksc.pdfviewer.model;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PathSave {
    public PointF start;

    public PointF end;
    public List<TwoPointF> moves;

    private int paintColor;
    private int paintWidth;
    private boolean isEraser;

    //绘制区域的匡高
    public float mStandardW;
    public float mStandardH;

    //偏移
    private float oX = 0F;
    private float oY = 0F;


    public PathSave() {
        start = new PointF(0f, 0f);
        end = new PointF(0f, 0f);
        moves = new ArrayList<>();
    }

    public void initDrawArea(float width, float height) {
        this.mStandardW = width;
        this.mStandardH = height;
    }

    public void setOffset(float x, float y) {
        Log.e("偏移", "x: " + x);
        this.oX = x;
        this.oY = y;
    }

    public void setStart(float x, float y) {
        start.x = x;
        start.y = y;
    }

    public void setEnd(float x, float y) {
        end.x = x;
        end.y = y;
    }

    public void addMove(float x1, float y1, float x2, float y2) {
        moves.add(new TwoPointF(x1, y1, x2, y2));
    }

    public Path generatePath() {
        Path path = new Path();
        path.moveTo(start.x, start.y);

        for (TwoPointF next : moves) {
            path.quadTo(next.p1.x, next.p1.y, next.p2.x, next.p2.y);
        }

        path.lineTo(end.x, end.y);

        return path;
    }

    public Path generatePath(float zoom) {
        Path path = new Path();
        path.moveTo(start.x * zoom, start.y * zoom);

        for (TwoPointF next : moves) {
            path.quadTo(next.p1.x * zoom, next.p1.y * zoom, next.p2.x * zoom, next.p2.y * zoom);
        }

        path.lineTo(end.x * zoom, end.y * zoom);

        return path;
    }

    /**
     * todo
     * 适配多分辨率的笔记
     *
     * @param zoom
     * @param zoomW
     * @param zoomH
     * @return
     */
    public Path generatePath(float zoom, float zoomW, float zoomH) {
        Path path = new Path();
        path.moveTo(start.x * zoom * zoomW , start.y * zoom * zoomH);

        for (TwoPointF next : moves) {
            path.quadTo(next.p1.x * zoom * zoomW,
                    next.p1.y * zoom * zoomH,
                    next.p2.x * zoom * zoomW,
                    next.p2.y * zoom * zoomH);
        }
        path.lineTo(end.x * zoom * zoomW , end.y * zoom * zoomH);

        return path;
    }

    public Paint generatePaint() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(paintWidth);

        if (isEraser) {
            paint.setColor(Color.parseColor("#00000000"));
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));
        } else {
            paint.setColor(paintColor);
        }
        return paint;
    }

    public void setPaint(int color, int width, boolean isEraser) {
        this.isEraser = isEraser;
        this.paintColor = color;
        this.paintWidth = width;
    }
}
