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

        // ?????? ?????? ????????? ?????? ?????? ?????? ??????
        myRef = database.getReference("chatroom").child(myUid);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    myRef = database.getReference("chatroom").child(myUid).child(dataSnapshot.getKey());
                    ChildEventListener childEventListener = new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            // ?????? ??????????????? ?????? ???????????? ??? ?????? x
                            ActivityManager activityManager = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
                            for (ActivityManager.RunningTaskInfo serviceInfo : activityManager.getRunningTasks(Integer.MAX_VALUE)) {
                                if (serviceInfo.topActivity.getClassName().equals("com.example.chatapplication.chat.ChatActivity")) {
                                    // ?????? ??????????????? ?????? ??? ??????????????? ???????????? ???????????? ??? ????????? ???????????? ?????? ????????? ???????????? ???.
                                    // Service ?????? ?????? Activity??? ????????? ????????? ?????? ?????? ?????? ??????.
                                    /*
                                    try {
                                        Method method = serviceInfo.topActivity.getClass().getMethod("getRoomKey");
                                        System.out.println("method.invoke(GroupChatActivity.class) = " + method.invoke(ChatActivity.class));
                                        if (method.invoke(GroupChatActivity.class).equals(dataSnapshot.getKey())) {
                                            System.out.println("?????? ???????????? = ?????? ?????? ?????????");
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
                                    // ???????????? ?????? ????????? ??? ?????? ??????
                                    my_intent = new Intent(getApplicationContext(), ChatActivity.class);
                                    // ????????? ????????? ??????????????? ??? ??????
                                    if (snapshot.child("uid").getValue(String.class).equals(dataSnapshot.getKey())) {
                                        String str_msg = snapshot.child("msg").getValue(String.class);
                                        // ????????? ?????? ????????? ????????? ?????? ???????????? ?????? ??????
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

                                            // ????????? ?????? ?????? ??? ?????? ??????, ??????
                                            myRef = database.getReference("member").child("UserAccount").child(snapshot.child("uid").getValue(String.class)).child("name");
                                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    String user_name = snapshot.getValue(String.class);

                                                    builder.setSmallIcon(R.drawable.ic_baseline_chat_24)
                                                            .setContentTitle(user_name)                                     // ?????? ??????
                                                            .setContentText(str_msg)                                        // ?????????
                                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)               // ?????? ?????? ??????
                                                            .setAutoCancel(true)                                            // ?????? ?????? ??? ?????? ?????? ??????
                                                            .setContentIntent(pendingIntent);                               // ?????? ?????? ??? ??????????????? ??????
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

        // ?????? ????????? ?????? ?????? ????????? ?????? ?????? ?????? ??????
        myRef = database.getReference("groupchatroom").child(myUid);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    myRef = database.getReference("groupchatroom").child(myUid).child(dataSnapshot.getKey()).child("msg");
                    ChildEventListener childEventListener = new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            // ?????? ??????????????? ?????? ???????????? ??? ?????? x
                            ActivityManager activityManager = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
                            for (ActivityManager.RunningTaskInfo serviceInfo : activityManager.getRunningTasks(Integer.MAX_VALUE)) {
                                if (serviceInfo.topActivity.getClassName().equals("com.example.chatapplication.chat.GroupChatActivity")) {
                                    // ?????? ??????????????? ?????? ??? ??????????????? ???????????? ???????????? ??? ????????? ???????????? ?????? ????????? ???????????? ???.
                                    /*
                                    try {
                                        Method method = serviceInfo.topActivity.getClass().getMethod("getRoomKey");
                                        System.out.println("method.invoke(GroupChatActivity.class) = " + method.invoke(GroupChatActivity.class));
                                        if (method.invoke(GroupChatActivity.class).equals(dataSnapshot.getKey())) {
                                            System.out.println("?????? ???????????? = ?????? ?????? ?????????");
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
                                    // ???????????? ?????? ????????? ??? ?????? ??????
                                    my_intent = new Intent(getApplicationContext(), GroupChatActivity.class);
                                    // ????????? ????????? ??????????????? ??? ??????
                                    if (!(snapshot.child("uid").getValue(String.class).equals(myUid))) {
                                        String str_msg = snapshot.child("msg").getValue(String.class);
                                        // ????????? ?????? ????????? ????????? ?????? ???????????? ?????? ??????
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

                                            // ????????? ?????? ?????? ??? ?????? ??????, ??????
                                            myRef = database.getReference("member").child("UserAccount");
                                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                                    Hashtable<String, String> Hash_user = new Hashtable<String, String>();
                                                    for (DataSnapshot dataSnapshot : snapshot2.getChildren()) {
                                                        // ???????????? Uid - name ????????? ??????
                                                        Hash_user.put(dataSnapshot.getKey(), dataSnapshot.child("name").getValue(String.class));
                                                    }
                                                    // ?????? ??????
                                                    String sender_name = Hash_user.get(snapshot.child("uid").getValue(String.class));
                                                    // ????????????
                                                    myRef = database.getReference("groupchatroom").child(myUid).child(dataSnapshot.getKey()).child("user");
                                                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            // ????????????
                                                            String userName_Room = "";
                                                            for (DataSnapshot dataSnapshot2 : snapshot.getChildren()) {
                                                                userName_Room += Hash_user.get(dataSnapshot2.getKey()) + ", ";
                                                            }
                                                            userName_Room = userName_Room.substring(0, userName_Room.length() - 2);

                                                            // ?????? ??????
                                                            builder.setSmallIcon(R.drawable.ic_baseline_chat_24)
                                                                    .setContentTitle(userName_Room)                                 // ?????? ???
                                                                    .setContentText(sender_name + " : " + str_msg)                  // ?????????
                                                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)               // ?????? ?????? ??????
                                                                    .setAutoCancel(true)                                            // ?????? ?????? ??? ?????? ?????? ??????
                                                                    .setContentIntent(pendingIntent);                               // ?????? ?????? ??? ??????????????? ??????
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

        // ??????????????? ????????? ??? ???????????? ?????? ?????? ??????
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
                            // ????????? ?????? ??? ???????????? ????????? ??????????
                            if (chatRooms.get(i).equals(snapshot.getKey())) {
                                firstBool = false;
                                break;
                            }
                        }
                        // ??? ???????????? ?????? ?????? ????????? ??? ??????
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

        // ??????????????? ????????? ??? ?????? ???????????? ?????? ?????? ??????
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
                            // ????????? ?????? ??? ???????????? ????????? ??????????
                            if (groupRooms.get(i).equals(snapshot.getKey())) {
                                firstBool = false;
                                break;
                            }
                        }
                        // ??? ???????????? ?????? ?????? ????????? ??? ??????
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
        Log.d("onDestory() ??????", "????????? ??????");
    }
}
