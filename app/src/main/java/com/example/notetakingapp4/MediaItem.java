package com.example.notetakingapp4;

import android.net.Uri;

public class MediaItem {

    public final static int IMAGE = 0;
    public final static int AUDIO = 1;

    private Uri uri;
    private int type;

    public boolean isDownloaded;

    public MediaItem(Uri uri, int type, boolean isDownloaded) {
        this.uri = uri;
        this.type = type;
        this.isDownloaded = isDownloaded;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}