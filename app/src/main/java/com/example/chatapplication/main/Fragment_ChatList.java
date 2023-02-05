package com.example.chatapplication.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.util.Collections;

public class Fragment_ChatList extends Fragment {
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;

    private View view;
    Context context;

    ProgressBar progressBar;

    ArrayList<String> uidArrayList = new ArrayList<>();
    ArrayList<String> chatList_name = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_chatlist, container, false);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        context = getActivity().getApplicationContext();
        uidArrayList.clear();
        chatList_name.clear();

        ListView listView = (ListView) view.findViewById(R.id.listView_menu2);
        ListAdapter listAdapter = new ListAdapter();
        listView.setAdapter(listAdapter);
        listAdapter.list_clear();

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);        // 로딩 바 활성화

        // DB에 등록된 채팅방 정보를 가져와 리스트에 추가
        myRef = database.getReference("chatroom").child(firebaseUser.getUid());
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> timerList = new ArrayList<>();
                Drawable drawable = getResources().getDrawable(R.drawable.baseimg);
                Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();

                // 각 채팅방의 마지막 채팅 시간을 timerList에 저장
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String timer_str = "";
                    for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                        timer_str = dataSnapshot2.getKey();
                    }
                    timerList.add(timer_str);
                    // listAdapter 개수 설정
                    listAdapter.addList(bitmap, "");
                }
                // timerList를 복사한 timerList2를 내림차순으로 정렬
                ArrayList<String> timerList2 = (ArrayList<String>) timerList.clone();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Collections.sort(timerList2, Collections.reverseOrder());
                }
                // 정렬된 timerList2와 timerList를 비교하여 기존 timerList 배열을 기준으로 내림차순의 인덱스를 순서대로 sort_Index 배열에 저장
                // ex) timerList의 최신 순이 3 2 4 1 인 경우 timerList2는 1 2 3 4 로 정렬됨. -> 비교하여 인덱스 값 그대로 sort_Index에 3 2 4 1 을 저장
                ArrayList<Integer> sort_Index = new ArrayList<>();
                for (int i=0; i<timerList.size(); i++) {
                    for (int j=0; j<timerList2.size(); j++) {
                        if (timerList2.get(j).equals(timerList.get(i))) {
                            if (!sort_Index.contains(j)) {
                                sort_Index.add(j);
                                break;
                            }
                        }
                    }
                }

                int sort_pos = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    myRef = database.getReference("member").child("UserAccount").child(dataSnapshot.getKey()).child("name");
                    int finalSort_pos = sort_pos;
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot2) {
                            // sort_Index를 이용하여 채팅방 리스트를 내림차순으로 수정
                            listAdapter.setListIndex(bitmap, snapshot2.getValue(String.class), sort_Index.get(finalSort_pos));
                            uidArrayList.add(dataSnapshot.getKey());
                            listAdapter.notifyDataSetChanged();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                    sort_pos++;
                }

                // 리스트 선택 시 해당 채팅방 입장
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("otherUid", uidArrayList.get(position));
                        startActivity(intent);
                    }
                });
                progressBar.setVisibility(View.GONE);       // 로딩 바 비활성화
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
