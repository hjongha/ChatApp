package com.example.chatapplication.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chatapplication.R;

import java.util.ArrayList;

public class ListAdapter extends BaseAdapter {
    private ArrayList<FndInfo> fndInfoArrayList = new ArrayList<>();

    @Override
    public int getCount() {return fndInfoArrayList.size();}

    @Override
    public Object getItem(int position) {return fndInfoArrayList.get(position);}

    @Override
    public long getItemId(int position) {return 0;}

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item, parent, false);
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
        imageView.setImageBitmap(fndInfoArrayList.get(position).getFnd_image());
        TextView fnd_name = (TextView) convertView.findViewById(R.id.textView);
        fnd_name.setText(fndInfoArrayList.get(position).getFnd_name());

        return convertView;
    }

    public void addList(Bitmap img, String name) {
        FndInfo fndInfo = new FndInfo();
        fndInfo.setFnd_image(img);
        fndInfo.setFnd_name(name);
        fndInfoArrayList.add(fndInfo);
    }

    public void list_clear() {fndInfoArrayList.clear();} // 리스트 초기화
    public void remove(int i) {fndInfoArrayList.remove(i);} // 리스트 인덱스 i 삭제
}