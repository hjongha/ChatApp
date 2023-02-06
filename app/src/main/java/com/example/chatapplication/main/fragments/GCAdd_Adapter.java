package com.example.chatapplication.main.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.example.chatapplication.R;
import com.example.chatapplication.main.FndInfo;

import java.util.ArrayList;

public class GCAdd_Adapter extends BaseAdapter {
    private ArrayList<GCAdd_FndInfo> fndInfoArrayList = new ArrayList<>();

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
        Drawable drawable = convertView.getResources().getDrawable(R.drawable.ic_baseline_check_box_outline_blank_24);
        imageView2.setImageDrawable(drawable);

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