package com.eightbitforest.awesomesms.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Handles creation, opening, updating, etc. of the text message database.
 *
 * @author Forrest Jones
 */

public class TextMessageDBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "AwesomeSMS.db";

    private static final String SQL_CREATE_TEXT_MESSAGES =
            "CREATE TABLE " + TextMessageDB.TABLE_NAME + " (" +
                    TextMessageDB.MESSAGE_ID + " INTEGER NOT NULL," +
                    TextMessageDB.PROTOCOL + " INTEGER NOT NULL," +
                    TextMessageDB.SENT + " BOOLEAN DEFAULT false," +
                    TextMessageDB.ERROR + " INTEGER," +
                    "UNIQUE(" + TextMessageDB.MESSAGE_ID + ", " + TextMessageDB.PROTOCOL + ") ON CONFLICT REPLACE)";

    public TextMessageDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_TEXT_MESSAGES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
