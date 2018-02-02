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

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "AwesomeSMS.db";

    public AwesomeSMSDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creates the text message and contact tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TextMessageDB.SQL_CREATE_TEXT_MESSAGES);
        db.execSQL(ContactDB.SQL_CREATE_CONTACTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
