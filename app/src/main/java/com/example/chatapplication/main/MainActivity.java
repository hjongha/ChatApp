package com.example.chatapplication.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.chatapplication.R;
import com.example.chatapplication.alarm.Alarm_Service;
import com.example.chatapplication.main.fragments.Fragment_ChatList;
import com.example.chatapplication.main.fragments.Fragment_FndList;
import com.example.chatapplication.main.fragments.Fragment_GroupChat;
import com.example.chatapplication.main.fragments.Fragment_Settings;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

// 로그인 후 실행되는 메인 액티비티
public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private Fragment_FndList frag_fndList = new Fragment_FndList();
    private Fragment_ChatList frag_chatList = new Fragment_ChatList();
    private Fragment_GroupChat frag_groupChat = new Fragment_GroupChat();
    private Fragment_Settings frag_settings = new Fragment_Settings();

    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;

    private PendingIntent pendingIntent;
    Intent my_intent;
    private AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

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
                    case R.id.menu_groupchat:
                        setFrag(2);
                        break;
                    case R.id.menu_settings:
                        setFrag(3);
                        break;
                }
                return false;
            }
        });

        my_intent = new Intent(MainActivity.this, Alarm_Service.class);
        my_intent.putExtra("myUid", firebaseUser.getUid());

        if (Build.VERSION.SDK_INT >= 26) {
            pendingIntent = (PendingIntent.getActivity(MainActivity.this, 0, my_intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        } else {
            pendingIntent = (PendingIntent.getActivity(MainActivity.this, 0, my_intent, PendingIntent.FLAG_UPDATE_CURRENT));
        }
        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, 0, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, 0, pendingIntent);
        }

        // 기존 서비스 실행 중인지 검사
        Boolean serviceStart = true;
        ActivityManager activityManager = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.example.chatapplication.alarm.Alarm_Service".equals(serviceInfo.service.getClassName())) {
                System.out.println("서비스가 이미 실행 중..");
                serviceStart = false;
            }
        }
        // 이미 실행 중인 서비스가 없다면
        if (serviceStart) {
            startService(my_intent);
            System.out.println("서비스 시작");
        }
    }

    // 화면 전환 수행
    public void setFrag(int n) {
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        switch(n) {
            case 0:
                fragmentTransaction.replace(R.id.main_frame, frag_fndList);
                fragmentTransaction.commit();
                findViewById(R.id.menu_person).setBackgroundColor(0xFFE0E0E0);
                findViewById(R.id.menu_chat).setBackgroundColor(0xFFFFFFFF);
                findViewById(R.id.menu_groupchat).setBackgroundColor(0xFFFFFFFF);
                findViewById(R.id.menu_settings).setBackgroundColor(0xFFFFFFFF);
                break;
            case 1:
                fragmentTransaction.replace(R.id.main_frame, frag_chatList);
                fragmentTransaction.commit();
                findViewById(R.id.menu_person).setBackgroundColor(0xFFFFFFFF);
                findViewById(R.id.menu_chat).setBackgroundColor(0xFFE0E0E0);
                findViewById(R.id.menu_groupchat).setBackgroundColor(0xFFFFFFFF);
                findViewById(R.id.menu_settings).setBackgroundColor(0xFFFFFFFF);
                break;
            case 2:
                fragmentTransaction.replace(R.id.main_frame, frag_groupChat);
                fragmentTransaction.commit();
                findViewById(R.id.menu_person).setBackgroundColor(0xFFFFFFFF);
                findViewById(R.id.menu_chat).setBackgroundColor(0xFFFFFFFF);
                findViewById(R.id.menu_groupchat).setBackgroundColor(0xFFE0E0E0);
                findViewById(R.id.menu_settings).setBackgroundColor(0xFFFFFFFF);
                break;
            case 3:
                fragmentTransaction.replace(R.id.main_frame, frag_settings);
                fragmentTransaction.commit();
                findViewById(R.id.menu_person).setBackgroundColor(0xFFFFFFFF);
                findViewById(R.id.menu_chat).setBackgroundColor(0xFFFFFFFF);
                findViewById(R.id.menu_groupchat).setBackgroundColor(0xFFFFFFFF);
                findViewById(R.id.menu_settings).setBackgroundColor(0xFFE0E0E0);
                break;
        }
    }
}