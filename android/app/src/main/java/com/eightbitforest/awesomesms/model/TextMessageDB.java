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

    public static final int ERROR_NO_PART = 1;
    public static final int ERROR_NO_ADDRESS = 2;
    public static final int ERROR_BODY_NULL = 3;
    public static final int ERROR_ADDRESS_NULL = 4;
    public static final int ERROR_UNKNOWN_ADDRESS_TYPE = 5;
    public static final int ERROR_MISSING_PART_FILE = 6;

    private TextMessageDB() { }
}
