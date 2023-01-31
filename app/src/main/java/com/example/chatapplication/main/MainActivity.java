package com.example.chatapplication.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.chatapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

// 로그인 후 실행되는 메인 액티비티
public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private Fragment_FndList frag_fndList = new Fragment_FndList();
    private Fragment_ChatList frag_chatList = new Fragment_ChatList();
    private Fragment_Settings frag_settings = new Fragment_Settings();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 초기 화면 frag_fndList
        setFrag(0);

        // bottomNavi 클릭 시 화면 전환
        bottomNavigationView = findViewById(R.id.bottom_Navi);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_person:
                        setFrag(0);
                        break;
                    case R.id.menu_chat:
                        setFrag(1);
                        break;
                    case R.id.menu_settings:
                        setFrag(2);
                        break;
                }
                return false;
            }
        });
    }

    // 화면 전환 수행
    public void setFrag(int n) {
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        switch(n) {
            case 0:
                fragmentTransaction.replace(R.id.main_frame, frag_fndList);
                fragmentTransaction.commit();
                System.out.println("setFrag 0");
                break;
            case 1:
                fragmentTransaction.replace(R.id.main_frame, frag_chatList);
                fragmentTransaction.commit();
                System.out.println("setFrag 1");
                break;
            case 2:
                fragmentTransaction.replace(R.id.main_frame, frag_settings);
                fragmentTransaction.commit();
                System.out.println("setFrag 2");
                break;
        }
    }
}