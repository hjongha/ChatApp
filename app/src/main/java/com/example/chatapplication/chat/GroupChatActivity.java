package com.example.chatapplication.chat;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.R;
import com.example.chatapplication.main.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;

public class GroupChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    ArrayList<ChatData> chatDataArrayList;
    ChatAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

    NotificationManager notificationManager;

    String roomKey;
    String myUid;

    String serviceInfo_str;

    // MenuItem (?????? ??? ????????????)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.item_menu, menu);
        menu.findItem(R.id.gc_user).setVisible(true);
        return true;
    }

    // MenuItem (?????? ??? ????????????) ?????? ?????????
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        AlertDialog.Builder dlg = new AlertDialog.Builder(GroupChatActivity.this);
        switch (item.getItemId()) {
            case R.id.delete_msg:
                // ?????? ?????? ??????
                dlg.setTitle("?????? ?????? ?????? ??????");
                dlg.setMessage("?????? ????????? ?????????????????????????");
                dlg.setPositiveButton("?????????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(GroupChatActivity.this, "?????????????????????.", Toast.LENGTH_SHORT).show();
                    }
                });
                dlg.setNegativeButton("???", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        myRef = database.getReference("groupchatroom").child(myUid).child(roomKey).child("msg");
                        myRef.setValue("");
                        Toast.makeText(GroupChatActivity.this, "?????? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                    }
                });
                dlg.show();
                return true;

            case R.id.delete_room:
                // ????????? ?????????
                dlg.setTitle("????????? ????????? ??????");
                dlg.setMessage("?????? ???????????? ??????????????????????\n?????? ????????? ?????? ????????? ???????????????.");
                dlg.setPositiveButton("?????????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(GroupChatActivity.this, "?????????????????????.", Toast.LENGTH_SHORT).show();
                    }
                });
                dlg.setNegativeButton("???", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String dateTime = dateFormat.format(calendar.getTime());

                        Hashtable<String, String> talks = new Hashtable<String, String>();
                        talks.put("uid", myUid);
                        talks.put("msg", "");

                        myRef = database.getReference("groupchatroom").child(myUid).child(roomKey).child("user");
                        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    // ?????? ????????? ???????????? ????????? ?????? ?????? ???????????? ?????? ??? DB ??????
                                    myRef = database.getReference("groupchatroom").child(dataSnapshot.getKey()).child(roomKey).child("msg");
                                    myRef.child(dateTime).setValue(talks);
                                    myRef = database.getReference("groupchatroom").child(dataSnapshot.getKey()).child(roomKey).child("user").child(myUid);
                                    myRef.removeValue();

                                    myRef = database.getReference("groupchatroom").child(myUid).child(roomKey);
                                    myRef.removeValue();
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                        Toast.makeText(GroupChatActivity.this, "???????????? ????????????.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                dlg.show();
                return true;

            case R.id.gc_user:
                Intent intent = new Intent(GroupChatActivity.this, GroupChat_UserList.class);
                intent.putExtra("roomKey", roomKey);
                startActivity(intent);
                return true;

        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        TextView textView = (TextView) findViewById(R.id.text_test);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setWillNotDraw(false); // ???????????? ??????

        chatDataArrayList = new ArrayList<>();
        mAdapter = new ChatAdapter(chatDataArrayList);
        recyclerView.setAdapter(mAdapter);
        chatDataArrayList.clear();

        myUid = firebaseUser.getUid();         // ??? UID
        roomKey = getIntent().getStringExtra("roomKey");   // ??? Key???

        // ????????? ?????? ??????
        myRef = database.getReference("groupchatroom").child(myUid).child(roomKey).child("user");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int user_count = 1;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    int finalUser_count = user_count;
                    myRef = database.getReference("member").child("UserAccount").child(dataSnapshot.getKey()).child("name");
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (finalUser_count != 1) {
                                textView.append(", ");
                            }
                            textView.append(snapshot.getValue(String.class));
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                    user_count++;
                }
                textView.append("("+Integer.toString(user_count)+") ");
                ImageView imageView = (ImageView) findViewById(R.id.image_chat);
                imageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_groups_24));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // ?????????(Key???) ?????? ??? ????????????
        Button btn_send = (Button) findViewById(R.id.btn_send);
        EditText editText = (EditText) findViewById(R.id.editText);

        // ?????? ?????? ?????????
        btn_set(btn_send, editText);

        // ?????? ??????(DB ??????) ??? ????????? ??????
        myRef = database.getReference("groupchatroom").child(myUid).child(roomKey).child("msg");
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ChatData chatData = new ChatData();
                chatData.setMsg(snapshot.child("msg").getValue(String.class));
                chatData.setUid(snapshot.child("uid").getValue(String.class));
                chatData.setTimer(snapshot.getKey());

                chatDataArrayList.add(chatData);
                mAdapter.notifyDataSetChanged();

                // ????????? ???????????? ?????????
                recyclerView.scrollToPosition(chatDataArrayList.size() - 1);
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        myRef.addChildEventListener(childEventListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // baseActivity??? MainActivity??? ?????? ??????
        if ("com.example.chatapplication.chat.ChatActivity".equals(serviceInfo_str)) {
            // ChatActivity??? ???????????? MainActivity??? ??????
            Intent intent = new Intent(GroupChatActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ????????? ?????? ??? ?????? ?????? ??????
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(0);

        // ?????? ?????? ?????? baseActivity??? ???????????? ?????? (?????? ????????? ?????? ChatActivity??? ???????????? MainActivity??? ???????????? ????????? ??????)
        ActivityManager activityManager = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningTaskInfo serviceInfo : activityManager.getRunningTasks(Integer.MAX_VALUE)) {
            serviceInfo_str = serviceInfo.baseActivity.getClassName();
        }
    }

    // ?????? ?????? ??????
    public void btn_set(Button btn, EditText editText) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String edit_str = editText.getText().toString();
                if (!edit_str.equals("")) {
                    Calendar calendar = Calendar.getInstance();
                    // ??????
                    // calendar.add(Calendar.HOUR_OF_DAY, 9);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String dateTime = dateFormat.format(calendar.getTime());

                    Hashtable<String, String> talks = new Hashtable<String, String>();
                    talks.put("uid", myUid);
                    talks.put("msg", edit_str);

                    myRef = database.getReference("groupchatroom").child(myUid).child(roomKey).child("user");
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                // ?????? ????????? ?????? ?????? ?????? ????????? ?????? ????????? DB ??????
                                myRef = database.getReference("groupchatroom").child(dataSnapshot.getKey()).child(roomKey).child("msg");
                                myRef.child(dateTime).setValue(talks);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                    myRef = database.getReference("groupchatroom").child(myUid).child(roomKey).child("msg");
                    myRef.child(dateTime).setValue(talks);
                    editText.setText("");
                }
            }
        });
    }

    public String getRoomKey() {
        return roomKey;
    }
}
