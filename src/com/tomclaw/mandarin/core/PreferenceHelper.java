package com.tomclaw.mandarin.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.text.TextUtils;
import com.tomclaw.mandarin.R;

/**
 * Created with IntelliJ IDEA.
 * User: solkin
 * Date: 11/15/13
 * Time: 1:56 PM
 */
public class PreferenceHelper {

    public static boolean isCollapseMessages(Context context) {
        return getBooleanPreference(context, R.string.pref_collapse_messages, R.bool.pref_collapse_messages_default);
    }

    public static boolean isShowTemp(Context context) {
        return getBooleanPreference(context, R.string.pref_show_temp, R.bool.pref_show_temp_default);
    }

    public static boolean isSystemNotifications(Context context) {
        return getBooleanPreference(context, R.string.pref_system_notifications, R.bool.pref_system_notifications_default);
    }

    public static boolean isVibrate(Context context) {
        return getBooleanPreference(context, R.string.pref_vibrate, R.bool.pref_vibrate_default);
    }

    public static boolean isAutorun(Context context) {
        return getBooleanPreference(context, R.string.pref_autorun, R.bool.pref_autorun_default);
    }

    public static Uri getNotificationUri(Context context) {
        String uriValue = getStringPreference(context, R.string.pref_notification_sound,
                R.string.pref_notification_sound_default);
        // Checking for default value found.
        if(TextUtils.equals(uriValue, context.getString(R.string.pref_notification_sound_default))) {
            return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        return Uri.parse(uriValue);
    }

    private static boolean getBooleanPreference(Context context, int preferenceKey, int defaultValueKey) {
        return getSharedPreferences(context).getBoolean(context.getResources().getString(preferenceKey),
                context.getResources().getBoolean(defaultValueKey));
    }

    private static String getStringPreference(Context context, int preferenceKey, int defaultValueKey) {
        return getSharedPreferences(context).getString(context.getResources().getString(preferenceKey),
                context.getResources().getString(defaultValueKey));
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(getDefaultSharedPreferencesName(context),
                getSharedPreferencesMode());
    }

    private static String getDefaultSharedPreferencesName(Context context) {
        return context.getPackageName() + "_preferences";
    }

    private static int getSharedPreferencesMode() {
        return Context.MODE_MULTI_PROCESS;
    }
}