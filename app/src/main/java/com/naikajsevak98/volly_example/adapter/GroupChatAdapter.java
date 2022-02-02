package com.naikajsevak98.volly_example.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.naikajsevak98.volly_example.R;
import com.naikajsevak98.volly_example.databinding.DeleteDialogBinding;
import com.naikajsevak98.volly_example.models.MessagesModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class GroupChatAdapter extends RecyclerView.Adapter{
    ArrayList<MessagesModel> arrayList;
    Context context;
    String recId;
    int SENDER_VIEW_TYPE = 1;
    int RECEIVER_VIEW_TYPE = 2;

    public GroupChatAdapter(ArrayList<MessagesModel> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    public GroupChatAdapter(ArrayList<MessagesModel> arrayList, Context context, String recId) {
        this.arrayList = arrayList;
        this.context = context;
        this.recId = recId;
    } 

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENDER_VIEW_TYPE){
            View view = LayoutInflater.from(context).inflate(R.layout.sample_sender,parent,false);
            return new SenderViewHolder(view);
        }
        View view = LayoutInflater.from(context).inflate(R.layout.sample_reciever,parent,false);
        return new ReceiverViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        if (arrayList.get(position).getUid().equals(FirebaseAuth.getInstance().getUid())){
            return RECEIVER_VIEW_TYPE;
        }
        return SENDER_VIEW_TYPE;

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessagesModel messagesModel = arrayList.get(position);
        int reaction [] = new int[]{
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry
        };

        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reaction)
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {
            if(holder.getClass() == SenderViewHolder.class){
                ImageView feel = holder.itemView.findViewById(R.id.sender_feeling);
                try {
                    feel.setImageResource(reaction[pos]);
                    feel.setVisibility(View.VISIBLE);
                }catch (Exception ignored){}
            }
            else {
                ImageView feel = holder.itemView.findViewById(R.id.receiver_feeling);
                try {
                    feel.setImageResource(reaction[pos]);
                    feel.setVisibility(View.VISIBLE);
                }catch (Exception e){}
            }
            messagesModel.setFeeling(pos);
          FirebaseDatabase.getInstance().getReference().child("public").
                  child(messagesModel.getMessageId()).setValue(messagesModel);

            return true; // true is closing popup, false is requesting a new selection
        });
        if(holder.getClass() == SenderViewHolder.class){
            SenderViewHolder viewHolder = (SenderViewHolder) holder;
            if (messagesModel.getMessage().equals("photo")){
                holder.itemView.findViewById(R.id.image_sender).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.sender_text).setVisibility(View.GONE);
                ImageView imageView = holder.itemView.findViewById(R.id.image_sender);
                Picasso.get().load(messagesModel.getMsgUrl()).placeholder(R.drawable.avatar).into((imageView));

            }
            ImageView feel;
            viewHolder.senderMsg.setText(messagesModel.getMessage());

            if(messagesModel.getFeeling()>=0){
                feel = holder.itemView.findViewById(R.id.sender_feeling);
                feel.setImageResource(reaction[messagesModel.getFeeling()]);
                feel.setVisibility(View.VISIBLE);
            }
            else {
                feel = holder.itemView.findViewById(R.id.sender_feeling);
                feel.setVisibility(View.GONE);
            }
                viewHolder.senderMsg.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        popup.onTouch(view,motionEvent);
                        return false;
                    }
                });
            viewHolder.itemView.findViewById(R.id.image_sender).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    popup.onTouch(view,motionEvent);
                    return false;
                }
            });
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    View view = LayoutInflater.from(context).inflate(R.layout.delete_dialog,null);
                    DeleteDialogBinding binding = DeleteDialogBinding.bind(view);
                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setTitle("Delete Message")
                            .setView(binding.getRoot())
                            .create();
                    binding.everyone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            messagesModel.setMessage("This message has deleted");
                            messagesModel.setFeeling(-1);
                            FirebaseDatabase.getInstance().getReference()
                                    .child("public")
                                    .child(messagesModel.getMessage())
                                    .setValue(messagesModel);
                            dialog.dismiss();
                        }
                    });

                    binding.delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            messagesModel.setMessage("");
                            messagesModel.setFeeling(-1);
                            FirebaseDatabase.getInstance().getReference()
                                    .child("public")
                                    .child(messagesModel.getMessage())
                                    .setValue(messagesModel);
                            dialog.dismiss();
                        }
                    });
                    binding.cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                    return true;
                }
            });
        }
        else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            if (messagesModel.getMessage().equals("photo")){
                holder.itemView.findViewById(R.id.image_receiver).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.receiver_text).setVisibility(View.GONE);
                ImageView imageView = holder.itemView.findViewById(R.id.image_receiver);
                Picasso.get().load(messagesModel.getMsgUrl()).placeholder(R.drawable.avatar).into((imageView));

            }
            ImageView feel;
            viewHolder.receiverMsg.setText(messagesModel.getMessage());
            if(messagesModel.getFeeling()>=0){
                feel = holder.itemView.findViewById(R.id.receiver_feeling);
                feel.setImageResource(reaction[messagesModel.getFeeling()]);
                feel.setVisibility(View.VISIBLE);
            }
            else {
                feel = holder.itemView.findViewById(R.id.receiver_feeling);
                feel.setVisibility(View.GONE);
            }
            viewHolder.receiverMsg.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    popup.onTouch(view,motionEvent);
                    return false;
                }
            });
            viewHolder.itemView.findViewById(R.id.image_receiver).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    popup.onTouch(view,motionEvent);
                    return false;
                }
            });
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    View view = LayoutInflater.from(context).inflate(R.layout.delete_dialog,null);
                    DeleteDialogBinding binding = DeleteDialogBinding.bind(view);
                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setTitle("Delete Message")
                            .setView(binding.getRoot())
                            .create();
                    binding.everyone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            messagesModel.setMessage("This message has deleted");
                            messagesModel.setFeeling(-1);
                            FirebaseDatabase.getInstance().getReference()
                                    .child("public")
                                    .child(messagesModel.getMessage())
                                    .setValue(messagesModel);
                            dialog.dismiss();
                        }
                    });

                    binding.delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            messagesModel.setMessage("");
                            messagesModel.setFeeling(-1);
                            FirebaseDatabase.getInstance().getReference()
                                    .child("public")
                                    .child(messagesModel.getMessage())
                                    .setValue(messagesModel);
                            dialog.dismiss();
                        }
                    });
                    binding.cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                    return true;
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {

        TextView receiverMsg,receiverTime;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverMsg = itemView.findViewById(R.id.receiver_text);
        }
    }
    public class SenderViewHolder extends RecyclerView.ViewHolder{
        TextView senderMsg,senderTime;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMsg = itemView.findViewById(R.id.sender_text);
        }
    }
}
