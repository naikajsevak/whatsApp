package com.naikajsevak98.volly_example.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.naikajsevak98.volly_example.databinding.ActivityPhoneNumberBinding;

public class PhoneNumber extends AppCompatActivity {
ActivityPhoneNumberBinding binding;
FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.ccp.registerCarrierNumberEditText(binding.phoneBox);

        auth = FirebaseAuth.getInstance();
        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PhoneNumber.this, OtpActivity.class);
                intent.putExtra("phone",binding.ccp.getFullNumberWithPlus().trim());
                startActivity(intent);
            }
        });

       if(auth.getCurrentUser()!=null){
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}
