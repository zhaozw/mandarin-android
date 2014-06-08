package com.tomclaw.mandarin.main.adapters;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.tomclaw.mandarin.R;
import com.tomclaw.mandarin.core.*;
import com.tomclaw.mandarin.im.StatusUtil;
import com.tomclaw.mandarin.main.BuddyInfoTask;
import com.tomclaw.mandarin.util.QueryBuilder;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created with IntelliJ IDEA.
 * User: Solkin
 * Date: 13.10.13
 * Time: 17:44
 */
public class RosterAlphabetAdapter extends CursorAdapter
        implements LoaderManager.LoaderCallbacks<Cursor>, StickyListHeadersAdapter {

    /**
     * Adapter ID
     */
    private static final int ADAPTER_ALPHABET_ID = -1;

    /**
     * Filter
     */
    public static final int FILTER_ALL_BUDDIES = 0x00;
    public static final int FILTER_ONLINE_ONLY = 0x01;

    /**
     * Columns
     */
    private static int COLUMN_ROW_AUTO_ID;
    private static int COLUMN_ROSTER_BUDDY_ID;
    private static int COLUMN_ROSTER_BUDDY_NICK;
    private static int COLUMN_ROSTER_BUDDY_STATUS;
    private static int COLUMN_ROSTER_BUDDY_STATUS_TITLE;
    private static int COLUMN_ROSTER_BUDDY_STATUS_MESSAGE;
    private static int COLUMN_ROSTER_BUDDY_ACCOUNT_TYPE;
    private static int COLUMN_ROSTER_BUDDY_ALPHABET_INDEX;
    private static int COLUMN_ROSTER_BUDDY_UNREAD_COUNT;
    private static int COLUMN_ROSTER_BUDDY_AVATAR_HASH;
    private static int COLUMN_ROSTER_BUDDY_DRAFT;

    /**
     * Variables
     */
    private Context context;
    private LayoutInflater inflater;
    private int filter;
    private boolean isShowTemp;
    private LoaderManager loaderManager;

    public RosterAlphabetAdapter(Activity context, LoaderManager loaderManager, int filter) {
        super(context, null, 0x00);
        this.context = context;
        this.inflater = context.getLayoutInflater();
        this.loaderManager = loaderManager;
        this.filter = filter;
        this.isShowTemp = PreferenceHelper.isShowTemp(context);
        initLoader();
        setFilterQueryProvider(new RosterFilterQueryProvider());
    }

    public void initLoader() {
        // Initialize loader for dialogs Id.
        loaderManager.restartLoader(ADAPTER_ALPHABET_ID, null, this);
    }

    /**
     * @see android.widget.ListAdapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Cursor cursor = getCursor();
        View view;
        try {
            if (cursor == null || !cursor.moveToPosition(position)) {
                throw new IllegalStateException("couldn't move cursor to position " + position);
            }
            if (convertView == null) {
                view = newView(context, cursor, parent);
            } else {
                view = convertView;
            }
            bindView(view, context, cursor);
        } catch (Throwable ex) {
            view = inflater.inflate(R.layout.buddy_item, parent, false);
            Log.d(Settings.LOG_TAG, "exception in getView: " + ex.getMessage());
        }
        return view;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.buddy_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Status image.
        String accountType = cursor.getString(COLUMN_ROSTER_BUDDY_ACCOUNT_TYPE);
        int statusIndex = cursor.getInt(COLUMN_ROSTER_BUDDY_STATUS);
        int statusImageResource = StatusUtil.getStatusDrawable(accountType, statusIndex);
        // Status text.
        String statusTitle = cursor.getString(COLUMN_ROSTER_BUDDY_STATUS_TITLE);
        String statusMessage = cursor.getString(COLUMN_ROSTER_BUDDY_STATUS_MESSAGE);
        if (statusIndex == StatusUtil.STATUS_OFFLINE
                || TextUtils.equals(statusTitle, statusMessage)) {
            // Buddy status is offline now or status message is only status title.
            // No status message could be displayed.
            statusMessage = "";
        }
        SpannableString statusString = new SpannableString(statusTitle + " " + statusMessage);
        statusString.setSpan(new StyleSpan(Typeface.BOLD), 0, statusTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Unread count.
        int unreadCount = cursor.getInt(COLUMN_ROSTER_BUDDY_UNREAD_COUNT);
        // Applying values.
        ((TextView) view.findViewById(R.id.buddy_nick)).setText(cursor.getString(COLUMN_ROSTER_BUDDY_NICK));
        ((ImageView) view.findViewById(R.id.buddy_status)).setImageResource(statusImageResource);
        ((TextView) view.findViewById(R.id.buddy_status_message)).setText(statusString);
        if (unreadCount > 0) {
            view.findViewById(R.id.counter_layout).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.counter_text)).setText(String.valueOf(unreadCount));
        } else {
            view.findViewById(R.id.counter_layout).setVisibility(View.GONE);
        }
        // Draft message.
        String buddyDraft = cursor.getString(COLUMN_ROSTER_BUDDY_DRAFT);
        view.findViewById(R.id.draft_indicator).setVisibility(
                TextUtils.isEmpty(buddyDraft) ? View.GONE : View.VISIBLE);
        // Avatar.
        final String avatarHash = cursor.getString(COLUMN_ROSTER_BUDDY_AVATAR_HASH);
        QuickContactBadge contactBadge = ((QuickContactBadge) view.findViewById(R.id.buddy_badge));
        BitmapCache.getInstance().getBitmapAsync(contactBadge, avatarHash, R.drawable.ic_default_avatar);
        // On-avatar click listener.
        final int buddyDbId = cursor.getInt(COLUMN_ROW_AUTO_ID);
        final BuddyInfoTask buddyInfoTask = new BuddyInfoTask(context, buddyDbId);
        contactBadge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskExecutor.getInstance().execute(buddyInfoTask);
            }
        });
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        Cursor cursor = getCursor();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.alphabet_header, parent, false);
        }
        if (cursor == null || !cursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        ((TextView) convertView.findViewById(R.id.header_text)).setText(String.valueOf(
                Character.toUpperCase((char) cursor.getInt(COLUMN_ROSTER_BUDDY_ALPHABET_INDEX))));
        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        Cursor cursor = getCursor();
        if (cursor == null || !cursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        return cursor.getInt(COLUMN_ROSTER_BUDDY_ALPHABET_INDEX);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return getDefaultQueryBuilder().createCursorLoader(context, Settings.BUDDY_RESOLVER_URI);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Detecting columns.
        COLUMN_ROW_AUTO_ID = cursor.getColumnIndex(GlobalProvider.ROW_AUTO_ID);
        COLUMN_ROSTER_BUDDY_ID = cursor.getColumnIndex(GlobalProvider.ROSTER_BUDDY_ID);
        COLUMN_ROSTER_BUDDY_NICK = cursor.getColumnIndex(GlobalProvider.ROSTER_BUDDY_NICK);
        COLUMN_ROSTER_BUDDY_STATUS = cursor.getColumnIndex(GlobalProvider.ROSTER_BUDDY_STATUS);
        COLUMN_ROSTER_BUDDY_STATUS_TITLE = cursor.getColumnIndex(GlobalProvider.ROSTER_BUDDY_STATUS_TITLE);
        COLUMN_ROSTER_BUDDY_STATUS_MESSAGE = cursor.getColumnIndex(GlobalProvider.ROSTER_BUDDY_STATUS_MESSAGE);
        COLUMN_ROSTER_BUDDY_ACCOUNT_TYPE = cursor.getColumnIndex(GlobalProvider.ROSTER_BUDDY_ACCOUNT_TYPE);
        COLUMN_ROSTER_BUDDY_ALPHABET_INDEX = cursor.getColumnIndex(GlobalProvider.ROSTER_BUDDY_ALPHABET_INDEX);
        COLUMN_ROSTER_BUDDY_UNREAD_COUNT = cursor.getColumnIndex(GlobalProvider.ROSTER_BUDDY_UNREAD_COUNT);
        COLUMN_ROSTER_BUDDY_AVATAR_HASH = cursor.getColumnIndex(GlobalProvider.ROSTER_BUDDY_AVATAR_HASH);
        COLUMN_ROSTER_BUDDY_DRAFT = cursor.getColumnIndex(GlobalProvider.ROSTER_BUDDY_DRAFT);
        swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Cursor cursor = swapCursor(null);
        // Maybe, previous non-closed cursor present?
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    public int getBuddyDbId(int position) {
        Cursor cursor = getCursor();
        if (cursor == null || !cursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        return cursor.getInt(COLUMN_ROW_AUTO_ID);
    }

    public void setRosterFilter(int filter) {
        this.filter = filter;
    }

    public int getRosterFilter() {
        return filter;
    }

    private QueryBuilder getDefaultQueryBuilder() {
        QueryBuilder queryBuilder = new QueryBuilder();
        switch (filter) {
            case FILTER_ONLINE_ONLY: {
                queryBuilder.columnNotEquals(GlobalProvider.ROSTER_BUDDY_STATUS, StatusUtil.STATUS_OFFLINE);
                break;
            }
            case FILTER_ALL_BUDDIES:
            default:
        }
        if (!isShowTemp) {
            queryBuilder.and().columnNotEquals(GlobalProvider.ROSTER_BUDDY_GROUP_ID, GlobalProvider.GROUP_ID_RECYCLE);
        }
        queryBuilder.and().columnNotEquals(GlobalProvider.ROSTER_BUDDY_OPERATION,
                GlobalProvider.ROSTER_BUDDY_OPERATION_REMOVE);
        queryBuilder.ascending(GlobalProvider.ROSTER_BUDDY_ALPHABET_INDEX);
        return queryBuilder;
    }

    private class RosterFilterQueryProvider implements FilterQueryProvider {

        @Override
        public Cursor runQuery(CharSequence constraint) {
            String searchField = constraint.toString().toUpperCase();
            QueryBuilder queryBuilder = getDefaultQueryBuilder();
            queryBuilder.and().startComplexExpression().like(GlobalProvider.ROSTER_BUDDY_SEARCH_FIELD, searchField)
                    .or().like(GlobalProvider.ROSTER_BUDDY_ID, constraint).finishComplexExpression();
            return queryBuilder.query(context.getContentResolver(), Settings.BUDDY_RESOLVER_URI);
        }
    }
}
