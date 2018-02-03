package com.eightbitforest.awesomesms.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Handles creation, opening, updating, etc. of the AwesomeSMS database.
 *
 * @author Forrest Jones
 */
public class AwesomeSMSDBHelper extends SQLiteOpenHelper {

    /**
     * Version of the database. If a change is made to the database schema, then this number
     * must increase.
     */
    public static final int DATABASE_VERSION = 1;
    /** The database file name */
    public static final String DATABASE_NAME = "AwesomeSMS.db";

    /**
     * Constructor.
     *
     * @param context Context of the running application.
     */
    public AwesomeSMSDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Uses the SQL provided in the TextMessageDB and ContactDB classes to construct the needed
     * tables.
     *
     * @param db The empty database ready to be constructed.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TextMessageDB.SQL_CREATE_TEXT_MESSAGES);
        db.execSQL(ContactDB.SQL_CREATE_CONTACTS);
    }

    /**
     * When the version number increases, this method gets called to update the database.
     *
     * @param db         The old database.
     * @param oldVersion The old version.
     * @param newVersion The new version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
