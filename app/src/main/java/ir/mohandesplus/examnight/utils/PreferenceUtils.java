package ir.mohandesplus.examnight.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceUtils {

    public static final String HAS_LOGGED_IN = "HasLoggedIn";

    public static void setHasLoggedIn(Context context, boolean hasLoggedIn) {
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(context).edit();
        editor.putBoolean(HAS_LOGGED_IN, hasLoggedIn);
        editor.apply();
    }

    public static boolean hasLoggedIn(Context context) {
        return PreferenceManager.
                getDefaultSharedPreferences(context).getBoolean(HAS_LOGGED_IN, false);
    }

}
