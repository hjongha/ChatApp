package com.example.chatapplication.main.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chatapplication.R;
import com.example.chatapplication.main.FndInfo;

import java.util.ArrayList;

public class ChatRoom_ListAdapter extends BaseAdapter {
    private ArrayList<ChatRoomData> chatRoomArrayList = new ArrayList<>();

    @Override
    public int getCount() {return chatRoomArrayList.size();}

    @Override
    public Object getItem(int position) {return chatRoomArrayList.get(position);}

    @Override
    public long getItemId(int position) {return 0;}

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.chatroomlist_item, parent, false);
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
        imageView.setImageBitmap(chatRoomArrayList.get(position).getFnd_image());
        TextView fnd_name = (TextView) convertView.findViewById(R.id.textView);
        fnd_name.setText(chatRoomArrayList.get(position).getFnd_name());
        TextView last_msg = (TextView) convertView.findViewById(R.id.textView_msg);
        last_msg.setText(chatRoomArrayList.get(position).getLast_msg());
        TextView last_time = (TextView) convertView.findViewById(R.id.text_chatroom_timer);
        last_time.setText(chatRoomArrayList.get(position).getLast_time());

        return convertView;
    }

    public void addList(Bitmap img, String name, String last_msg, String last_time) {
        ChatRoomData chatRoomData = new ChatRoomData();
        chatRoomData.setFnd_image(img);
        chatRoomData.setFnd_name(name);
        chatRoomData.setLast_msg(last_msg);
        chatRoomData.setLast_time(last_time);
        chatRoomArrayList.add(chatRoomData);
    }

    public void setListIndex(Bitmap img, String name, String last_msg, String last_time, int index) {
        ChatRoomData chatRoomData = new ChatRoomData();
        chatRoomData.setFnd_image(img);
        chatRoomData.setFnd_name(name);
        chatRoomData.setLast_msg(last_msg);
        chatRoomData.setLast_time(last_time);
        chatRoomArrayList.set(index, chatRoomData);
    }

    public void list_clear() {chatRoomArrayList.clear();} // 리스트 초기화
    public void remove(int i) {chatRoomArrayList.remove(i);} // 리스트 인덱스 i 삭제
}