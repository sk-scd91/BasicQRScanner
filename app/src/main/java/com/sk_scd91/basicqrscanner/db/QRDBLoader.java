package com.sk_scd91.basicqrscanner.db;

/**
 * (c) 2017 Sean Deneen
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
        super(context.getApplicationContext());
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
    protected void onReset() {
        super.onReset();
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
