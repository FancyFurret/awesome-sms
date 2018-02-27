package com.eightbitforest.awesomesms.model;

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

    /** If a message is not found in the part table */
    public static final byte ERROR_NO_PART = 1;
    /** If no addresses could be found with a message */
    public static final byte ERROR_NO_ADDRESS = 2;
    /** If the body of a message is null when it shouldn't be. */
    public static final byte ERROR_BODY_NULL = 3;
    /** If the address of a message is null when it shouldn't be. */
    public static final byte ERROR_ADDRESS_NULL = 4;
    /** When the address for a message is not TO, FROM, or CC */
    public static final byte ERROR_UNKNOWN_ADDRESS_TYPE = 5;
    /** When a part of the attachment could not be loaded. */
    public static final byte ERROR_MISSING_PART_FILE = 6;
    /** When an SMS address is invalid. */
    public static final byte ERROR_INVALID_ADDRESS= 7;

    static final String SQL_CREATE_TEXT_MESSAGES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    MESSAGE_ID + " INTEGER NOT NULL," +
                    PROTOCOL + " TINYINT NOT NULL," +
                    SENT + " BOOLEAN DEFAULT 0," +
                    ERROR + " TINYINT," +
                    "UNIQUE(" + MESSAGE_ID + ", " + PROTOCOL + ") ON CONFLICT REPLACE)";
}
