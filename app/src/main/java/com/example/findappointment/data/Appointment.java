package com.example.findappointment.data;

import com.google.firebase.Timestamp;

public class Appointment {
    private String id;
    private String userId;
    private String businessId;
    private Timestamp time;

    public Appointment(String id) {
        this.id = id;
    }

    public Appointment(String id, String userId, String businessId, Timestamp time) {
        this.id = id;
        this.userId = userId;
        this.businessId = businessId;
        this.time = time;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", businessId='" + businessId + '\'' +
                ", time=" + time +
                '}';
    }
}
