package com.example.chatapplication.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapplication.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SearchActivity extends AppCompatActivity {
    private EditText searchEmail, searchAnswer;
    private DatabaseReference myRef;
    Boolean search_bool = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchEmail = (EditText) findViewById(R.id.search_id);
        searchAnswer = (EditText) findViewById(R.id.search_answer);
        Button search_btn = (Button) findViewById(R.id.btn_search);
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strEmail = searchEmail.getText().toString();
                String strAnswer = searchAnswer.getText().toString();

                myRef = FirebaseDatabase.getInstance().getReference("member").child("UserAccount");
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            UserAccount userAccount = new UserAccount();
                            userAccount = dataSnapshot.getValue(UserAccount.class);

                            // 등록된 Email과 답변이 일치하는지 확인
                            if (userAccount.getEmail().equals(strEmail)) {
                                if (userAccount.getAuth_answer().equals(strAnswer)) {
                                    search_bool = true;
                                    Intent intent = new Intent(SearchActivity.this, ModifyPasswd.class);
                                    intent.putExtra("uid", userAccount.getUid());
                                    startActivity(intent);
                                    finish();
                                    break;
                                }
                            }
                        }
                        if (!search_bool) {
                            Toast.makeText(SearchActivity.this, "입력한 정보가 맞지 않습니다.\n다시 확인해주세요.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
        });
    }
}
