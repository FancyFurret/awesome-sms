package com.eightbitforest.awesomesms;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.eightbitforest.awesomesms.model.TextMessageDBHelper;
import com.eightbitforest.awesomesms.network.Messenger;
import com.eightbitforest.awesomesms.text_listener.TextListener;

/**
 * Service that starts the TextListener.
 *
 * @author Forrest Jones
 */
public class AwesomeSMSService extends Service {

    // TODO: start sticky
    @Override
    public void onCreate() {
        TextMessageDBHelper dbHelper = new TextMessageDBHelper(getBaseContext());
        new TextListener().start(new Messenger(), dbHelper.getWritableDatabase(), getContentResolver());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
