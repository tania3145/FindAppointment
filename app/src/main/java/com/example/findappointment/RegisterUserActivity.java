package com.example.findappointment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.findappointment.databinding.ActivityRegisterUserBinding;

public class RegisterUserActivity extends AppCompatActivity {

    private Services services;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityRegisterUserBinding binding =
                ActivityRegisterUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        services = ((MainApplication) getApplication()).getServices();
    }

    @NonNull
    public Services getServices() {
        return services;
    }
}