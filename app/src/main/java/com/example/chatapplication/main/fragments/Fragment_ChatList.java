package com.example.chatapplication.main.fragments;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
        ChatRoom_ListAdapter listAdapter = new ChatRoom_ListAdapter();
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
                ArrayList<String> lastmsgList = new ArrayList<>();
                Drawable drawable = getResources().getDrawable(R.drawable.baseimg);
                Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();

                // 각 채팅방의 마지막 채팅 시간을 timerList에 저장, 마지막 메시지를 lastmsgLst에 저장
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String timer_str = "";
                    String last_msg = "";
                    for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                        timer_str = dataSnapshot2.getKey();
                        // 마지막 메시지가 방을 나간 메시지가 아니라면 last_msg에 저장
                        if (!dataSnapshot2.child("msg").getValue(String.class).equals("")) {
                            last_msg = dataSnapshot2.child("msg").getValue(String.class);
                        }
                    }
                    timerList.add(timer_str);
                    lastmsgList.add(last_msg);
                    // listAdapter 개수 설정
                    listAdapter.addList(bitmap, "", "", "");
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

                // 채팅방을 리스트에 최신순으로 추가
                int sort_pos = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    myRef = database.getReference("member").child("UserAccount").child(dataSnapshot.getKey()).child("name");
                    int finalSort_pos = sort_pos;
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot2) {
                            String last_time ="";
                            String[] timer_split = new String[2];
                            timer_split = timerList.get(finalSort_pos).split(" ");

                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            String dateTime = dateFormat.format(calendar.getTime());
                            if (dateTime.equals(timer_split[0])) {
                                // 마지막으로 전송한 메시지의 날짜가 오늘과 같은 경우
                                last_time = timer_split[1].substring(0, timer_split[1].length()-3);
                            }
                            else {
                                // 마지막으로 전송한 메시지의 날짜가 오늘과 다른 경우
                                last_time = timer_split[0].substring(5);
                            }

                            // sort_Index를 이용하여 채팅방 리스트를 내림차순으로 수정
                            listAdapter.setListIndex(bitmap, snapshot2.getValue(String.class), lastmsgList.get(finalSort_pos), last_time, sort_Index.get(finalSort_pos));
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
