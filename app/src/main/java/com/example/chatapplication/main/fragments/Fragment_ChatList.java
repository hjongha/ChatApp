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
        progressBar.setVisibility(View.VISIBLE);        // ?????? ??? ?????????

        // DB??? ????????? ????????? ????????? ????????? ???????????? ??????
        myRef = database.getReference("chatroom").child(firebaseUser.getUid());
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> timerList = new ArrayList<>();
                ArrayList<String> lastmsgList = new ArrayList<>();
                Drawable drawable = getResources().getDrawable(R.drawable.baseimg);
                Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();

                // ??? ???????????? ????????? ?????? ????????? timerList??? ??????, ????????? ???????????? lastmsgLst??? ??????
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String timer_str = "";
                    String last_msg = "";
                    for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                        timer_str = dataSnapshot2.getKey();
                        // ????????? ???????????? ?????? ?????? ???????????? ???????????? last_msg??? ??????
                        if (!dataSnapshot2.child("msg").getValue(String.class).equals("")) {
                            last_msg = dataSnapshot2.child("msg").getValue(String.class);
                        }
                    }
                    timerList.add(timer_str);
                    lastmsgList.add(last_msg);
                    // listAdapter ?????? ??????
                    listAdapter.addList(bitmap, "", "", "");
                    uidArrayList.add("");
                }
                // timerList??? ????????? timerList2??? ?????????????????? ??????
                ArrayList<String> timerList2 = (ArrayList<String>) timerList.clone();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Collections.sort(timerList2, Collections.reverseOrder());
                }
                // ????????? timerList2??? timerList??? ???????????? ?????? timerList ????????? ???????????? ??????????????? ???????????? ???????????? sort_Index ????????? ??????
                // ex) timerList??? ?????? ?????? 3 2 4 1 ??? ?????? timerList2??? 1 2 3 4 ??? ?????????. -> ???????????? ????????? ??? ????????? sort_Index??? 3 2 4 1 ??? ??????
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

                // ???????????? ???????????? ??????????????? ??????
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
                            if (!timerList.get(finalSort_pos).equals("")) {
                                if (dateTime.equals(timer_split[0])) {
                                    // ??????????????? ????????? ???????????? ????????? ????????? ?????? ??????
                                    last_time = timer_split[1].substring(0, timer_split[1].length() - 3);
                                } else {
                                    // ??????????????? ????????? ???????????? ????????? ????????? ?????? ??????
                                    last_time = timer_split[0].substring(5);
                                }
                            }

                            // sort_Index??? ???????????? ????????? ???????????? ?????????????????? ??????
                            listAdapter.setListIndex(bitmap, snapshot2.getValue(String.class), lastmsgList.get(finalSort_pos), last_time, sort_Index.get(finalSort_pos));
                            uidArrayList.set(sort_Index.get(finalSort_pos), dataSnapshot.getKey());
                            listAdapter.notifyDataSetChanged();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                    sort_pos++;
                }

                // ????????? ?????? ??? ?????? ????????? ??????
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("otherUid", uidArrayList.get(position));
                        startActivity(intent);
                    }
                });
                progressBar.setVisibility(View.GONE);       // ?????? ??? ????????????
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
