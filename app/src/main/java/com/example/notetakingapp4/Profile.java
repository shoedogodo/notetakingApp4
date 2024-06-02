package com.example.notetakingapp4;

import android.media.Image;

import com.google.firebase.Timestamp;

public class Profile {
    Image profile_pic;
    String username;
    String message;

    public Profile() {
    }

    public Image getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(Image profile_pic) {
        this.profile_pic = profile_pic;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
