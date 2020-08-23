package com.nnoboa.duchesschat.model;

public class Messages {

    private String mText;

    private String mUsername;

    private String mFileUrl;

    private long mTimeStamp;

    private String mUID;

    public Messages(){}

    public Messages (String text, String username, String uid, String fileUrl, long timeStamp){
        mText = text;
        mUID = uid;
        mUsername = username;
        mFileUrl = fileUrl;
        mTimeStamp = timeStamp;
    }

    public String getmText() {
        return mText;
    }

    public void setmTimeStamp(long mTimeStamp) {
        this.mTimeStamp = mTimeStamp;
    }

    public String getmUID() {
        return mUID;
    }

    public void setmUID(String mUID) {
        this.mUID = mUID;
    }

    public long getmTimeStamp() {
        return mTimeStamp;
    }

    public String getmFileUrl() {
        return mFileUrl;
    }

    public String getmUsername() {
        return mUsername;
    }

    public void setmText(String mText) {
        this.mText = mText;
    }

    public void setmFileUrl(String mFileUrl) {
        this.mFileUrl = mFileUrl;
    }

    public void setmUsername(String mUsername) {
        this.mUsername = mUsername;
    }

}
