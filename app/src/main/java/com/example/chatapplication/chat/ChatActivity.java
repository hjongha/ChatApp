package com.example.chatapplication.chat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.R;
import com.example.chatapplication.login.LoginActivity;
import com.example.chatapplication.login.ModifyPasswd;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;

// 채팅 액티비티
public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    ChatAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

    NotificationManager notificationManager;
    NotificationChannel channel;
    private PendingIntent pendingIntent;
    Intent my_intent;

    String myUid, otherUid;

    // MenuItem (우측 위 옵션메뉴)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.item_menu, menu);

        return true;
    }

    // MenuItem (우측 위 옵션메뉴) 선택 리스너
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_msg:
                // 대화 내용 삭제
                AlertDialog.Builder dlg = new AlertDialog.Builder(ChatActivity.this);

                dlg.setTitle("대화 내용 삭제 확인");
                dlg.setMessage("대화 내용을 삭제하시겠습니까?");
                dlg.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(ChatActivity.this, "취소하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
                dlg.setNegativeButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        myRef = database.getReference("chatroom").child(myUid).child(otherUid);
                        myRef.removeValue();
                        Toast.makeText(ChatActivity.this, "대화 내용이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
                dlg.show();
                return true;

            case R.id.menu_item1:
                return true;

            case R.id.menu_item2:
                return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setWillNotDraw(false); // 스크롤바 표시

        ArrayList<ChatData> chatDataArrayList = new ArrayList<>();
        mAdapter = new ChatAdapter(chatDataArrayList);
        recyclerView.setAdapter(mAdapter);
        chatDataArrayList.clear();

        myUid = firebaseUser.getUid();         // 내 UID
        otherUid = getIntent().getStringExtra("otherUid");   // 상대방 UID

        // 상대방 이름, 이미지 확인
        myRef = database.getReference("member").child("UserAccount").child(otherUid).child("name");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ImageView imageView = (ImageView) findViewById(R.id.image_chat);
                imageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.baseimg));
                TextView textView = (TextView) findViewById(R.id.text_test);
                textView.setText(snapshot.getValue(String.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 채팅방(Key값) 확인 후 추출하기
        Button btn_send = (Button) findViewById(R.id.btn_send);
        EditText editText = (EditText) findViewById(R.id.editText);

        // 전송 버튼 활성화
        btn_set(btn_send, editText, myUid, otherUid);

        // 채팅 입력(DB 추가) 시 리스트 추가
        myRef = database.getReference("chatroom").child(myUid).child(otherUid);
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ChatData chatData = new ChatData();
                chatData.setMsg(snapshot.child("msg").getValue(String.class));
                chatData.setUid(snapshot.child("uid").getValue(String.class));
                chatData.setTimer(snapshot.getKey());

                chatDataArrayList.add(chatData);
                mAdapter.notifyDataSetChanged();

                // 채팅방 포커스를 아래로
                recyclerView.scrollToPosition(chatDataArrayList.size() - 1);
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
    protected void onResume() {
        super.onResume();

        // 채팅방 재입장 시 알림 삭제
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.deleteNotificationChannel("0");
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        myRef = database.getReference("chatroom").child(myUid).child(otherUid);
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // 상대방이 보낸 문자일 때 알림 수행
                NotificationCompat.Builder builder;
                my_intent = new Intent(getApplicationContext(), ChatActivity.class);

                // 전송된 문자가 상대로부터 온 경우
                if (snapshot.child("uid").getValue(String.class).equals(otherUid)) {
                    my_intent.putExtra("msg", snapshot.child("msg").getValue(String.class));
                    my_intent.putExtra("otherUid", otherUid);

                    if (Build.VERSION.SDK_INT >= 26) {
                        channel = new NotificationChannel("0","channel", NotificationManager.IMPORTANCE_DEFAULT);
                        ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
                        builder = new NotificationCompat.Builder(ChatActivity.this, "0");
                        pendingIntent = (PendingIntent.getActivity(ChatActivity.this, 0, my_intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
                    }
                    else {
                        builder = new NotificationCompat.Builder(ChatActivity.this);
                        pendingIntent = (PendingIntent.getActivity(ChatActivity.this, 0, my_intent, PendingIntent.FLAG_UPDATE_CURRENT));

                    }

                    // 상대방 이름 추출 후 알림 세팅, 등록
                    myRef = database.getReference("member").child("UserAccount").child(otherUid).child("name");
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String user_name = snapshot.getValue(String.class);

                            builder.setSmallIcon(R.drawable.ic_baseline_chat_24)
                                    .setContentTitle(user_name)                                     // 상대 이름
                                    .setContentText(snapshot.child("msg").getValue(String.class))   // 메시지
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

    // 전송 버튼 수행
    public void btn_set(Button btn, EditText editText, String myUid, String otherUid) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String edit_str = editText.getText().toString();
                if (!edit_str.equals("")) {

                    Calendar calendar = Calendar.getInstance();
                    // 시차
                    // calendar.add(Calendar.HOUR_OF_DAY, 9);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String dateTime = dateFormat.format(calendar.getTime());

                    Hashtable<String, String> talks = new Hashtable<String, String>();
                    talks.put("uid", myUid);
                    talks.put("msg", edit_str);

                    myRef = database.getReference("chatroom").child(myUid).child(otherUid);
                    myRef.child(dateTime).setValue(talks);
                    myRef = database.getReference("chatroom").child(otherUid).child(myUid);
                    myRef.child(dateTime).setValue(talks);
                    editText.setText("");
                }
            }
        });
    }
}