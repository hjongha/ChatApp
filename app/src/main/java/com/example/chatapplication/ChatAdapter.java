package com.example.chatapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private ArrayList<ChatData> chatDataArrayList = new ArrayList<>();

    // ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.textView_chat);
        }
        public TextView getTextView() {
            return textView;
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
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {
        holder.getTextView().setText(chatDataArrayList.get(position).getMsg());
    }

    @Override
    public int getItemCount() {
        return chatDataArrayList.size();
    }
}
