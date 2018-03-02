package com.eightbitforest.awesomesms.messaging;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;

import com.eightbitforest.awesomesms.model.TextMessage;
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

    public static void sendMessage(Context context, String body, String[] addresses, int threadId, TextMessage.Attachment[] attachments) {
        Settings settings = new Settings();
        settings.setUseSystemSending(true);

        Transaction transaction = new Transaction(context, settings);
        Message message = new Message(body, addresses);
        for (TextMessage.Attachment attachment : attachments) {
            message.addMedia(Base64.decode(attachment.getData(), Base64.DEFAULT), attachment.getMime());
        }
        transaction.sendNewMessage(message, threadId);
    }
}
