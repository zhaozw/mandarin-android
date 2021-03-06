package com.tomclaw.mandarin.core;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.TextUtils;

import com.tomclaw.mandarin.R;
import com.tomclaw.mandarin.main.ChatActivity;
import com.tomclaw.mandarin.main.MainActivity;
import com.tomclaw.mandarin.util.BitmapHelper;
import com.tomclaw.mandarin.util.Logger;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: Solkin
 * Date: 28.09.13
 * Time: 15:40
 */
public class HistoryDispatcher {

    private static final long HISTORY_DISPATCH_DELAY = 750;

    public static String EXTRA_READ_MESSAGES = "read_messages";

    private Context context;
    private NotificationManager notificationManager;
    private ContentResolver contentResolver;
    private ContentObserver historyObserver;
    private volatile long notificationCancelTime = 0;

    private static final int NOTIFICATION_ID = 0x01;

    private final int largeIconSize;
    private final int previewSize;

    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private boolean privateNotifications, settingsChanged;

    public HistoryDispatcher(Context context) {
        // Variables.
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        contentResolver = context.getContentResolver();
        // Creating observers.
        historyObserver = new HistoryObserver();
        largeIconSize = BitmapCache.convertDpToPixel(64, context);
        previewSize = BitmapCache.BITMAP_SIZE_ORIGINAL;
    }

    public void startObservation() {
        // Registering created observers.
        contentResolver.registerContentObserver(
                Settings.HISTORY_RESOLVER_URI, true, historyObserver);

        historyObserver.onChange(true);

        observePreferences();
    }

