package com.example.findappointment.ui.register;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.findappointment.MainApplication;
import com.example.findappointment.R;
import com.example.findappointment.RegisterBusinessActivity;
import com.example.findappointment.Services;
import com.example.findappointment.services.Utility;
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
    private EditText nameField;
    private EditText emailField;
    private EditText phoneField;
    private EditText descriptionField;
    private EditText addressField;
    private Button submit;
    private GoogleMap map;
    private LatLng addressSearch;

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

    private void verifyName() {
        String name = nameField.getText().toString();
        if (!services.getDatabase().getValidator().isBusinessNameValid(name)) {
            nameField.setError("Name cannot be empty");
        }
    }

    private void verifyEmail() {
        String email = emailField.getText().toString();
        if (!services.getDatabase().getValidator().isEmailValid(email)) {
            emailField.setError("Email is invalid");
        }
    }

    private void verifyPhone() {
        String phone = phoneField.getText().toString();
        if (!services.getDatabase().getValidator().isPhoneValid(phone)) {
            phoneField.setError("Phone is invalid");
        }
    }

    private void verifyDescription() {
        String desc = descriptionField.getText().toString();
        if (!services.getDatabase().getValidator().isDescriptionValid(desc)) {
            phoneField.setError("Description is invalid");
        }
    }

    private void verifyAddress() {
        if (addressSearch == null) {
            addressField.setError("You must specify a valid address");
            return;
        }
        String address = addressField.getText().toString();
        if (!services.getDatabase().getValidator().isAddressValid(address)) {
            addressField.setError("Address cannot be empty");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialise form
        nameField = view.findViewById(R.id.name_field);
        emailField = view.findViewById(R.id.email_field);
        phoneField = view.findViewById(R.id.phone_field);
        descriptionField = view.findViewById(R.id.description_field);
        addressField = view.findViewById(R.id.address_field);
        submit = view.findViewById(R.id.register_button);
        addressField.addTextChangedListener(new TextWatcher() {
            private final long DELAY = 1000; // Milliseconds
            private Timer timer = new Timer();

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                addressSearch = null;
                timer.cancel();
                String address = editable.toString();
                if (!services.getDatabase().getValidator().isAddressValid(address)) {
                    return;
                }
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        services.getLocation().getLocationFromAddress(address)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        map.clear();
                                        Marker m = map.addMarker(new MarkerOptions()
                                                .position(task.getResult()));
                                        assert m != null;
                                        services.getLocation()
                                                .moveMapCameraToLocation(map, m.getPosition());
                                        addressSearch = m.getPosition();
                                    } else {
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
        nameField.setOnFocusChangeListener((elView, hasFocus) -> {
            if (hasFocus) {
                return;
            }
            verifyName();
        });
        emailField.setOnFocusChangeListener((elView, hasFocus) -> {
            if (hasFocus) {
                return;
            }
            verifyEmail();
        });
        phoneField.setOnFocusChangeListener((elView, hasFocus) -> {
            if (hasFocus) {
                return;
            }
            verifyPhone();
        });
        descriptionField.setOnFocusChangeListener((elView, hasFocus) -> {
            if (hasFocus) {
                return;
            }
            verifyDescription();
        });
        submit.setOnClickListener(elView -> {
            ProgressDialog pd = new ProgressDialog(requireActivity());
            pd.setMessage("Loading ...");
            pd.show();
            verifyAddress();
            verifyName();
            verifyEmail();
            verifyPhone();
            verifyDescription();

            services.getDatabase().getSignedInUser()
                    .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    services.getDatabase().registerBusiness(task.getResult().getId(),
                            nameField.getText().toString(),
                            emailField.getText().toString(), phoneField.getText().toString(),
                            descriptionField.getText().toString(), addressSearch)
                            .addOnCompleteListener(businessTask -> {
                                pd.dismiss();
                                if (businessTask.isSuccessful()) {
                                    requireActivity().setResult(Activity.RESULT_OK);
                                    requireActivity().finish();
                                } else {
                                    services.getUtility().showDialog(requireActivity(),
                                            Utility.DialogType.INFO, businessTask.getException().getMessage());
                                }
                            });
                } else {
                    pd.dismiss();
                    services.getUtility().showDialog(requireActivity(),
                            Utility.DialogType.INFO, task.getException().getMessage());
                }
            });
        });
        nameField.setText("Restaurant");
        emailField.setText("restaurant@gmail.com");
        phoneField.setText("0735271827");
        descriptionField.setText("Mancare buna. Veniti la noi <3 :*!!");
        addressField.setText("Ardealului, Caransebes");
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.map = map;
        map.getUiSettings().setAllGesturesEnabled(false);
        services.getLocation().centerUserLocation(requireActivity(), map);
    }
}