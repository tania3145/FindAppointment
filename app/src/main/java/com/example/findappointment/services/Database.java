package com.example.findappointment.services;

import com.example.findappointment.data.Business;
import com.example.findappointment.data.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

        public boolean isAddressValid(String address) {
            return !address.isEmpty();
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

    // TODO: Implement user
    public Task<User> getSignedInUser() {
        TaskCompletionSource<User> taskSource = new TaskCompletionSource<>();
        if (auth.getCurrentUser() == null) {
            taskSource.setException(new Exception("User is not logged in."));
            return taskSource.getTask();
        }
        db.collection("users")
                .document(auth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();
                        if (!snapshot.exists()) {
                            taskSource.setException(new Exception("Could not find user details."));
                            return;
                        }
                        User user = new User(auth.getCurrentUser().getUid());
                        if (snapshot.contains("first_name")) {
                            user.setFirstName(snapshot.getString("first_name"));
                        }
                        if (snapshot.contains("last_name")) {
                            user.setLastName(snapshot.getString("last_name"));
                        }
                        if (snapshot.contains("email")) {
                            user.setEmail(snapshot.getString("email"));
                        }
                        taskSource.setResult(user);
                    } else {
                        taskSource.setException(task.getException());
                    }
                });
        return taskSource.getTask();
    }

    public Task<User> registerUser(String firstName, String lastName, String email,
                             String password, String confirmPassword) {
        TaskCompletionSource<User> taskSource = new TaskCompletionSource<>();
        if (!validator.isNameValid(firstName) || !validator.isNameValid(lastName) ||
                !validator.isLoginEmailValid(email) || !validator.isPasswordValid(password) ||
                !validator.isConfirmPasswordValid(password, confirmPassword)) {
            taskSource.setException(new IllegalArgumentException("Credentials are invalid."));
            return taskSource.getTask();
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    User user = new User(task.getResult().getUser().getUid(),
                            firstName, lastName, email);

                    db.collection("users")
                            .document(user.getId())
                            .set(new HashMap<String, Object>() {{
                                put("first_name", user.getFirstName());
                                put("last_name", user.getLastName());
                                put("email", user.getEmail());
                            }}).addOnCompleteListener(storeTask -> {
                        if (storeTask.isSuccessful()) {
                            auth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(signInTask -> {
                                        if (signInTask.isSuccessful()) {
                                            taskSource.setResult(user);
                                        } else {
                                            taskSource.setException(signInTask.getException());
                                        }
                                    });
                        } else {
                            taskSource.setException(storeTask.getException());
                        }
                    });
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
        db.collection("businesses")
                .get()
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
