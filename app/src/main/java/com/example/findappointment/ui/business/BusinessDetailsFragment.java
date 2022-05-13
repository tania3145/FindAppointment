package com.example.findappointment.ui.business;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.findappointment.BusinessDetailsActivity;
import com.example.findappointment.MainApplication;
import com.example.findappointment.R;
import com.example.findappointment.Services;
import com.example.findappointment.data.Business;
import com.example.findappointment.data.User;
import com.example.findappointment.services.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class BusinessDetailsFragment extends Fragment {

    private Services services;
    private Business business;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        services = ((MainApplication) requireActivity().getApplication()).getServices();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_business_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String businessId = ((BusinessDetailsActivity) requireActivity()).getBusinessId();
        services.getDatabase().getBusiness(businessId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                business = task.getResult();

                TextView name = view.findViewById(R.id.name_text_input);
                name.setText(business.getName());

                TextView email = view.findViewById(R.id.email_text_input);
                email.setText(business.getEmail());

                TextView phone = view.findViewById(R.id.phone_text_input);
                phone.setText(business.getPhone());

                TextView description = view.findViewById(R.id.description_text_input);
                description.setText(business.getDescription());

                TextView address = view.findViewById(R.id.address_text_input);
                address.setText(business.getAddress());

                LinearLayout l = view.findViewById(R.id.appointments);
                l.removeAllViews();

                services.getDatabase().getSignedInUser().addOnSuccessListener(user -> {
                    if (!user.getId().equals(business.getOwner())) {
                        return;
                    }
                    TextView appointmentText = view.findViewById(R.id.appointments_text);
                    appointmentText.setVisibility(View.VISIBLE);
                    if (true) {
                        TextView noAppointmentsText = new TextView(requireContext());
                        noAppointmentsText.setText("You do not have any appointments");
                        noAppointmentsText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        l.addView(noAppointmentsText);
                        return;
                    }
                });
            } else {
                services.getUtility().showDialog(requireActivity(), Utility.DialogType.ERROR,
                        task.getException().getMessage());
                requireActivity().finish();
            }
        });
    }
}