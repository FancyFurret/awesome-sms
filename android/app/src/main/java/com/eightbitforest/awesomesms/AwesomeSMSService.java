package com.eightbitforest.awesomesms;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.eightbitforest.awesomesms.model.AwesomeSMSDBHelper;
import com.eightbitforest.awesomesms.network.Messenger;
import com.eightbitforest.awesomesms.observer.ContactObserver;
import com.eightbitforest.awesomesms.observer.TextObserver;

// TODO: Ask for permissions

/**
 * Service that starts the content observers.
 *
 * @author Forrest Jones
 */
public class AwesomeSMSService extends Service {

    // TODO: start sticky
    @Override
    public void onCreate() {
        Messenger messenger = new Messenger(getBaseContext());
        AwesomeSMSDBHelper helper = new AwesomeSMSDBHelper(getBaseContext());
        SQLiteDatabase database = helper.getWritableDatabase();

        // Create and register TextObserver
        TextObserver textObserver = new TextObserver(messenger, getBaseContext(), database, getContentResolver());
        textObserver.register();

        // Create and register ContactObserver
        ContactObserver contactObserver = new ContactObserver(messenger, getBaseContext(), database, getContentResolver());
        contactObserver.register();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
