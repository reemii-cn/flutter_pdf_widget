/*
 * Copyright (C) 2016 Bartosz Schiller.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.flutter_reader_pdf_widget.lib.source;


import android.content.Context;
import android.os.ParcelFileDescriptor;

import com.example.flutter_reader_pdf_widget.lib.util.FileUtils;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.IOException;

public class AssetSource implements DocumentSource {

    private final String assetName;

    public AssetSource(String assetName) {
        this.assetName = assetName;
    }

    @Override
    public PdfDocument createDocument(Context context, PdfiumCore core, String password) throws IOException {
        File f = FileUtils.fileFromAsset(context, assetName);
        ParcelFileDescriptor pfd = ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
        return core.newDocument(pfd, password);
    }
}
