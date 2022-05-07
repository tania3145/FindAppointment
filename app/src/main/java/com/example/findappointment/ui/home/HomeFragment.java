package com.example.findappointment.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.findappointment.MainActivity;
import com.example.findappointment.R;
import com.example.findappointment.Services;
import com.example.findappointment.data.Business;
import com.example.findappointment.databinding.FragmentHomeBinding;
import com.example.findappointment.services.Utility;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.List;

public class HomeFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener,
        GoogleMap.OnCameraMoveStartedListener {

    private static final float MAP_CAMERA_ZOOM = 13.5f;

    private static class MarkerData {
        private Business business;

        public MarkerData(Business business) {
            this.business = business;
        }

        public Business getBusiness() {
            return business;
        }

        public void setBusiness(Business business) {
            this.business = business;
        }
    }

    private GoogleMap map;
    private BottomSheetBehavior sheet;
    private HomeViewModel viewModel;

    private HomeViewModel createViewModel() {
        Services services = ((MainActivity) requireActivity()).getServices();
        return new ViewModelProvider(this,
                (ViewModelProvider.Factory) new HomeViewModel.Factory(services))
                .get(HomeViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModel = createViewModel();

        FragmentHomeBinding binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set location permissions.
        viewModel.getServices().getPermissions().requireLocation(getActivity());

        // Initialise map.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        View bottomSheet = view.findViewById(R.id.bottom_sheet);
        sheet = BottomSheetBehavior.from(bottomSheet);
        sheet.setState(BottomSheetBehavior.STATE_HIDDEN);

        Button makeAppointment = view.findViewById(R.id.make_appointment_button);
        makeAppointment.setOnClickListener(elView -> {
            if (!viewModel.getServices().getDatabase().isUserLoggedIn()) {
                viewModel.getServices().getUtility().showToast(requireActivity(), "Please login");
                ((MainActivity) requireActivity()).getNavController().navigate(R.id.nav_login);
                return;
            }
            System.out.println("Make appointment");
        });
    }

    private Location getLastKnownLocation() {
        LocationManager locationManager = (LocationManager) requireActivity()
                .getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            @SuppressLint("MissingPermission") Location l = locationManager
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

    private void centerUserLocation() {
        Location currentLocation = getLastKnownLocation();
        if (currentLocation == null) {
            viewModel.getServices().getUtility().showDialog(getActivity(),
                    Utility.DialogType.WARNING, "Could not retrieve user location.");
            return;
        }
        LatLng currentLocationLatLng = new LatLng(currentLocation.getLatitude(),
                currentLocation.getLongitude());
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocationLatLng, MAP_CAMERA_ZOOM));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.map = map;

        viewModel.getBusinesses().observe(getViewLifecycleOwner(), businesses -> {
            this.map.clear();
            for (Business business : businesses) {
                Marker m = this.map.addMarker(new MarkerOptions()
                        .position(business.getLocation())
                        .title(business.getName()));
                assert m != null;
                m.setTag(new MarkerData(business));
            }
        });
        map.setMyLocationEnabled(true);
        map.setOnMarkerClickListener(this);
        map.setOnMapClickListener(this);
        map.setOnCameraMoveStartedListener(this);
        try {
            if (!this.map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(requireActivity(), R.raw.google_maps))) {
                Log.e(getString(R.string.app_tag), "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(getString(R.string.app_tag), "Can't find style. Error: ", e);
        }
        centerUserLocation();
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        sheet.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void onCameraMoveStarted(int i) {
        if (sheet.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            sheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull final Marker marker) {
        map.animateCamera(CameraUpdateFactory
                .newLatLngZoom(marker.getPosition(), MAP_CAMERA_ZOOM), 100,
                new GoogleMap.CancelableCallback() {
                    @Override
                    public void onCancel() { }
                    @Override
                    public void onFinish() { }
                });

        marker.hideInfoWindow();
        if (sheet.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
            sheet.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        }

        final MarkerData data = (MarkerData) marker.getTag();
        assert data != null;
        TextView businessNameText = requireView().findViewById(R.id.bottom_sheet_business_name);
        businessNameText.setText(data.getBusiness().getName());

        return true;
    }
}