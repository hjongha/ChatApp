package com.example.chatapplication.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapplication.R;
import com.example.chatapplication.main.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Map;
import java.util.Set;

public class Intro_Activity extends AppCompatActivity {
    SharedPreferences auto;
    Intent intent;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

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

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (auto.getString("auto_Id", "").equals("") || auto.getString("auto_Pwd", "").equals("")) {
                    intent = new Intent(Intro_Activity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    String autoId = auto.getString("auto_Id", "");
                    String autoPwd = auto.getString("auto_Pwd", "");
                    System.out.println("autoId : " + autoId);
                    System.out.println("autoPwd : " + autoPwd);
                    mFirebaseAuth.signInWithEmailAndPassword(autoId, autoPwd).addOnCompleteListener(Intro_Activity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // 자동 로그인 성공
                                intent = new Intent(Intro_Activity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else {
                                intent = new Intent(Intro_Activity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                                Toast.makeText(Intro_Activity.this, "자동 로그인 실패", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        }, 2000);
    }
}
