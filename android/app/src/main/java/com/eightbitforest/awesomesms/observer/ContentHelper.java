package com.eightbitforest.awesomesms.observer;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.CursorJoiner;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.eightbitforest.awesomesms.observer.exception.InvalidCursorException;
import com.eightbitforest.awesomesms.util.DescIntCursorJoiner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;


class ContentHelper {

    private static ArrayList<Cursor> cursors = new ArrayList<>();

    /**
     * Helper method to query a table and get a cursor. Will return the first item.
     *
     * @param uri  The table to query.
     * @param sort How to sort the table.
     * @return The first cursor in the specified table with the specified sort.
     * @throws InvalidCursorException If the cursor could not be found or is invalid.
     */
    static Cursor getCursor(Uri uri, String sort, ContentResolver contentResolver) throws InvalidCursorException {
        return getCursor(uri, null, sort, contentResolver);
    }

    /**
     * Helper method to query a table and get a cursor. Will return the first item.
     *
     * @param uri   The table to query.
     * @param where 'Where' clause to pass to sql.
     * @param sort  How to sort the table.
     * @return The first cursor in the specified table with the specified sort.
     * @throws InvalidCursorException If the cursor could not be found or is invalid.
     */
    static Cursor getCursor(Uri uri, String where, String sort, ContentResolver contentResolver) throws InvalidCursorException {
        return getCursor(uri, where, sort, contentResolver, false);
    }

    /**
     * Helper method to query a table and get a cursor. Will return the first item.
     *
     * @param uri       The table to query.
     * @param where     'Where' clause to pass to sql.
     * @param sort      How to sort the table.
     * @param allowNone Whether to check if the cursor found any rows.
     * @return The first cursor in the specified table with the specified sort.
     * @throws InvalidCursorException If the cursor could not be found or is invalid.
     */
    static Cursor getCursor(Uri uri, String where, String sort, ContentResolver contentResolver, boolean allowNone) throws InvalidCursorException {
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
     * @param sort     How to sort the table.
     * @return The first cursor in the specified table with the specified sort. May have 0 items.
     * @throws InvalidCursorException If the cursor could not be found or is invalid.
     */
    static Cursor getCursor(SQLiteDatabase database, String table, String sort) throws InvalidCursorException {
        return getCursor(database, table, null, sort);
    }

    /**
     * Helper method to query a table and get a cursor. Will return the first item. Takes
     * an SQLiteDatabase instead of a URI.
     *
     * @param database The SQLiteDatabase to query.
     * @param where    'Where' clause to pass to sql.
     * @param sort     How to sort the table.
     * @return The first cursor in the specified table with the specified sort. May have 0 items.
     * @throws InvalidCursorException If the cursor could not be found or is invalid.
     */
    static Cursor getCursor(SQLiteDatabase database, String table, String where, String sort) throws InvalidCursorException {
        Cursor cursor = database.query(table, null, where, null, null, null, sort);
        cursors.add(cursor);
        if (cursor == null) // Not checking cursor.moveToFirst() because this can have 0 results
            throw new InvalidCursorException(table);
        cursor.moveToFirst();
        return cursor;
    }

    /**
     * The OnChange method so helpfully leaves out the very important information of which
     * row changed because that would just be too easy. Instead I have to make a whole database
     * just to see which rows were added. This method compares my text messages database
     * with android's databases to find which messages have been added.
     *
     * @param protocol The protocol of the messages to compare.
     */
    static void joinOnInt(Cursor cursorLeft, String columnLeft,
                          Cursor cursorRight, String columnRight,
                          Consumer<Integer> onLeft, Consumer<Integer> onRight) {
        DescIntCursorJoiner joiner = new DescIntCursorJoiner(
                cursorLeft, new String[]{columnLeft},
                cursorRight, new String[]{columnRight});

        ArrayList<Integer> left = new ArrayList<>();
        ArrayList<Integer> right = new ArrayList<>();

        for (DescIntCursorJoiner.Result result : joiner) {
            if (result == DescIntCursorJoiner.Result.RIGHT && onRight != null)
                onRight.accept(getInt(cursorRight, columnRight));
//                right.add(getInt(cursorRight, columnRight));
            else if (result == DescIntCursorJoiner.Result.LEFT && onLeft != null)
                onLeft.accept(getInt(cursorLeft, columnLeft));
//                left.add(getInt(cursorLeft, columnLeft));
        }

        // Because the stupid CursorJoiner only works in ascending order,
        // we need to reverse the lists before we iterate through them to
        // call the consumers.
//        Collections.reverse(left);
//        Collections.reverse(right);
//
//        if (onLeft != null)
//            for (Integer i : left)
//                onLeft.accept(i);
//        if (onRight != null)
//            for (Integer i : right)
//                onRight.accept(i);
    }


    /**
     * Helper method to get a string from a cursor.
     *
     * @param cursor The cursor to use.
     * @param column The name of the column to find.
     * @return The string value at the specified column.
     */
    static String getString(Cursor cursor, String column) {
        return cursor.getString(cursor.getColumnIndexOrThrow(column));
    }

    /**
     * Helper method to get an int from a cursor.
     *
     * @param cursor The cursor to use.
     * @param column The name of the column to find.
     * @return The int value at the specified column.
     */
    static int getInt(Cursor cursor, String column) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(column));
    }

    /**
     * Helper method to get a long from a cursor.
     *
     * @param cursor The cursor to use.
     * @param column The name of the column to find.
     * @return The long value at the specified column.
     */
    static long getLong(Cursor cursor, String column) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(column));
    }

    /**
     * Helper method to get a byte array from a cursor.
     *
     * @param cursor The cursor to use.
     * @param column The name of the column to find.
     * @return The byte array at the specified column.
     */
    static byte[] getBlob(Cursor cursor, String column) {
        return cursor.getBlob(cursor.getColumnIndexOrThrow(column));
    }

    /**
     * Helper method to close a cursor if it isn't null.
     *
     * @param cursor The cursor to close, may be null.
     */
    static void close(@Nullable Cursor cursor) {
        if (cursor != null)
            cursor.close();
    }

    static void closeAllCursors() {
        for (Cursor cursor : cursors)
            close(cursor);
        cursors.clear();
    }
}
