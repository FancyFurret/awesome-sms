package com.eightbitforest.awesomesms.text_listener;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.CursorJoiner;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import com.eightbitforest.awesomesms.AwesomeSMS;
import com.eightbitforest.awesomesms.model.TextMessage;
import com.eightbitforest.awesomesms.model.TextMessageDB;
import com.google.android.mms.ContentType;
import com.google.android.mms.pdu_alt.PduHeaders;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static android.provider.Telephony.Mms;
import static android.provider.Telephony.Sms;

/**
 * The class listens for changes in the content://mms-sms database, and sends the new messages
 * to an ITextListener. Google has provided no nice api for interacting with the message
 * databases, so this deals with them directly. Once a message as been parsed and sent to the
 * ITextListener, it is added to the text message database. This prevents updates from happening
 * multiple times, as well as messages in the middle of sending getting overwritten by
 * received messages.
 * <p>
 * Relies on a bunch of undocumented features that could break with any android update. Yay.
 * <p>
 * The bitterness in my comments is completely justified.
 *
 * @author Forrest Jones
 */
public class TextObserver extends ContentObserver {

    /**
     * A few constants that for whatever reason are left out of the official library.
     */
    private static final Uri MMS_PART = Uri.parse("content://mms/part");
    private static final String MMS_ADDRESS = "content://mms/%s/addr";

    /**
     * The listener to send text updates to.
     */
    private ITextListener listener;
    /**
     * The database that holds all messages that have already been parsed.
     */
    private SQLiteDatabase messageDatabase;
    /**
     * Android's content resolver to get content providers.
     */
    private ContentResolver contentResolver;

    /**
     * Creates the text observer. Does nothing special.
     *
     * @param handler         Idk what this is but the ContentObserver needs it.
     * @param listener        The listener to send text updates to.
     * @param messageDatabase The database that holds all messages that have already been parsed.
     *                        MUST include the following columns:
     *                        m_id - The message id.
     *                        protocol - SMS/MMS.
     *                        error - Any error encountered during parsing.
     * @param contentResolver Android's content resolver to get content providers.
     */
    TextObserver(Handler handler, ITextListener listener, SQLiteDatabase messageDatabase, ContentResolver contentResolver) {
        super(handler);

        this.listener = listener;
        this.messageDatabase = messageDatabase;
        this.contentResolver = contentResolver;
    }

