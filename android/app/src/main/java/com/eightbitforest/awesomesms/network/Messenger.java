package com.eightbitforest.awesomesms.network;

import android.util.Log;

import com.eightbitforest.awesomesms.AwesomeSMS;
import com.eightbitforest.awesomesms.model.TextMessage;
import com.eightbitforest.awesomesms.text_listener.ITextListener;

/**
 * Communicates with the server. Eventually.
 *
 * @author Forrest Jones
 */

public class Messenger implements ITextListener {
    public void send(String[] from, String message, long date) {
    }

    @Override
    public void TextSent(TextMessage text) {
        Log.i(AwesomeSMS.TAG, text.toString());
    }

    @Override
    public void TextReceived(TextMessage text) {
        Log.i(AwesomeSMS.TAG, text.toString());
    }
}
