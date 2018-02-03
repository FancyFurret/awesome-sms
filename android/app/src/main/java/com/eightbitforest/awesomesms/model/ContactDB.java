package com.eightbitforest.awesomesms.model;

/**
 * Hold the various columns and constants used in the contacts database.
 *
 * @author Forrest Jones
 */
public class ContactDB {

    public static final String TABLE_NAME = "contacts";
    public static final String CONTACT_ID = "_id";

    public static final String REMOVE = "remove";
    public static final String SENT = "sent";
    public static final String ERROR = "error";

    /** If this contact has no phone numbers attached */
    public static final byte ERROR_NO_PHONE = 1;

    static final String SQL_CREATE_CONTACTS =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    CONTACT_ID + " INTEGER NOT NULL," +
                    REMOVE + " BOOLEAN DEFAULT 0," +
                    SENT + " BOOLEAN DEFAULT 0," +
                    ERROR + " TINYINT," +
                    "UNIQUE(" + CONTACT_ID + ") ON CONFLICT REPLACE)";
}
