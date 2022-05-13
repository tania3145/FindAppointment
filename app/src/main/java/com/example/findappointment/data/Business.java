package com.example.findappointment.data;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Map;

public class Business {
    private String id;
    private String owner;
    private String name;
    private String email;
    private String description;
    private String phone;
    private LatLng location;

    public Business(String id) {
        this.id = id;
    }

    public Business(String id, String owner, String name, String email,
                    String description, String phone, LatLng location) {
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.email = email;
        this.description = description;
        this.phone = phone;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }
}
