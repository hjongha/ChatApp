package com.example.chatapplication.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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

public class Fragment_ChatList extends Fragment {
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;

    private View view;
    Context context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_chatlist, container, false);
        context = getActivity().getApplicationContext();

        ListView listView = (ListView) view.findViewById(R.id.listView_menu2);
        ListAdapter listAdapter = new ListAdapter();
        listView.setAdapter(listAdapter);
        listAdapter.list_clear();

        // DB에 등록된 채팅방 정보를 가져와 리스트에 추가
        myRef = database.getReference("chatroom").child(firebaseUser.getUid());
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> uidArrayList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    myRef = database.getReference("member").child("UserAccount").child(dataSnapshot.getKey());
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot2) {
                            listAdapter.addList(ContextCompat.getDrawable(context, R.drawable.baseimg), snapshot2.child("name").getValue(String.class));
                            uidArrayList.add(snapshot2.child("uid").getValue(String.class));
                            listAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }

                // 리스트 선택 시 해당 채팅방 입장
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("myUid", firebaseUser.getUid());
                        intent.putExtra("otherUid", uidArrayList.get(position));
                        startActivity(intent);
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        return view;
    }
}
