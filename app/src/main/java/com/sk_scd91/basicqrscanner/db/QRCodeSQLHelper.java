package com.sk_scd91.basicqrscanner.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * (c) 2017 Sean Deneen
 */

public class QRCodeSQLHelper extends SQLiteOpenHelper {
    public static final int VERSION = 3;
    private static final String DBNAME = "qrcodes.db";

    public QRCodeSQLHelper(Context context) {
        super(context, DBNAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(QRDB.CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + QRDB.NAME);
        onCreate(db);
    }
}
