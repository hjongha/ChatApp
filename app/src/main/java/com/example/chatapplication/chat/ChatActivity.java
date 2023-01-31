package com.example.chatapplication.chat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.R;
import com.example.chatapplication.alarm.Alarm_Reciver;
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

    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

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

        String myUid = getIntent().getStringExtra("myUid");         // 내 UID
        String otherUid = getIntent().getStringExtra("otherUid");   // 상대방 UID

        Intent my_intent = new Intent(ChatActivity.this, Alarm_Reciver.class);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

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

                // 상대방이 보낸 문자일 때 알림 수행
                if (snapshot.child("uid").getValue(String.class).equals(otherUid)) {
                    my_intent.putExtra("msg", chatData.getMsg());
                    my_intent.putExtra("uid", otherUid);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        pendingIntent = (PendingIntent.getBroadcast(ChatActivity.this, 0, my_intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
                    } else {
                        pendingIntent = (PendingIntent.getBroadcast(ChatActivity.this, 0, my_intent, PendingIntent.FLAG_UPDATE_CURRENT));
                    }
                    if (Build.VERSION.SDK_INT >= 23) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, 0, pendingIntent);
                    } else {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, 0, pendingIntent);
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

    // 전송 버튼 수행
    public void btn_set(Button btn, EditText editText, String myUid, String otherUid) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String edit_str = editText.getText().toString();
                if (!edit_str.equals("")) {

                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.HOUR_OF_DAY, 9);
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