package com.eightbitforest.awesomesms.network.firebase;

import android.util.Log;

import com.eightbitforest.awesomesms.AwesomeSMS;
import com.eightbitforest.awesomesms.messaging.SmsSender;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MessagingService extends FirebaseMessagingService {
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

        // TODO: Run in new thread
        Log.i(AwesomeSMS.TAG, "Sending: " + body + " to " + addressesStr);
        SmsSender.sendMessage(getApplicationContext(), body, addresses, threadId);

    }
}

