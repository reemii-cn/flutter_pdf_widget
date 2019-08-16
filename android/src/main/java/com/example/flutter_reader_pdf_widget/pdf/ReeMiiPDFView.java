package com.example.flutter_reader_pdf_widget.pdf;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.flutter_reader_pdf_widget.R;
import com.github.barteksc.pdfviewer.DrawSurface;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnDrawListener;

import java.io.File;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;

public class ReeMiiPDFView implements PlatformView, MethodChannel.MethodCallHandler {

    final String METHOD_GET_LINE_PATH = "getLinePath";
    final String METHOD_GET_CURRENT_PAGE = "getCurrentPage";
    final String METHOD_GET_CURRENT_PAGE_COUNT = "getCurrentPageCount";
    final String METHOD_EDIT = "edit";
    final String METHOD_CHANGE_LINE_SIZE = "changeLineSize";
    final String METHOD_CHANGE_LINE_COLOR = "changeLineColor";

    private Context mContext;
    private BinaryMessenger mMessenger;
    private int mId;

    private FrameLayout mRootView;
    private PDFView mPDFView;
    private DrawSurface mDrawView;

    private boolean isDrawMode = false;


    public ReeMiiPDFView(Context context, BinaryMessenger messenger, int id) {
        this.mContext = context;
        this.mMessenger = messenger;
        this.mId = id;

        mRootView = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.view_pdf_view, null);
        mPDFView = mRootView.findViewById(R.id.view_pdf);
        mDrawView = mRootView.findViewById(R.id.view_draw);

        initChannel();

        initPDF();
    }

    @Override
    public View getView() {
        return mRootView;
    }

    @Override
    public void dispose() {

    }

    private void initChannel() {
        MethodChannel channel = new MethodChannel(mMessenger, "flutter_reader_pdf_widget_".concat(String.valueOf(mId)));
        channel.setMethodCallHandler(this);
    }

    private void initPDF() {
        openFile();
    }

    private void openFile() {
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        filePath = filePath.concat("/reemii/pdf/c20ad4d76fe97759aa27a0c99bff6710/1522316469836.pdf");
        mPDFView.fromFile(new File(filePath))
                .enableDoubletap(false)
                .onDraw(new OnDrawListener() {
                    @Override
                    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage, float zoom, float xOffset, float yOffset) {
                        //todo 对于不同屏幕的画布大小  尤其是宽度获与屏幕尺寸不一致会，所以页面大小比较关键
                        mDrawView.setPDFSize(pageWidth, pageHeight, xOffset, yOffset, mPDFView.getCurrentPage() + 1);
                        mDrawView.drawTMD(canvas, zoom);
                    }
                })
                .load();
        mDrawView.setOnDrawCallback(new DrawSurface.OnDrawCallback() {
            @Override
            public void onDraw() {
                mPDFView.invalidate();
            }
        });
    }

    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        switch (methodCall.method) {
            case METHOD_GET_CURRENT_PAGE:
                result.success(mPDFView.getCurrentPage());
                break;
            case METHOD_GET_CURRENT_PAGE_COUNT:
                result.success(mPDFView.getPageCount());
                break;
            case METHOD_GET_LINE_PATH:
                result.success(mDrawView.getDrawHistory());
                break;
            case METHOD_EDIT:
                isDrawMode = !isDrawMode;
                mDrawView.setIsDrawMode(isDrawMode);
                result.success(null);
                break;
            case METHOD_CHANGE_LINE_COLOR:
                String color = methodCall.arguments();
                int colorParsed = Color.parseColor(color);
                mDrawView.setLineColor(colorParsed);
                result.success(null);
                break;
            case METHOD_CHANGE_LINE_SIZE:
                int width = methodCall.arguments();
                mDrawView.setLineStyle(width);
                result.success(null);
                break;
        }
    }
}
