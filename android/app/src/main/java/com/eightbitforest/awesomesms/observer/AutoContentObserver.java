package com.eightbitforest.awesomesms.observer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;

/**
 * A content observer that is able to register an unregister itself.
 *
 * @author Forrest Jones
 */
public abstract class AutoContentObserver extends ContentObserver {

    /** Android's content resolver to get content providers. */
    ContentResolver contentResolver;

    /** A database used be children to store already changed rows. */
    SQLiteDatabase trackingDatabase;

    /** The context of the running application */
    Context context;

    /** The uri that this ContentObserver should observe. */
    private Uri observeUri;

    /**
     * Constructs an AutoContentObserver. Does not register withe the ContentResolver, you must call
     * register().
     *
     * @param trackingDatabase The database to track already changed rows.
     * @param context         The context of the app.
     * @param contentResolver  Android's content resolver to get content providers.
     * @param observeUri       The uri that this ContentObserver should observe.
     */
    AutoContentObserver(SQLiteDatabase trackingDatabase, Context context, ContentResolver contentResolver, Uri observeUri) {
        super(new Handler());

        this.trackingDatabase = trackingDatabase;
        this.context = context;
        this.contentResolver = contentResolver;
        this.observeUri = observeUri;
    }

    /**
     * Registers this ContentObserver on the provided Uri.
     */
    public void register() {
        contentResolver.registerContentObserver(observeUri, true, this);
    }

    /**
     * Unregisters this ContentObserver from the provided Uri.
     */
    public void unregister() {
        contentResolver.unregisterContentObserver(this);
    }
}
