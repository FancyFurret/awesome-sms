package com.eightbitforest.awesomesms.messaging;

import android.content.Context;

import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;

public class SmsSender {
    public static void sendMessage(Context context, String body, String[] addresses, int threadId) {
        Settings settings = new Settings();
        settings.setUseSystemSending(true);

        Transaction transaction = new Transaction(context, settings);
        Message message = new Message(body, addresses);
        transaction.sendNewMessage(message, threadId);
    }
}
