package com.tomclaw.mandarin.core;

import android.util.Log;
import com.tomclaw.mandarin.R;
import com.tomclaw.mandarin.im.AccountRoot;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: solkin
 * Date: 3/28/13
 * Time: 2:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class SessionHolder {

    private List<AccountRoot> accountRootList = new ArrayList<AccountRoot>();

    public void load() {
        // Loading accounts from local storage.
        for(int c=0;c<5;c++){
            AccountRoot accountRoot = new AccountRoot() {
                @Override
                public int getServiceIcon() {
                    return R.drawable.ic_launcher;
                }
            };
            accountRoot.setUserId("7068514");
            accountRoot.setUserNick("Solkin");
            accountRoot.setUserPassword("112");
            accountRootList.add(accountRoot);
        }
        Log.d("MandarinLog", "loaded " + accountRootList.size() + " accounts");
    }

    public void save() {
        // Saving account to local storage.
    }

    public List<AccountRoot> getAccountsList() {
        return accountRootList;
    }
}
