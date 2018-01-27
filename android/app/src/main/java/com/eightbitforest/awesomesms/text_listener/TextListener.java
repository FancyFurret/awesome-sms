package com.eightbitforest.awesomesms.text_listener;

import android.content.ContentResolver;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.provider.Telephony;
import android.util.Log;

import com.eightbitforest.awesomesms.AwesomeSMS;

/**
 * Starts and registers the TextObserver.
 *
 * @author Forrest Jones
 */

public class TextListener {
    public void start(ITextListener listener, SQLiteDatabase message_database, ContentResolver contentResolver) {
        Log.i(AwesomeSMS.TAG, "Starting TextListener...");

        TextObserver textObserver = new TextObserver(new Handler(), listener, message_database, contentResolver);
        contentResolver.registerContentObserver(Telephony.MmsSms.CONTENT_URI, true, textObserver);
    }
}
