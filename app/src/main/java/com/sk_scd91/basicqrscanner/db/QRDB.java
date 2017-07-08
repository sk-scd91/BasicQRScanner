package com.sk_scd91.basicqrscanner.db;

/**
 * (c) 2017 Sean Deneen
 */

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.gms.vision.barcode.Barcode;

/**
 * Schema and utility functions for the QR code database.
 */
public final class QRDB {
    public static final String NAME = "QRCODES";
    public static final class Columns {
        public static final String TIMESTAMP = "TIMESTAMP"; // For sorting by latest
        public static final String TYPE = "TYPE";
        public static final String DISPLAY_TEXT = "DISPLAY_TEXT";
        public static final String RAW_TEXT = "RAW_TEXT";
    }

    public static final String CREATE = "CREATE TABLE " + NAME + "("
            + Columns.TIMESTAMP + " INTEGER NOT NULL, "
            + Columns.TYPE + " INTEGER NOT NULL DEFAULT " + Barcode.TEXT + ", "
            + Columns.DISPLAY_TEXT + "TEXT, "
            + Columns.RAW_TEXT + "TEXT"
            + ")";

    private QRDB() {
        // empty private constructor.
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
        Cursor cursor = db.query(NAME, new String[] {"_id"},
                Columns.RAW_TEXT + " = ? AND " + Columns.TYPE + " = ?",
                new String[]{rawText, String.valueOf(type)},
                null, null, null);
        try {
            if (cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndex("_id"));
                if (cursor.moveToNext()) { // Delete duplicates.
                    db.delete(NAME, "_id != ?", new String[]{String.valueOf(id)});
                }
                ContentValues cv = new ContentValues();
                cv.put(Columns.TIMESTAMP, timestamp);
                db.update(NAME, cv, "_id = ?", new String[]{String.valueOf(id)});
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

        return db.insert(NAME, null, cv);
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
        return db.delete(NAME, Columns.RAW_TEXT + " = ? AND " + Columns.TYPE + " = ?",
                new String[]{rawText, String.valueOf(type)});
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
