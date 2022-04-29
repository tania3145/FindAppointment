package com.example.findappointment.services;

import android.util.Log;

import com.example.findappointment.R;
import com.example.findappointment.data.Business;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Database {

    private FirebaseFirestore db;

    public Database() {
        db = FirebaseFirestore.getInstance();
    }

    public void getBusinesses(Function<List<Business>, Void> successCallback, Function<Error, Void> failureCallback) {
        db.collection("businesses").get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<Business> businesses = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        businesses.add(Business.fromSnapshot(document));
                    }
                    successCallback.apply(businesses);
                } else {
                    failureCallback.apply(new Error("Could not find any businesses."));
                }
            });
    }
}
