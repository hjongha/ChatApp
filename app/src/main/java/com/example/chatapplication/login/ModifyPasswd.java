package com.example.chatapplication.login;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapplication.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ModifyPasswd extends AppCompatActivity {
    private DatabaseReference myRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.passwd_modify);

        String myUid = getIntent().getStringExtra("uid");

        EditText editText = (EditText) findViewById(R.id.et_modifyPasswd);
        Button button = (Button) findViewById(R.id.btn_modify);
        // 버튼 클릭 시 리스너
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 알림 확인 창 출력
                AlertDialog.Builder dlg = new AlertDialog.Builder(ModifyPasswd.this);

                dlg.setTitle("비밀번호 변경 확인");
                dlg.setMessage("입력하신 비밀번호로 변경하시겠습니까?");
                dlg.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(ModifyPasswd.this, "취소하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
                dlg.setNegativeButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        myRef = FirebaseDatabase.getInstance().getReference("member").child("UserAccount").child(myUid).child("password");
                        myRef.setValue(editText.getText().toString());

                        Toast.makeText(ModifyPasswd.this, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ModifyPasswd.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
                dlg.show();
            }
        });
    }
}
