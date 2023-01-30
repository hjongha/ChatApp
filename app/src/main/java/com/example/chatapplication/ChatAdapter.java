package com.example.chatapplication;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private ArrayList<ChatData> chatDataArrayList = new ArrayList<>();
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

    // ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final TextView textView_timer;
        private final TextView textView_day;
        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.textView_chat);
            textView_timer = view.findViewById(R.id.timer_chat);
            textView_day = view.findViewById(R.id.day_chat);
        }
        public TextView getTextView() {
            return textView;
        }
        public TextView getTextView_timer() {
            return textView_timer;
        }
        public TextView getTextView_day() {
            return textView_day;
        }
    }

    // Adapter 데이터 초기화
    public ChatAdapter(ArrayList<ChatData> dataSet) {
        chatDataArrayList = dataSet;
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 새로운 뷰 생성
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.text_view, parent, false);
        // 전송자가 본인인 경우 우측 텍스트 뷰 사용
        if (viewType == 1) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.text_view_right, parent, false);
        }
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {
        String[] timer_split = new String[2];
        timer_split = chatDataArrayList.get(position).getTimer().split(" ");
        if (position == 0) {
            // 첫 채팅일 경우
            holder.getTextView_day().setVisibility(View.VISIBLE);
            holder.getTextView_day().setHeight(90);
            holder.getTextView_day().setText(timer_split[0]);
        }
        else {
            String[] ago_timer = new String[2];
            ago_timer = chatDataArrayList.get(position-1).getTimer().split(" ");
            if (timer_split[0].equals(ago_timer[0])) {
                // 현 데이터의 년월일이 전 데이터와 같다면
                holder.getTextView_day().setVisibility(View.INVISIBLE);
                holder.getTextView_day().setHeight(1);
            }
            else {
                // 현 데이터의 년월일이 전 데이터와 다르다면
                holder.getTextView_day().setVisibility(View.VISIBLE);
                holder.getTextView_day().setHeight(90);
                holder.getTextView_day().setText(timer_split[0]);
            }
        }
        // 초 단위 자르기
        String timer = timer_split[1].substring(0, timer_split[1].length()-3);
        // 메시지를 보낸 시간
        holder.getTextView_timer().setText(timer);
        // 메시지 내용
        holder.getTextView().setText(chatDataArrayList.get(position).getMsg());
    }

    @Override
    public int getItemViewType(int position) {
        // 전송자가 본인인 경우
        if (chatDataArrayList.get(position).getUid().equals(firebaseUser.getUid())) {
            return 1;
        }
        else {
            return 2;
        }
    }

    @Override
    public int getItemCount() {
        return chatDataArrayList.size();
    }
}
