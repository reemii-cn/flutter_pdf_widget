package com.example.flutter_reader_pdf_widget.pdf;

import android.content.Context;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;

public class PDFViewFactory extends PlatformViewFactory {

    private final BinaryMessenger messenger;

    public PDFViewFactory(BinaryMessenger messenger) {
        super(StandardMessageCodec.INSTANCE);
        this.messenger = messenger;
    }

    @Override
    public PlatformView create(Context context, int id, Object o) {
        return new ReeMiiPDFView(context, messenger, id, o);
    }
}
