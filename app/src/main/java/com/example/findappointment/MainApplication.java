package com.example.findappointment;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

public class MainApplication extends Application {
    private Services services;

    @Override
    public void onCreate() {
        super.onCreate();
        services = new Services(this);
    }

    @NonNull
    public Services getServices() {
        return services;
    }
}
