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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.chatapplication.R;
import com.example.chatapplication.chat.GroupChatActivity;
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
import java.util.Hashtable;

public class Fragment_GroupChat  extends Fragment {
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;

    private View view;
    Context context;

    ProgressBar progressBar;

    ArrayList<String> roomArrayList = new ArrayList<>();
    ArrayList<String> chatList_name = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_groupchat, container, false);
        context = getActivity().getApplicationContext();

        ImageButton imageButton = (ImageButton) view.findViewById(R.id.groupchat_imageBtn);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, GroupChat_Add.class);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        context = getActivity().getApplicationContext();
        roomArrayList.clear();
        chatList_name.clear();

        ListView listView = (ListView) view.findViewById(R.id.listView_menu2);
        ChatRoom_ListAdapter listAdapter = new ChatRoom_ListAdapter();
        listView.setAdapter(listAdapter);
        listAdapter.list_clear();

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);        // 로딩 바 활성화

        myRef = database.getReference("groupchatroom").child(firebaseUser.getUid());
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> groupRoomKey_arr = new ArrayList<>();
                ArrayList<String> timerList = new ArrayList<>();
                ArrayList<String> lastmsgList = new ArrayList<>();

                Drawable drawable = getResources().getDrawable(R.drawable.groupimg);
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    groupRoomKey_arr.add(dataSnapshot.getKey());
                    String timer_str = "";
                    String last_msg = "";
                    // 채팅방의 마지막 채팅 시간을 timerList에 저장
                    for (DataSnapshot dataSnapshot2 : dataSnapshot.child("msg").getChildren()) {
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
                    roomArrayList.add("");
                }

                // timerList를 복사한 timerList2를 내림차순으로 정렬
                ArrayList<String> timerList2 = (ArrayList<String>) timerList.clone();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Collections.sort(timerList2, Collections.reverseOrder());
                }
                // 정렬된 timerList2와 timerList를 비교하여 기존 timerList 배열을 기준으로 내림차순의 인덱스를 순서대로 sort_Index 배열에 저장
                // ex) timerList의 최신 순이 3 2 4 1 인 경우 timerList2는 1 2 3 4 로 정렬됨. -> 비교하여 인덱스 값 그대로 sort_Index에 3 2 4 1 을 저장
                ArrayList<Integer> sort_Index = new ArrayList<>();
                for (int i = 0; i < timerList.size(); i++) {
                    for (int j = 0; j < timerList2.size(); j++) {
                        if (timerList2.get(j).equals(timerList.get(i))) {
                            if (!sort_Index.contains(j)) {
                                sort_Index.add(j);
                                break;
                            }
                        }
                    }
                }

                myRef = database.getReference("member").child("UserAccount");
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot2) {
                        Hashtable<String, String> Hash_user = new Hashtable<String, String>();
                        for (DataSnapshot dataSnapshot : snapshot2.getChildren()) {
                            // 사용자의 Uid - name 데이터 저장
                            Hash_user.put(dataSnapshot.getKey(), dataSnapshot.child("name").getValue(String.class));
                        }

                        for (int i=0; i<groupRoomKey_arr.size(); i++) {
                            myRef = database.getReference("groupchatroom").child(firebaseUser.getUid()).child(groupRoomKey_arr.get(i)).child("user");
                            int finalI = i;
                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    // 사용자 이름, 이름, 이름 ... 으로 단체 채팅방 이름을 설정
                                    String userName_Room = "";
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        userName_Room += Hash_user.get(dataSnapshot.getKey()) + ", ";
                                    }
                                    userName_Room = userName_Room.substring(0, userName_Room.length()-2);

                                    String last_time ="";
                                    String[] timer_split = new String[2];
                                    timer_split = timerList.get(finalI).split(" ");

                                    Calendar calendar = Calendar.getInstance();
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                    String dateTime = dateFormat.format(calendar.getTime());
                                    if (!timerList.get(finalI).equals("")) {
                                        if (dateTime.equals(timer_split[0])) {
                                            // 마지막으로 전송한 메시지의 날짜가 오늘과 같은 경우
                                            last_time = timer_split[1].substring(0, timer_split[1].length() - 3);
                                        }
                                        else {
                                            // 마지막으로 전송한 메시지의 날짜가 오늘과 다른 경우
                                            last_time = timer_split[0].substring(5);
                                        }
                                    }

                                    // sort_Index를 이용하여 채팅방 리스트를 내림차순으로 수정
                                    listAdapter.setListIndex(bitmap, userName_Room, lastmsgList.get(finalI), last_time, sort_Index.get(finalI));
                                    roomArrayList.set(sort_Index.get(finalI), groupRoomKey_arr.get(finalI));
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

                // 리스트 선택 시 해당 채팅방 입장
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(context, GroupChatActivity.class);
                        intent.putExtra("roomKey", roomArrayList.get(position));
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
