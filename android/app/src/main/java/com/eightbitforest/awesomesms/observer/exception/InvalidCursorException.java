package com.eightbitforest.awesomesms.observer.exception;

import android.net.Uri;

/**
 * Exception for invalid cursors. Can either take the URI or table that was trying
 * to be used.
 */
public class InvalidCursorException extends Exception {
    public InvalidCursorException(Uri uri) {
        super("Could not get cursor from uri: " + uri);
    }

    public InvalidCursorException(String table) {
        super("Could not get cursor from: " + table);
    }
}
