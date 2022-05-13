package com.example.findappointment.ui.home;

import android.annotation.SuppressLint;
import android.content.res.Resources;
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
import com.example.findappointment.data.User;
import com.example.findappointment.databinding.FragmentHomeBinding;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.concurrent.atomic.AtomicReference;

public class HomeFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener,
        GoogleMap.OnCameraMoveStartedListener {

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

        FragmentHomeBinding binding = FragmentHomeBinding.inflate(inflater,
                container, false);
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
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        View bottomSheet = view.findViewById(R.id.bottom_sheet);
        sheet = BottomSheetBehavior.from(bottomSheet);
        sheet.setState(BottomSheetBehavior.STATE_HIDDEN);

        Button makeAppointment = view.findViewById(R.id.make_appointment_button);
        makeAppointment.setOnClickListener(elView -> {
            if (!viewModel.getServices().getDatabase().isUserLoggedIn()) {
                viewModel.getServices().getUtility().showToast(requireActivity(),
                        "Please login");
                ((MainActivity) requireActivity()).goLogin();
                return;
            }
            System.out.println("Make appointment");
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.map = map;

        viewModel.getBusinesses().observe(getViewLifecycleOwner(), businesses -> {
            this.map.clear();
            AtomicReference<String> userId = new AtomicReference<>("");
            viewModel.getServices().getDatabase().getSignedInUser()
                    .addOnSuccessListener(user -> {
                userId.set(user.getId());
            }).addOnCompleteListener(task -> {
                for (Business business : businesses) {
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(business.getLocation())
                            .title(business.getName());

                    if (!userId.get().isEmpty()) {
                        markerOptions.icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    }

                    Marker m = this.map.addMarker(markerOptions);
                    assert m != null;
                    m.setTag(new MarkerData(business));
                }
            });
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
        viewModel.getServices().getLocation().centerUserLocation(requireActivity(), map);
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
        viewModel.getServices().getLocation().moveMapCameraToLocation(map, marker.getPosition());
        marker.hideInfoWindow();
        if (sheet.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
            sheet.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        }

        final MarkerData data = (MarkerData) marker.getTag();
        assert data != null;
        TextView businessNameText = requireView().findViewById(R.id.bottom_sheet_business_name);
        businessNameText.setText(data.getBusiness().getName());

        TextView businessAddressText = requireView()
                .findViewById(R.id.bottom_sheet_business_address);
        businessAddressText.setText(data.getBusiness().getAddress());

        TextView businessPhoneText = requireView()
                .findViewById(R.id.bottom_sheet_business_phone);
        businessPhoneText.setText(data.getBusiness().getPhone());

        TextView businessEmailText = requireView()
                .findViewById(R.id.bottom_sheet_business_email);
        businessEmailText.setText(data.getBusiness().getEmail());

        return true;
    }
}