    private void observePreferences() {
        // Observing notification preferences to immediately update current notification.
        privateNotifications = PreferenceHelper.isPrivateNotifications(context);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (TextUtils.equals(key, context.getString(R.string.pref_private_notifications))) {
                    boolean privateNotifications = PreferenceHelper.isPrivateNotifications(context);
                    if (HistoryDispatcher.this.privateNotifications != privateNotifications) {
                        HistoryDispatcher.this.privateNotifications = privateNotifications;
                        settingsChanged = true;
                        historyObserver.onChange(true);
                    }
                }
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    private class HistoryObserver extends ContentObserver {

        ExecutorService executor;
        HistoryDispatcherTask historyDispatcherTask;
        Runnable taskWrapper;

        /**
         * Creates a content observer.
         */
        public HistoryObserver() {
            super(null);
            executor = Executors.newSingleThreadExecutor();
            historyDispatcherTask = new HistoryDispatcherTask();
            taskWrapper = new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(HISTORY_DISPATCH_DELAY);
                    } catch (InterruptedException ignored) {
                    }
                    TaskExecutor.getInstance().execute(historyDispatcherTask);
                }
            };
        }

        @Override
        public void onChange(boolean selfChange) {
            Logger.log("HistoryObserver: onChange [selfChange = " + selfChange + "]");
            executor.submit(taskWrapper);
        }
    }

    private class HistoryDispatcherTask extends Task {

        @Override
        @SuppressWarnings("unchecked")
        public void executeBackground() throws Throwable {
            long time = System.currentTimeMillis();
            // Obtain last unread for buddy. If exist.
            Bundle bundle = contentResolver.call(Settings.HISTORY_RESOLVER_URI, GlobalProvider.METHOD_GET_MESSAGES_COUNT, null, null);
            int unshown = bundle.getInt(GlobalProvider.KEY_UNSHOWN);
            int justShown = bundle.getInt(GlobalProvider.KEY_JUST_SHOWN);
            int onScreen = bundle.getInt(GlobalProvider.KEY_ON_SCREEN);
            // Checking for non-shown messages exist.
            // If yes - we must update notification with all unread messages. If no - nothing to do now.
            if (unshown > 0 || justShown > 0 || onScreen > 0 || settingsChanged) {
                bundle = contentResolver.call(Settings.HISTORY_RESOLVER_URI, GlobalProvider.METHOD_GET_UNREAD, null, null);
                ArrayList<NotificationData> unreadList =
                        (ArrayList<NotificationData>) bundle.getSerializable(GlobalProvider.KEY_NOTIFICATION_DATA);
                // Checking for unread messages exist. If no, we must cancel notification.
                if (unreadList != null && !unreadList.isEmpty()) {
                    boolean isAlarmRequired = (unshown > 0);
                    // Notification styles for multiple and single sender respectively.
                    NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                    NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
                    NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
                    // Building variables.
                    int unread = 0;
                    boolean multipleSenders = (unreadList.size() > 1);
                    StringBuilder nickNamesBuilder = new StringBuilder();
                    // Last-message variables.
                    int buddyDbId = 0;
                    Bitmap largeIcon = null;
                    String message = "";
                    int contentType = GlobalProvider.HISTORY_CONTENT_TYPE_TEXT;
                    String previewHash = "";
                    for (NotificationData data : unreadList) {
                        // Obtaining and collecting message-specific data.
                        unread += data.getUnreadCount();
                        message = data.getMessageText();
                        buddyDbId = data.getBuddyDbId();
                        String nickName = data.getBuddyNick();
                        String avatarHash = data.getBuddyAvatarHash();
                        contentType = data.getContentType();
                        previewHash = data.getPreviewHash();
                        if (TextUtils.isEmpty(nickName)) {
                            nickName = context.getString(R.string.unknown_buddy);
                            avatarHash = null;
                        }
                        if (TextUtils.isEmpty(nickNamesBuilder)) {
                            // This is first buddy with unread message.
                            if (!TextUtils.isEmpty(avatarHash)) {
                                // Obtain avatar for notification.
                                largeIcon = BitmapCache.getInstance().getBitmapSync(
                                        avatarHash, largeIconSize, largeIconSize, true, true);
                                // Make round avatar for lollipop and newer.
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    largeIcon = BitmapHelper.getRoundBitmap(largeIcon);
                                }
                            }
                        } else {
                            nickNamesBuilder.append(", ");
                            // There are some buddies with unread - no avatar can be placed.
                            largeIcon = null;
                        }
                        nickNamesBuilder.append(nickName);
                        // Checking for style type for correct filling.
                        if (multipleSenders) {
                            inboxStyle.addLine(Html.fromHtml("<b>" + nickName + "</b> " + message));
                        }
                    }
                    // Show chat activity with concrete buddy.
                    PendingIntent replyNowIntent = PendingIntent.getActivity(context, 1,
                            new Intent(context, ChatActivity.class)
                                    .putExtra(GlobalProvider.HISTORY_BUDDY_DB_ID, buddyDbId)
                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
                            PendingIntent.FLAG_CANCEL_CURRENT);
                    // Mark all messages as read.
                    PendingIntent readAllIntent = PendingIntent.getService(context, 2,
                            new Intent(context, CoreService.class)
                                    .putExtra(EXTRA_READ_MESSAGES, true),
                            PendingIntent.FLAG_CANCEL_CURRENT);
                    // Simply open chats list.
                    PendingIntent openChatsIntent = PendingIntent.getActivity(context, 3,
                            new Intent(context, MainActivity.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
                            PendingIntent.FLAG_CANCEL_CURRENT);
                    // Common notification variables.
                    String title;
                    String content;
                    int actionIcon;
                    String actionButton;
                    PendingIntent actionIntent;
                    String readButton;
                    NotificationCompat.Style style;
                    boolean privateNotifications = PreferenceHelper.isPrivateNotifications(context);
                    // Checking for required style.
                    if (multipleSenders || privateNotifications) {
                        title = context.getResources().getQuantityString(R.plurals.count_new_messages, unread, unread);
                        content = nickNamesBuilder.toString();
                        actionIcon = R.drawable.ic_reply;
                        actionButton = context.getString(R.string.reply_now);
                        actionIntent = replyNowIntent;
                        readButton = context.getString(R.string.mark_as_read_all);
                        if (privateNotifications) {
                            style = null;
                        } else {
                            inboxStyle.setBigContentTitle(title);
                            style = inboxStyle;
                        }
                    } else {
                        title = nickNamesBuilder.toString();
                        content = message;
                        actionIcon = R.drawable.ic_chat;
                        actionButton = context.getString(R.string.dialogs);
                        actionIntent = openChatsIntent;
                        readButton = context.getString(R.string.mark_as_read);
                        if ((contentType == GlobalProvider.HISTORY_CONTENT_TYPE_PICTURE ||
                                contentType == GlobalProvider.HISTORY_CONTENT_TYPE_VIDEO) &&
                                !TextUtils.isEmpty(previewHash)) {
                            Bitmap previewFull = BitmapCache.getInstance().getBitmapSync(previewHash, previewSize, previewSize, true, false);
                            bigPictureStyle.bigPicture(previewFull);
                            bigPictureStyle.setSummaryText(message);
                            style = bigPictureStyle;
                        } else {
                            bigTextStyle.bigText(message);
                            bigTextStyle.setBigContentTitle(title);
                            style = bigTextStyle;
                        }
                    }
                    // Notification prepare.
                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                            .setContentTitle(title)
                            .setContentText(content)
                            .setSmallIcon(R.drawable.ic_notification)
                            .setStyle(style)
                            .addAction(actionIcon, actionButton, actionIntent)
                            .addAction(R.drawable.ic_action_read, readButton, readAllIntent)
                            .setContentIntent(multipleSenders ? openChatsIntent : replyNowIntent)
                            .setLargeIcon(largeIcon)
                            .setColor(context.getResources().getColor(R.color.accent_color));
                    if (isAlarmRequired && isNotificationCompleted()) {
                        if (PreferenceHelper.isSound(context)) {
                            notificationBuilder.setSound(PreferenceHelper.getNotificationUri(context));
                        }
                        if (PreferenceHelper.isVibrate(context)) {
                            notificationBuilder.setVibrate(new long[]{0, 750});
                        }
                        if (PreferenceHelper.isLights(context)) {
                            notificationBuilder.setLights(Settings.LED_COLOR_RGB,
                                    Settings.LED_BLINK_DELAY, Settings.LED_BLINK_DELAY);
                        }
                        onNotificationShown();
                    }
                    Notification notification = notificationBuilder.build();
                    // Notify it right now!
                    notificationManager.notify(NOTIFICATION_ID, notification);
                    // Update shown messages flag.
                    QueryHelper.updateShownMessagesFlag(contentResolver);
                } else {
                    Logger.log("HistoryObserver: No unread messages found");
                    onNotificationCancel();
                    notificationManager.cancel(NOTIFICATION_ID);
                }
                if (onScreen > 0) {
                    Logger.log("HistoryObserver: Vibrate a little");
                    Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(80);
                    QueryHelper.updateOnScreenMessages(contentResolver);
                }
            } else {
                Logger.log("HistoryObserver: Non-shown messages not found");
            }
            Logger.log("History dispatching time: " + (System.currentTimeMillis() - time));
            // Call to update unread count.
            contentResolver.call(Settings.BUDDY_RESOLVER_URI, GlobalProvider.METHOD_UPDATE_UNREAD, null, null);
            settingsChanged = false;
        }

        private void onNotificationShown() {
            notificationCancelTime = System.currentTimeMillis() + Settings.NOTIFICATION_MIN_DELAY;
        }

        private void onNotificationCancel() {
            long notificationRemain = getNotificationRemain();
            if (notificationRemain > 0) {
                try {
                    // Take some time to read this message and notification to be shown
                    Thread.sleep(notificationRemain);
                } catch (InterruptedException ignored) {
                }
            }
        }

        private boolean isNotificationCompleted() {
            return getNotificationRemain() <= 0;
        }

        private long getNotificationRemain() {
            return notificationCancelTime - System.currentTimeMillis();
        }
    }
}
