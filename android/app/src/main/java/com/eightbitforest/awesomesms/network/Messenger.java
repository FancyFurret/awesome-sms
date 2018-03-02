package com.eightbitforest.awesomesms.network;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.eightbitforest.awesomesms.AwesomeSMS;
import com.eightbitforest.awesomesms.model.Contact;
import com.eightbitforest.awesomesms.model.TextMessage;
import com.eightbitforest.awesomesms.observer.IContactListener;
import com.eightbitforest.awesomesms.observer.ITextListener;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import static com.eightbitforest.awesomesms.AwesomeSMS.SERVER_IP;

/**
 * Communicates with the server.
 *
 * @author Forrest Jones
 */
public class Messenger implements ITextListener, IContactListener {

    private static final String INSERT_MESSAGE = "/insert_message";
    private static final String UPDATE_CONTACT = "/update_contact";
    private static final String DELETE_CONTACT = "/delete_contact";

    private Gson gson;
    private RequestQueue requestQueue;

    public Messenger(Context context) {
        this.gson = new Gson();
        requestQueue = Volley.newRequestQueue(context);
    }

    @Override
    public void NewText(TextMessage text) {
        Log.i(AwesomeSMS.TAG, text.toString());
        // TODO: Attachments can be big. Send separately?
        // FIXME: Object -> Json -> String -> Json is ridiculous and extremely slow.
        try {
            //Log.i(AwesomeSMS.TAG, "Sending: " + new JSONObject(gson.toJson(text)).toString());
            JsonObjectRequest request = new JsonObjectRequest(SERVER_IP + INSERT_MESSAGE, new JSONObject(gson.toJson(text)),
                    response -> Log.i(AwesomeSMS.TAG, "Successfully sent message to server! " + response),
                    error -> Log.e(AwesomeSMS.TAG, "Unable to send message to server!" + error)) {
            };
            request.setRetryPolicy(new DefaultRetryPolicy(5000,
                    5,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void ContactUpdated(Contact contact) {
        try {
            Log.i(AwesomeSMS.TAG, "Sending: " + new JSONObject(gson.toJson(contact)).toString());
            JsonObjectRequest request = new JsonObjectRequest(SERVER_IP + UPDATE_CONTACT, new JSONObject(gson.toJson(contact)),
                    response -> Log.i(AwesomeSMS.TAG, "Successfully sent message to server! " + response),
                    error -> Log.e(AwesomeSMS.TAG, "Unable to send message to server!" + error)) {
            };
            request.setRetryPolicy(new DefaultRetryPolicy(5000,
                    5,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void ContactRemoved(int id) {
        try {
            JSONObject removedContact = new JSONObject();
            removedContact.put("id", id);
            Log.i(AwesomeSMS.TAG, "Sending: " + removedContact.toString());
            JsonObjectRequest request = new JsonObjectRequest(SERVER_IP + DELETE_CONTACT, removedContact,
                    response -> Log.i(AwesomeSMS.TAG, "Successfully sent message to server! " + response),
                    error -> Log.e(AwesomeSMS.TAG, "Unable to send message to server!" + error)) {
            };
            request.setRetryPolicy(new DefaultRetryPolicy(5000,
                    5,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
