package com.example.chatapplication.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private ArrayList<ChatData> chatDataArrayList = new ArrayList<>();
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;

    // ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final TextView textView_timer;
        private final TextView textView_day;
        private final TextView textView_name;
        private final ImageView imageView;
        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.textView_chat);
            textView_timer = view.findViewById(R.id.timer_chat);
            textView_day = view.findViewById(R.id.day_chat);
            textView_name = view.findViewById(R.id.text_name);
            imageView = view.findViewById(R.id.text_image);
        }
        public TextView getTextView() {return textView;}
        public TextView getTextView_timer() {return textView_timer;}
        public TextView getTextView_day() {return textView_day;}
        public TextView getTextView_name() {return textView_name;}
        public ImageView getImageView() {return imageView;}
    }

    // Adapter ????????? ?????????
    public ChatAdapter(ArrayList<ChatData> dataSet) {
        chatDataArrayList = dataSet;
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ????????? ??? ??????
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.text_view, parent, false);
        // ???????????? ????????? ?????? ?????? ????????? ??? ??????
        if (viewType == 1) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.text_view_right, parent, false);
        }
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {
        if (chatDataArrayList.get(position).getMsg().equals("")) {
            // ???????????? ?????? ??????
            holder.getTextView_day().setVisibility(View.VISIBLE);
            holder.getTextView_day().setHeight(90);
            holder.getTextView().setHeight(0);
            holder.getTextView_name().setHeight(0);
            holder.getImageView().setVisibility(View.INVISIBLE);
            holder.getImageView().setMaxHeight(0);
            holder.getTextView_timer().setHeight(0);
            myRef = database.getReference("member").child("UserAccount").child(chatDataArrayList.get(position).getUid()).child("name");
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // ????????? name
                    holder.getTextView_day().setText(snapshot.getValue(String.class) + "?????? ?????? ???????????????.");
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
        else {
            String[] timer_split = new String[2];
            timer_split = chatDataArrayList.get(position).getTimer().split(" ");
            if (position == 0) {
                // ??? ????????? ??????
                holder.getTextView_day().setVisibility(View.VISIBLE);
                holder.getTextView_day().setHeight(90);
                holder.getTextView_day().setText(timer_split[0]);
            } else {
                String[] ago_timer = new String[2];
                ago_timer = chatDataArrayList.get(position - 1).getTimer().split(" ");
                if (timer_split[0].equals(ago_timer[0])) {
                    // ??? ???????????? ???????????? ??? ???????????? ?????????
                    holder.getTextView_day().setVisibility(View.INVISIBLE);
                    holder.getTextView_day().setHeight(1);

                    String now_timer_hm = timer_split[1].substring(0, timer_split[1].length() - 3);
                    String ago_timer_hm = ago_timer[1].substring(0, timer_split[1].length() - 3);
                    if (chatDataArrayList.get(position).getUid().equals(chatDataArrayList.get(position - 1).getUid()) && now_timer_hm.equals(ago_timer_hm)) {
                        // ??? ???????????? ???, ?????? ??? ???????????? ?????? ?????? ???????????? ?????????
                        holder.getImageView().setVisibility(View.INVISIBLE);
                        holder.getTextView_name().setHeight(0);
                    }
                } else {
                    // ??? ???????????? ???????????? ??? ???????????? ????????????
                    holder.getTextView_day().setText(timer_split[0]);
                    holder.getTextView_day().setVisibility(View.VISIBLE);
                    holder.getTextView_day().setHeight(90);
                }
            }

            // ??? ?????? ?????????
            String timer = timer_split[1].substring(0, timer_split[1].length() - 3);
            // ???????????? ?????? ??????
            holder.getTextView_timer().setText(timer);
            // ????????? ??????
            holder.getTextView().setText(chatDataArrayList.get(position).getMsg());
            // ????????? name
            myRef = database.getReference("member").child("UserAccount").child(chatDataArrayList.get(position).getUid()).child("name");
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    holder.getTextView_name().setText(snapshot.getValue(String.class));
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        // ???????????? ????????? ??????
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
