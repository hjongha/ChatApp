package com.example.chatapplication.login;

// DB에 등록된 사용자 상세 정보 클래스
public class UserAccount {
    private String Uid;
    private String email;
    private String password;
    private String name;

    public String getUid() {return Uid;}

    public void setUid(String uid) {Uid = uid;}

    public String getEmail() {return email;}

    public void setEmail(String email) {this.email = email;}

    public String getPassword() {return password;}

    public void setPassword(String password) {this.password = password;}

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}
}
