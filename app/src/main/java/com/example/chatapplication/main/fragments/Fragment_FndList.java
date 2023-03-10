package com.example.chatapplication.main.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.chatapplication.chat.ChatActivity;
import com.example.chatapplication.R;
import com.example.chatapplication.main.Add_Friend;
import com.example.chatapplication.main.ListAdapter;
import com.example.chatapplication.main.Remove_Friend;
import com.example.chatapplication.main.Search_Friend;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;

public class Fragment_FndList extends Fragment {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

    private View view;
    Context context;

    ProgressBar progressBar;

    StorageReference storageRef;
    File localFile;

    // MenuItem (?????? ??? ????????????)
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_item_menu, menu);
    }

    // MenuItem (????????????) ?????? ??? ??? ???????????? ??????
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
                intent = new Intent(context, Remove_Friend.class);
                startActivity(intent);
                return true;
        }
        return false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        view = inflater.inflate(R.layout.fragment_fndlist, container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        context = getActivity().getApplicationContext();
        storageRef = FirebaseStorage.getInstance().getReference();

        ListView listView = (ListView) view.findViewById(R.id.listView_fndlist);
        ListAdapter listAdapter = new ListAdapter();
        listView.setAdapter(listAdapter);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);        // ?????? ??? ?????????

        // ?????? ?????? ????????????
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

        // DB??? ????????? ??? ?????? ??????
        myRef = database.getReference("member").child("UserAccount").child(firebaseUser.getUid());
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ImageView imageView = (ImageView) view.findViewById(R.id.image_fndlist);
                TextView textView = (TextView) view.findViewById(R.id.text_fndlist);
                textView.setText(snapshot.child("name").getValue(String.class));

                // ?????????????????? storage?????? ????????? uid??? ???????????? ????????? ??????
                try {
                    localFile = File.createTempFile("images", "jpg");
                    StorageReference mountainImagesRef = storageRef.child("users").child(firebaseUser.getUid()).child("profile.jpg");
                    mountainImagesRef.getFile(localFile)
                            .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    // ???????????? ????????? ?????? ?????? ???????????? ??????
                                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                    imageView.setImageBitmap(bitmap);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // ???????????? ????????? ?????? ?????? ?????????
                                    imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.baseimg));
                                }
                            });
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // DB??? ????????? ???????????? ?????? ????????? ????????? ???????????? ??????
        myRef = database.getReference("friend").child(firebaseUser.getUid());
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> uidList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    myRef = database.getReference("member").child("UserAccount").child(dataSnapshot.getKey()).child("name");
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot2) {
                            // ?????????????????? storage?????? ????????? uid??? ???????????? ????????? ??????
                            /*
                            try {
                                localFile = File.createTempFile("images", "jpg");
                                StorageReference mountainImagesRef = storageRef.child("users").child(dataSnapshot.getKey()).child("profile.jpg");
                                mountainImagesRef.getFile(localFile)
                                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                // ???????????? ????????? ?????? ?????? ???????????? ??????
                                                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                                listAdapter.addList(bitmap, snapshot2.getValue(String.class));
                                                uidList.add(dataSnapshot.getKey());
                                                listAdapter.notifyDataSetChanged();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // ???????????? ????????? ?????? ?????? ?????????
                                                Toast.makeText(context, "????????? ???????????? ???????????????.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                            */
                            Drawable drawable = getResources().getDrawable(R.drawable.baseimg);
                            Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
                            listAdapter.addList(bitmap, dataSnapshot.getValue(String.class));
                            uidList.add(dataSnapshot.getKey());
                            listAdapter.notifyDataSetChanged();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
                // ????????? ?????? ??? ?????? ??????????????? ????????? ??????
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("myUid", firebaseUser.getUid());
                        intent.putExtra("otherUid", uidList.get(position));
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