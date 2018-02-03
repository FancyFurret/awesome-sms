package com.eightbitforest.awesomesms.observer;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.util.Log;

import com.eightbitforest.awesomesms.AwesomeSMS;
import com.eightbitforest.awesomesms.model.Contact;
import com.eightbitforest.awesomesms.model.ContactDB;
import com.eightbitforest.awesomesms.observer.exception.InvalidCursorException;
import com.eightbitforest.awesomesms.observer.exception.ParseException;

import java.util.ArrayList;

import static android.provider.ContactsContract.CommonDataKinds.Phone;
import static android.provider.ContactsContract.CommonDataKinds.Photo;
import static android.provider.ContactsContract.Contacts;
import static android.provider.ContactsContract.Data;
import static com.eightbitforest.awesomesms.util.ContentHelper.close;
import static com.eightbitforest.awesomesms.util.ContentHelper.closeAllCursors;
import static com.eightbitforest.awesomesms.util.ContentHelper.getBlob;
import static com.eightbitforest.awesomesms.util.ContentHelper.getCursor;
import static com.eightbitforest.awesomesms.util.ContentHelper.getInt;
import static com.eightbitforest.awesomesms.util.ContentHelper.getLong;
import static com.eightbitforest.awesomesms.util.ContentHelper.getString;
import static com.eightbitforest.awesomesms.util.ContentHelper.joinOnInt;


public class ContactObserver extends AutoContentObserver {

    /**
     * The listener to send contact updates to.
     */
    private IContactListener listener;

    // TODO: Store in shared prefs
    private long lastUpdate;

    /**
     * Creates the contact observer.
     *
     * @param listener        The listener to send contact updates to.
     * @param contactDatabase The database that holds all contacts that have already been parsed.
     *                        MUST include the following columns:
     *                        _id - The contact id.
     *                        error - Any error encountered during parsing.
     * @param contentResolver Android's content resolver to get content providers.
     */
    public ContactObserver(IContactListener listener, SQLiteDatabase contactDatabase, ContentResolver contentResolver) {
        super(contactDatabase, contentResolver, ContactsContract.RawContacts.CONTENT_URI);

        this.listener = listener;
    }


    /**
     * Gets called whenever the content://com.android.contact/contact database has been changed.
     *
     * @param selfChange True if this is a self-change notification.
     */
    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        try {
            // TODO: These may take a very long time. Move to different thread and show notification

            // Find any updated contacts
            updateContacts();

            // Remove deleted contacts
            trimContacts();

        } catch (InvalidCursorException e) {
            e.printStackTrace();
            Log.w(AwesomeSMS.TAG, e.getMessage());
        } finally {
            closeAllCursors();
        }
    }

    private void updateContacts() throws InvalidCursorException {
        Cursor contentCursor = getCursor(Contacts.CONTENT_URI,
                Contacts.CONTACT_LAST_UPDATED_TIMESTAMP + ">" + lastUpdate,
                Contacts.CONTACT_LAST_UPDATED_TIMESTAMP + " DESC", contentResolver, true);
        if (contentCursor.getCount() > 0) {
            lastUpdate = getLong(contentCursor, Contacts.CONTACT_LAST_UPDATED_TIMESTAMP);

            do {
                int id = getInt(contentCursor, Contacts._ID);
                try {
                    Contact contact = parseContact(id);
                    if (contact == null)
                        continue;

                    // We successfully parsed the contact, so add it to the database
                    ContentValues values = new ContentValues();
                    values.put(ContactDB.CONTACT_ID, id);
                    trackingDatabase.insert(ContactDB.TABLE_NAME, null, values);
                    listener.ContactUpdated(contact);
                } catch (ParseException e) {
                    // Error parsing the contact
                    ContentValues values = new ContentValues();
                    values.put(ContactDB.CONTACT_ID, id);
                    values.put(ContactDB.ERROR, e.getError());
                    trackingDatabase.insert(ContactDB.TABLE_NAME, null, values);
                    Log.e(AwesomeSMS.TAG, e.getMessage());
                }
            } while (contentCursor.moveToNext());
        }
    }

    private void trimContacts() throws InvalidCursorException {
        // Get contacts in our database that aren't already marked for deletion
        Cursor contactCursor = getCursor(trackingDatabase, ContactDB.TABLE_NAME,
                ContactDB.REMOVE + "=0", ContactDB.CONTACT_ID + " DESC");
        // Get contacts in android's db
        Cursor contentCursor = getCursor(Contacts.CONTENT_URI, Contacts._ID + " DESC", contentResolver);
        joinOnInt(contactCursor, ContactDB.CONTACT_ID, contentCursor, Contacts._ID, id -> {
            // Mark to delete in database
            ContentValues values = new ContentValues();
            values.put(ContactDB.CONTACT_ID, id);
            values.put(ContactDB.REMOVE, true);
            trackingDatabase.insert(ContactDB.TABLE_NAME, null, values);
            listener.ContactRemoved(id);
        }, null);
    }

    public Contact parseContact(int id) throws ParseException {
        try {
            // The only information we care about is phone numbers.
            Cursor phoneCursor = getCursor(Data.CONTENT_URI,
                    Data.CONTACT_ID + "=" + id + " AND " +
                            Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'",
                    null, contentResolver, true);

            // Make sure we found a contact with the specified id that has at least 1 phone number.
            if (phoneCursor.getCount() == 0)
                throw new ParseException(ContactDB.ERROR_NO_PHONE, "Cannot find any phone numbers for id: " + id);

            String name = getString(phoneCursor, Data.DISPLAY_NAME);
            ArrayList<Contact.Phone> phones = new ArrayList<>();
            do {
                phones.add(new Contact.Phone(
                        getString(phoneCursor, Phone.NUMBER),
                        getInt(phoneCursor, Phone.TYPE)));
            } while (phoneCursor.moveToNext());

            // Get thumbnail photo
            Cursor thumbnailCursor = getCursor(Data.CONTENT_URI,
                    Data.CONTACT_ID + "=" + id + " AND " +
                            Data.MIMETYPE + "='" + Photo.CONTENT_ITEM_TYPE + "'",
                    null, contentResolver);

            byte[] photo = null;
            if (thumbnailCursor.getCount() != 0) {
                photo = getBlob(thumbnailCursor, Photo.PHOTO);
            }

            close(thumbnailCursor);
            close(phoneCursor);

            return new Contact(
                    id,
                    name,
                    phones,
                    photo);
        } catch (InvalidCursorException e) {
            Log.w(AwesomeSMS.TAG, e.getMessage());
            return null;
        }
    }
}
