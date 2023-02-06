package com.example.chatapplication.main.fragments;

import android.graphics.Bitmap;

// 채팅방 정보(사용자 정보, 마지막 메시지(내용, 시간)) 클래스
public class ChatRoomData {
    private Bitmap fnd_image;
    private String fnd_name;
    private String last_msg;
    private String last_time;

    public String getLast_msg() {return last_msg;}

    public void setLast_msg(String last_msg) {this.last_msg = last_msg;}

    public String getLast_time() {return last_time;}

    public void setLast_time(String last_time) {this.last_time = last_time;}

    public Bitmap getFnd_image() {return fnd_image;}

    public void setFnd_image(Bitmap fnd_image) {this.fnd_image = fnd_image;}

    public String getFnd_name() {return fnd_name;}

    public void setFnd_name(String fnd_name) {this.fnd_name = fnd_name;}
}