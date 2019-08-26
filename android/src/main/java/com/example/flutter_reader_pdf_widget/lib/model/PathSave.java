package com.example.flutter_reader_pdf_widget.lib.model;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PathSave {
    private final String TAG = PathSave.class.getSimpleName();

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

    public PathSave(Map<String, Object> params) {
        Log.v(TAG, "fromMap");
        Log.v(TAG, params.toString());
        String color = (String) params.get("paintColor");
        if (color.startsWith("-")) {
            paintColor = Color.parseColor("#".concat(color.substring(1)));
        } else if (color.startsWith("#")) {
            paintColor = Color.parseColor(color);
        } else {
            paintColor = Color.parseColor("#".concat(color));
        }

        paintWidth = (int) (double) params.get("paintWidth");
        mStandardH = (float) (double) params.get("mStandardH");
        mStandardW = (float) (double) params.get("mStandardW");
        isEraser = (boolean) params.get("isEraser");
        oX = params.get("oX") == null ? 0F : (float) params.get("oX");
        oY = params.get("oY") == null ? 0F : (float) params.get("oY");
        Map<String, Object> startMap = (Map<String, Object>) params.get("start");
        Map<String, Object> endMap = (Map<String, Object>) params.get("end");
        start = new PointF(
                startMap.get("dx") == null ? 0F : (int) (double) startMap.get("dx"),
                startMap.get("dy") == null ? 0F : (int) (double) startMap.get("dy")
        );
        end = new PointF(
                endMap.get("dx") == null ? 0F : (int) (double) endMap.get("dx"),
                endMap.get("dy") == null ? 0F : (int) (double) endMap.get("dy")
        );

        List<Map<String, Object>> moves = (List<Map<String, Object>>) params.get("moves");
        TwoPointF tpf;
        this.moves = new ArrayList<>();

        for (int pos = 0, size = moves.size(); pos < size; ) {
            tpf = new TwoPointF(
                    (float) (double) moves.get(pos).get("dx"),
                    (float) (double) moves.get(pos).get("dy"),
                    (float) (double) moves.get(pos + 1).get("dx"),
                    (float) (double) moves.get(pos + 1).get("dy")
            );
            pos += 2;
            this.moves.add(tpf);
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> params = new HashMap<>();

        Log.v(TAG, String.format("#%06X", (0xFFFFFF & paintColor)));
        params.put("paintColor", String.format("%06X", (0xFFFFFF & paintColor)));
        params.put("paintWidth", paintWidth);
        params.put("mStandardH", mStandardH);
        params.put("mStandardW", mStandardW);
        params.put("isEraser", isEraser);
        params.put("oX", oX);
        params.put("oY", oY);
        Map<String, Object> start = new HashMap<>();
        start.put("dx", this.start.x);
        start.put("dy", this.start.y);
        params.put("start", start);

        Map<String, Object> end = new HashMap<>();
        end.put("dx", this.end.x);
        end.put("dy", this.end.y);
        params.put("end", end);

        Map<String, Object> m;
        List<Map<String, Object>> moves = new ArrayList<>();
        for (TwoPointF tfboy : this.moves) {
            m = new HashMap<>();
            m.put("dx", tfboy.p1.x);
            m.put("dy", tfboy.p1.y);
            moves.add(m);

            m = new HashMap<>();
            m.put("dx", tfboy.p2.x);
            m.put("dy", tfboy.p2.y);
            moves.add(m);
        }

        params.put("moves", moves);

        Log.v(TAG, "toMap");
        Log.v(TAG, params.toString());
        return params;
    }

    public void initDrawArea(float width, float height) {
        this.mStandardW = width;
        this.mStandardH = height;
    }

    public void setOffset(float x, float y) {
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
        path.moveTo(start.x * zoom * zoomW, start.y * zoom * zoomH);

        for (TwoPointF next : moves) {
            path.quadTo(next.p1.x * zoom * zoomW,
                    next.p1.y * zoom * zoomH,
                    next.p2.x * zoom * zoomW,
                    next.p2.y * zoom * zoomH);
        }
        path.lineTo(end.x * zoom * zoomW, end.y * zoom * zoomH);

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
