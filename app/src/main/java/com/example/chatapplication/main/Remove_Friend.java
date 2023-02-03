package com.example.chatapplication.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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

public class Remove_Friend extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.remove_friend);

        ListView listView = (ListView) findViewById(R.id.listView_removeF);
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
                // 리스트 선택 시 삭제 확인 알림창 출력
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        AlertDialog.Builder dlg = new AlertDialog.Builder(Remove_Friend.this);

                        dlg.setTitle("친구 삭제 확인");
                        dlg.setMessage("해당 친구를 정말 삭제하시겠습니까?\n삭제 시 채팅 기록도 전부 삭제됩니다.");
                        dlg.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(Remove_Friend.this, "취소하였습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
                        dlg.setNegativeButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // 친구 목록에서 해당 친구 삭제
                                myRef = database.getReference("friend").child(firebaseUser.getUid());
                                myRef.child(uidList.get(position)).removeValue();
                                // 해당 친구와의 채팅 기록 삭제
                                myRef = database.getReference("chatroom").child(firebaseUser.getUid());
                                myRef.child(uidList.get(position)).removeValue();
                                Toast.makeText(Remove_Friend.this, "친구 삭제가 완료되었습니다.", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                        dlg.show();
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
