package com.naikajsevak98.volly_example.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.naikajsevak98.volly_example.R;
import com.naikajsevak98.volly_example.activity.ChatsActivity;
import com.naikajsevak98.volly_example.models.Users;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolderClass> {

    ArrayList<Users> list;
    Context context;


    public UserAdapter(ArrayList<Users> list, Context context) {
        this.list = list;
        this.context = context;
    }


    @NonNull
    public ViewHolderClass onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout, parent, false);
        return new ViewHolderClass(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderClass holder, int position) {
        Users users = list.get(position);

        String senderId = FirebaseAuth.getInstance().getUid();

        String senderRoom = senderId + users.getUserId();
try {
    FirebaseDatabase.getInstance().getReference()
            .child("Chats").child(senderRoom).
            addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    TextView last, LastTime;
                    last = holder.itemView.findViewById(R.id.last_msg);
                    LastTime = holder.itemView.findViewById(R.id.time);
                    if (snapshot.exists()) {
                        String lastMsg = snapshot.child("lastMsg").getValue(String.class);

                      long time = snapshot.child("lastMsgTime").getValue(Long.class);

                        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                        last.setText(lastMsg);
                        LastTime.setText(dateFormat.format(new Date(time)));
                    } else {
                        last.setText("Tap to chat");
                        LastTime.setText("");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
}
catch (Exception e){
    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
}
        holder.userName.setText(users.getUserName());
        Picasso.get().load(users.getProfile()).placeholder(R.drawable.avatar).into(holder.image);
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, ChatsActivity.class);
            intent.putExtra("name",users.getUserName());
            intent.putExtra("profileImage",users.getProfile());
            intent.putExtra("uid",users.getUserId());
            intent.putExtra("token",users.getToken());
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolderClass extends RecyclerView.ViewHolder {
        ImageView image;
        TextView userName, last,time;

        public ViewHolderClass(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.profile_image);
            userName = itemView.findViewById(R.id.user_name);
            last = itemView.findViewById(R.id.last_msg);
            time = itemView.findViewById(R.id.time);
        }

    }
}
