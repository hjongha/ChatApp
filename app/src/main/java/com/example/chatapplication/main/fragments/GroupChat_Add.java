package com.example.chatapplication.main.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapplication.R;
import com.example.chatapplication.chat.ChatActivity;
import com.example.chatapplication.chat.GroupChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupChat_Add extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

    ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_groupchat);

        ListView listView = (ListView) findViewById(R.id.listView_addG);
        GCAdd_Adapter listAdapter = new GCAdd_Adapter();
        listView.setAdapter(listAdapter);
        listAdapter.list_clear();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);        // 로딩 바 활성화

        // 친구 이름 업데이트
        myRef = database.getReference("friend").child(firebaseUser.getUid());
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    myRef = database.getReference("member").child("UserAccount");
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot2 : snapshot.getChildren()) {
                                if (dataSnapshot.getKey().equals(dataSnapshot2.getKey())) {
                                    myRef = database.getReference("friend").child(firebaseUser.getUid()).child(dataSnapshot.getKey());
                                    myRef.setValue(dataSnapshot2.child("name").getValue(String.class));
                                    break;
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });


        // DB에 저장된 사용자의 친구 정보를 가져와 리스트에 추가
        myRef = database.getReference("friend").child(firebaseUser.getUid());
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    myRef = database.getReference("member").child("UserAccount").child(dataSnapshot.getKey()).child("name");
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot2) {
                            Drawable drawable = getResources().getDrawable(R.drawable.baseimg);
                            Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
                            listAdapter.addList(bitmap, dataSnapshot.getValue(String.class), dataSnapshot.getKey());
                            listAdapter.notifyDataSetChanged();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
                // 리스트 선택 시 체크박스 체크
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        ImageView imageView = (ImageView) view.findViewById(R.id.imagecheck_GCAdd);
                        if (listAdapter.getItem(position).getCheck_bool()) {
                            listAdapter.getItem(position).setCheck_bool(false);
                            Drawable drawable = getResources().getDrawable(R.drawable.ic_baseline_check_box_outline_blank_24);
                            imageView.setImageDrawable(drawable);
                        }
                        else {
                            listAdapter.getItem(position).setCheck_bool(true);
                            Drawable drawable = getResources().getDrawable(R.drawable.ic_baseline_check_box_24);
                            imageView.setImageDrawable(drawable);
                        }
                    }
                });
                progressBar.setVisibility(View.GONE);       // 로딩 바 비활성화
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        Button btn_GCAdd = (Button) findViewById(R.id.btn_GCAdd);
        btn_GCAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myRef = database.getReference("groupchatroom");
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Boolean num_setting = true;
                        Boolean overlap = false;
                        int room_num = 0;
                        while (num_setting) {
                            room_num = (int) (Math.random() * 10000);
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                    if (Integer.toString(room_num).equals(dataSnapshot1.getKey())) {
                                        // 방 번호 중복된 방 찾을 시
                                        overlap = true;
                                        break;
                                    }
                                }
                                if (overlap) {
                                    // 방 번호 중복된 방 찾을 시 겉 for 문 종료
                                    break;
                                }
                            }
                            if (overlap) {
                                // 탐색 종료 시점 방 번호 중복일 시
                                overlap = false;
                            }
                            else {
                                // 탐색 종료 시점 방 번호 중복이 없을 시
                                num_setting = false;
                            }
                        }
                        System.out.println("Room Number : " + Integer.toString(room_num));
                        myRef = database.getReference("groupchatroom").child(firebaseUser.getUid()).child(Integer.toString(room_num)).child("user");

                        ArrayList<String> groupUid_List = new ArrayList<>();
                        // 체크된 친구들의 Uid를 그룹 채팅 방 DB에 저장
                        for (int i=0; i<listAdapter.getCount(); i++) {
                            if (listAdapter.getItem(i).getCheck_bool()) {
                                myRef.child(listAdapter.getItem(i).getFnd_uid()).setValue("");
                                groupUid_List.add(listAdapter.getItem(i).getFnd_uid());
                            }
                        }

                        // 채팅 기록을 따로 관리해야 하기 때문에 그룹 채팅에 입장한 모든 Uid에 대해 그룹 채팅방 DB 생성
                        for (int i=0; i<groupUid_List.size(); i++) {
                            myRef = database.getReference("groupchatroom").child(groupUid_List.get(i)).child(Integer.toString(room_num)).child("user");
                            myRef.child(firebaseUser.getUid()).setValue("");
                            for (int j=0; j<groupUid_List.size(); j++) {
                                if (i!=j) {
                                    myRef.child(groupUid_List.get(j)).setValue("");
                                }
                            }
                        }

                        // 그룹 채팅방 생성 완료
                        Toast.makeText(GroupChat_Add.this, "그룹 채팅방이 생성되었습니다.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(GroupChat_Add.this, GroupChatActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
        });
    }
}
