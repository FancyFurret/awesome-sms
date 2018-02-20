package com.eightbitforest.awesomesms.util;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.eightbitforest.awesomesms.AwesomeSMS;
import com.eightbitforest.awesomesms.R;

/**
 * Some utility methods used elsewhere in the app.
 *
 * @author Forrest Jones
 */
public class Util {

    /**
     * Normalizes a phone number by removing everything but numbers except for a leading +.
     * If a country code is not supplied, we add the default country code used by this phone.
     *
     * @param context Context of the app.
     * @param phone The phone number to normalize.
     * @return Returns a normalized version of the provided phone number.
     */
    public static String normalizePhone(Context context, String phone) {
        boolean hasCountryCode = phone.startsWith("+");
        String strippedPhone = phone.replaceAll("[^0-9]", "");

        if (hasCountryCode)
            return "+" + strippedPhone;
        else {
            // Thanks Wais, https://stackoverflow.com/questions/5402253/getting-telephone-country-code-with-android
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String countryId = manager.getSimCountryIso().toUpperCase();
            String[] countryCodeResources = context.getResources().getStringArray(R.array.CountryCodes);
            for (String countryCodeResource : countryCodeResources) {
                String[] countryCode = countryCodeResource.split(",");
                if (countryCode[1].equals(countryId)) {
                    return "+" + countryCode[0] + strippedPhone;
                }
            }
            Log.e(AwesomeSMS.TAG, "Could not find country code for id: " + countryId);
            return null;
        }
    }
}
