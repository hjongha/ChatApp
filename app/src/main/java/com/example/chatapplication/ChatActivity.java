package com.example.chatapplication;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.login.UserAccount;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

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

        // 상대방 이름 확인
        myRef = database.getReference("member").child("UserAccount").child(otherUid).child("name");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                TextView textView = (TextView) findViewById(R.id.text_test);
                textView.setText(" " + snapshot.getValue(String.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 채팅방(Key값) 확인 후 추출하기
        myRef = database.getReference("chatroom");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean newChat = true;
                String chatroomKey = "";
                Button btn_send = (Button) findViewById(R.id.btn_send);
                EditText editText = (EditText) findViewById(R.id.editText);
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String[] uidList = new String[2];
                    uidList = dataSnapshot.getKey().split(" ");
                    if (uidList[0].equals(myUid) && uidList[1].equals(otherUid)) {
                        chatroomKey = dataSnapshot.getKey();
                        newChat = false;
                        break;
                    }
                    else if (uidList[0].equals(otherUid) && uidList[1].equals(myUid)) {
                        chatroomKey = dataSnapshot.getKey();
                        newChat = false;
                        break;
                    }
                }

                if (newChat) {
                    chatroomKey = myUid + " " + otherUid;
                }

                // 전송 버튼 활성화
                btn_set(btn_send, editText, myUid, chatroomKey);
                // 채팅 입력(DB 추가) 시 리스트 추가
                myRef = database.getReference("chatroom").child(chatroomKey);
                ChildEventListener childEventListener = new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        ChatData chatData = new ChatData();
                        chatData.setMsg(snapshot.child("msg").getValue(String.class));
                        chatData.setUid(snapshot.child("uid").getValue(String.class));

                        chatDataArrayList.add(chatData);
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) { }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                };
                myRef.addChildEventListener(childEventListener);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // 전송 버튼 수행
    public void btn_set(Button btn, EditText editText, String myUid, String strKey) {
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

                    myRef = database.getReference("chatroom").child(strKey);
                    myRef.child(dateTime).setValue(talks);
                    editText.setText("");
                }
            }
        });
    }
}


