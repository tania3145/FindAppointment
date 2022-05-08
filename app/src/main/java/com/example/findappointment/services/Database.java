package com.example.findappointment.services;

import androidx.annotation.NonNull;

import com.example.findappointment.data.Business;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
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

    public Task<AuthResult> registerUser(String firstName, String lastName, String email,
                             String password, String confirmPassword) {
        TaskCompletionSource<AuthResult> taskSource = new TaskCompletionSource<>();
        if (!validator.isNameValid(firstName) || !validator.isNameValid(lastName) ||
                !validator.isLoginEmailValid(email) || !validator.isPasswordValid(password) ||
                !validator.isConfirmPasswordValid(password, confirmPassword)) {
            taskSource.setException(new IllegalArgumentException("Credentials are invalid."));
            return taskSource.getTask();
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    auth.signInWithEmailAndPassword(email, password);
                    taskSource.setResult(task.getResult());
                } else {
                    taskSource.setException(task.getException());
                }
            });
        return taskSource.getTask();
    }

    public Task<AuthResult> loginUser(String email, String password) {
        if (!validator.isLoginEmailValid(email) || !validator.isPasswordValid(password)) {
            TaskCompletionSource<AuthResult> taskSource = new TaskCompletionSource<>();
            taskSource.setException(new IllegalArgumentException("Credentials are invalid."));
            return taskSource.getTask();
        }
        return auth.signInWithEmailAndPassword(email, password);
    }

    public void logout() {
        auth.signOut();
    }

    public Task<List<Business>> getBusinesses() {
        TaskCompletionSource<List<Business>> taskSource = new TaskCompletionSource<>();
        db.collection("businesses").get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<Business> businesses = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        businesses.add(Business.fromSnapshot(document));
                    }
                    taskSource.setResult(businesses);
                } else {
                    taskSource.setException(task.getException());
                }
            });
        return taskSource.getTask();
    }
}
