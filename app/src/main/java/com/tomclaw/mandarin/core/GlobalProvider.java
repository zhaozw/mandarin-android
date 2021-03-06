package com.tomclaw.mandarin.core;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.tomclaw.mandarin.util.Logger;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: solkin
 * Date: 4/23/13
 * Time: 12:53 AM
 */
public class GlobalProvider extends ContentProvider {

    // Table
    public static final String REQUEST_TABLE = "requests";
    public static final String ACCOUNTS_TABLE = "accounts";
    public static final String ROSTER_GROUP_TABLE = "roster_group";
    public static final String ROSTER_BUDDY_TABLE = "roster_buddy";
    public static final String CHAT_HISTORY_TABLE = "chat_history";
    public static final String CHAT_HISTORY_TABLE_DISTINCT = "chat_history_distinct";

    // Fields
    public static final String ROW_AUTO_ID = "_id";

    public static final String REQUEST_TYPE = "request_type";
    public static final String REQUEST_CLASS = "request_class";
    public static final String REQUEST_SESSION = "request_session";
    public static final String REQUEST_PERSISTENT = "request_persistent";
    public static final String REQUEST_ACCOUNT_DB_ID = "account_db_id";
    public static final String REQUEST_STATE = "request_state";
    public static final String REQUEST_BUNDLE = "request_bundle";
    public static final String REQUEST_TAG = "request_tag";

    public static final String ACCOUNT_NAME = "account_name";
    public static final String ACCOUNT_TYPE = "account_type";
    public static final String ACCOUNT_USER_ID = "account_user_id";
    public static final String ACCOUNT_USER_PASSWORD = "account_user_password";
    public static final String ACCOUNT_STATUS = "account_status";
    public static final String ACCOUNT_STATUS_TITLE = "account_status_title";
    public static final String ACCOUNT_STATUS_MESSAGE = "account_status_message";
    public static final String ACCOUNT_CONNECTING = "account_connecting";
    public static final String ACCOUNT_BUNDLE = "account_bundle";
    public static final String ACCOUNT_AVATAR_HASH = "account_avatar_hash";

    public static final String ROSTER_GROUP_ACCOUNT_DB_ID = "account_db_id";
    public static final String ROSTER_GROUP_NAME = "group_name";
    public static final String ROSTER_GROUP_ID = "group_id";
    public static final String ROSTER_GROUP_TYPE = "group_type";
    public static final String ROSTER_GROUP_UPDATE_TIME = "group_update_time";

    public static final String GROUP_TYPE_SYSTEM = "group_system";
    public static final String GROUP_TYPE_DEFAULT = "group_default";
    public static final int GROUP_ID_RECYCLE = -1;

    public static final String ROSTER_BUDDY_ACCOUNT_DB_ID = "account_db_id";
    public static final String ROSTER_BUDDY_ACCOUNT_TYPE = "account_id";
    public static final String ROSTER_BUDDY_ID = "buddy_id";
    public static final String ROSTER_BUDDY_NICK = "buddy_nick";
    public static final String ROSTER_BUDDY_STATUS = "buddy_status";
    public static final String ROSTER_BUDDY_STATUS_TITLE = "buddy_status_title";
    public static final String ROSTER_BUDDY_STATUS_MESSAGE = "buddy_status_message";
    public static final String ROSTER_BUDDY_GROUP_ID = "buddy_group_id";
    public static final String ROSTER_BUDDY_GROUP = "buddy_group";
    public static final String ROSTER_BUDDY_DIALOG = "buddy_dialog";
    public static final String ROSTER_BUDDY_UPDATE_TIME = "buddy_update_time";
    public static final String ROSTER_BUDDY_ALPHABET_INDEX = "buddy_alphabet_index";
    public static final String ROSTER_BUDDY_UNREAD_COUNT = "buddy_unread_count";
    public static final String ROSTER_BUDDY_AVATAR_HASH = "buddy_avatar_hash";
    public static final String ROSTER_BUDDY_SEARCH_FIELD = "buddy_search_field";
    public static final String ROSTER_BUDDY_DRAFT = "buddy_draft";
    public static final String ROSTER_BUDDY_LAST_SEEN = "buddy_last_seen";
    public static final String ROSTER_BUDDY_LAST_TYPING = "buddy_last_typing";
    public static final String ROSTER_BUDDY_OPERATION = "buddy_operation";
    public static final String ROSTER_BUDDY_LAST_MESSAGE_TIME = "buddy_last_message_time";

