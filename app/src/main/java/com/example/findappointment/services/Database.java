package com.example.findappointment.services;

import androidx.annotation.NonNull;

import com.example.findappointment.data.Business;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Database {
    public class Validator {
        public boolean isLoginEmailValid(String email) {
            return !email.isEmpty() && email.contains("@");
        }

        public boolean isNameValid(String name) {
            return !name.isEmpty();
        }

        public boolean isPasswordValid(String password) {
            return password.length() >= 6;
        }

        public boolean isConfirmPasswordValid(String password, String confirmPassword) {
            return password.equals(confirmPassword);
        }
    }

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Validator validator;

    public Database() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        validator = new Validator();
    }

    public Validator getValidator() {
        return validator;
    }

    public void onAuthChanged(FirebaseAuth.AuthStateListener listener) {
        auth.addAuthStateListener(listener);
    }

    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public FirebaseUser getSignedInUser() {
        return auth.getCurrentUser();
    }

    public void registerUser(String firstName, String lastName, String email,
                             String password, String confirmPassword,
                             Function<FirebaseUser, Void> successCallback,
                             Function<Error, Void> failureCallback) {
        if (!validator.isNameValid(firstName) || !validator.isNameValid(lastName) ||
                !validator.isLoginEmailValid(email) || !validator.isPasswordValid(password) ||
                !validator.isConfirmPasswordValid(password, confirmPassword)) {
            failureCallback.apply(new Error("Credentials are invalid."));
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    auth.signInWithEmailAndPassword(email, password);
                    successCallback.apply(auth.getCurrentUser());
                } else {
                    failureCallback.apply(new Error(task.getException().getMessage()));
                }
            });
    }

    public void loginUser(String email, String password) throws Exception {
        if (!validator.isLoginEmailValid(email) || !validator.isPasswordValid(password)) {
            throw new IllegalArgumentException("Credentials are invalid.");
        }
        auth.signInWithEmailAndPassword(email, password);
    }

    public void logout() {
        auth.signOut();
    }

    public void getBusinesses(Function<List<Business>, Void> successCallback,
                              Function<Error, Void> failureCallback) {
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
