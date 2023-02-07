package com.example.chatapplication.chat;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupChat_UserList extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

    String roomKey, myUid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_userlist);

        ListView listView = (ListView) findViewById(R.id.listView_userlist);
        GCUserList_ListAdapter listAdapter = new GCUserList_ListAdapter();
        listView.setAdapter(listAdapter);

        roomKey = getIntent().getStringExtra("roomKey");   // 방 Key값
        myUid = firebaseUser.getUid();

        // 채팅방에 있는 사용자들의 정보를 가져와 리스트에 추가
        myRef = database.getReference("groupchatroom").child(myUid).child(roomKey).child("user");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> uidList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Drawable drawable = getResources().getDrawable(R.drawable.baseimg);
                    Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
                    // 친구 데이터 추출 후 리스트에 추가
                    myRef = database.getReference("member").child("UserAccount").child(dataSnapshot.getKey()).child("name");
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            listAdapter.addList(bitmap, snapshot.getValue(String.class), dataSnapshot.getKey());
                            uidList.add(dataSnapshot.getKey());
                            listAdapter.notifyDataSetChanged();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