    public static final int ROSTER_BUDDY_OPERATION_NO = 0;
    public static final int ROSTER_BUDDY_OPERATION_ADD = 1;
    public static final int ROSTER_BUDDY_OPERATION_RENAME = 2;
    public static final int ROSTER_BUDDY_OPERATION_REMOVE = 3;

    public static final String HISTORY_BUDDY_ACCOUNT_DB_ID = "account_db_id";
    public static final String HISTORY_BUDDY_DB_ID = "buddy_db_id";
    public static final String HISTORY_MESSAGE_TYPE = "message_type";
    public static final String HISTORY_MESSAGE_COOKIE = "message_cookie";
    public static final String HISTORY_MESSAGE_STATE = "message_state";
    public static final String HISTORY_MESSAGE_TIME = "message_time";
    public static final String HISTORY_MESSAGE_TEXT = "message_text";
    public static final String HISTORY_MESSAGE_READ = "message_read";
    public static final String HISTORY_NOTICE_SHOWN = "notice_shown";
    public static final String HISTORY_SEARCH_FIELD = "search_field";
    public static final String HISTORY_CONTENT_TYPE = "content_type";
    public static final String HISTORY_CONTENT_SIZE = "content_size";
    public static final String HISTORY_CONTENT_STATE = "content_state";
    public static final String HISTORY_CONTENT_PROGRESS = "content_progress";
    public static final String HISTORY_CONTENT_URI = "content_uri";
    public static final String HISTORY_CONTENT_NAME = "content_name";
    public static final String HISTORY_PREVIEW_HASH = "preview_hash";
    public static final String HISTORY_CONTENT_TAG = "content_tag";

    public static final int HISTORY_MESSAGE_TYPE_ERROR = 0;
    public static final int HISTORY_MESSAGE_TYPE_INCOMING = 1;
    public static final int HISTORY_MESSAGE_TYPE_OUTGOING = 2;

    public static final int HISTORY_MESSAGE_STATE_UNDETERMINED = 0;
    public static final int HISTORY_MESSAGE_STATE_ERROR = 1;
    public static final int HISTORY_MESSAGE_STATE_SENDING = 2;
    public static final int HISTORY_MESSAGE_STATE_SENT = 3;
    public static final int HISTORY_MESSAGE_STATE_DELIVERED = 4;

    public static final int HISTORY_CONTENT_TYPE_TEXT = 0;
    public static final int HISTORY_CONTENT_TYPE_PICTURE = 1;
    public static final int HISTORY_CONTENT_TYPE_VIDEO = 2;
    public static final int HISTORY_CONTENT_TYPE_FILE = 3;

    public static final int HISTORY_CONTENT_STATE_STABLE = 0;
    public static final int HISTORY_CONTENT_STATE_INTERRUPT = 1;
    public static final int HISTORY_CONTENT_STATE_STOPPED = 2;
    public static final int HISTORY_CONTENT_STATE_WAITING = 3;
    public static final int HISTORY_CONTENT_STATE_RUNNING = 4;
    public static final int HISTORY_CONTENT_STATE_FAILED = 5;

    // Database create scripts.
    protected static final String DB_CREATE_REQUEST_TABLE_SCRIPT = "create table " + REQUEST_TABLE + "("
            + ROW_AUTO_ID + " integer primary key autoincrement, " + REQUEST_TYPE + " int, "
            + REQUEST_CLASS + " text, " + REQUEST_SESSION + " text, "
            + REQUEST_PERSISTENT + " int, " + REQUEST_ACCOUNT_DB_ID + " int, "
            + REQUEST_STATE + " int, " + REQUEST_BUNDLE + " text, " + REQUEST_TAG + " text" + ");";

    protected static final String DB_CREATE_ACCOUNT_TABLE_SCRIPT = "create table " + ACCOUNTS_TABLE + "("
            + ROW_AUTO_ID + " integer primary key autoincrement, "
            + ACCOUNT_NAME + " text, " + ACCOUNT_TYPE + " text, "
            + ACCOUNT_USER_ID + " text, " + ACCOUNT_USER_PASSWORD + " text, "
            + ACCOUNT_STATUS + " int, " + ACCOUNT_STATUS_TITLE + " text, "
            + ACCOUNT_STATUS_MESSAGE + " text, " + ACCOUNT_CONNECTING + " int, "
            + ACCOUNT_BUNDLE + " text, " + ACCOUNT_AVATAR_HASH + " text" + ");";

