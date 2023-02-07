package com.example.chatapplication.chat;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.R;
import com.example.chatapplication.main.MainActivity;
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

public class GroupChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    ArrayList<ChatData> chatDataArrayList;
    ChatAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

    NotificationManager notificationManager;

    String roomKey;
    String myUid;

    String serviceInfo_str;

    // MenuItem (우측 위 옵션메뉴)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.item_menu, menu);
        menu.findItem(R.id.gc_user).setVisible(true);
        return true;
    }

    // MenuItem (우측 위 옵션메뉴) 선택 리스너
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        AlertDialog.Builder dlg = new AlertDialog.Builder(GroupChatActivity.this);
        switch (item.getItemId()) {
            case R.id.delete_msg:
                // 대화 내용 삭제
                dlg.setTitle("대화 내용 삭제 확인");
                dlg.setMessage("대화 내용을 삭제하시겠습니까?");
                dlg.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(GroupChatActivity.this, "취소하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
                dlg.setNegativeButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        myRef = database.getReference("groupchatroom").child(myUid).child(roomKey).child("msg");
                        myRef.setValue("");
                        Toast.makeText(GroupChatActivity.this, "대화 내용이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(GroupChatActivity.this, "취소하였습니다.", Toast.LENGTH_SHORT).show();
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

                        myRef = database.getReference("groupchatroom").child(myUid).child(roomKey).child("user");
                        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    // 그룹 채팅방 나갔음을 알리고 해당 그룹 채팅방에 대한 내 DB 삭제
                                    myRef = database.getReference("groupchatroom").child(dataSnapshot.getKey()).child(roomKey).child("msg");
                                    myRef.child(dateTime).setValue(talks);
                                    myRef = database.getReference("groupchatroom").child(dataSnapshot.getKey()).child(roomKey).child("user").child(myUid);
                                    myRef.removeValue();

                                    myRef = database.getReference("groupchatroom").child(myUid).child(roomKey);
                                    myRef.removeValue();
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                        Toast.makeText(GroupChatActivity.this, "채팅방을 나갑니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                dlg.show();
                return true;

            case R.id.gc_user:
                Intent intent = new Intent(GroupChatActivity.this, GroupChat_UserList.class);
                intent.putExtra("roomKey", roomKey);
                startActivity(intent);
                return true;

        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        TextView textView = (TextView) findViewById(R.id.text_test);
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
        roomKey = getIntent().getStringExtra("roomKey");   // 방 Key값

        // 상대방 이름 확인
        myRef = database.getReference("groupchatroom").child(myUid).child(roomKey).child("user");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int user_count = 1;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    int finalUser_count = user_count;
                    myRef = database.getReference("member").child("UserAccount").child(dataSnapshot.getKey()).child("name");
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (finalUser_count != 1) {
                                textView.append(", ");
                            }
                            textView.append(snapshot.getValue(String.class));
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                    user_count++;
                }
                textView.append("("+Integer.toString(user_count)+") ");
                ImageView imageView = (ImageView) findViewById(R.id.image_chat);
                imageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_groups_24));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 채팅방(Key값) 확인 후 추출하기
        Button btn_send = (Button) findViewById(R.id.btn_send);
        EditText editText = (EditText) findViewById(R.id.editText);

        // 전송 버튼 활성화
        btn_set(btn_send, editText);

        // 채팅 입력(DB 추가) 시 리스트 추가
        myRef = database.getReference("groupchatroom").child(myUid).child(roomKey).child("msg");
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
    protected void onDestroy() {
        super.onDestroy();
        // baseActivity가 MainActivity가 아닐 경우
        if ("com.example.chatapplication.chat.ChatActivity".equals(serviceInfo_str)) {
            // ChatActivity를 종료하면 MainActivity를 실행
            Intent intent = new Intent(GroupChatActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 채팅방 입장 시 푸시 알림 삭제
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(0);

        // 현재 실행 중인 baseActivity가 무엇인지 체크 (푸시 알림을 통해 ChatActivity에 접속하여 MainActivity가 안켜졌을 경우를 대비)
        ActivityManager activityManager = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningTaskInfo serviceInfo : activityManager.getRunningTasks(Integer.MAX_VALUE)) {
            serviceInfo_str = serviceInfo.baseActivity.getClassName();
        }
    }

    // 전송 버튼 수행
    public void btn_set(Button btn, EditText editText) {
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

                    myRef = database.getReference("groupchatroom").child(myUid).child(roomKey).child("user");
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                // 그룹 채팅방 내에 있는 모든 유저의 채팅 메시지 DB 추가
                                myRef = database.getReference("groupchatroom").child(dataSnapshot.getKey()).child(roomKey).child("msg");
                                myRef.child(dateTime).setValue(talks);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                    myRef = database.getReference("groupchatroom").child(myUid).child(roomKey).child("msg");
                    myRef.child(dateTime).setValue(talks);
                    editText.setText("");
                }
            }
        });
    }

    public String getRoomKey() {
        return roomKey;
    }
}
