package com.example.chatapplication.main;

import android.graphics.Bitmap;

// 사용자 정보(이미지+이름) 클래스
public class FndInfo {
    private Bitmap fnd_image;
    private String fnd_name;

    public Bitmap getFnd_image() {return fnd_image;}

    public void setFnd_image(Bitmap fnd_image) {this.fnd_image = fnd_image;}

    public String getFnd_name() {return fnd_name;}

    public void setFnd_name(String fnd_name) {this.fnd_name = fnd_name;}
}
