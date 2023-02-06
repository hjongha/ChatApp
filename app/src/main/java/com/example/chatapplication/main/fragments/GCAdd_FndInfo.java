package com.example.chatapplication.main.fragments;

import android.graphics.Bitmap;

public class GCAdd_FndInfo {
    private Bitmap fnd_image;
    private String fnd_name;
    private String fnd_uid;
    private Boolean check_bool;

    public String getFnd_uid() {return fnd_uid;}

    public void setFnd_uid(String fnd_uid) {this.fnd_uid = fnd_uid;}

    public Boolean getCheck_bool() {return check_bool;}

    public void setCheck_bool(Boolean check_bool) {this.check_bool = check_bool;}

    public Bitmap getFnd_image() {return fnd_image;}

    public void setFnd_image(Bitmap fnd_image) {this.fnd_image = fnd_image;}

    public String getFnd_name() {return fnd_name;}

    public void setFnd_name(String fnd_name) {this.fnd_name = fnd_name;}
}
