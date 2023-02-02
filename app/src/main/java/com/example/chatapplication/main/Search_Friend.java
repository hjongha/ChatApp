package com.example.chatapplication.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.chatapplication.R;
import com.example.chatapplication.chat.ChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Search_Friend extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_friend);

        ListView listView = (ListView) findViewById(R.id.listView_menu);
        ListAdapter listAdapter = new ListAdapter();
        listView.setAdapter(listAdapter);

        // DB에 저장된 사용자의 친구 정보를 가져와 리스트에 추가
        myRef = database.getReference("friend").child(firebaseUser.getUid());
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> uidList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // 친구 데이터 추출 후 리스트에 추가
                    listAdapter.addList(ContextCompat.getDrawable(getApplicationContext(), R.drawable.baseimg), dataSnapshot.getValue(String.class));
                    uidList.add(dataSnapshot.getKey());
                }
                listAdapter.notifyDataSetChanged();
                // 리스트 선택 시 해당 사용자와의 채팅방 개설
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(Search_Friend.this, ChatActivity.class);
                        intent.putExtra("myUid", firebaseUser.getUid());
                        intent.putExtra("otherUid", uidList.get(position));
                        startActivity(intent);
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        Button btn_search = (Button) findViewById(R.id.search_btn);
        EditText editText = (EditText) findViewById(R.id.editText);
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myRef = database.getReference("friend").child(firebaseUser.getUid());
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String edit_str = editText.getText().toString();
                        listAdapter.list_clear();
                        ArrayList<String> uidList = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if (dataSnapshot.getValue(String.class).contains(edit_str)) {
                                // 친구 목록의 name에 검색 단어가 포함되면 사용자 데이터 추출 후 리스트에 추가
                                listAdapter.addList(ContextCompat.getDrawable(Search_Friend.this, R.drawable.baseimg), dataSnapshot.getValue(String.class));
                                uidList.add(dataSnapshot.getKey());
                            }
                        }
                        listAdapter.notifyDataSetChanged();

                        // 리스트 선택 시 해당 사용자와의 채팅방 개설
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Intent intent = new Intent(Search_Friend.this, ChatActivity.class);
                                intent.putExtra("myUid", firebaseUser.getUid());
                                intent.putExtra("otherUid", uidList.get(position));
                                startActivity(intent);
                            }
                        });

                        // 검색 결과가 없을 경우
                        if (listAdapter.getCount() == 0) {
                            Toast.makeText(Search_Friend.this, "검색 결과가 존재하지 않습니다..", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
        });
    }
}
