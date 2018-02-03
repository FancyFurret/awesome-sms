package com.eightbitforest.awesomesms.observer.exception;

/**
 * Holds data for message parsing errors. Requires an error code that will be placed
 * in the text message database. These are found in TextMessageDB and ContactDB.
 *
 * @author Forrest Jones
 */
public class ParseException extends Exception {
    private byte error;

    /**
     * Constructs a ParseException.
     *
     * @param error   The error from TextMessageDB or ContactDB.
     * @param message A description of the error.
     */
    public ParseException(byte error, String message) {
        super(message);
        this.error = error;
    }

    public byte getError() {
        return error;
    }
}