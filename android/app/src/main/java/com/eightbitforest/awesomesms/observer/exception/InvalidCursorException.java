package com.eightbitforest.awesomesms.observer.exception;

import android.net.Uri;

/**
 * Exception for invalid cursors. Can either take the URI or table that was trying
 * to be used.
 *
 * @author Forrest Jones
 */
public class InvalidCursorException extends Exception {

    /**
     * Constructs an InvalidCursorException.
     *
     * @param uri The uri that failed to open.
     */
    public InvalidCursorException(Uri uri) {
        super("Could not get cursor from uri: " + uri);
    }

    /**
     * Constructs an InvalidCursorException.
     *
     * @param table The table that failed to open.
     */
    public InvalidCursorException(String table) {
        super("Could not get cursor from: " + table);
    }
}
