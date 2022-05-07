package com.example.findappointment.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.findappointment.MainActivity;
import com.example.findappointment.R;
import com.example.findappointment.Services;
import com.example.findappointment.data.Business;
import com.example.findappointment.databinding.FragmentHomeBinding;
import com.example.findappointment.services.Utility;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private FragmentHomeBinding binding;
    private GoogleMap map;
    private BottomSheetBehavior bottomSheetBehavior;
    private HomeViewModel viewModel;

    private HomeViewModel createViewModel() {
        Services services = ((MainActivity) getActivity()).getServices();
        HomeViewModel.Factory factory = new HomeViewModel.Factory(services);
        return new ViewModelProvider(this, factory).get(HomeViewModel.class);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModel = createViewModel();

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set location permissions.
        viewModel.getServices().getPermissions().requireLocation(getActivity());

        // Initialise map.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return root;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        View bottomSheet = view.findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private Location getLastKnownLocation() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            @SuppressLint("MissingPermission") Location l = locationManager.getLastKnownLocation(provider);
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

    private void centerUserLocation() {
        Location currentLocation = getLastKnownLocation();
        if (currentLocation == null) {
            viewModel.getServices().getUtility().showDialog(getActivity(), Utility.DialogType.WARNING, "Could not retrieve user location.");
            return;
        }
        LatLng currentLocationLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocationLatLng, 13.5f));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.map = map;

        viewModel.getBusinesses().observe(getViewLifecycleOwner(), businesses -> {
            this.map.clear();
            for (Business business : businesses) {
                this.map.addMarker(new MarkerOptions()
                        .position(business.getLocation())
                        .title(business.getName()));
            }
        });
        map.setMyLocationEnabled(true);
        try {
            if (!this.map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.google_maps))) {
                Log.e(getString(R.string.app_tag), "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(getString(R.string.app_tag), "Can't find style. Error: ", e);
        }
        centerUserLocation();
    }
}