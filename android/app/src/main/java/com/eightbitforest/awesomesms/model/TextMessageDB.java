package com.eightbitforest.awesomesms.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Hold the various columns and constants used in the text message database.
 *
 * @author Forrest Jones
 */

public class TextMessageDB {

    public static final String TABLE_NAME = "text_messages";
    public static final String MESSAGE_ID = "m_id";
    public static final String PROTOCOL = "protocol";
    public static final String SENT = "sent";
    public static final String ERROR = "error";

    public static final byte ERROR_NO_PART = 1;
    public static final byte ERROR_NO_ADDRESS = 2;
    public static final byte ERROR_BODY_NULL = 3;
    public static final byte ERROR_ADDRESS_NULL = 4;
    public static final byte ERROR_UNKNOWN_ADDRESS_TYPE = 5;
    public static final byte ERROR_MISSING_PART_FILE = 6;

    static final String SQL_CREATE_TEXT_MESSAGES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    MESSAGE_ID + " INTEGER NOT NULL," +
                    PROTOCOL + " TINYINT NOT NULL," +
                    SENT + " BOOLEAN DEFAULT 0," +
                    ERROR + " TINYINT," +
                    "UNIQUE(" + MESSAGE_ID + ", " + PROTOCOL + ") ON CONFLICT REPLACE)";
}
