package com.example.chatapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FndListActivity extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("member").child("UserAccount");
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fndlist);

        ListView listView = (ListView) findViewById(R.id.listView_menu);
        ListAdapter listAdapter = new ListAdapter();
        listView.setAdapter(listAdapter);

        // DB에 저장된 사용자 정보를 가져와 리스트에 추가
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> uidList = new ArrayList<>();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if(!firebaseUser.getUid().equals(dataSnapshot.child("uid").getValue(String.class))) {
                        listAdapter.addList(ContextCompat.getDrawable(getApplicationContext(), R.drawable.baseimg), dataSnapshot.child("name").getValue(String.class));
                        uidList.add(dataSnapshot.child("uid").getValue(String.class));
                    }
                }
                listAdapter.notifyDataSetChanged();

                // 리스트 선택 시 해당 사용자와의 채팅방 개설
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(FndListActivity.this, ChatActivity.class);
                        intent.putExtra("myUid", firebaseUser.getUid());
                        intent.putExtra("otherUid", uidList.get(position));
                        startActivity(intent);
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}