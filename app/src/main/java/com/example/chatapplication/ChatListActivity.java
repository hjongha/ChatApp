package com.example.chatapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.chatapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatListActivity extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("chatroom");
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatlist);

        ListView listView = (ListView) findViewById(R.id.listView_menu2);
        ListAdapter listAdapter = new ListAdapter();
        listView.setAdapter(listAdapter);

        // DB에 등록된 채팅방 정보를 가져와 리스트에 추가
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> uidArrayList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String[] uidList = new String[2];
                    uidList = dataSnapshot.getKey().split(" ");
                    if (uidList[0].equals(firebaseUser.getUid())) {
                        myRef = database.getReference("member").child("UserAccount").child(uidList[1]);
                        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                listAdapter.addList(ContextCompat.getDrawable(getApplicationContext(), R.drawable.baseimg), snapshot2.child("name").getValue(String.class));
                                uidArrayList.add(snapshot2.child("uid").getValue(String.class));
                                listAdapter.notifyDataSetChanged();
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }
                    else if (uidList[1].equals(firebaseUser.getUid())) {
                        myRef = database.getReference("member").child("UserAccount").child(uidList[0]);
                        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                listAdapter.addList(ContextCompat.getDrawable(getApplicationContext(), R.drawable.baseimg), snapshot2.child("name").getValue(String.class));
                                uidArrayList.add(snapshot2.child("uid").getValue(String.class));
                                listAdapter.notifyDataSetChanged();
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }
                }

                // 리스트 선택 시 해당 채팅방 입장
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
                        intent.putExtra("myUid", firebaseUser.getUid());
                        intent.putExtra("otherUid", uidArrayList.get(position));
                        startActivity(intent);
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
