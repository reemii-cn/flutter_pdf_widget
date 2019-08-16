package com.example.flutter_reader_pdf_widget;

import com.example.flutter_reader_pdf_widget.pdf.PDFViewFactory;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterReaderPdfWidgetPlugin
 */
public class FlutterReaderPdfWidgetPlugin {
    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        registrar.platformViewRegistry().registerViewFactory("flutter_reader_pdf_widget", new PDFViewFactory(registrar.messenger()));
    }
}
