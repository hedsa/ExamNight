package ir.mohandesplus.examnight.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceUtils {

    public static final String
            HAS_LOGGED_IN = "HasLoggedIn",
            EMAIL = "Email",
            PASSWORD = "Password";

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

    public static void setEmail(Context context, String email) {
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(context).edit();
        editor.putString(EMAIL, email);
        editor.apply();
    }

    public static String getEmail(Context context) {
        return PreferenceManager.
                getDefaultSharedPreferences(context).getString(EMAIL, null);
    }

    public static void setPassword(Context context, String password) {
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(context).edit();
        editor.putString(PASSWORD, password);
        editor.apply();
    }

    public static String getPassword(Context context) {
        return PreferenceManager.
                getDefaultSharedPreferences(context).getString(PASSWORD, null);
    }

}
