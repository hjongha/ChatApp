package com.example.chatapplication.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapplication.MainActivity;
import com.example.chatapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mFirebaseAuth;         // 파이어베이스 인증
    private FirebaseDatabase database;
    private DatabaseReference mDatabaseRef;     // 실시간 데이터베이스
    private EditText mEtId, mEtPwd;          // 로그인 입력필드
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mFirebaseAuth = FirebaseAuth.getInstance();

        mEtId = (EditText) findViewById(R.id.et_id);
        mEtPwd = (EditText) findViewById(R.id.et_pwd);

        database = FirebaseDatabase.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("member").child("UserAccount");

        // 로그인 버튼
        Button btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strId = mEtId.getText().toString();
                String strPwd = mEtPwd.getText().toString();

                // 회원 확인
                mFirebaseAuth.signInWithEmailAndPassword(strId, strPwd).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 로그인 성공
                            intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        // 회원 가입 버튼
        Button btn_join = findViewById(R.id.btn_join);
        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 회원가입 화면으로 이동
                Intent intent = new Intent(LoginActivity.this, JoinActivity.class);
                startActivity(intent);
            }
        });

        // 아이디 찾기 버튼
        Button btn_search = findViewById(R.id.btn_search);
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 아이디 찾기 화면으로 이동
                Intent intent = new Intent(LoginActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });
    }
}
