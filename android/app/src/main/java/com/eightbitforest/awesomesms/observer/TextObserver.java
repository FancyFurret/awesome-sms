package com.eightbitforest.awesomesms.observer;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.eightbitforest.awesomesms.AwesomeSMS;
import com.eightbitforest.awesomesms.model.TextMessage;
import com.eightbitforest.awesomesms.model.TextMessageDB;
import com.eightbitforest.awesomesms.observer.exception.InvalidCursorException;
import com.eightbitforest.awesomesms.observer.exception.ParseException;
import com.google.android.mms.ContentType;
import com.google.android.mms.pdu_alt.PduHeaders;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static android.provider.Telephony.Mms;
import static android.provider.Telephony.MmsSms;
import static android.provider.Telephony.Sms;
import static com.eightbitforest.awesomesms.observer.ContentHelper.close;
import static com.eightbitforest.awesomesms.observer.ContentHelper.closeAllCursors;
import static com.eightbitforest.awesomesms.observer.ContentHelper.getCursor;
import static com.eightbitforest.awesomesms.observer.ContentHelper.getInt;
import static com.eightbitforest.awesomesms.observer.ContentHelper.getLong;
import static com.eightbitforest.awesomesms.observer.ContentHelper.getString;
import static com.eightbitforest.awesomesms.observer.ContentHelper.joinOnInt;

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
public class TextObserver extends AutoContentObserver {

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
     * Creates the text observer.
     *
     * @param listener        The listener to send text updates to.
     * @param messageDatabase The database that holds all messages that have already been parsed.
     *                        Should be created with TextMessageDB.Helper
     * @param contentResolver Android's content resolver to get content providers.
     */
    public TextObserver(ITextListener listener, SQLiteDatabase messageDatabase, ContentResolver contentResolver) {
        super(messageDatabase, contentResolver, MmsSms.CONTENT_CONVERSATIONS_URI);

        this.listener = listener;
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

            // Try to parse new SMS
            parseAndSendNewMessages(
                    getCursor(Sms.CONTENT_URI, Sms._ID + " DESC", contentResolver), Sms._ID, TextMessage.PROTOCOL_SMS);

            // Try to parse new MMS
            parseAndSendNewMessages(
                    getCursor(Mms.CONTENT_URI, Mms._ID + " DESC", contentResolver), Mms._ID, TextMessage.PROTOCOL_MMS);

        } catch (InvalidCursorException e) {
            Log.w(AwesomeSMS.TAG, e.getMessage());
        } finally {
            closeAllCursors();
        }
    }

    private void parseAndSendNewMessages(Cursor contentCursor, String contentId, byte protocol) throws InvalidCursorException {
        Cursor messageCursor = getCursor(trackingDatabase, TextMessageDB.TABLE_NAME,
                TextMessageDB.PROTOCOL + "=" + protocol, TextMessageDB.MESSAGE_ID + " DESC");
        joinOnInt(messageCursor, TextMessageDB.MESSAGE_ID, contentCursor, contentId, null, id -> {
            try {
                TextMessage text = parseMessage(id, protocol);
                if (text == null)
                    return;

                // We successfully parsed the message, so add it to the database so it doesn't get parsed again.
                ContentValues values = new ContentValues();
                values.put(TextMessageDB.MESSAGE_ID, id);
                values.put(TextMessageDB.PROTOCOL, protocol);
                trackingDatabase.insert(TextMessageDB.TABLE_NAME, null, values);

                if (text.getBox() == TextMessage.BOX_SENT)
                    listener.TextSent(text);
                else
                    listener.TextReceived(text);

            } catch (ParseException e) {
                // There was some unexpected error parsing the message. We still need to add it to the database
                // so it doesn't get parsed again.
                ContentValues values = new ContentValues();
                values.put(TextMessageDB.MESSAGE_ID, id);
                values.put(TextMessageDB.PROTOCOL, protocol);
                values.put(TextMessageDB.ERROR, e.getError());
                trackingDatabase.insert(TextMessageDB.TABLE_NAME, null, values);
                Log.e(AwesomeSMS.TAG, e.getMessage());
            }
        });
    }

    /**
     * Main meat of this class. Assumes that the message is ready for parsing.
     *
     * @param id       The id of the message to parse.
     * @param protocol The protocol of the message.
     * @return True if the parse was successful.
     */
    public TextMessage parseMessage(int id, byte protocol) throws ParseException {
        try {
            // Get the cursor pointing to the message to parse.
            Cursor msgCursor = getTextMsgCursor(id, protocol, contentResolver);

            // Get the msgBox (inbox or sent)
            byte msgBox = getTextMsgBox(msgCursor, protocol);
            if (msgBox == -1)
                return null; // Message is not ready

            // Make sure the message is ready first.
            // Only have to check mms, not sms (hopefully...)
            if (protocol == TextMessage.PROTOCOL_MMS && !isMessageReady(msgCursor, msgBox))
                return null;

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

            close(msgCursor);

            // Construct the text message using the found information and return it.
            return new TextMessage(
                    id,
                    message,
                    addresses,
                    attachments,
                    thread,
                    date,
                    protocol,
                    msgBox);
        } catch (InvalidCursorException e) {
            Log.w(AwesomeSMS.TAG, e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves the part data from an mms message. This includes the text message, images,
     * sound, videos, etc.
     *
     * @param attachments Where to store the found attachments.
     * @return The string message.
     * @throws ParseException If the part data could not be parsed.
     */
    private String getMmsPartData(int id, ArrayList<TextMessage.Attachment> attachments) throws ParseException {
        try {
            // Get part cursor
            String where = Mms.Part.MSG_ID + "=" + id;
            Cursor partCursor = getCursor(MMS_PART, where, null, contentResolver);

            // Extract part data (text, images, etc.)
            String message = null;
            do {
                String contentType = getString(partCursor, Mms.Part.CONTENT_TYPE);
                if (contentType == null)
                    continue;

                if (contentType.equals(ContentType.TEXT_PLAIN)) {
                    message = getString(partCursor, Mms.Part.TEXT);
                    if (message == null)
                        throw new ParseException(TextMessageDB.ERROR_BODY_NULL, "MMS Text body was null for id: " + id);
                } else if (ContentType.isSupportedImageType(contentType) || // Only send supported media files
                        ContentType.isSupportedAudioType(contentType) ||
                        ContentType.isSupportedVideoType(contentType)) {
                    int partId = getInt(partCursor, Mms.Part._ID);
                    attachments.add(new TextMessage.Attachment(contentType, getMmsPartPartBytes(partId)));
                } else
                    Log.w(AwesomeSMS.TAG, "Unknown Mime type: " + contentType + " for id: " + id);
            } while (partCursor.moveToNext());

            close(partCursor);
            return message;
        } catch (InvalidCursorException e) {
            throw new ParseException(TextMessageDB.ERROR_NO_PART, "MMS had no part for id: " + id);
        }
    }

    /**
     * Gets the bytes of data of a stored part.
     *
     * @param id The id of the part.
     * @return Raw byte array of the stored data.
     * @throws ParseException If the data could not be read.
     */
    private byte[] getMmsPartPartBytes(int id) throws ParseException {
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
            throw new ParseException(TextMessageDB.ERROR_MISSING_PART_FILE, "MMS had no part file for id: " + id);
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Gets the addresses from an MMS. Addresses are stored in a separate table so this queries
     * that table. Can give duplicate addresses. Cause' google.
     *
     * @param id The id of the message to use.
     * @return A list of all the address associated with the message.
     * @throws ParseException If the address table could not be found, or for any other
     *                        parsing errors.
     */
    private ArrayList<TextMessage.Address> getMmsAddresses(int id) throws ParseException {
        try {
            // Get address cursor
            Uri uri = Uri.parse(String.format(MMS_ADDRESS, id));
            String where = Mms.Addr.MSG_ID + "=" + id;
            Cursor addressCursor = getCursor(uri, where, null, contentResolver);

            // Find all addresses
            ArrayList<TextMessage.Address> addresses = new ArrayList<>();
            do {
                String address = getString(addressCursor, Mms.Addr.ADDRESS);
                int addressType = getInt(addressCursor, Mms.Addr.TYPE);
                byte msgAddressType;
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
                        throw new ParseException(TextMessageDB.ERROR_UNKNOWN_ADDRESS_TYPE, "Unknown address type " + addressType);
                }

                addresses.add(new TextMessage.Address(address, msgAddressType));
                if (addresses.get(addresses.size() - 1).getAddress() == null)
                    throw new ParseException(TextMessageDB.ERROR_ADDRESS_NULL, "MMS Address was null for id: " + id);

            } while (addressCursor.moveToNext());

            close(addressCursor);
            return addresses;
        } catch (InvalidCursorException e) {
            throw new ParseException(TextMessageDB.ERROR_NO_ADDRESS, "Unable to find MMS address for id: " + id);
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
    private boolean isMessageReady(Cursor msgCursor, byte msgBox) {
        String dateSent = getString(msgCursor, Mms.DATE_SENT);
        String mId = getString(msgCursor, Mms.MESSAGE_ID);
        if (msgBox == TextMessage.BOX_INBOX)
            return !"0".equals(dateSent);
        else if (msgBox == TextMessage.BOX_SENT)
            return mId != null;
        return true;
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
    private static byte getTextMsgBox(Cursor msgCursor, byte protocol) {
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
     * Gets the specific MMS or SMS cursor based off an ID.
     *
     * @param id       The id of the message to find.
     * @param protocol The protocol of the message.
     * @return The correct cursor pointing the the message with the specified id.
     */
    private static Cursor getTextMsgCursor(int id, byte protocol, ContentResolver contentResolver) throws InvalidCursorException {
        if (protocol == TextMessage.PROTOCOL_SMS)
            return getCursor(Sms.CONTENT_URI, Sms._ID + "=" + id, Sms.DEFAULT_SORT_ORDER, contentResolver);
        else
            return getCursor(Mms.CONTENT_URI, Mms._ID + "=" + id, Mms.DEFAULT_SORT_ORDER, contentResolver);
    }
}
