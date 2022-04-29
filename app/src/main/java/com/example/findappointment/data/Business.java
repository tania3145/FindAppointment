package com.example.findappointment.data;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Map;

public class Business {
    private String name;
    private BusinessType type;
    private LatLng location;

    public Business(String name, BusinessType type, LatLng location) {
        this.name = name;
        this.type = type;
        this.location = location;
    }

    public static Business fromSnapshot(QueryDocumentSnapshot document) {
        String name = document.getString("name");
        BusinessType type = BusinessType.valueOf(document.getString("type"));
        GeoPoint point = document.getGeoPoint("location");
        LatLng location = new LatLng(point.getLatitude(), point.getLongitude());
        return new Business(name, type, location);
    }

    @Override
    public String toString() {
        return "Business{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", location=" + location +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BusinessType getType() {
        return type;
    }

    public void setType(BusinessType type) {
        this.type = type;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }
}
