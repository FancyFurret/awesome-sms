package com.eightbitforest.awesomesms.util;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.eightbitforest.awesomesms.observer.exception.InvalidCursorException;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Contains a bunch of helpful method for dealing with cursors.
 *
 * @author Forrest Jones
 */
public class ContentHelper {

    /** Stores a list of all returned cursors so they can easily be closed later. */
    private static ArrayList<Cursor> cursors = new ArrayList<>();

    /**
     * Helper method to query a table and get a cursor. Will return the first item.
     *
     * @param uri             The table to query.
     * @param sort            How to sort the table.
     * @param contentResolver The content resolver to use to get the cursor.
     * @return The first cursor in the specified table with the specified sort.
     * @throws InvalidCursorException If the cursor could not be found or is invalid.
     */
    public static Cursor getCursor(Uri uri, String sort, ContentResolver contentResolver) throws InvalidCursorException {
        return getCursor(uri, null, sort, contentResolver);
    }

    /**
     * Helper method to query a table and get a cursor. Will return the first item.
     *
     * @param uri             The table to query.
     * @param where           'Where' clause to pass to sql.
     * @param sort            How to sort the table.
     * @param contentResolver The content resolver to use to get the cursor.
     * @return The first cursor in the specified table with the specified sort.
     * @throws InvalidCursorException If the cursor could not be found or is invalid.
     */
    public static Cursor getCursor(Uri uri, String where, String sort, ContentResolver contentResolver) throws InvalidCursorException {
        return getCursor(uri, where, sort, contentResolver, false);
    }

    /**
     * Helper method to query a table and get a cursor. Will return the first item.
     *
     * @param uri             The table to query.
     * @param where           'Where' clause to pass to sql.
     * @param sort            How to sort the table.
     * @param contentResolver The content resolver to use to get the cursor.
     * @param allowNone       Whether to check if the cursor found any rows.
     * @return The first cursor in the specified table with the specified sort.
     * @throws InvalidCursorException If the cursor could not be found or is invalid.
     */
    public static Cursor getCursor(Uri uri, String where, String sort, ContentResolver contentResolver, boolean allowNone) throws InvalidCursorException {
        Cursor cursor = contentResolver.query(uri, null, where, null, sort);
        cursors.add(cursor);
        if (cursor == null)
            throw new InvalidCursorException(uri);
        if (!cursor.moveToFirst())
            if (!allowNone)
                throw new InvalidCursorException(uri);
        return cursor;
    }

    /**
     * Helper method to query a table and get a cursor. Will return the first item. Takes
     * an SQLiteDatabase instead of a URI.
     *
     * @param database The SQLiteDatabase to query.
     * @param table    The table in the database to use.
     * @param sort     How to sort the table.
     * @return The first cursor in the specified table with the specified sort. May have 0 items.
     * @throws InvalidCursorException If the cursor could not be found or is invalid.
     */
    public static Cursor getCursor(SQLiteDatabase database, String table, String sort) throws InvalidCursorException {
        return getCursor(database, table, null, sort);
    }

    /**
     * Helper method to query a table and get a cursor. Will return the first item. Takes
     * an SQLiteDatabase instead of a URI.
     *
     * @param database The SQLiteDatabase to query.
     * @param table    The table in the database to use.
     * @param where    'Where' clause to pass to sql.
     * @param sort     How to sort the table.
     * @return The first cursor in the specified table with the specified sort. May have 0 items.
     * @throws InvalidCursorException If the cursor could not be found or is invalid.
     */
    public static Cursor getCursor(SQLiteDatabase database, String table, String where, String sort) throws InvalidCursorException {
        Cursor cursor = database.query(table, null, where, null, null, null, sort);
        cursors.add(cursor);
        if (cursor == null) // Not checking cursor.moveToFirst() because this can have 0 results
            throw new InvalidCursorException(table);
        cursor.moveToFirst();
        return cursor;
    }

    /**
     * Joins two cursors on one column. Allows you to see which items are unique to either
     * database. Must be sorted in descending order, and the joined columns must be integers.
     *
     * @param cursorLeft  Cursor to the left database.
     * @param columnLeft  Column to join on the left database.
     * @param cursorRight Cursor to the right database.
     * @param columnRight Column to join on the left database.
     * @param onLeft      Function to call when the left database has a unique row. Passes the
     *                    value of the joined column.
     * @param onRight     Function to call when the right database has a unique row. Passes the
     *                    value of the joined column.
     */
    public static void joinOnInt(Cursor cursorLeft, String columnLeft,
                                 Cursor cursorRight, String columnRight,
                                 Consumer<Integer> onLeft, Consumer<Integer> onRight) {
        DescIntCursorJoiner joiner = new DescIntCursorJoiner(
                cursorLeft, new String[]{columnLeft},
                cursorRight, new String[]{columnRight});

        for (DescIntCursorJoiner.Result result : joiner) {
            if (result == DescIntCursorJoiner.Result.RIGHT && onRight != null)
                onRight.accept(getInt(cursorRight, columnRight));
            else if (result == DescIntCursorJoiner.Result.LEFT && onLeft != null)
                onLeft.accept(getInt(cursorLeft, columnLeft));
        }
    }


    /**
     * Helper method to get a string from a cursor.
     *
     * @param cursor The cursor to use.
     * @param column The name of the column to find.
     * @return The string value at the specified column.
     */
    public static String getString(Cursor cursor, String column) {
        return cursor.getString(cursor.getColumnIndexOrThrow(column));
    }

    /**
     * Helper method to get an int from a cursor.
     *
     * @param cursor The cursor to use.
     * @param column The name of the column to find.
     * @return The int value at the specified column.
     */
    public static int getInt(Cursor cursor, String column) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(column));
    }

    /**
     * Helper method to get a long from a cursor.
     *
     * @param cursor The cursor to use.
     * @param column The name of the column to find.
     * @return The long value at the specified column.
     */
    public static long getLong(Cursor cursor, String column) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(column));
    }

    /**
     * Helper method to get a byte array from a cursor.
     *
     * @param cursor The cursor to use.
     * @param column The name of the column to find.
     * @return The byte array at the specified column.
     */
    public static byte[] getBlob(Cursor cursor, String column) {
        return cursor.getBlob(cursor.getColumnIndexOrThrow(column));
    }

    /**
     * Helper method to close a cursor if it isn't null.
     *
     * @param cursor The cursor to close, may be null.
     */
    public static void close(@Nullable Cursor cursor) {
        if (cursor != null) {
            cursor.close();
            cursors.remove(cursor);
        }
    }

    /**
     * Closes all the cursors that were created by this class.
     */
    public static void closeAllCursors() {
        for (Cursor cursor : cursors)
            close(cursor);
        cursors.clear();
    }
}
