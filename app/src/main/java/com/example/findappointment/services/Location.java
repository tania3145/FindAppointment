package com.example.findappointment.services;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Message;
import android.util.JsonReader;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Location {
    private static final float MAP_CAMERA_ZOOM = 13.5f;
    public static final String GOOGLE_GEOCODING_API_KEY = "AIzaSyCzLlLgUXlmao9sD9LedwbOi6IOr2h9ydg";

    private Application application;
    private Permissions permissions;
    private Utility utility;

    public Location(Application application, Permissions permissions, Utility utility) {
        this.application = application;
        this.permissions = permissions;
        this.utility = utility;
    }

    private android.location.Location getLastKnownLocation() {
        LocationManager locationManager = (LocationManager) application
                .getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        android.location.Location bestLocation = null;
        for (String provider : providers) {
            @SuppressLint("MissingPermission") android.location.Location l = locationManager
                    .getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    public void moveMapCameraToLocation(GoogleMap map, LatLng position) {
        map.animateCamera(CameraUpdateFactory
                        .newLatLngZoom(position, MAP_CAMERA_ZOOM), 100,
                new GoogleMap.CancelableCallback() {
                    @Override
                    public void onCancel() { }
                    @Override
                    public void onFinish() { }
                });
    }

    public void centerUserLocation(Activity activity, GoogleMap map) {
        permissions.requireLocation(activity);
        android.location.Location currentLocation = getLastKnownLocation();
        if (currentLocation == null) {
            utility.showDialog(activity,
                    Utility.DialogType.WARNING, "Could not retrieve user location.");
            return;
        }
        LatLng currentLocationLatLng = new LatLng(currentLocation.getLatitude(),
                currentLocation.getLongitude());
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocationLatLng, MAP_CAMERA_ZOOM));
    }

    public Task<LatLng> getLocationFromAddress(String address) {
        TaskCompletionSource<LatLng> taskSource = new TaskCompletionSource<>();
        Thread thread = new Thread(() -> {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL("https://maps.googleapis.com/maps/api/geocode/json?address=" +
                        Uri.encode(address) +
                        "&sensor=true&key=" + GOOGLE_GEOCODING_API_KEY);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                Scanner scanner = new Scanner(in);
                StringBuilder sb = new StringBuilder();
                while (scanner.hasNextLine()) {
                    sb.append(scanner.nextLine());
                }
                JSONObject obj = new JSONObject(sb.toString());
                double latitude = ((JSONArray) obj.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lat");
                double longitude = ((JSONArray) obj.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lng");
                taskSource.setResult(new LatLng(latitude, longitude));
            } catch (Exception e) {
                taskSource.setException(e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        });
        thread.start();
        return taskSource.getTask();
    }
}
