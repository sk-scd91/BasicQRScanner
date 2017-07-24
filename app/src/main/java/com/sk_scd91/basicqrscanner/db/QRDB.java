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

import android.content.ContentValues;
import android.database.ContentObservable;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.gms.vision.barcode.Barcode;

/**
 * Schema and utility functions for the QR code database.
 */
public final class QRDB {
    public static final String NAME = "QRCODES";
    public static final class Columns {
        public static final String ID = "ID";
        public static final String TIMESTAMP = "TIMESTAMP"; // For sorting by latest
        public static final String TYPE = "TYPE";
        public static final String DISPLAY_TEXT = "DISPLAY_TEXT";
        public static final String RAW_TEXT = "RAW_TEXT";
    }

    public static final String CREATE = "CREATE TABLE " + NAME + "("
            + Columns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + Columns.TIMESTAMP + " INTEGER NOT NULL, "
            + Columns.TYPE + " INTEGER NOT NULL DEFAULT " + Barcode.TEXT + ", "
            + Columns.DISPLAY_TEXT + " TEXT, "
            + Columns.RAW_TEXT + " TEXT"
            + ")";

    private QRDB() {
        // empty private constructor.
    }

    private static final ContentObservable qrdbObservable = new ContentObservable();

    /**
     * Get a content observable to notify when an item is added or removed from the database.
     *
     * @return the {@link ContentObservable} to register database changes for.
     */
    static ContentObservable getQrdbObservable() {
        return qrdbObservable;
    }

    /**
     * Insert the given values into the database, or update the timestamp if the row already exists.
     *
     * @param db The SQLite database to insert to.
     * @param type Type id for the barcode's value.
     * @param displayText The barcode's text encoded in a user friendly way.
     * @param rawText The barcode's raw value.
     * @return The id of the newly inserted or recently updated row.
     */
    public static long insertToDB(SQLiteDatabase db, int type, String displayText, String rawText) {
        long timestamp = System.currentTimeMillis();

        // If there is already a matching row inserted, just update it with the new timestamp.
        Cursor cursor = db.query(NAME, new String[] {Columns.ID},
                Columns.RAW_TEXT + " = ? AND " + Columns.TYPE + " = ?",
                new String[]{rawText, String.valueOf(type)},
                null, null, null);
        try {
            if (cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndex(Columns.ID));
                if (cursor.moveToNext()) { // Delete duplicates.
                    db.delete(NAME, Columns.ID + " != ?", new String[]{String.valueOf(id)});
                }
                ContentValues cv = new ContentValues();
                cv.put(Columns.TIMESTAMP, timestamp);
                db.update(NAME, cv, Columns.ID + " = ?", new String[]{String.valueOf(id)});
                qrdbObservable.dispatchChange(false, null); // Notify the database changed.
                return id;
            }
        } finally {
            cursor.close();
        }

        ContentValues cv = new ContentValues();
        cv.put(Columns.TIMESTAMP, timestamp);
        cv.put(Columns.TYPE, type);
        cv.put(Columns.DISPLAY_TEXT, displayText);
        cv.put(Columns.RAW_TEXT, rawText);

        long returnId = db.insert(NAME, null, cv);
        qrdbObservable.dispatchChange(false, null);
        return returnId;
    }

    /**
     * Insert the barcode into the database, or update its timestamp if the row already exists.
     *
     * @param db The SQLite database to insert to.
     * @param barcode The barcode to insert into the database.
     * @return The id of the newly inserted or recently updated row.
     */
    public static long insertToDB(SQLiteDatabase db, Barcode barcode) {
        return insertToDB(db, barcode.valueFormat, barcode.displayValue, barcode.rawValue);
    }

    /**
     * Delete rows from the database with the matching type and raw text.
     *
     * @param db The SQLite database to insert to.
     * @param type Type id for the barcode's value.
     * @param rawText The barcode's raw value.
     * @return The number of rows deleted matching the criteria.
     */
    public static int deleteFromDB(SQLiteDatabase db, int type, String rawText) {
        int deleteCount = db.delete(NAME, Columns.RAW_TEXT + " = ? AND " + Columns.TYPE + " = ?",
                new String[]{rawText, String.valueOf(type)});
        qrdbObservable.dispatchChange(false, null);
        return deleteCount;
    }

    /**
     * Deletes rows from the database matching the values of a Barcode.
     *
     * @param db The SQLite database to insert to.
     * @param barcode The barcode to delete from the database.
     * @return The number of rows deleted matching the barcode.
     */
    public static int deleteFromDB(SQLiteDatabase db, Barcode barcode) {
        return deleteFromDB(db, barcode.valueFormat, barcode.rawValue);
    }

    /**
     * Retrieve a {@link Barcode} object from the database.
     *
     * @param cursor The cursor that
     * @return A barcode object decoded from the database containing just the valueFormat, displayValue,
     * and rawValue fields.
     */
    public static Barcode getBarcodeFromCursor(Cursor cursor) {
        Barcode result = new Barcode();

        result.valueFormat = cursor.getInt(cursor.getColumnIndex(Columns.TYPE));
        result.displayValue = cursor.getString(cursor.getColumnIndex(Columns.DISPLAY_TEXT));
        result.rawValue = cursor.getString(cursor.getColumnIndex(Columns.RAW_TEXT));

        return result;
    }
}
