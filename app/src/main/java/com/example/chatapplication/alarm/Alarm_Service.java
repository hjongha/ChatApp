package com.example.chatapplication.alarm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.chatapplication.R;
import com.example.chatapplication.chat.ChatActivity;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Alarm_Service extends Service {
    private static final String TAG = Alarm_Service.class.getSimpleName();

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;

    NotificationCompat.Builder builder;
    NotificationManager notificationManager;
    NotificationChannel channel;
    private PendingIntent pendingIntent;
    Intent my_intent;

    String myUid;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public void onCreate() {super.onCreate();}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
            wakelock.acquire();
            wakelock.release();
        }
        catch (Exception e){ e.printStackTrace(); }

        myUid = intent.getStringExtra("myUid");

        // 모든 채팅 상대에 대한 푸시 알림 설정
        myRef = database.getReference("chatroom").child(myUid);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    myRef = database.getReference("chatroom").child(myUid).child(dataSnapshot.getKey());
                    ChildEventListener childEventListener = new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            // 상대방이 보낸 문자일 때 알림 수행
                            my_intent = new Intent(getApplicationContext(), ChatActivity.class);
                            // 전송된 문자가 상대로부터 온 경우
                            if (snapshot.child("uid").getValue(String.class).equals(dataSnapshot.getKey())) {
                                String str_msg = snapshot.child("msg").getValue(String.class);
                                my_intent.putExtra("otherUid", dataSnapshot.getKey());

                                if (Build.VERSION.SDK_INT >= 26) {
                                    channel = new NotificationChannel("0", "channel", NotificationManager.IMPORTANCE_DEFAULT);
                                    ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
                                    builder = new NotificationCompat.Builder(Alarm_Service.this, "0");
                                    pendingIntent = (PendingIntent.getActivity(Alarm_Service.this, 0, my_intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
                                } else {
                                    builder = new NotificationCompat.Builder(Alarm_Service.this);
                                    pendingIntent = (PendingIntent.getActivity(Alarm_Service.this, 0, my_intent, PendingIntent.FLAG_UPDATE_CURRENT));
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
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("onDestory() 실행", "서비스 파괴");
    }
}
