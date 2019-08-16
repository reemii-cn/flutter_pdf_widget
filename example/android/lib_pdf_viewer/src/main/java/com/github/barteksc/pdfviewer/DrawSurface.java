package com.github.barteksc.pdfviewer;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;

import com.github.barteksc.pdfviewer.model.PathSave;
import com.github.barteksc.pdfviewer.model.TwoPointF;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created by nova on 26/03/2018.
 */

public class DrawSurface extends SurfaceView {

    private Paint paint;
    private Paint paintEraser;
    private PointF pointPre;
    private Path path;
    private PathSave mPathSave;

    private List<PathSave> mListRedoPath;
    private List<PathSave> mListPathSave;

    private int W;
    private int H;


    private int mLineWidth = 5;
    private final int mEraserLineWidth = 50;
    private int mLineColor = Color.parseColor("#50616D");
    private boolean isEraserMode = false;
    private boolean isDrawMode = false;
    private boolean isScaled = false;

    public float realHeight = 0F;
    public float realWidth = 0F;

    private float diffY = 0;
    private float diffX = 0;

    private OnDrawCallback onDrawCallback;


    public DrawSurface(Context context) {
        super(context);
        init();
    }

    public DrawSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawSurface(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DrawSurface(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setBackgroundColor(Color.TRANSPARENT);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(mLineWidth);
        paint.setColor(mLineColor);

        // 橡皮擦
        paintEraser = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintEraser.setStyle(Paint.Style.STROKE);
        paintEraser.setStrokeWidth(50);
        paintEraser.setColor(Color.parseColor("#00000000"));
        paintEraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        pointPre = new PointF();
        mListRedoPath = new ArrayList<>();
        mListPathSave = new ArrayList<>();

        load();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isDrawMode) {
            return false;
        }
        //竖直方向的偏移
        if (pH > 0 && realHeight > 0) {
            diffY = realHeight - pH - oY;
        }
        //水平方向的偏移
        if (pW > 0 && realWidth > 0) {
            diffX = realWidth - pW - (pW * (page - 1) + oX);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path = new Path();
                mPathSave = new PathSave();

                //初始化回执区域的大小
                mPathSave.initDrawArea(pW, pH);

                pointPre.x = event.getX() - diffX;
                pointPre.y = event.getY() - diffY;

                mPathSave.setStart(event.getX() - diffX, event.getY() - diffY);

                path.moveTo(pointPre.x, pointPre.y);

                detectIfDelete(event.getX(), event.getY() - diffY);
                break;
            case MotionEvent.ACTION_MOVE:
                PointF pointNew = new PointF(event.getX() - diffX, event.getY() - diffY);
                // Bezier
                // 这里注意，和lineTo类似，quadTo之后，path的位置在最后一个点
                // 所以每一次quadTo的时候，Bezier曲线的起始点就是前两个点的中点，终点是当前的点
                path.quadTo(pointPre.x, pointPre.y, (pointPre.x + pointNew.x) / 2, (pointPre.y + pointNew.y) / 2);
                // 存储
                mPathSave.addMove(pointPre.x, pointPre.y, (pointPre.x + pointNew.x) / 2, (pointPre.y + pointNew.y) / 2);
                pointPre.x = pointNew.x;
                pointPre.y = pointNew.y;

                detectIfDelete(event.getX() - diffX, event.getY() - diffY);
                break;
            case MotionEvent.ACTION_UP:
                // 最后再连起来剩下的半段 这个时候是直线 用lineTo就好了
                path.lineTo(event.getX() - diffX, event.getY() - diffY);
                mPathSave.setEnd(event.getX() - diffX, event.getY() - diffY);

                if (!isEraserMode) {
                    mPathSave.setPaint(mLineColor, mLineWidth, false);
                    mListPathSave.add(mPathSave);
                    // 落笔即删除备份
                    mListRedoPath.clear();
                } else {
                    detectIfDelete(event.getX() - diffX, event.getY() - diffY);
                }

                break;
        }
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        this.realWidth = canvas.getWidth();
        this.realHeight = canvas.getHeight();
        if (onDrawCallback != null) {
            onDrawCallback.onDraw();
        }
    }

    public interface OnDrawCallback {
        void onDraw();
    }

    private float pW = 0F;
    private float pH = 0F;
    private float oX = 0F;
    private float oY = 0F;
    private int page = 0;