    protected static final String DB_CREATE_GROUP_TABLE_SCRIPT = "create table " + ROSTER_GROUP_TABLE + "("
            + ROW_AUTO_ID + " integer primary key autoincrement, "
            + ROSTER_GROUP_ACCOUNT_DB_ID + " int, " + ROSTER_GROUP_NAME + " text, "
            + ROSTER_GROUP_ID + " text, " + ROSTER_GROUP_TYPE + " int, "
            + ROSTER_GROUP_UPDATE_TIME + " int" + ");";

    protected static final String DB_CREATE_BUDDY_TABLE_SCRIPT = "create table " + ROSTER_BUDDY_TABLE + "("
            + ROW_AUTO_ID + " integer primary key autoincrement, "
            + ROSTER_BUDDY_ACCOUNT_DB_ID + " int, " + ROSTER_BUDDY_ACCOUNT_TYPE + " int, "
            + ROSTER_BUDDY_ID + " text, " + ROSTER_BUDDY_NICK + " text, "
            + ROSTER_BUDDY_STATUS + " int, " + ROSTER_BUDDY_STATUS_TITLE + " text, "
            + ROSTER_BUDDY_STATUS_MESSAGE + " text, " + ROSTER_BUDDY_GROUP_ID + " int, "
            + ROSTER_BUDDY_GROUP + " text, " + ROSTER_BUDDY_DIALOG + " int, "
            + ROSTER_BUDDY_UPDATE_TIME + " int, " + ROSTER_BUDDY_ALPHABET_INDEX + " int, "
            + ROSTER_BUDDY_UNREAD_COUNT + " int default 0, " + ROSTER_BUDDY_AVATAR_HASH + " text, "
            + ROSTER_BUDDY_SEARCH_FIELD + " text, " + ROSTER_BUDDY_DRAFT + " text, "
            + ROSTER_BUDDY_LAST_SEEN + " int default -1, " + ROSTER_BUDDY_LAST_TYPING + " int default 0, "
            + ROSTER_BUDDY_OPERATION + " int default " + ROSTER_BUDDY_OPERATION_NO + ", "
            + ROSTER_BUDDY_LAST_MESSAGE_TIME + " int default 0" + ");";

    protected static final String DB_CREATE_HISTORY_TABLE_SCRIPT = "create table " + CHAT_HISTORY_TABLE + "("
            + ROW_AUTO_ID + " integer primary key autoincrement, "
            + HISTORY_BUDDY_ACCOUNT_DB_ID + " int, " + HISTORY_BUDDY_DB_ID + " int, "
            + HISTORY_MESSAGE_TYPE + " int, " + HISTORY_MESSAGE_COOKIE + " text, "
            + HISTORY_MESSAGE_STATE + " int, " + HISTORY_MESSAGE_TIME + " int, "
            + HISTORY_MESSAGE_READ + " int, " + HISTORY_NOTICE_SHOWN + " int, "
            + HISTORY_MESSAGE_TEXT + " text, " + HISTORY_SEARCH_FIELD + " text, "
            + HISTORY_CONTENT_TYPE + " int default " + HISTORY_CONTENT_TYPE_TEXT + ", "
            + HISTORY_CONTENT_SIZE + " bigint default 0, "
            + HISTORY_CONTENT_STATE + " int default " + HISTORY_CONTENT_STATE_STABLE + ", "
            + HISTORY_CONTENT_PROGRESS + " int default 0, "
            + HISTORY_CONTENT_URI + " text, " + HISTORY_CONTENT_NAME + " text, "
            + HISTORY_PREVIEW_HASH + " text, " + HISTORY_CONTENT_TAG + " text" + ");";

    protected static final String DB_CREATE_HISTORY_INDEX_BUDDY_SCRIPT = "CREATE INDEX Idx1 ON " +
            GlobalProvider.CHAT_HISTORY_TABLE + "(" +
            GlobalProvider.HISTORY_BUDDY_DB_ID + ");";

    protected static final String DB_CREATE_HISTORY_INDEX_MESSAGE_SCRIPT = "CREATE INDEX Idx2 ON " +
            GlobalProvider.CHAT_HISTORY_TABLE + "(" +
            GlobalProvider.HISTORY_BUDDY_DB_ID + "," +
            GlobalProvider.HISTORY_MESSAGE_READ + "," +
            GlobalProvider.HISTORY_MESSAGE_TYPE + ");";

