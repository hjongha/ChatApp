package com.example.chatapplication.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapplication.main.MainActivity;
import com.example.chatapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Map;
import java.util.Set;

// 로그인 액티비티
public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mFirebaseAuth;         // 파이어베이스 인증
    private EditText mEtId, mEtPwd;          // 로그인 입력필드
    private Intent intent;
    SharedPreferences auto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auto = new SharedPreferences() {        // SharedPreferences 객체 생성
            @Override
            public Map<String, ?> getAll() {return null;}

            @Nullable
            @Override
            public String getString(String key, @Nullable String defValue) {return null;}

            @Nullable
            @Override
            public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {return null;}

            @Override
            public int getInt(String key, int defValue) {return 0;}

            @Override
            public long getLong(String key, long defValue) {return 0;}

            @Override
            public float getFloat(String key, float defValue) {return 0;}

            @Override
            public boolean getBoolean(String key, boolean defValue) {return false;}

            @Override
            public boolean contains(String key) {return false;}

            @Override
            public Editor edit() {return null;}

            @Override
            public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {}

            @Override
            public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {}
        };
        auto = getSharedPreferences("autoLogin", MODE_PRIVATE);

        mFirebaseAuth = FirebaseAuth.getInstance();

        mEtId = (EditText) findViewById(R.id.et_id);
        mEtPwd = (EditText) findViewById(R.id.et_pwd);

        // 로그인 버튼
        Button btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strId = mEtId.getText().toString();
                String strPwd = mEtPwd.getText().toString();

                if (strId.isEmpty() || strPwd.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
                else {
                    // 회원 확인
                    mFirebaseAuth.signInWithEmailAndPassword(strId, strPwd).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // 로그인 성공
                                // 자동 로그인 변수, 값 저장
                                SharedPreferences.Editor autoLogin = auto.edit();
                                autoLogin.putString("auto_Id", strId);
                                autoLogin.putString("auto_Pwd", strPwd);
                                autoLogin.commit();

                                intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else {
                                Toast.makeText(LoginActivity.this, "이메일 혹은 비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
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
