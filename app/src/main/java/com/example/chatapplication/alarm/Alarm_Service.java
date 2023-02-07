package com.example.chatapplication.alarm;

import android.app.Activity;
import android.app.ActivityManager;
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
import com.example.chatapplication.chat.GroupChatActivity;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Hashtable;

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
                            // 현재 액티비티가 해당 채팅방일 때 알림 x
                            ActivityManager activityManager = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
                            for (ActivityManager.RunningTaskInfo serviceInfo : activityManager.getRunningTasks(Integer.MAX_VALUE)) {
                                if (serviceInfo.topActivity.getClassName().equals("com.example.chatapplication.chat.ChatActivity")) {
                                    // 현재 액티비티가 같을 때 액티비티의 메소드를 실행하여 방 번호를 받아내어 해당 방인지 비교해야 함.
                                    // Service 실행 중에 Activity의 메소드 호출에 대한 오류 해결 필요.
                                    /*
                                    try {
                                        Method method = serviceInfo.topActivity.getClass().getMethod("getRoomKey");
                                        System.out.println("method.invoke(GroupChatActivity.class) = " + method.invoke(ChatActivity.class));
                                        if (method.invoke(GroupChatActivity.class).equals(dataSnapshot.getKey())) {
                                            System.out.println("현재 액티비티 = 알림 발송 채팅방");
                                        }
                                    } catch (NoSuchMethodException e) {
                                        e.printStackTrace();
                                    } catch (InvocationTargetException e) {
                                        e.printStackTrace();
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                    */
                                }
                                else {
                                    // 상대방이 보낸 문자일 때 알림 수행
                                    my_intent = new Intent(getApplicationContext(), ChatActivity.class);
                                    // 전송된 문자가 상대로부터 온 경우
                                    if (snapshot.child("uid").getValue(String.class).equals(dataSnapshot.getKey())) {
                                        String str_msg = snapshot.child("msg").getValue(String.class);
                                        // 전송된 문자 내용이 상대가 나간 메시지가 아닌 경우
                                        if (!(str_msg.equals(""))) {
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
                                }
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

        // 그룹 채팅의 모든 채팅 상대에 대한 푸시 알림 설정
        myRef = database.getReference("groupchatroom").child(myUid);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    myRef = database.getReference("groupchatroom").child(myUid).child(dataSnapshot.getKey()).child("msg");
                    ChildEventListener childEventListener = new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            // 현재 액티비티가 해당 채팅방일 때 알림 x
                            ActivityManager activityManager = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
                            for (ActivityManager.RunningTaskInfo serviceInfo : activityManager.getRunningTasks(Integer.MAX_VALUE)) {
                                if (serviceInfo.topActivity.getClassName().equals("com.example.chatapplication.chat.GroupChatActivity")) {
                                    // 현재 액티비티가 같을 때 액티비티의 메소드를 실행하여 방 번호를 받아내어 해당 방인지 비교해야 함.
                                    /*
                                    try {
                                        Method method = serviceInfo.topActivity.getClass().getMethod("getRoomKey");
                                        System.out.println("method.invoke(GroupChatActivity.class) = " + method.invoke(GroupChatActivity.class));
                                        if (method.invoke(GroupChatActivity.class).equals(dataSnapshot.getKey())) {
                                            System.out.println("현재 액티비티 = 알림 발송 채팅방");
                                        }
                                    } catch (NoSuchMethodException e) {
                                        e.printStackTrace();
                                    } catch (InvocationTargetException e) {
                                        e.printStackTrace();
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                    */
                                }
                                else {
                                    // 상대방이 보낸 문자일 때 알림 수행
                                    my_intent = new Intent(getApplicationContext(), GroupChatActivity.class);
                                    // 전송된 문자가 상대로부터 온 경우
                                    if (!(snapshot.child("uid").getValue(String.class).equals(myUid))) {
                                        String str_msg = snapshot.child("msg").getValue(String.class);
                                        // 전송된 문자 내용이 상대가 나간 메시지가 아닌 경우
                                        if (!(str_msg.equals(""))) {
                                            my_intent.putExtra("roomKey", dataSnapshot.getKey());

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
                                            myRef = database.getReference("member").child("UserAccount");
                                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                                    Hashtable<String, String> Hash_user = new Hashtable<String, String>();
                                                    for (DataSnapshot dataSnapshot : snapshot2.getChildren()) {
                                                        // 사용자의 Uid - name 데이터 저장
                                                        Hash_user.put(dataSnapshot.getKey(), dataSnapshot.child("name").getValue(String.class));
                                                    }
                                                    // 보낸 사람
                                                    String sender_name = Hash_user.get(snapshot.child("uid").getValue(String.class));
                                                    // 그룹원들
                                                    myRef = database.getReference("groupchatroom").child(myUid).child(dataSnapshot.getKey()).child("user");
                                                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            // 그룹원들
                                                            String userName_Room = "";
                                                            for (DataSnapshot dataSnapshot2 : snapshot.getChildren()) {
                                                                userName_Room += Hash_user.get(dataSnapshot2.getKey()) + ", ";
                                                            }
                                                            userName_Room = userName_Room.substring(0, userName_Room.length() - 2);

                                                            // 알림 세팅
                                                            builder.setSmallIcon(R.drawable.ic_baseline_chat_24)
                                                                    .setContentTitle(userName_Room)                                 // 그룹 명
                                                                    .setContentText(sender_name + " : " + str_msg)                  // 메시지
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
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {}
                                            });
                                        }
                                    }
                                }
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

        // 백그라운드 상태일 때 채팅방이 새로 생긴 경우
        myRef = database.getReference("chatroom").child(myUid);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> chatRooms = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    chatRooms.add(dataSnapshot.getKey());
                }
                myRef = database.getReference("chatroom").child(myUid);
                ChildEventListener childEventListener = new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        boolean firstBool = true;
                        for (int i=0; i<chatRooms.size(); i++) {
                            System.out.println("chatRooms.get(i) : " + chatRooms.get(i));
                            System.out.println("snapshot.getKey() : " + snapshot.getKey());
                            // 리스너 발동 시 채팅방이 기존에 있는가?
                            if (chatRooms.get(i).equals(snapshot.getKey())) {
                                firstBool = false;
                                break;
                            }
                        }
                        // 새 채팅방일 경우 알림 서비스 재 실행
                        if (firstBool) {
                            stopSelf();
                            Intent intent1 = new Intent(getApplicationContext(), Alarm_Service.class);
                            intent1.putExtra("myUid", myUid);
                            startService(intent1);
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

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 백그라운드 상태일 때 그룹 채팅방이 새로 생긴 경우
        myRef = database.getReference("groupchatroom").child(myUid);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> groupRooms = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    groupRooms.add(dataSnapshot.getKey());
                }
                myRef = database.getReference("groupchatroom").child(myUid);
                ChildEventListener childEventListener2 = new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        boolean firstBool = true;
                        for (int i=0; i<groupRooms.size(); i++) {
                            System.out.println("groupRooms.get(i) : " + groupRooms.get(i));
                            System.out.println("snapshot.getKey() : " + snapshot.getKey());
                            // 리스너 발동 시 채팅방이 기존에 있는가?
                            if (groupRooms.get(i).equals(snapshot.getKey())) {
                                firstBool = false;
                                break;
                            }
                        }
                        // 새 채팅방일 경우 알림 서비스 재 실행
                        if (firstBool) {
                            stopSelf();
                            Intent intent1 = new Intent(getApplicationContext(), Alarm_Service.class);
                            intent1.putExtra("myUid", myUid);
                            startService(intent1);
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
                myRef.addChildEventListener(childEventListener2);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("onDestory() 실행", "서비스 파괴");
    }
}
