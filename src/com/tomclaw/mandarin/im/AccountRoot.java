package com.tomclaw.mandarin.im;

import android.os.Parcel;
import com.tomclaw.mandarin.core.CoreObject;

/**
 * Created with IntelliJ IDEA.
 * User: solkin
 * Date: 3/28/13
 * Time: 1:54 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AccountRoot extends CoreObject {

    /** Constants **/
    public static final int ACCOUNT_TYPE_ICQ = 0x01;
    /** Account info **/
    protected int accountDbId; // TODO: main account identification.
    /** User info **/
    protected String userId;
    protected String userNick;
    protected String userPassword;
    protected int statusIndex;
    protected String statusText;
    /** Service info **/
    protected String serviceHost;
    protected int servicePort;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserNick() {
        return userNick;
    }

    public void setUserNick(String userNick) {
        this.userNick = userNick;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public int getStatusIndex() {
        return statusIndex;
    }

    public abstract void connect();

    public abstract void disconnect();

    public abstract int getAccountType();

    public abstract int getAccountLayout();

    public void writeInstanceData(Parcel dest) {
        dest.writeString(userId);
        dest.writeString(userNick);
        dest.writeString(userPassword);
        dest.writeInt(statusIndex);
        dest.writeString(statusText);
        dest.writeString(serviceHost);
        dest.writeInt(servicePort);
    }

    public void readInstanceData(Parcel in) {
        userId = in.readString();
        userNick = in.readString();
        userPassword = in.readString();
        statusIndex = in.readInt();
        statusText = in.readString();
        serviceHost = in.readString();
        servicePort = in.readInt();
    }
}
