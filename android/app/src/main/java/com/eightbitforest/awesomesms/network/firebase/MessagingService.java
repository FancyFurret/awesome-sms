package com.eightbitforest.awesomesms.network.firebase;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.eightbitforest.awesomesms.AwesomeSMS;
import com.eightbitforest.awesomesms.messaging.SmsSender;
import com.eightbitforest.awesomesms.model.TextMessage;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import static com.eightbitforest.awesomesms.AwesomeSMS.SERVER_IP;

public class MessagingService extends FirebaseMessagingService {

    private static final String GET_ATTACHMENTS = "/get_attachments";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        if (data.size() > 0) {
            Log.i(AwesomeSMS.TAG, "Message data payload: " + remoteMessage.getData());
            String event = data.get("event");
            switch (event) {
                case "send_message":
                    sendMessage(data);
            }
        }
    }

    private void sendMessage(Map<String, String> data) {
        String body = data.get("body");
        int threadId = Integer.parseInt(data.get("threadId"));

        String addressesStr = data.get("addresses");
        addressesStr = addressesStr.replaceAll("[^0-9,+]", ""); // Remove non-phone characters
        String[] addresses = addressesStr.split(",");

        String attachmentsStr = data.get("attachments");
        attachmentsStr = attachmentsStr.replaceAll("[^0-9,]", ""); // Remove non-numbers
        String[] attachments = attachmentsStr.split(",");

        if (attachments.length > 0) {

            StringBuilder params = new StringBuilder("?");
            for (String attachment : attachments)
                params.append("attachments=").append(attachment).append("&");
            params.replace(params.length() - 1, params.length(), "");


            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.GET,
                    SERVER_IP + GET_ATTACHMENTS + params.toString(),
                    null,
                    response -> {
                        JSONArray attachmentsJson;
                        try {
                            attachmentsJson = response.getJSONArray("attachments");

                            TextMessage.Attachment[] attachmentsData = new TextMessage.Attachment[attachmentsJson.length()];
                            for (int i = 0; i < attachmentsJson.length(); i++) {
                                attachmentsData[i] = new TextMessage.Attachment(
                                        -1,
                                        attachmentsJson.getJSONObject(i).getString("mime"),
                                        attachmentsJson.getJSONObject(i).getString("data")
                                );
                            }

                            SmsSender.sendMessage(getApplicationContext(), body, addresses, threadId, attachmentsData);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Log.e(AwesomeSMS.TAG, "Unable to get attachments from server!" + error)) {
            };

            Volley.newRequestQueue(getApplicationContext()).add(request);
        } else {
            // TODO: Run in new thread
            Log.i(AwesomeSMS.TAG, "Sending: " + body + " to " + addressesStr);
            SmsSender.sendMessage(getApplicationContext(), body, addresses, threadId);
        }
    }
}

