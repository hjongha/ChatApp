package com.example.chatapplication.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.chatapplication.R;
import com.example.chatapplication.chat.ChatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// 로그인 후 실행되는 메인 액티비티
public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private Fragment_FndList frag_fndList = new Fragment_FndList();
    private Fragment_ChatList frag_chatList = new Fragment_ChatList();
    private Fragment_Settings frag_settings = new Fragment_Settings();

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

    NotificationManager notificationManager;
    NotificationChannel channel;
    private PendingIntent pendingIntent;
    Intent my_intent;

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

        // 모든 채팅 상대에 대한 푸시 알림 설정
        myRef = database.getReference("chatroom").child(firebaseUser.getUid());
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    myRef = database.getReference("chatroom").child(firebaseUser.getUid()).child(dataSnapshot.getKey());
                    ChildEventListener childEventListener = new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            // 상대방이 보낸 문자일 때 알림 수행
                            NotificationCompat.Builder builder;
                            my_intent = new Intent(getApplicationContext(), ChatActivity.class);

                            // 전송된 문자가 상대로부터 온 경우
                            if (snapshot.child("uid").getValue(String.class).equals(dataSnapshot.getKey())) {
                                String str_msg = snapshot.child("msg").getValue(String.class);
                                my_intent.putExtra("otherUid", snapshot.child("uid").getValue(String.class));

                                if (Build.VERSION.SDK_INT >= 26) {
                                    channel = new NotificationChannel("0", "channel", NotificationManager.IMPORTANCE_DEFAULT);
                                    ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
                                    builder = new NotificationCompat.Builder(MainActivity.this, "0");
                                    pendingIntent = (PendingIntent.getActivity(MainActivity.this, 0, my_intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
                                } else {
                                    builder = new NotificationCompat.Builder(MainActivity.this);
                                    pendingIntent = (PendingIntent.getActivity(MainActivity.this, 0, my_intent, PendingIntent.FLAG_UPDATE_CURRENT));
                                }

                                // 상대방 이름 추출 후 알림 세팅, 등록
                                myRef = database.getReference("member").child("UserAccount").child(snapshot.child("uid").getValue(String.class)).child("name");
                                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String user_name = snapshot.getValue(String.class);

                                        builder.setSmallIcon(R.drawable.ic_baseline_chat_24)
                                                .setContentTitle(user_name)                                     // 상대 이름
                                                .setContentText(str_msg)                                        // 메시지
                                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)               // 알림 우선 순위
                                                .setAutoCancel(true)                                            // 알림 선택 시 알림 자동 삭제
                                                .setContentIntent(pendingIntent);                               // 알림 선택 시 채팅방으로 이동
                                        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                        notificationManager.notify(0, builder.build());
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                                });
                            }
                        }
                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
                        @Override
                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    };
                    myRef.addChildEventListener(childEventListener);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
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
                findViewById(R.id.menu_person).setBackgroundColor(0xFFE0E0E0);
                findViewById(R.id.menu_chat).setBackgroundColor(0xFFFFFFFF);
                findViewById(R.id.menu_settings).setBackgroundColor(0xFFFFFFFF);
                break;
            case 1:
                fragmentTransaction.replace(R.id.main_frame, frag_chatList);
                fragmentTransaction.commit();
                findViewById(R.id.menu_person).setBackgroundColor(0xFFFFFFFF);
                findViewById(R.id.menu_chat).setBackgroundColor(0xFFE0E0E0);
                findViewById(R.id.menu_settings).setBackgroundColor(0xFFFFFFFF);
                break;
            case 2:
                fragmentTransaction.replace(R.id.main_frame, frag_settings);
                fragmentTransaction.commit();
                findViewById(R.id.menu_person).setBackgroundColor(0xFFFFFFFF);
                findViewById(R.id.menu_chat).setBackgroundColor(0xFFFFFFFF);
                findViewById(R.id.menu_settings).setBackgroundColor(0xFFE0E0E0);
                break;
        }
    }


}