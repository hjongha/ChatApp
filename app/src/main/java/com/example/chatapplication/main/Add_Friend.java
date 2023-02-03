package com.example.chatapplication.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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

public class Add_Friend extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_friend);

        ListView listView = (ListView) findViewById(R.id.listView_addF);
        ListAdapter listAdapter = new ListAdapter();
        listAdapter.list_clear();
        listView.setAdapter(listAdapter);

        EditText editText = (EditText) findViewById(R.id.editText_addF);
        Button addF_btn = (Button) findViewById(R.id.addF_btn);
        addF_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str_Email = editText.getText().toString();

                // Email로 해당 계정 탐색 후 리스트에 추가
                myRef = database.getReference("member").child("UserAccount");
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<String> uidList = new ArrayList<>();
                        ArrayList<String> nameList = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if (dataSnapshot.child("email").getValue(String.class).equals(str_Email)) {
                                listAdapter.addList(ContextCompat.getDrawable(getApplicationContext(), R.drawable.baseimg), dataSnapshot.child("name").getValue(String.class));
                                uidList.add(dataSnapshot.child("uid").getValue(String.class));
                                nameList.add(dataSnapshot.child("name").getValue(String.class));
                            }
                        }
                        listAdapter.notifyDataSetChanged();
                        // 리스트 선택 시 친구 추가 확인창 출력
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                AlertDialog.Builder dlg = new AlertDialog.Builder(Add_Friend.this);

                                dlg.setTitle("친구 추가 확인");
                                dlg.setMessage("해당 친구를 추가하시겠습니까?");
                                dlg.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Toast.makeText(Add_Friend.this, "취소하였습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                dlg.setNegativeButton("예", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        myRef = database.getReference("friend").child(firebaseUser.getUid());
                                        myRef.child(uidList.get(position)).setValue(nameList.get(position));
                                        Toast.makeText(Add_Friend.this, "친구 추가가 완료되었습니다.", Toast.LENGTH_SHORT).show();
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
        });
    }
}
