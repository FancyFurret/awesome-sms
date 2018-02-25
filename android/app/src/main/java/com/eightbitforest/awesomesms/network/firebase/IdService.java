package com.eightbitforest.awesomesms.network.firebase;

import android.util.Log;

import com.eightbitforest.awesomesms.AwesomeSMS;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class IdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        // TODO: Send token to server
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.i(AwesomeSMS.TAG, "Refreshed token: " + refreshedToken);
    }
}
