package com.example.chatapplication.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.chatapplication.chat.ChatActivity;
import com.example.chatapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Fragment_FndList extends Fragment {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

    private View view;
    Context context;

    // MenuItem (우측 위 옵션메뉴)
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_item_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.searchF_item:
                intent = new Intent(context, Search_Friend.class);
                startActivity(intent);
                return true;

            case R.id.addF_item:
                intent = new Intent(context, Add_Friend.class);
                startActivity(intent);
                return true;

            case R.id.removeF_item:

                return true;
        }
        return false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        view = inflater.inflate(R.layout.fragment_fndlist, container, false);
        context = getActivity().getApplicationContext();

        ListView listView = (ListView) view.findViewById(R.id.listView_fndlist);
        ListAdapter listAdapter = new ListAdapter();
        listView.setAdapter(listAdapter);

        // DB에 저장된 내 정보 등록
        myRef = database.getReference("member").child("UserAccount").child(firebaseUser.getUid());
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ImageView imageView = (ImageView) view.findViewById(R.id.image_fndlist);
                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.baseimg));
                TextView textView = (TextView) view.findViewById(R.id.text_fndlist);
                textView.setText(snapshot.child("name").getValue(String.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // DB에 저장된 사용자의 친구 정보를 가져와 리스트에 추가
        myRef = database.getReference("friend").child(firebaseUser.getUid());
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> uidList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    listAdapter.addList(ContextCompat.getDrawable(context, R.drawable.baseimg), dataSnapshot.getValue(String.class));
                    uidList.add(dataSnapshot.getKey());
                }
                listAdapter.notifyDataSetChanged();

                // 리스트 선택 시 해당 사용자와의 채팅방 개설
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("myUid", firebaseUser.getUid());
                        intent.putExtra("otherUid", uidList.get(position));
                        startActivity(intent);
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        ListView listView = (ListView) view.findViewById(R.id.listView_fndlist);
        ListAdapter listAdapter = new ListAdapter();
        listView.setAdapter(listAdapter);

        // DB에 저장된 내 정보 등록
        myRef = database.getReference("member").child("UserAccount").child(firebaseUser.getUid());
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ImageView imageView = (ImageView) view.findViewById(R.id.image_fndlist);
                imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.baseimg));
                TextView textView = (TextView) view.findViewById(R.id.text_fndlist);
                textView.setText(snapshot.child("name").getValue(String.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // DB에 저장된 사용자의 친구 정보를 가져와 리스트에 추가
        myRef = database.getReference("friend").child(firebaseUser.getUid());
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> uidList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    listAdapter.addList(ContextCompat.getDrawable(context, R.drawable.baseimg), dataSnapshot.getValue(String.class));
                    uidList.add(dataSnapshot.getKey());
                }
                listAdapter.notifyDataSetChanged();

                // 리스트 선택 시 해당 사용자와의 채팅방 개설
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("myUid", firebaseUser.getUid());
                        intent.putExtra("otherUid", uidList.get(position));
                        startActivity(intent);
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}