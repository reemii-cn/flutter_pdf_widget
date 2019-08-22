package com.example.flutter_reader_pdf_widget.pdf;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.example.flutter_reader_pdf_widget.R;
import com.example.flutter_reader_pdf_widget.lib.DrawSurface;
import com.example.flutter_reader_pdf_widget.lib.PDFView;
import com.example.flutter_reader_pdf_widget.lib.listener.OnDrawListener;
import com.example.flutter_reader_pdf_widget.lib.listener.OnPageChangeListener;
import com.example.flutter_reader_pdf_widget.lib.model.PathSave;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;

public class ReeMiiPDFView implements PlatformView, MethodChannel.MethodCallHandler {
    private final String TAG = "ReeMiiPDFView";

    final String METHOD_GET_LINE_PATH = "getLinePath";
    final String METHOD_GET_CURRENT_PAGE = "getCurrentPage";
    final String METHOD_GET_CURRENT_PAGE_COUNT = "getCurrentPageCount";
    final String METHOD_EDIT = "edit";
    final String METHOD_CHANGE_LINE_SIZE = "changeLineSize";
    final String METHOD_CHANGE_LINE_COLOR = "changeLineColor";

    private Context mContext;
    private BinaryMessenger mMessenger;
    private int mId;
    private MethodChannel mChannel;

    private FrameLayout mRootView;
    private PDFView mPDFView;
    private DrawSurface mDrawView;
    private int mCurrentPage;
    private int mLastPage;

    private boolean isDrawMode = false;

    private String mPDFFilePath;
    private Gson mGson = new Gson();

    public ReeMiiPDFView(Context context, BinaryMessenger messenger, int id, Object o) {
        this.mContext = context;
        this.mMessenger = messenger;
        this.mId = id;
        this.mPDFFilePath = (String) o;

        mRootView = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.activity_pdf, null);
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
        mChannel = new MethodChannel(mMessenger, "flutter_reader_pdf_widget_event");
    }

    private void initPDF() {
        openFile();
    }

    private void openFile() {
//        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
//        filePath = filePath.concat("/reemii/pdf/c20ad4d76fe97759aa27a0c99bff6710/1522316469836.pdf");
        String filePath = mPDFFilePath;
        mPDFView.fromFile(new File(filePath))
                .enableDoubletap(false)
                .onDraw(new OnDrawListener() {
                    @Override
                    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage, float zoom, float xOffset, float yOffset) {
                        //todo 对于不同屏幕的画布大小  尤其是宽度获与屏幕尺寸不一致会，所以页面大小比较关键
                        mDrawView.setPDFSize(pageWidth, pageHeight, xOffset, yOffset, mCurrentPage);
                        mDrawView.drawTMD(canvas, zoom);
                    }
                })
                .beforePageChange(new OnPageChangeListener() {
                    @Override
                    public void onPageChanged(int page, int pageCount) {
                        Log.v(TAG, "beforePageChange " + page);
                        mChannel.invokeMethod(METHOD_GET_LINE_PATH, mDrawView.getDrawHistoryList(), new MethodChannel.Result() {
                            @Override
                            public void success(Object o) {
                                Log.v(TAG, "invokeMethod success " + METHOD_GET_LINE_PATH);

                            }

                            @Override
                            public void error(String s, String s1, Object o) {
                                Log.v(TAG, "invokeMethod error " + METHOD_GET_LINE_PATH);
                                Log.v(TAG, "invokeMethod error " + s);
                                Log.v(TAG, "invokeMethod error " + s1);
                            }

                            @Override
                            public void notImplemented() {
                                Log.v(TAG, "invokeMethod notImplemented " + METHOD_GET_LINE_PATH);
                            }
                        });
                    }
                })
                .onPageChange(new OnPageChangeListener() {
                    @Override
                    public void onPageChanged(int page, int pageCount) {
                        Log.v(TAG, "onPageChanged " + page);
                        mLastPage = mCurrentPage;
                        mCurrentPage = page;

                        mChannel.invokeMethod(METHOD_GET_CURRENT_PAGE, mCurrentPage, new MethodChannel.Result() {
                            @Override
                            public void success(Object o) {
                                Log.v(TAG, "invokeMethod success " + METHOD_GET_CURRENT_PAGE);
                                Log.v(TAG, "invokeMethod success " + o.toString());
                                List<Map<String, Object>> params = (List<Map<String, Object>>) o;
                                List<PathSave> line = new ArrayList<>();
                                for (Map<String, Object> m : params) {
                                    Log.e("PDF-Plugin", m.toString());
                                    line.add(new PathSave(m));
                                }
                                mDrawView.setDrawHistory(mGson.toJson(line));
                            }

                            @Override
                            public void error(String s, String s1, Object o) {
                                Log.v(TAG, "invokeMethod error " + METHOD_GET_CURRENT_PAGE);
                                Log.v(TAG, "invokeMethod error " + s);
                                Log.v(TAG, "invokeMethod error " + s1);
                            }

                            @Override
                            public void notImplemented() {
                                Log.v(TAG, "invokeMethod notImplemented " + METHOD_GET_CURRENT_PAGE);
                            }
                        });
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
            case METHOD_EDIT:
                isDrawMode = !isDrawMode;
                mDrawView.setIsDrawMode(isDrawMode);
                result.success(null);
                break;
            case METHOD_CHANGE_LINE_COLOR:
                String color = methodCall.arguments();
                int colorParsed = Color.parseColor("#" + color);
                mDrawView.setLineColor(colorParsed);
                result.success(null);
                break;
            case METHOD_CHANGE_LINE_SIZE:
                int width = methodCall.arguments();
                if (width == 0) {
                    mDrawView.enableEraser();
                } else {
                    mDrawView.disableEraser();
                    mDrawView.setLineStyle(width);
                }

                result.success(null);
                break;
        }
    }
}
