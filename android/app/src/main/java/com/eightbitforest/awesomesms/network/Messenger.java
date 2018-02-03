package com.eightbitforest.awesomesms.network;

import android.util.Log;

import com.eightbitforest.awesomesms.AwesomeSMS;
import com.eightbitforest.awesomesms.model.Contact;
import com.eightbitforest.awesomesms.model.TextMessage;
import com.eightbitforest.awesomesms.observer.IContactListener;
import com.eightbitforest.awesomesms.observer.ITextListener;

/**
 * Communicates with the server. Eventually.
 *
 * @author Forrest Jones
 */
public class Messenger implements ITextListener, IContactListener {

    @Override
    public void TextSent(TextMessage text) {
        Log.i(AwesomeSMS.TAG, text.toString());
    }

    @Override
    public void TextReceived(TextMessage text) {
        Log.i(AwesomeSMS.TAG, text.toString());
    }

    @Override
    public void ContactUpdated(Contact contact) {
        Log.i(AwesomeSMS.TAG, contact.toString());
    }

    @Override
    public void ContactRemoved(int id) {
        Log.i(AwesomeSMS.TAG, "Deleted " + id);
    }
}
