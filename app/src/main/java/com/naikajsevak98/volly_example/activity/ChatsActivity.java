package com.naikajsevak98.volly_example.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.naikajsevak98.volly_example.R;
import com.naikajsevak98.volly_example.adapter.ChatAdapter;
import com.naikajsevak98.volly_example.databinding.ActivityChatsBinding;
import com.naikajsevak98.volly_example.models.MessagesModel;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChatsActivity extends AppCompatActivity {
    ActivityChatsBinding binding;
    ChatAdapter adapter;
    String senderRoom;
    String receiverRoom;
    ArrayList<MessagesModel> messagesModelArrayList;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog dialog;
    String senderUid;
    String receiverUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        messagesModelArrayList = new ArrayList<>();
        String name = getIntent().getStringExtra("name");
        String profile = getIntent().getStringExtra("profileImage");
        String token = getIntent().getStringExtra("token");
        receiverUid = getIntent().getStringExtra("uid");
        binding.name.setText(name);
        Picasso.get().load(profile).placeholder(R.drawable.avatar).into((binding.profileImage));
        binding.leftBaseWest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChatsActivity.this,MainActivity.class));
                finishAffinity();
            }
        });
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        senderUid = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               if(snapshot.exists()){
                   String status = snapshot.getValue(String.class);
                   if (!status.isEmpty()) {
                       binding.online.setText(status);
                       if(status.equals("Offline")){
                           binding.online.setVisibility(View.GONE);
                       }
                       else {
                           binding.online.setText(status);
                           binding.online.setVisibility(View.VISIBLE);
                       }
                   }
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading image...");
        dialog.setCancelable(false);
        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid ;
        adapter = new ChatAdapter(this,messagesModelArrayList,senderRoom,receiverRoom);
        binding.recyclerViewActivityChats.setAdapter(adapter);
        binding.recyclerViewActivityChats.setLayoutManager(new LinearLayoutManager(this));

        database.getReference().child("Chats")
                .child(senderRoom).child("messages")
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
                String randomKey = database.getReference().push().getKey();


                database.getReference().child("Chats").
                        child(senderRoom)
                        .child("messages")
                        .child(Objects.requireNonNull(randomKey))
                        .setValue(messagesModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        database.getReference().child("Chats")
                                .child(receiverRoom)
                                .child("messages")
                                .child(randomKey)
                                .setValue(messagesModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                HashMap<String,Object> hashMap = new HashMap<>();
                                hashMap.put("lastMsg",messagesModel.getMessage());
                                hashMap.put("lastMsgTime",date.getTime());

                                database.getReference().child("Chats").child(senderRoom)
                                        .updateChildren(hashMap);
                                database.getReference().child("Chats").child(receiverRoom)
                                        .updateChildren(hashMap);
                                sendNotification(name,messagesModel.getMessage(),token);
                            }
                        });
                    }
                });
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
        final Handler handler = new Handler();
        binding.msgBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                database.getReference().child("presence").child(senderUid).setValue("typing...");
                 handler.removeCallbacksAndMessages(null);
                 handler.postDelayed(runnable,1000);
            }
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("presence").child(senderUid).setValue("Online");
                }
            };
        });
    }

    void sendNotification(String name,String msg,String token){
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);

            String url = "https://fcm.googleapis/fcm/send";

            JSONObject data = new JSONObject();
            data.put("title", name);
            data.put("body", msg);
            JSONObject notificationData = new JSONObject();
            notificationData.put("notification",data);
            notificationData.put("to",token);

            JsonObjectRequest request = new JsonObjectRequest(url, notificationData,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Toast.makeText(ChatsActivity.this, "Success", Toast.LENGTH_SHORT).show();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(ChatsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("TAG", error.getMessage());
                }
        }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String ,String> hashMap = new HashMap<>();
                    String key="key=AAAAdOIG9yw:APA91bEgDIoy3cWwDVwsW7N8QQtvETkF9tdwgmzxxmFHjBFQNaQw4nJIThEXfmb2E8IJpf2qBmRAhVeRkGzEL37SPqbx1YTDOMJRLrNFpYHs_ZR-xGm5fBGKo2Ticn7nvVxjHVTME2O6";
                    hashMap.put("Authorization",key);
                    hashMap.put("Contact-Type","application/json");
                    return hashMap;
                }
            };
            requestQueue.add(request);
        }catch (Exception e){}
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
                                        String randomKey = database.getReference().push().getKey();


                                        database.getReference().child("Chats").
                                                child(senderRoom)
                                                .child("messages")
                                                .child(Objects.requireNonNull(randomKey))
                                                .setValue(messagesModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                database.getReference().child("Chats")
                                                        .child(receiverRoom)
                                                        .child("messages")
                                                        .child(randomKey)
                                                        .setValue(messagesModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        HashMap<String,Object> hashMap = new HashMap<>();
                                                        hashMap.put("lastMsg",messagesModel.getMessage());
                                                        hashMap.put("lastMsgTime",date.getTime());

                                                        database.getReference().child("Chats").child(senderRoom)
                                                                .updateChildren(hashMap);
                                                        database.getReference().child("Chats").child(receiverRoom)
                                                                .updateChildren(hashMap);
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        database.getReference().child("presence").child(FirebaseAuth.getInstance().getUid()).setValue("Online");
    }

    @Override
    protected void onPause() {
        database.getReference().child("presence").child(FirebaseAuth.getInstance().getUid()).setValue("Offline");
        super.onPause();
    }

}