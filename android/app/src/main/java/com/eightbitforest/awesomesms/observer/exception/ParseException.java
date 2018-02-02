package com.eightbitforest.awesomesms.observer.exception;

/**
 * Holds data for message parsing errors. Requires an error code that will be placed
 * in the text message database. These are found in TextMessageDB.
 */
public class ParseException extends Exception {
    private byte error;

    public ParseException(byte error, String message) {
        super(message);
        this.error = error;
    }

    public byte getError() {
        return error;
    }
}