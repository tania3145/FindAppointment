package com.example.findappointment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.findappointment.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    private Services services;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityRegisterBinding binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        services = ((MainApplication) getApplication()).getServices();
    }

    @NonNull
    public Services getServices() {
        return services;
    }
}