package com.naikajsevak98.volly_example;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.naikajsevak98.volly_example.adapter.GroupChatAdapter;
import com.naikajsevak98.volly_example.adapter.TopStatusAdapter;
import com.naikajsevak98.volly_example.adapter.UserAdapter;
import com.naikajsevak98.volly_example.databinding.ActivityGroupeChatBinding;
import com.naikajsevak98.volly_example.models.MessagesModel;
import com.naikajsevak98.volly_example.models.UserStatus;
import com.naikajsevak98.volly_example.models.Users;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class GroupeChat extends AppCompatActivity {
ActivityGroupeChatBinding binding;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ArrayList<Users> list;
    GroupChatAdapter adapter;
    ArrayList<MessagesModel> messagesModelArrayList;
    TopStatusAdapter statusAdapter;
    ProgressDialog dialog;
    String senderUid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupeChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().setTitle("Group");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        senderUid = FirebaseAuth.getInstance().getUid();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading image...");
        dialog.setCancelable(false);
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        messagesModelArrayList = new ArrayList<>();

        adapter = new GroupChatAdapter(messagesModelArrayList,this);
        binding.recyclerViewActivityChats.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewActivityChats.setAdapter(adapter);

        database.getReference().child("public")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messagesModelArrayList.clear();
                        for (DataSnapshot snapshot1:snapshot.getChildren()){
                            MessagesModel messagesModel = snapshot1.getValue(MessagesModel.class);
                            Objects.requireNonNull(messagesModel).setMessageId(snapshot1.getKey());
                            messagesModelArrayList.add(messagesModel);
                        }
                        adapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.sendImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = binding.msgBox.getText().toString();
                Date date = new Date();
                MessagesModel messagesModel = new MessagesModel(senderUid,msg,date.getTime());
                binding.msgBox.setText("");
                database.getReference().child("public").push().setValue(messagesModel);
            }
        });
        binding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,25);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 25){
            if (data!=null){
                if (data.getData()!=null){
                    Uri selectedImage = data.getData();
                    Calendar calendar = Calendar.getInstance();
                    StorageReference reference = storage.getReference().child("Chats")
                            .child(calendar.getTimeInMillis()+"");
                    dialog.show();
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        dialog.dismiss();
                                        String filePath = uri.toString();
                                        String msg = binding.msgBox.getText().toString();
                                        Date date = new Date();
                                        MessagesModel messagesModel = new MessagesModel(senderUid,msg,date.getTime());
                                        messagesModel.setMessage("photo");
                                        messagesModel.setMsgUrl(filePath);
                                        binding.msgBox.setText("");

                                        database.getReference().child("public")
                                                .push()
                                                .setValue(messagesModel);
                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
    }
}