    private static final StringBuilder ROSTER_BUDDY_UPDATE_UNREAD =
            new StringBuilder().append("UPDATE ").append(ROSTER_BUDDY_TABLE).append(" SET ")
                    .append(ROSTER_BUDDY_UNREAD_COUNT).append("=").append("(")
                    .append("SELECT COUNT(*) FROM ").append(CHAT_HISTORY_TABLE)
                    .append(" WHERE ")
                    .append(CHAT_HISTORY_TABLE).append(".").append(HISTORY_MESSAGE_READ).append("=").append("0").append(" AND ")
                    .append(CHAT_HISTORY_TABLE).append(".").append(HISTORY_MESSAGE_TYPE).append("=").append("1").append(" AND ")
                    .append(ROSTER_BUDDY_TABLE).append(".").append(ROSTER_BUDDY_DIALOG).append("=").append("1").append(" AND ")
                    .append(CHAT_HISTORY_TABLE).append(".").append(HISTORY_BUDDY_DB_ID)
                    .append("=")
                    .append(ROSTER_BUDDY_TABLE).append(".").append(ROW_AUTO_ID)
                    .append(");");

    private static final String TMP_C1 = "c1";
    private static final String TMP_C2 = "c2";
    private static final String TMP_R1 = "r1";

    private static final String HISTORY_GET_UNREAD_SB =
            new StringBuilder().append("SELECT").append(' ')
                    .append(HISTORY_MESSAGE_TEXT).append(',').append(HISTORY_BUDDY_DB_ID).append(',').append(HISTORY_CONTENT_TYPE).append(',').append(HISTORY_PREVIEW_HASH).append(',')
                    .append('(')
                    .append("SELECT").append(' ')
                    .append(ROSTER_BUDDY_NICK).append(' ')
                    .append("FROM").append(' ').append(ROSTER_BUDDY_TABLE).append(' ').append(TMP_R1).append(' ')
                    .append("WHERE").append(' ').append(TMP_C1).append('.').append(HISTORY_BUDDY_DB_ID).append('=').append(TMP_R1).append('.').append(ROW_AUTO_ID)
                    .append(')').append(' ').append("AS").append(' ').append(ROSTER_BUDDY_NICK).append(',')

                    .append('(')
                    .append("SELECT").append(' ')
                    .append(ROSTER_BUDDY_AVATAR_HASH).append(' ')
                    .append("FROM").append(' ').append(ROSTER_BUDDY_TABLE).append(' ').append(TMP_R1).append(' ')
                    .append("WHERE").append(' ').append(TMP_C1).append('.').append(HISTORY_BUDDY_DB_ID).append('=').append(TMP_R1).append('.').append(ROW_AUTO_ID)
                    .append(')').append(' ').append("AS").append(' ').append(ROSTER_BUDDY_AVATAR_HASH).append(',')

                    .append('(')
                    .append("SELECT").append(' ')
                    .append("COUNT(*)").append(' ')
                    .append("FROM").append(' ').append(CHAT_HISTORY_TABLE).append(' ').append(TMP_C2).append(' ')
                    .append("WHERE").append(' ').append(TMP_C2).append('.').append(HISTORY_BUDDY_DB_ID).append('=').append(TMP_C1).append('.').append(HISTORY_BUDDY_DB_ID)
                    .append(' ').append("AND").append(' ').append(HISTORY_MESSAGE_READ).append('=').append(0).append(' ').append("AND").append(' ').append(HISTORY_MESSAGE_TYPE).append('=').append(1)
                    .append(')').append(' ').append("AS").append(' ').append(ROSTER_BUDDY_UNREAD_COUNT).append(' ')

                    .append("FROM").append(' ').append(CHAT_HISTORY_TABLE).append(' ').append(TMP_C1).append(' ')
                    .append("WHERE").append(' ').append(HISTORY_MESSAGE_TYPE).append('=').append(1).append(' ')
                    .append("AND").append(' ').append(HISTORY_MESSAGE_READ).append('=').append(0).append(' ')
                    .append("GROUP BY").append(' ').append(HISTORY_BUDDY_DB_ID).append(' ')
                    .append("ORDER BY").append(' ').append(ROW_AUTO_ID).append(' ').append("ASC").append(';')
                    .toString();