    public void setPDFSize(float pageWidth, float pageHeight, float w, float h, int page) {
        Log.e("=====", "页面宽: " + pageWidth + "页面高: " + pageHeight + "x偏移: " + w + "y偏移: " + h);
        pW = pageWidth;
        pH = pageHeight;
        oX = w;
        oY = h;
        if (mPathSave != null) {
            mPathSave.setOffset(w, h);
        }
        this.page = page;
    }

    public void setOnDrawCallback(OnDrawCallback listener) {
        onDrawCallback = listener;
    }

    //绘制入口
    public void drawTMD(Canvas canvas, float zoom) {
//        Log.e("画板参数", "宽: " + canvas.getWidth() + "高: " + canvas.getHeight() + "缩放系数" + zoom);
        for (PathSave ps : mListPathSave) {
//            Log.e("屏幕变化比例", "drawTMD: " + ScreenUtils.getScreenWidth() + "||" + ps.mStandardW + ScreenUtils.getScreenWidth() / ps.mStandardW);
            canvas.drawPath(ps.generatePath(zoom,
                    pW / ps.mStandardW,
                    pH / ps.mStandardH)
                    , ps.generatePaint());
        }
        if (path != null && !isEraserMode) {
            canvas.drawPath(path, paint);
        }
    }

    private void detectIfDelete(float x, float y) {
        if (!isEraserMode) {
            return;
        }
        Iterator<PathSave> iterator = mListPathSave.iterator();
        while (iterator.hasNext()) {
            PathSave next = iterator.next();
            if (hasTouchLine(next, x, y)) {
                mListRedoPath.add(mListRedoPath.size(), next);
                iterator.remove();
            }
        }
    }

    public void undo() {
        if (mListPathSave.size() > 0) {
            path = null;
            mListRedoPath.add(mListPathSave.get(mListPathSave.size() - 1));
            mListPathSave.remove(mListPathSave.size() - 1);
            invalidate();
        }
    }

    public void redo() {
        if (mListRedoPath.size() > 0) {
            mListPathSave.add(mListRedoPath.get(mListRedoPath.size() - 1));
            mListRedoPath.remove(mListRedoPath.size() - 1);
            invalidate();
        }
    }

    public void setLineStyle(int value) {
        mLineWidth = value;

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(mLineWidth);
        paint.setColor(mLineColor);
    }

    public void setLineColor(int color) {
//        mLineColor = ResUtils.getColor(color, getContext());
        mLineColor = color;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(mLineWidth);
        paint.setColor(mLineColor);
    }

    public boolean enableEraser() {
        isEraserMode = !isEraserMode;
        return isEraserMode;
    }

    public void save() {
//        TokenManager.getInstance().setString(TokenManager.KEY_TEST_PATH, new Gson().toJson(mListPathSave));
    }

    private void load() {
//        String string = TokenManager.getInstance().getStringDefaultEmpty(TokenManager.KEY_TEST_PATH);
//
//        if (!TextUtils.isEmpty(string)) {
//            mListPathSave = new Gson().fromJson(string, new TypeToken<List<PathSave>>() {
//            }.getType());
//            invalidate();
//        }
    }

    public void setDrawHistory(String json) {
        path = null;
        if (!TextUtils.isEmpty(json)) {
            //解析出笔记
            mListPathSave = new Gson().fromJson(json, new TypeToken<List<PathSave>>() {
            }.getType());
            //更重绘
            invalidate();
        }
    }

    public String getDrawHistory() {
        return new Gson().toJson(mListPathSave);
    }

    private boolean hasTouchLine(PathSave p, float x, float y) {
        if (hasTouch(p.start.x, p.start.y, x, y)) {
            return true;
        }

        if (hasTouch(p.end.x, p.end.y, x, y)) {
            return true;
        }

        for (TwoPointF tpf : p.moves) {
            if (hasTouch(2 * (tpf.p2.x) - tpf.p1.x, 2 * (tpf.p2.y) - tpf.p1.y, x, y)) {
                return true;
            }
        }

        return false;
    }

    private final boolean hasTouch(float x1, float y1, float x2, float y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)) < mEraserLineWidth;
    }

    public void setIsDrawMode(boolean isDrawMode) {
        this.isDrawMode = isDrawMode;
    }

    private float mZoom;
    private PointF mZoomPoint;

    public void scaleTo(float zoom, PointF pointF) {
        mZoom = zoom;
        mZoomPoint = pointF;
        isScaled = true;
        invalidate();
    }

    public void resetScale() {
//        isScaled = false;
//        invalidate();

    }
}
