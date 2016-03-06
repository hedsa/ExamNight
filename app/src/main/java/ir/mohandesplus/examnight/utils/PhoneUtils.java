package ir.mohandesplus.examnight.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

public class PhoneUtils {

    public static String getDeviceIMEI(Context context) {
        return ((TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
    }

}