    private static final String UNREAD_UNSHOWN_COUNT = "unread_unshown_count";
    private static final String SHOWN_COUNT = "shown_count";
    private static final String ON_SCREEN_COUNT = "on_screen_count";

    private static final String COUNT_QUERY = new StringBuilder()
            .append("SELECT").append(' ')
            .append('(')
            .append("SELECT").append(' ').append("COUNT(*)").append(' ').append("FROM").append(' ').append(CHAT_HISTORY_TABLE).append(' ')
            .append("WHERE").append(' ').append(HISTORY_MESSAGE_READ).append('=').append(0).append(' ')
            .append("AND").append(' ').append(HISTORY_NOTICE_SHOWN).append('=').append(0).append(' ')
            .append("AND").append(' ').append(HISTORY_MESSAGE_TYPE).append('=').append(1)
            .append(')').append(' ').append("AS").append(' ').append(UNREAD_UNSHOWN_COUNT).append(',')
            .append('(')
            .append("SELECT").append(' ').append("COUNT(*)").append(' ').append("FROM").append(' ').append(CHAT_HISTORY_TABLE).append(' ')
            .append("WHERE").append(' ').append(HISTORY_MESSAGE_READ).append('=').append(1).append(' ')
            .append("AND").append(' ').append(HISTORY_NOTICE_SHOWN).append('=').append(0).append(' ')
            .append("AND").append(' ').append(HISTORY_MESSAGE_TYPE).append('=').append(1)
            .append(')').append(' ').append("AS").append(' ').append(ON_SCREEN_COUNT).append(',')
            .append('(')
            .append("SELECT").append(' ').append("COUNT(*)").append(' ').append("FROM").append(' ').append(CHAT_HISTORY_TABLE).append(' ')
            .append("WHERE").append(' ').append(HISTORY_NOTICE_SHOWN).append('=').append(-1).append(' ')
            .append("AND").append(' ').append(HISTORY_MESSAGE_TYPE).append('=').append(1)
            .append(')').append(' ').append("AS").append(' ').append(SHOWN_COUNT).append(';')
            .toString();

    public static final int ROW_INVALID = -1;

    // Database helper object.
    private DatabaseHelper databaseHelper;
    private SQLiteDatabase sqLiteDatabase;

    // Methods.
    public static String METHOD_UPDATE_UNREAD = "update_unread";
    public static String METHOD_GET_UNREAD = "get_unread";
    public static String METHOD_GET_MESSAGES_COUNT = "get_messages_count";
    public static String METHOD_UPDATE_ROSTER = "update_roster";

    public static String KEY_NOTIFICATION_DATA = "key_notification_data";
    public static String KEY_UNSHOWN = "key_unshown";
    public static String KEY_JUST_SHOWN = "key_just_shown";
    public static String KEY_ON_SCREEN = "key_on_screen";
    public static String KEY_ACCOUNT_DB_ID = "key_account_db_id";
    public static String KEY_ACCOUNT_TYPE = "key_account_type";
    public static String KEY_GROUP_DATAS = "key_group_datas";

    // URI id.
    private static final int URI_REQUEST = 1;
    private static final int URI_ACCOUNT = 2;
    private static final int URI_BUDDY = 3;
    private static final int URI_GROUP = 4;
    private static final int URI_HISTORY = 5;
    private static final int URI_HISTORY_DISTINCT = 6;

