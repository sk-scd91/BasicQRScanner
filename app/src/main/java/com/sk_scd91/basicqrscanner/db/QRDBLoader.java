package com.sk_scd91.basicqrscanner.db;

/**
 * Copyright 2017 Sean Deneen
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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.AsyncTaskLoader;

import com.google.android.gms.vision.barcode.Barcode;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link AsyncTaskLoader} that loads barcode data from the SQLite database in reverse-chronological
 * order.
 */
public class QRDBLoader extends AsyncTaskLoader<List<Barcode>> {
    private final QRCodeSQLHelper sqlHelper;
    private List<Barcode> mBarcodes;
    private ForceLoadContentObserver mContentObserver;

    public QRDBLoader(Context context) {
        super(context);
        sqlHelper = new QRCodeSQLHelper(context.getApplicationContext());
        mContentObserver = new ForceLoadContentObserver();
    }

    @Override
    protected void onStartLoading() {
        QRDB.getQrdbObservable().registerObserver(mContentObserver);
        if (mBarcodes != null)
            deliverResult(mBarcodes);
        if (takeContentChanged() || mBarcodes == null)
            forceLoad();
    }

    @Override
    public List<Barcode> loadInBackground() {
        List<Barcode> barcodes = new ArrayList<>();
        SQLiteDatabase db = sqlHelper.getReadableDatabase();
        Cursor cursor = db.query(QRDB.NAME,
                new String[]{QRDB.Columns.TYPE, QRDB.Columns.DISPLAY_TEXT, QRDB.Columns.RAW_TEXT},
                null, null, null, null,
                QRDB.Columns.TIMESTAMP + " DESC");
        try {
            while (cursor.moveToNext()) {
                barcodes.add(QRDB.getBarcodeFromCursor(cursor));
            }
        } finally {
            cursor.close();
            sqlHelper.close();
        }
        return barcodes;
    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();
        QRDB.getQrdbObservable().unregisterObserver(mContentObserver);
    }

    @Override
    protected void onReset() {
        super.onReset();
        if (isStarted())
            QRDB.getQrdbObservable().unregisterObserver(mContentObserver);
        mBarcodes = null;
    }

    @Override
    public void deliverResult(List<Barcode> barcodes) {
        if (!isReset()) {
            mBarcodes = barcodes;

            if (isStarted())
                super.deliverResult(barcodes);
        }
    }
}
