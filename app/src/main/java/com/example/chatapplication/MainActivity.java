package com.example.chatapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

// 로그인 후 실행되는 메인 액티비티
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 친구 목록 페이지로 넘어가는 버튼
        Button fnd_btn = (Button) findViewById(R.id.btn_fnd);
        fnd_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FndListActivity.class);
                startActivity(intent);
            }
        });

        // 채팅 목록 페이지로 넘어가는 버튼
        Button chat_btn = (Button) findViewById(R.id.btn_chat);
        chat_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChatListActivity.class);
                startActivity(intent);
            }
        });
    }
}