package com.example.chatapplication.chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.chatapplication.R;
import com.example.chatapplication.main.fragments.GCAdd_FndInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GCUserList_ListAdapter extends BaseAdapter {
    private ArrayList<GCAdd_FndInfo> fndInfoArrayList = new ArrayList<>();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

    @Override
    public int getCount() {return fndInfoArrayList.size();}

    @Override
    public GCAdd_FndInfo getItem(int position) {return fndInfoArrayList.get(position);}

    @Override
    public long getItemId(int position) {return 0;}

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.grouplist_item, parent, false);
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView_GCAdd);
        imageView.setImageBitmap(fndInfoArrayList.get(position).getFnd_image());
        TextView fnd_name = (TextView) convertView.findViewById(R.id.textView_GCAdd);
        fnd_name.setText(fndInfoArrayList.get(position).getFnd_name());
        ImageView imageView2 = (ImageView) convertView.findViewById(R.id.imagecheck_GCAdd);

        // 해당 친구 데이터가 이미 친구 상태인지 확인
        myRef = database.getReference("friend").child(firebaseUser.getUid());
        View finalConvertView = convertView;
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot1) {
                Boolean fnd_bool = false;   // 친구 상태 확인
                for (DataSnapshot dataSnapshot1 : snapshot1.getChildren()) {
                    if (fndInfoArrayList.get(position).getFnd_uid().equals(dataSnapshot1.getKey())) {
                        fnd_bool = true;
                        break;
                    }
                }
                // 해당 사용자와 친구 상태가 아니라면 친구 추가 아이콘 추가 및 클릭 리스너 추가
                if (!fnd_bool) {
                    Drawable drawable = finalConvertView.getResources().getDrawable(R.drawable.ic_baseline_person_add_alt_1_24);
                    imageView2.setImageDrawable(drawable);
                    imageView2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            myRef = database.getReference("friend").child(firebaseUser.getUid()).child(fndInfoArrayList.get(position).getFnd_uid());
                            myRef.setValue(fndInfoArrayList.get(position).getFnd_name());

                            // 친구 추가 후 설정 초기화
                            imageView2.setImageDrawable(null);
                            imageView2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Toast.makeText(context, "친구 추가가 완료되었습니다.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        return convertView;
    }

    public void addList(Bitmap img, String name, String uid) {
        GCAdd_FndInfo gcAdd_fndInfo = new GCAdd_FndInfo();
        gcAdd_fndInfo.setFnd_image(img);
        gcAdd_fndInfo.setFnd_name(name);
        gcAdd_fndInfo.setFnd_uid(uid);
        gcAdd_fndInfo.setCheck_bool(false);
        fndInfoArrayList.add(gcAdd_fndInfo);
    }

    public void list_clear() {fndInfoArrayList.clear();} // 리스트 초기화
    public void remove(int i) {fndInfoArrayList.remove(i);} // 리스트 인덱스 i 삭제
}