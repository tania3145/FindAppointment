package com.example.findappointment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.findappointment.data.Business;
import com.example.findappointment.databinding.ActivityBusinessDetailsBinding;
import com.example.findappointment.databinding.ActivityRegisterBusinessBinding;
import com.example.findappointment.services.Utility;

public class BusinessDetailsActivity extends AppCompatActivity {

    private Services services;
    private String businessId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityBusinessDetailsBinding binding =
                ActivityBusinessDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        services = ((MainApplication) getApplication()).getServices();

        Intent intent = getIntent();
        businessId = intent.getStringExtra("businessId");
    }

    @NonNull
    public Services getServices() {
        return services;
    }

    @NonNull
    public String getBusinessId() {
        return businessId;
    }
}