    /**
     * Gets called whenever the content://mms-sms database has been changed.
     *
     * @param selfChange True if this is a self-change notification.
     */
    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        try {
            // TODO: These may take a very long time. Move to different thread and show notification

            // For whatever the stupid CursorJoiner doesn't allow you to sort descending. Instead,
            // I'm doing that here by going through the array backwards.

            ArrayList<Integer> newMessages;

            // Try to parse new SMS
            newMessages = getNewMessages(TextMessage.PROTOCOL_SMS);
            for (int i = newMessages.size() - 1; i >= 0; i--)
                parseMessage(newMessages.get(i), TextMessage.PROTOCOL_SMS);

            // Try to parse new MMS
            newMessages = getNewMessages(TextMessage.PROTOCOL_MMS);
            for (int i = newMessages.size() - 1; i >= 0; i--)
                parseMessage(newMessages.get(i), TextMessage.PROTOCOL_MMS);

        } catch (InvalidCursorException e) {
            Log.w(AwesomeSMS.TAG, e.getMessage());
        }
    }

    /**
     * Main meat of this class. If the message is ready, parse it, create a TextMessage
     * object, and fire the listener.
     *
     * @param id       The id of the message to parse.
     * @param protocol The protocol of the message.
     * @return True if the parse was successful.
     * @throws InvalidCursorException If the cursor for the provided id and protocol could not be found.
     */
    // TODO: return the message, don't send it
    private boolean parseMessage(int id, int protocol) throws InvalidCursorException {
        Cursor msgCursor = null;
        try {
            // Get the cursor pointing to the message to parse.
            msgCursor = getMsgCursor(id, protocol);

            // Get the msgBox (inbox or sent)
            int msgBox = getMsgBox(msgCursor, protocol);
            if (msgBox == -1)
                return false; // Message is not ready

            // Make sure the message is ready first.
            // Only have to check mms, not sms (hopefully...)
            if (protocol == TextMessage.PROTOCOL_MMS && !isMessageReady(msgCursor, msgBox))
                return false;

            // We are now good to (try to) parse the message.
            String message;
            ArrayList<TextMessage.Address> addresses = new ArrayList<>();
            ArrayList<TextMessage.Attachment> attachments = new ArrayList<>();
            int thread;
            long date;

            if (protocol == TextMessage.PROTOCOL_MMS) {
                message = getMmsPartData(id, attachments);
                addresses = getMmsAddresses(id); // TODO: Normalize, remove duplicates
                thread = getInt(msgCursor, Mms.THREAD_ID);
                date = getLong(msgCursor, Mms.DATE); // TODO: Normalize dates
            } else {
                message = getString(msgCursor, Sms.BODY);
                addresses.add(new TextMessage.Address(
                        getString(msgCursor, Sms.ADDRESS),
                        msgBox == TextMessage.BOX_INBOX ? TextMessage.Address.TYPE_FROM : TextMessage.Address.TYPE_TO
                ));
                thread = getInt(msgCursor, Sms.THREAD_ID);
                date = getLong(msgCursor, Sms.DATE);
            }

            // Construct the text message using the found information and fire the correct
            // ITextListener method.
            TextMessage text = new TextMessage(
                    id,
                    message,
                    addresses,
                    attachments,
                    thread,
                    date,
                    protocol,
                    msgBox);
            if (msgBox == TextMessage.BOX_SENT)
                listener.TextSent(text);
            else
                listener.TextReceived(text);

            // We successfully parsed the message, so add it to the database so it doesn't get parsed again.
            ContentValues values = new ContentValues();
            values.put(TextMessageDB.MESSAGE_ID, id);
            values.put(TextMessageDB.PROTOCOL, protocol);
            messageDatabase.insert(TextMessageDB.TABLE_NAME, null, values);

            return true;
        } catch (MessageParseException e) {
            // There was some unexpected error parsing the message. We still need to add it to the database
            // so it doesn't get parsed again.
            ContentValues values = new ContentValues();
            values.put(TextMessageDB.MESSAGE_ID, id);
            values.put(TextMessageDB.PROTOCOL, protocol);
            values.put(TextMessageDB.ERROR, e.getError());
            messageDatabase.insert(TextMessageDB.TABLE_NAME, null, values);

            Log.e(AwesomeSMS.TAG, e.getMessage());
            return false;
        } finally {
            close(msgCursor);
        }
    }

    /**
     * The OnChange method so helpfully leaves out the very important information of which
     * row changed because that would just be too easy. Instead I have to make a whole database
     * just to see which rows were added. This method compares my text messages database
     * with android's databases to find which messages have been added.
     *
     * @param protocol The protocol of the messages to compare.
     * @return A BACKWARDS (Thanks CursorJoiner) list of messages not in the text message database.
     * @throws InvalidCursorException If the cursor for the provided protocol could not be found.
     */
    private ArrayList<Integer> getNewMessages(int protocol) throws InvalidCursorException {
        ArrayList<Integer> newMessages = new ArrayList<>();

        Cursor messageDatabaseCursor = null;
        Cursor allMessagesCursor = null;
        try {
            // Grab the cursors for my database and android's.
            messageDatabaseCursor = getCursor(messageDatabase, TextMessageDB.TABLE_NAME,
                    TextMessageDB.PROTOCOL + "=" + protocol, TextMessageDB.MESSAGE_ID);
            allMessagesCursor = getCursor(
                    protocol == TextMessage.PROTOCOL_SMS ? Sms.CONTENT_URI : Mms.CONTENT_URI, Sms._ID);

            // Create the joiner with my database on the left.
            CursorJoiner joiner = new CursorJoiner(
                    messageDatabaseCursor, new String[]{TextMessageDB.MESSAGE_ID},
                    allMessagesCursor, new String[]{Sms._ID});
            for (CursorJoiner.Result result : joiner) {
                // Right mean's this row is exclusive to android's db
                if (result != CursorJoiner.Result.RIGHT)
                    continue;
                newMessages.add(getInt(allMessagesCursor, Sms._ID));
            }

            return newMessages;
        } finally {
            close(messageDatabaseCursor);
            close(allMessagesCursor);
        }
    }

    /**
     * Gets the correct message box (inbox/sent) from the provided cursor and protocol.
     *
     * @param msgCursor The cursor the the message.
     * @param protocol  Why does this need the protocol, you ask? Well because the guys at google
     *                  thought it would be a great idea to name the same column different things
     *                  for SMS and MMS.
     * @return The message box of the given message. -1 if the box is unknown.
     */
    private int getMsgBox(Cursor msgCursor, int protocol) {
        if (protocol == TextMessage.PROTOCOL_SMS) {
            int type = getInt(msgCursor, Sms.TYPE); // This column is called type
            if (type == Sms.MESSAGE_TYPE_INBOX)
                return TextMessage.BOX_INBOX;
            else if (type == Sms.MESSAGE_TYPE_SENT)
                return TextMessage.BOX_SENT;
            else
                return -1;
        } else {
            int type = getInt(msgCursor, Mms.MESSAGE_BOX); // And this one is message box. WTF????
            if (type == Mms.MESSAGE_BOX_INBOX)
                return TextMessage.BOX_INBOX;
            else if (type == Mms.MESSAGE_BOX_SENT)
                return TextMessage.BOX_SENT;
            else
                return -1;
        }
    }

    /**
     * Checks to see if an MMS message is ready to be parsed (all addresses have been added, and all
     * attachments have been downloaded.)
     * <p>
     * No easy way to do this, so I am using the following things that *seem* to work.
     * - date_sent is 0 if a received message has not been completely downloaded.
     * - m_id is null if a sent message has not been successfully sent.
     *
     * @param msgCursor The cursor to the current message.
     * @param msgBox    The box of the message.
     * @return True if the message has been completely downloaded/sent.
     */
    private boolean isMessageReady(Cursor msgCursor, int msgBox) {
        String dateSent = getString(msgCursor, Mms.DATE_SENT);
        String mId = getString(msgCursor, Mms.MESSAGE_ID);
        if (msgBox == TextMessage.BOX_INBOX)
            return !"0".equals(dateSent);
        else if (msgBox == TextMessage.BOX_SENT)
            return mId != null;
        return true;
    }

    /**
     * Helper method to query a table and get a cursor. Will return the first item.
     *
     * @param uri  The table to query.
     * @param sort How to sort the table.
     * @return The first cursor in the specified table with the specified sort.
     * @throws InvalidCursorException If the cursor could not be found or is invalid.
     */
    private Cursor getCursor(Uri uri, String sort) throws InvalidCursorException {
        return getCursor(uri, null, sort);
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
    private Cursor getCursor(Uri uri, String where, String sort) throws InvalidCursorException {
        Cursor cursor = contentResolver.query(uri, null, where, null, sort);
        if (cursor == null || !cursor.moveToFirst())
            throw new InvalidCursorException(uri);
        return cursor;
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
    private Cursor getCursor(SQLiteDatabase database, String table, String where, String sort) throws InvalidCursorException {
        Cursor cursor = database.query(table, null, where, null, null, null, sort);
        if (cursor == null) // Not checking cursor.moveToFirst() because this can have 0 results
            throw new InvalidCursorException(table);
        cursor.moveToFirst();
        return cursor;
    }

    /**
     * Gets the specific MMS or SMS cursor based off an ID.
     *
     * @param id       The id of the message to find.
     * @param protocol The protocol of the message.
     * @return The correct cursor pointing the the message with the specified id.
     */
    private Cursor getMsgCursor(int id, int protocol) throws InvalidCursorException {
        if (protocol == TextMessage.PROTOCOL_SMS)
            return getCursor(Sms.CONTENT_URI, Sms._ID + "=" + id, Sms.DEFAULT_SORT_ORDER);
        else
            return getCursor(Mms.CONTENT_URI, Mms._ID + "=" + id, Mms.DEFAULT_SORT_ORDER);
    }

    /**
     * Helper method to get a string from a cursor.
     *
     * @param cursor The cursor to use.
     * @param column The name of the column to find.
     * @return The string value at the specified column.
     */
    private String getString(Cursor cursor, String column) {
        return cursor.getString(cursor.getColumnIndexOrThrow(column));
    }

    /**
     * Helper method to get an int from a cursor.
     *
     * @param cursor The cursor to use.
     * @param column The name of the column to find.
     * @return The int value at the specified column.
     */
    private int getInt(Cursor cursor, String column) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(column));
    }

    /**
     * Helper method to get a long from a cursor.
     *
     * @param cursor The cursor to use.
     * @param column The name of the column to find.
     * @return The long value at the specified column.
     */
    private long getLong(Cursor cursor, String column) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(column));
    }

    /**
     * Retrieves the part data from an mms message. This includes the text message, images,
     * sound, videos, etc.
     *
     * @param attachments Where to store the found attachments.
     * @return The string message.
     * @throws MessageParseException If the part data could not be parsed.
     */
    private String getMmsPartData(int id, ArrayList<TextMessage.Attachment> attachments) throws MessageParseException {
        Cursor partCursor = null;
        try {
            // Get part cursor
            String where = Mms.Part.MSG_ID + "=" + id;
            partCursor = getCursor(MMS_PART, where, null);

            // Extract part data (text, images, etc.)
            String message = null;
            do {
                String contentType = getString(partCursor, Mms.Part.CONTENT_TYPE);
                if (contentType == null)
                    continue;

                if (contentType.equals(ContentType.TEXT_PLAIN)) {
                    message = getString(partCursor, Mms.Part.TEXT);
                    if (message == null)
                        throw new MessageParseException(TextMessageDB.ERROR_BODY_NULL, "MMS Text body was null for id: " + id);
                } else if (ContentType.isSupportedImageType(contentType) || // Only send supported media files
                        ContentType.isSupportedAudioType(contentType) ||
                        ContentType.isSupportedVideoType(contentType)) {
                    int partId = getInt(partCursor, Mms.Part._ID);
                    attachments.add(new TextMessage.Attachment(contentType, getMmsPartPartBytes(partId)));
                } else
                    Log.w(AwesomeSMS.TAG, "Unknown Mime type: " + contentType + " for id: " + id);
            } while (partCursor.moveToNext());

            return message;
        } catch (InvalidCursorException e) {
            throw new MessageParseException(TextMessageDB.ERROR_NO_PART, "MMS had no part for id: " + id);
        } finally {
            close(partCursor);
        }
    }

    /**
     * Gets the bytes of data of a stored part.
     *
     * @param id The id of the part.
     * @return Raw byte array of the stored data.
     * @throws MessageParseException If the data could not be read.
     */
    private byte[] getMmsPartPartBytes(int id) throws MessageParseException {
        InputStream inputStream = null;
        try {
            Uri uri = Uri.parse(MMS_PART + "/" + id);
            inputStream = contentResolver.openInputStream(uri);
            if (inputStream == null)
                throw new IOException("Could not open input stream.");

            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            return bytes;
        } catch (IOException e) {
            throw new MessageParseException(TextMessageDB.ERROR_MISSING_PART_FILE, "MMS had no part file for id: " + id);
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException ignored) { }
        }
    }

    /**
     * Gets the addresses from an MMS. Addresses are stored in a separate table so this queries
     * that table. Can give duplicate addresses. Cause' google.
     *
     * @param id The id of the message to use.
     * @return A list of all the address associated with the message.
     * @throws MessageParseException If the address table could not be found, or for any other
     *                               parsing errors.
     */
    private ArrayList<TextMessage.Address> getMmsAddresses(int id) throws MessageParseException {
        Cursor addressCursor = null;
        try {
            // Get address cursor
            Uri uri = Uri.parse(String.format(MMS_ADDRESS, id));
            String where = Mms.Addr.MSG_ID + "=" + id;
            addressCursor = getCursor(uri, where, null);

            // Find all addresses
            ArrayList<TextMessage.Address> addresses = new ArrayList<>();
            do {
                String address = getString(addressCursor, Mms.Addr.ADDRESS);
                int addressType = getInt(addressCursor, Mms.Addr.TYPE);
                int msgAddressType;
                switch (addressType) {
                    // Why do I have to download a whole 3rd party library just to use these
                    // constants...
                    case PduHeaders.TO:
                        msgAddressType = TextMessage.Address.TYPE_TO;
                        break;
                    case PduHeaders.FROM:
                        msgAddressType = TextMessage.Address.TYPE_FROM;
                        break;
                    case PduHeaders.CC:
                        msgAddressType = TextMessage.Address.TYPE_CC;
                        break;
                    default:
                        throw new MessageParseException(TextMessageDB.ERROR_UNKNOWN_ADDRESS_TYPE, "Unknown address type " + addressType);
                }

                addresses.add(new TextMessage.Address(address, msgAddressType));
                if (addresses.get(addresses.size() - 1).getAddress() == null)
                    throw new MessageParseException(TextMessageDB.ERROR_ADDRESS_NULL, "MMS Address was null for id: " + id);

            } while (addressCursor.moveToNext());

            return addresses;
        } catch (InvalidCursorException e) {
            throw new MessageParseException(TextMessageDB.ERROR_NO_ADDRESS, "Unable to find MMS address for id: " + id);
        } finally {
            close(addressCursor);
        }
    }

    /**
     * Helper method to close a cursor if it isn't null.
     *
     * @param cursor The cursor to close, may be null.
     */
    private void close(@Nullable Cursor cursor) {
        if (cursor != null)
            cursor.close();
    }

    /**
     * Holds data for message parsing errors. Requires an error code that will be placed
     * in the text message database. These are found in TextMessageDB.
     */
    private static class MessageParseException extends Exception {
        private int error;

        MessageParseException(int error, String message) {
            super(message);
            this.error = error;
        }

        int getError() {
            return error;
        }
    }

    /**
     * Exception for invalid cursors. Can either take the URI or table that was trying
     * to be used.
     */
    private static class InvalidCursorException extends Exception {
        InvalidCursorException(Uri uri) {
            super("Could not get cursor from uri: " + uri);
        }

        InvalidCursorException(String table) {
            super("Could not get cursor from: " + table);
        }
    }
}
