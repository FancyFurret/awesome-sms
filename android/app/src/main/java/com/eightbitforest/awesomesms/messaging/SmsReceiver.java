package com.eightbitforest.awesomesms.messaging;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.eightbitforest.awesomesms.AwesomeSMS;
import com.klinker.android.send_message.MmsSentReceiver;

/**
 * Created by osum4est on 2/21/18.
 */

public class SmsReceiver extends MmsSentReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(AwesomeSMS.TAG, "Successfully sent message!");
    }
}
