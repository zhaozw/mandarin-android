package com.tomclaw.mandarin.im.icq;

import android.content.Intent;

import com.tomclaw.mandarin.core.BitmapRequest;
import com.tomclaw.mandarin.core.CoreService;
import com.tomclaw.mandarin.core.QueryHelper;

/**
 * Created by Solkin on 25.07.2014.
 */
public class LargeAvatarRequest extends BitmapRequest<IcqAccountRoot> {

    private String buddyId;

    public LargeAvatarRequest() {
    }

    public LargeAvatarRequest(String buddyId, String url) {
        super(url);
        this.buddyId = buddyId;
    }

    @Override
    protected void onBitmapSaved(String hash) {
        QueryHelper.updateBuddyOrAccountAvatar(getAccountRoot(), buddyId, hash);
        Intent intent = new Intent(CoreService.ACTION_CORE_SERVICE);
        intent.putExtra(CoreService.EXTRA_STAFF_PARAM, false);
        intent.putExtra(BuddyInfoRequest.ACCOUNT_DB_ID, getAccountRoot().getAccountDbId());
        intent.putExtra(BuddyInfoRequest.BUDDY_ID, buddyId);
        intent.putExtra(BuddyInfoRequest.BUDDY_AVATAR_HASH, hash);
        getService().sendBroadcast(intent);
    }
}
