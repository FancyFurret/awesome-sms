package com.eightbitforest.awesomesms.observer;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;


public abstract class AutoContentObserver extends ContentObserver {

    /**
     * Android's content resolver to get content providers.
     */
    ContentResolver contentResolver;

    /**
     * The database that holds all messages that have already been parsed.
     */
    SQLiteDatabase trackingDatabase;

    Uri observeUri;

    AutoContentObserver(SQLiteDatabase trackingDatabase, ContentResolver contentResolver, Uri observeUri) {
        super(new Handler());

        this.trackingDatabase = trackingDatabase;
        this.contentResolver = contentResolver;
        this.observeUri = observeUri;
    }

    public void register() {
        contentResolver.registerContentObserver(observeUri, true, this);
    }

    public void unregister() {
        contentResolver.unregisterContentObserver(this);
    }
}
