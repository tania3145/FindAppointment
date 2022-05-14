package com.example.findappointment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.findappointment.databinding.ActivityMakeAppointmentBinding;
import com.example.findappointment.databinding.ActivityRegisterBusinessBinding;

public class MakeAppointmentActivity extends AppCompatActivity {
    private Services services;
    private String userId;
    private String businessId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMakeAppointmentBinding binding =
                ActivityMakeAppointmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        services = ((MainApplication) getApplication()).getServices();

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
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

    @NonNull
    public String getUserId() {
        return userId;
    }
}