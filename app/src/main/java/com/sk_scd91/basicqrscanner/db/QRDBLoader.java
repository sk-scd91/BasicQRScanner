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

    public QRDBLoader(Context context) {
        super(context.getApplicationContext());
        sqlHelper = new QRCodeSQLHelper(context.getApplicationContext());
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
        }
        return barcodes;
    }
}
