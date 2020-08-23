package com.nnoboa.duchesschat.model;

import android.net.Uri;

public class Users {
    private String mUserDisplayName;

    private String mUserDisplayPhotoUrl;

    private String mUID;

    public Users(String displayName, Uri displayPhotoUrl, String uid){
        mUserDisplayName = displayName;
        mUserDisplayPhotoUrl = String.valueOf(displayPhotoUrl);
        mUID = uid;
    }

    public Users(){

    }

    public String getmUID() {
        return mUID;
    }

    public String getmUserDisplayName() {
        return mUserDisplayName;
    }

    public String getmUserDisplayPhotoUrl() {
        return mUserDisplayPhotoUrl;
    }
}
