package com.example.chatapplication.login;

// DB에 등록된 사용자 상세 정보 클래스
public class UserAccount {
    private String uid;
    private String email;
    private String password;
    private String name;
    private String auth_answer;

    public String getAuth_answer() {return auth_answer;}

    public void setAuth_answer(String auth_answer) {this.auth_answer = auth_answer;}

    public String getUid() {return uid;}

    public void setUid(String uid) {this.uid = uid;}

    public String getEmail() {return email;}

    public void setEmail(String email) {this.email = email;}

    public String getPassword() {return password;}

    public void setPassword(String password) {this.password = password;}

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}
}
