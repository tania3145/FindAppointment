package com.example.findappointment.ui.register;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.findappointment.MainApplication;
import com.example.findappointment.R;
import com.example.findappointment.Services;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Timer;
import java.util.TimerTask;

public class RegisterBusinessDetailsFragment extends Fragment implements OnMapReadyCallback {
    private Services services;
    private EditText addressField;
    private GoogleMap map;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        services = ((MainApplication) requireActivity().getApplication()).getServices();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_register_business_details,
                container, false);

        // Initialise map.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        return root;
    }

    private void verifyAddress() {
        String address = addressField.getText().toString();
        if (!services.getDatabase().getValidator().isAddressValid(address)) {
            addressField.setError("Address cannot be empty");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialise form
        addressField = view.findViewById(R.id.address_field);
        addressField.addTextChangedListener(new TextWatcher() {
            private final long DELAY = 1000; // Milliseconds
            private Timer timer = new Timer();

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                timer.cancel();
                String address = editable.toString();
                if (!services.getDatabase().getValidator().isAddressValid(address)) {
                    return;
                }
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        System.out.println("RUN ME PLEASE");
                        services.getLocation().getLocationFromAddress(address)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        map.clear();
                                        Marker m = map.addMarker(new MarkerOptions()
                                                .position(task.getResult()));
                                        assert m != null;
                                        services.getLocation()
                                                .moveMapCameraToLocation(map, m.getPosition());
                                    } else {
                                        System.err.println(task.getException().getMessage());
                                        addressField.setError("Couldn't find address");
                                    }
                                });
                    }
                }, DELAY);
            }
        });
        addressField.setOnFocusChangeListener((elView, hasFocus) -> {
            if (hasFocus) {
                return;
            }
            verifyAddress();
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.map = map;
        map.getUiSettings().setAllGesturesEnabled(false);
        services.getLocation().centerUserLocation(requireActivity(), map);
    }
}