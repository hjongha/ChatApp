package com.example.chatapplication.main;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

import com.example.chatapplication.R;
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

                // Email??? ?????? ?????? ?????? ??? ???????????? ??????
                myRef = database.getReference("member").child("UserAccount");
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<String> uidList = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if (dataSnapshot.child("email").getValue(String.class).equals(str_Email)) {
                                Drawable drawable = getResources().getDrawable(R.drawable.baseimg);
                                Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
                                listAdapter.addList(bitmap, dataSnapshot.child("name").getValue(String.class));
                                uidList.add(dataSnapshot.getKey());
                                listAdapter.notifyDataSetChanged();
                            }
                        }
                        // ????????? ?????? ??? ?????? ?????? ????????? ??????
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                AlertDialog.Builder dlg = new AlertDialog.Builder(Add_Friend.this);

                                dlg.setTitle("?????? ?????? ??????");
                                dlg.setMessage("?????? ????????? ?????????????????????????");
                                dlg.setPositiveButton("?????????", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Toast.makeText(Add_Friend.this, "?????????????????????.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                dlg.setNegativeButton("???", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        myRef = database.getReference("friend").child(firebaseUser.getUid());
                                        myRef.child(uidList.get(position)).setValue(" ");
                                        Toast.makeText(Add_Friend.this, "?????? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
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
