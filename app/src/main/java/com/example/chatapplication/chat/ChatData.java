package com.example.chatapplication.chat;

// 채팅 정보 클래스
public class ChatData {
    private String uid;
    private String msg;
    private String timer;

    public String getTimer() {return timer;}

    public void setTimer(String timer) {this.timer = timer;}

    public String getUid() {return uid;}

    public void setUid(String uid) {this.uid = uid;}

    public String getMsg() {return msg;}

    public void setMsg(String msg) {this.msg = msg;}
}
