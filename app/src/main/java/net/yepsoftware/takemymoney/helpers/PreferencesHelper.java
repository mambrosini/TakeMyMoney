package net.yepsoftware.takemymoney.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by Maxi on 23/1/2017.
 */
public class PreferencesHelper {
    public static final String APP_STATE_UNREGISTERED = "unregistered";
    public static final String APP_STATE_UNAUTHENTICATED = "unauthenticated";
    public static final String APP_STATE_AUTHENTICATED = "authenticated";

    private static final String SARED_PREFS_NAME = "takemymoney.prefs";
    private static final String AUTHENTICATED_PREF = "pref.auth";
    private static final String USERNAME_PREF = "pref.user";
    private static final String PASSWORD_PREF = "pref.pswd";
    private static final String AUTOMATIC_LOGIN_PREF = "pref.autologin";
    private static final String UID_PREF = "pref.uid";
    private static final String PHONE_PREF = "pref.phone";
    private static final String SECONDARY_MAIL_PREF = "pref.user.secondary";

    public static void setAppState(Context context, String appState){
        SharedPreferences preferences = context.getSharedPreferences(SARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(AUTHENTICATED_PREF, appState);
        editor.commit();
        Log.d("PreferencesHelper", "App state set to: " + appState);
    }

    public static String getAppState(Context context){
        SharedPreferences preferences = context.getSharedPreferences(SARED_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(AUTHENTICATED_PREF, APP_STATE_UNREGISTERED);
    }

    public static void saveMail(Context context, String username){
        SharedPreferences preferences = context.getSharedPreferences(SARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(USERNAME_PREF, username);
        editor.commit();
    }

    public static String getMail(Context context){
        SharedPreferences preferences = context.getSharedPreferences(SARED_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(USERNAME_PREF, "");
    }

    public static void savePassword(Context context, String password){
        SharedPreferences preferences = context.getSharedPreferences(SARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PASSWORD_PREF, password);
        editor.commit();
    }

    public static String getPassword(Context context){
        SharedPreferences preferences = context.getSharedPreferences(SARED_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PASSWORD_PREF, "");
    }

    public static void setAutoLogin(Context context, boolean autoLogin){
        SharedPreferences preferences = context.getSharedPreferences(SARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(AUTOMATIC_LOGIN_PREF, autoLogin);
        editor.commit();
    }

    public static boolean isAutoLogin(Context context){
        SharedPreferences preferences = context.getSharedPreferences(SARED_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(AUTOMATIC_LOGIN_PREF, true);
    }

    public static void saveUserId(Context context, String userId){
        SharedPreferences preferences = context.getSharedPreferences(SARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(UID_PREF, userId);
        editor.commit();
    }

    public static String getUserId(Context context){
        SharedPreferences preferences = context.getSharedPreferences(SARED_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(UID_PREF, "");
    }

    public static void resetSettingsAndUnlinkDevice(Context context){
        setAppState(context, APP_STATE_UNREGISTERED);
        saveMail(context, "");
        savePassword(context, "");
        saveUserId(context, "");
        saveSecondaryMail(context, "");
        savePhone(context, "");
        setAutoLogin(context, true);
    }

    public static void setTestAccount(Context context){
        setAppState(context, APP_STATE_AUTHENTICATED);
        saveMail(context, "test@takemymoney.com");
        savePassword(context, "Maxman16");
        saveUserId(context, "yJUHfWPhMBZaTILVAOjmtCO52wA2");
        saveSecondaryMail(context, "");
        savePhone(context, "2613847779");
        setAutoLogin(context, true);
    }




    public static void saveSecondaryMail(Context context, String username){
        SharedPreferences preferences = context.getSharedPreferences(SARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SECONDARY_MAIL_PREF, username);
        editor.commit();
    }

    public static String getSecondaryMail(Context context){
        SharedPreferences preferences = context.getSharedPreferences(SARED_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(SECONDARY_MAIL_PREF, "");
    }

    public static void savePhone(Context context, String username){
        SharedPreferences preferences = context.getSharedPreferences(SARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PHONE_PREF, username);
        editor.commit();
    }

    public static String getPhone(Context context){
        SharedPreferences preferences = context.getSharedPreferences(SARED_PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(PHONE_PREF, "");
    }
}
