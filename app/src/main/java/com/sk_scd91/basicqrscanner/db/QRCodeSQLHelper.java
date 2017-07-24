package com.sk_scd91.basicqrscanner.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

import com.google.android.gms.vision.barcode.Barcode;

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

/**
 * A {@link SQLiteOpenHelper} for the QR Code database.
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

    public AsyncTask<Void, Void, Void> insertAsync(Barcode barcode) {
        return new DBTask(DBTask.INSERT_FLAG, this, barcode);
    }

    public AsyncTask<Void, Void, Void> deleteAsync(Barcode barcode) {
        return new DBTask(DBTask.DELETE_FLAG, this, barcode);
    }

    private static class DBTask extends AsyncTask<Void, Void, Void> {
        public static final int INSERT_FLAG = 0;
        public static final int DELETE_FLAG = 1;

        private final int mOpFlag;
        private final SQLiteOpenHelper mSqlHelper;
        private final Barcode mBarcode;

        public DBTask(int opFlag, SQLiteOpenHelper sqlHelper, Barcode barcode) {
            super();
            mOpFlag = opFlag;
            mSqlHelper = sqlHelper;
            mBarcode = barcode;
        }

        @Override
        protected Void doInBackground(Void... params) {

            SQLiteDatabase db = mSqlHelper.getWritableDatabase();
            try {
                switch (mOpFlag) {
                    case INSERT_FLAG:
                        QRDB.insertToDB(db, mBarcode);
                        break;
                    case DELETE_FLAG:
                        QRDB.deleteFromDB(db, mBarcode);
                        break;
                }
            } finally {
                db.close();
            }

            return null;
        }
    }

}
