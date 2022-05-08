package com.example.findappointment;

import android.app.Application;

import androidx.annotation.NonNull;

import com.example.findappointment.services.Database;
import com.example.findappointment.services.Location;
import com.example.findappointment.services.Permissions;
import com.example.findappointment.services.Utility;

public class Services {

    @NonNull
    private final Application application;
    @NonNull
    private final Utility utility;
    @NonNull
    private final Permissions permissions;
    @NonNull
    private final Database database;
    @NonNull
    private final Location location;

    public Services(@NonNull Application application) {
        this.application = application;

        utility = new Utility(application);
        permissions = new Permissions();
        database = new Database();
        location = new Location(application, permissions, utility);
    }

    @NonNull
    public Application getApplication() {
        return application;
    }

    @NonNull
    public Utility getUtility() {
        return utility;
    }

    @NonNull
    public Permissions getPermissions() {
        return permissions;
    }

    @NonNull
    public Database getDatabase() {
        return database;
    }

    @NonNull
    public Location getLocation() {
        return location;
    }
}