    // URI tool instance.
    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(Settings.GLOBAL_AUTHORITY, REQUEST_TABLE, URI_REQUEST);
        uriMatcher.addURI(Settings.GLOBAL_AUTHORITY, ACCOUNTS_TABLE, URI_ACCOUNT);
        uriMatcher.addURI(Settings.GLOBAL_AUTHORITY, ROSTER_GROUP_TABLE, URI_GROUP);
        uriMatcher.addURI(Settings.GLOBAL_AUTHORITY, ROSTER_BUDDY_TABLE, URI_BUDDY);
        uriMatcher.addURI(Settings.GLOBAL_AUTHORITY, CHAT_HISTORY_TABLE, URI_HISTORY);
        uriMatcher.addURI(Settings.GLOBAL_AUTHORITY, CHAT_HISTORY_TABLE_DISTINCT, URI_HISTORY_DISTINCT);
    }

    @Override
    public boolean onCreate() {
        Logger.log("GlobalProvider onCreate");
        databaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String table;
        boolean isDistinct = false;
        // проверяем Uri
        switch (uriMatcher.match(uri)) {
            case URI_REQUEST: // Default Uri
                // Default sort if not specified
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = ROW_AUTO_ID + " ASC";
                }
                table = REQUEST_TABLE;
                break;
            case URI_ACCOUNT: // Default Uri
                // Default sort if not specified
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = ACCOUNT_NAME + " ASC";
                }
                table = ACCOUNTS_TABLE;
                break;
            case URI_GROUP: // Default Uri
                // Default sort if not specified
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = ROSTER_GROUP_NAME + " ASC";
                }
                table = ROSTER_GROUP_TABLE;
                break;
            case URI_BUDDY: // Default Uri
                // Default sort if not specified
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = ROSTER_BUDDY_ID + " ASC";
                }
                table = ROSTER_BUDDY_TABLE;
                break;
            case URI_HISTORY: // Default Uri
                // Default sort if not specified
                table = CHAT_HISTORY_TABLE;
                break;
            case URI_HISTORY_DISTINCT:
                table = CHAT_HISTORY_TABLE;
                isDistinct = true;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        sqLiteDatabase = databaseHelper.getWritableDatabase();
        Cursor cursor;
        if (isDistinct) {
            cursor = sqLiteDatabase.query(true, table, projection, selection, selectionArgs, null, null, sortOrder, null);
        } else {
            cursor = sqLiteDatabase.query(table, projection, selection, selectionArgs, null, null, sortOrder);
        }
        // Cursor cursor = sqLiteDatabase.query(distinct, table, projection, selection, selectionArgs, null, null, sortOrder, null);

        // Cursor cursor = sqLiteDatabase.query(true, ROSTER_GROUP_TABLE, new String[]{ROSTER_GROUP_NAME}, null, null, null, null, null, null);
        // Logger.log("Cursor items count: " + cursor.getCount());
        // просим ContentResolver уведомлять этот курсор
        // об изменениях данных в GROUP_RESOLVER_URI
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        Logger.log("getType, " + uri.toString());
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        sqLiteDatabase = databaseHelper.getWritableDatabase();
        long rowId = sqLiteDatabase.insert(getTableName(uri), null, values);
        Uri resultUri = ContentUris.withAppendedId(uri, rowId);
        // Notify ContentResolver about data changes.
        getContext().getContentResolver().notifyChange(resultUri, null);
        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        sqLiteDatabase = databaseHelper.getWritableDatabase();
        int rows = sqLiteDatabase.delete(getTableName(uri), selection, selectionArgs);
        // Notify ContentResolver about data changes.
        if (rows > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        sqLiteDatabase = databaseHelper.getWritableDatabase();
        int rows = sqLiteDatabase.update(getTableName(uri), values, selection, selectionArgs);
        // Notify ContentResolver about data changes.
        if (rows > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rows;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Bundle call(String method, String arg, Bundle extras) {
        if (method.equals(METHOD_UPDATE_UNREAD)) {
            long time = System.currentTimeMillis();
            sqLiteDatabase.beginTransaction();
            try {
                sqLiteDatabase.execSQL(ROSTER_BUDDY_UPDATE_UNREAD.toString());
                sqLiteDatabase.setTransactionSuccessful();
            } finally {
                sqLiteDatabase.endTransaction();
            }
            Logger.log("Update unread time: " + (System.currentTimeMillis() - time));
            getContext().getContentResolver().notifyChange(Settings.BUDDY_RESOLVER_URI, null);
        } else if (method.equals(METHOD_GET_UNREAD)) {
            long time = System.currentTimeMillis();
            Cursor cursor = sqLiteDatabase.rawQuery(HISTORY_GET_UNREAD_SB, null);
            Bundle bundle = new Bundle();
            if (cursor.moveToFirst()) {
                int messageTextColumn = cursor.getColumnIndex(HISTORY_MESSAGE_TEXT);
                int buddyDbIdColumn = cursor.getColumnIndex(HISTORY_BUDDY_DB_ID);
                int buddyNickColumn = cursor.getColumnIndex(ROSTER_BUDDY_NICK);
                int buddyAvatarHashColumn = cursor.getColumnIndex(ROSTER_BUDDY_AVATAR_HASH);
                int unreadCountColumn = cursor.getColumnIndex(ROSTER_BUDDY_UNREAD_COUNT);
                int contentTypeColumn = cursor.getColumnIndex(HISTORY_CONTENT_TYPE);
                int previewHashColumn = cursor.getColumnIndex(HISTORY_PREVIEW_HASH);
                ArrayList<NotificationData> data = new ArrayList<>();
                do {
                    NotificationData row = new NotificationData(
                            cursor.getString(messageTextColumn),
                            cursor.getInt(buddyDbIdColumn),
                            cursor.getString(buddyNickColumn),
                            cursor.getString(buddyAvatarHashColumn),
                            cursor.getInt(unreadCountColumn),
                            cursor.getInt(contentTypeColumn),
                            cursor.getString(previewHashColumn));
                    data.add(row);
                } while (cursor.moveToNext());
                bundle.putSerializable(KEY_NOTIFICATION_DATA, data);
            }
            cursor.close();
            Logger.log("Get unread time: " + (System.currentTimeMillis() - time));
            return bundle;
        } else if (method.equals(METHOD_GET_MESSAGES_COUNT)) {
            Cursor cursor = sqLiteDatabase.rawQuery(COUNT_QUERY, null);
            Bundle bundle = new Bundle();
            int unshown = 0, justShown = 0, onScreen = 0;
            if (cursor.moveToFirst()) {
                int unreadUnshownColumn = cursor.getColumnIndex(UNREAD_UNSHOWN_COUNT);
                int shownColumn = cursor.getColumnIndex(SHOWN_COUNT);
                int onScreenColumn = cursor.getColumnIndex(ON_SCREEN_COUNT);
                unshown = cursor.getInt(unreadUnshownColumn);
                justShown = cursor.getInt(shownColumn);
                onScreen = cursor.getInt(onScreenColumn);
            }
            cursor.close();
            bundle.putInt(KEY_UNSHOWN, unshown);
            bundle.putInt(KEY_JUST_SHOWN, justShown);
            bundle.putInt(KEY_ON_SCREEN, onScreen);
            return bundle;
        } else if (method.equals(METHOD_UPDATE_ROSTER)) {
            long updateTime = System.currentTimeMillis();
            int accountDbId = extras.getInt(KEY_ACCOUNT_DB_ID);
            String accountType = extras.getString(KEY_ACCOUNT_TYPE);
            ArrayList<GroupData> groupDatas = (ArrayList<GroupData>) extras.getSerializable(KEY_GROUP_DATAS);
            int buddiesCount = 0;
            sqLiteDatabase.beginTransaction();
            try {
                for (GroupData groupData : groupDatas) {
                    QueryHelper.updateOrCreateGroup(sqLiteDatabase, accountDbId, updateTime,
                            groupData.getGroupName(), groupData.getGroupId());
                    for (BuddyData buddyData : groupData.getBuddyDatas()) {
                        buddiesCount++;
                        QueryHelper.updateOrCreateBuddy(sqLiteDatabase, accountDbId, accountType, updateTime,
                                buddyData.getGroupId(), buddyData.getGroupName(), buddyData.getBuddyId(),
                                buddyData.getBuddyNick(), buddyData.getStatusIndex(), buddyData.getStatusTitle(),
                                buddyData.getStatusMessage(), buddyData.getBuddyIcon(), buddyData.getLastSeen());
                    }
                }
                QueryHelper.removeOutdatedBuddies(sqLiteDatabase, accountDbId, updateTime);
                sqLiteDatabase.setTransactionSuccessful();
            } finally {
                sqLiteDatabase.endTransaction();
            }
            long updateDelay = System.currentTimeMillis() - updateTime;
            // Show some tasty info :)
            Logger.log("roster processing " + buddiesCount + " buddies/" + updateDelay + " msec " +
                    "(speed: " + (buddiesCount * 1000 / updateDelay) + " buddies/sec)");
            // Notify interested observers.
            getContext().getContentResolver().notifyChange(Settings.GROUP_RESOLVER_URI, null);
            getContext().getContentResolver().notifyChange(Settings.BUDDY_RESOLVER_URI, null);
        }
        return null;
    }

    private static String getTableName(Uri uri) {
        String table;
        switch (uriMatcher.match(uri)) {
            case URI_REQUEST:
                table = REQUEST_TABLE;
                break;
            case URI_ACCOUNT:
                table = ACCOUNTS_TABLE;
                break;
            case URI_GROUP:
                table = ROSTER_GROUP_TABLE;
                break;
            case URI_BUDDY:
                table = ROSTER_BUDDY_TABLE;
                break;
            case URI_HISTORY:
                table = CHAT_HISTORY_TABLE;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        return table;
    }
}
