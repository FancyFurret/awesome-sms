package com.eightbitforest.awesomesms.network;

import android.util.Log;

import com.eightbitforest.awesomesms.AwesomeSMS;
import com.eightbitforest.awesomesms.model.Contact;
import com.eightbitforest.awesomesms.model.TextMessage;
import com.eightbitforest.awesomesms.observer.IContactListener;
import com.eightbitforest.awesomesms.observer.ITextListener;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Communicates with the server.
 *
 * @author Forrest Jones
 */
public class Messenger implements ITextListener, IContactListener {

    private static final String IP = "192.168.1.79:11150"; // TODO: Move to settings
    private Gson gson;

    public Messenger() {
        this.gson = new Gson();
    }

    @Override
    public void NewText(TextMessage text) {
        Log.i(AwesomeSMS.TAG, "Message JSON: " + gson.toJson(text));
    }

    @Override
    public void ContactUpdated(Contact contact) {
        Log.i(AwesomeSMS.TAG, "Contact JSON: " + gson.toJson(contact));
    }

    @Override
    public void ContactRemoved(int id) {
    }
}
