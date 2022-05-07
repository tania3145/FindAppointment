package com.example.findappointment.services;

import com.example.findappointment.data.Business;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Database {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public Database() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public boolean isLoginEmailValid(String email) {
        return !email.isEmpty() && email.contains("@");
    }

    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public void onAuthChanged(FirebaseAuth.AuthStateListener listener) {
        auth.addAuthStateListener(listener);
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
