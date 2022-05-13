package com.example.findappointment.services;

import androidx.annotation.Nullable;

import com.example.findappointment.data.Business;
import com.example.findappointment.data.User;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Database {
    public class Validator {
        public boolean isUserNameValid(String name) {
            return !name.isEmpty();
        }

        public boolean isPasswordValid(String password) {
            return password.length() >= 6;
        }

        public boolean isConfirmPasswordValid(String password, String confirmPassword) {
            return password.equals(confirmPassword);
        }

        public boolean isEmailValid(String email) {
            return !email.isEmpty() && email.contains("@");
        }

        public boolean isBusinessNameValid(String name) {
            return !name.isEmpty();
        }

        public boolean isPhoneValid(String phone) {
            Pattern pattern = Pattern.compile("^[0-9]*$");
            Matcher matcher = pattern.matcher(phone);
            return !phone.isEmpty() && matcher.find();
        }

        public boolean isDescriptionValid(String desc) {
            return true;
        }

        public boolean isAddressValid(String address) {
            return !address.isEmpty();
        }

        public boolean isLocationValid(LatLng location) {
            return location != null;
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

    public Task<User> getSignedInUser() {
        TaskCompletionSource<User> taskSource = new TaskCompletionSource<>();
        if (auth.getCurrentUser() == null) {
            taskSource.setException(new Exception("User is not logged in."));
            return taskSource.getTask();
        }
        getUser(auth.getCurrentUser().getUid()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                taskSource.setResult(task.getResult());
            } else {
                taskSource.setException(task.getException());
            }
        });
        return taskSource.getTask();
    }

    public Task<User> registerUser(String firstName, String lastName, String email,
                             String password, String confirmPassword) {
        TaskCompletionSource<User> taskSource = new TaskCompletionSource<>();
        if (!validator.isUserNameValid(firstName) || !validator.isUserNameValid(lastName) ||
                !validator.isEmailValid(email) || !validator.isPasswordValid(password) ||
                !validator.isConfirmPasswordValid(password, confirmPassword)) {
            taskSource.setException(new IllegalArgumentException("Credentials are invalid."));
            return taskSource.getTask();
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    User user = new User(task.getResult().getUser().getUid(),
                            firstName, lastName, email, new ArrayList<>());

                    db.collection("users")
                            .document(user.getId())
                            .set(new HashMap<String, Object>() {{
                                put("first_name", user.getFirstName());
                                put("last_name", user.getLastName());
                                put("email", user.getEmail());
                                put("businesses", new ArrayList<String>());
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

    public Task<Business> registerBusiness(String userId, String name, String email,
                                   String phone, String description, LatLng location,
                                           String address) {
        TaskCompletionSource<Business> taskSource = new TaskCompletionSource<>();
        if (userId == null || !validator.isBusinessNameValid(name) ||
                !validator.isEmailValid(email) || !validator.isDescriptionValid(description) ||
                !validator.isPhoneValid(phone) || !validator.isLocationValid(location) ||
                !validator.isAddressValid(address)) {
            taskSource.setException(new IllegalArgumentException("Credentials are invalid."));
            return taskSource.getTask();
        }
        Business business = new Business(null, userId, name, email,
                description, phone, location, address);
        Map<String, Object> data = new HashMap<String, Object>() {{
            put("name", business.getName());
            put("owner", business.getOwner());
            put("email", business.getEmail());
            put("description", business.getDescription());
            put("phone", business.getPhone());
            put("location", new GeoPoint(business.getLocation().latitude,
                    business.getLocation().longitude));
            put("address", business.getAddress());
        }};
        db.collection("businesses")
                .add(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        getUser(business.getOwner()).addOnCompleteListener(userSearch -> {
                            if (userSearch.isSuccessful()) {
                                List<String> businesses = userSearch.getResult().getBusinesses();
                                businesses.add(task.getResult().getId());
                                db.collection("users")
                                        .document(userSearch.getResult().getId())
                                        .update(new HashMap<String, Object>() {{
                                            put("businesses", businesses);
                                        }});
                                taskSource.setResult(business);
                            } else {
                                taskSource.setException(task.getException());
                            }
                        });
                    } else {
                        taskSource.setException(task.getException());
                    }
                });
        return taskSource.getTask();
    }

    public Task<User> getUser(String userId) {
        TaskCompletionSource<User> taskSource = new TaskCompletionSource<>();
        db.collection("users")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();
                        if (!snapshot.exists()) {
                            taskSource.setException(new Exception("Could not find user details."));
                            return;
                        }
                        User user = new User(userId);
                        if (snapshot.contains("first_name")) {
                            user.setFirstName(snapshot.getString("first_name"));
                        }
                        if (snapshot.contains("last_name")) {
                            user.setLastName(snapshot.getString("last_name"));
                        }
                        if (snapshot.contains("email")) {
                            user.setEmail(snapshot.getString("email"));
                        }
                        if (snapshot.contains("businesses")) {
                            user.setBusinesses((List<String>) snapshot.get("businesses"));
                        }
                        taskSource.setResult(user);
                    } else {
                        taskSource.setException(task.getException());
                    }
                });
        return taskSource.getTask();
    }

    private Business getBusinessFromSnapshot(DocumentSnapshot snapshot) {
        Business business = new Business(snapshot.getId());
        if (snapshot.contains("owner")) {
            business.setOwner(snapshot.getString("owner"));
        }
        if (snapshot.contains("name")) {
            business.setName(snapshot.getString("name"));
        }
        if (snapshot.contains("email")) {
            business.setEmail(snapshot.getString("email"));
        }
        if (snapshot.contains("phone")) {
            business.setPhone(snapshot.getString("phone"));
        }
        if (snapshot.contains("description")) {
            business.setDescription(snapshot.getString("description"));
        }
        if (snapshot.contains("address")) {
            business.setAddress(snapshot.getString("address"));
        }
        if (snapshot.contains("location")) {
            GeoPoint point = snapshot.getGeoPoint("location");
            LatLng location = new LatLng(point.getLatitude(), point.getLongitude());
            business.setLocation(location);
        }
        return business;
    }

    public Task<Business> getBusiness(String businessId) {
        TaskCompletionSource<Business> taskSource = new TaskCompletionSource<>();
        db.collection("businesses")
                .document(businessId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();
                        if (!snapshot.exists()) {
                            taskSource.setException(
                                    new Exception("Could not find business details."));
                            return;
                        }
                        taskSource.setResult(getBusinessFromSnapshot(snapshot));
                    } else {
                        taskSource.setException(task.getException());
                    }
                });
        return taskSource.getTask();
    }

    public Task<AuthResult> loginUser(String email, String password) {
        if (!validator.isEmailValid(email) || !validator.isPasswordValid(password)) {
            TaskCompletionSource<AuthResult> taskSource = new TaskCompletionSource<>();
            taskSource.setException(new IllegalArgumentException("Credentials are invalid."));
            return taskSource.getTask();
        }
        return auth.signInWithEmailAndPassword(email, password);
    }

    public void logout() {
        auth.signOut();
    }

    public Task<List<Business>> getAllBusinesses() {
        TaskCompletionSource<List<Business>> taskSource = new TaskCompletionSource<>();
        db.collection("businesses")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Business> businesses = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            businesses.add(getBusinessFromSnapshot(document));
                        }
                        taskSource.setResult(businesses);
                    } else {
                        taskSource.setException(task.getException());
                    }
                });
        return taskSource.getTask();
    }

    public void subscribeToBusinesses(EventListener<List<Business>> listener) {
        db.collection("businesses")
                .addSnapshotListener((snapshot, error) -> {
                    List<Business> businesses = new ArrayList<>();
                    if (error != null || snapshot == null) {
                        listener.onEvent(businesses, error);
                        return;
                    }
                    for (QueryDocumentSnapshot document : snapshot) {
                        businesses.add(getBusinessFromSnapshot(document));
                    }
                    listener.onEvent(businesses, null);
                });
    }
}
