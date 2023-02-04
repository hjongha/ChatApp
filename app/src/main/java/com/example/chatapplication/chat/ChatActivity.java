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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
    ArrayList<ChatData> chatDataArrayList;
    ChatAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

    NotificationManager notificationManager;

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
        AlertDialog.Builder dlg = new AlertDialog.Builder(ChatActivity.this);
        switch (item.getItemId()) {
            case R.id.delete_msg:
                // 대화 내용 삭제
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
                        myRef.setValue("");
                        Toast.makeText(ChatActivity.this, "대화 내용이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
                dlg.show();
                return true;

            case R.id.delete_room:
                // 채팅방 나가기
                dlg.setTitle("채팅방 나가기 확인");
                dlg.setMessage("정말 채팅방을 나가시겠습니까?\n방을 나가면 채팅 내용이 삭제됩니다.");
                dlg.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(ChatActivity.this, "취소하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
                dlg.setNegativeButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String dateTime = dateFormat.format(calendar.getTime());

                        Hashtable<String, String> talks = new Hashtable<String, String>();
                        talks.put("uid", myUid);
                        talks.put("msg", "");

                        myRef = database.getReference("chatroom").child(otherUid).child(myUid);
                        myRef.child(dateTime).setValue(talks);

                        myRef = database.getReference("chatroom").child(myUid).child(otherUid);
                        myRef.removeValue();
                        Toast.makeText(ChatActivity.this, "채팅방을 나갑니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                dlg.show();
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

        chatDataArrayList = new ArrayList<>();
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
        // 채팅방 입장 시 푸시 알림 삭제
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
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