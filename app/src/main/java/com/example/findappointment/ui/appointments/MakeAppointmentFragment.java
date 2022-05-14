package com.example.findappointment.ui.appointments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.findappointment.BusinessDetailsActivity;
import com.example.findappointment.MainApplication;
import com.example.findappointment.MakeAppointmentActivity;
import com.example.findappointment.R;
import com.example.findappointment.Services;
import com.example.findappointment.data.Business;
import com.example.findappointment.ui.calendar.CalendarFragment;

public class MakeAppointmentFragment extends Fragment {
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
        return inflater.inflate(R.layout.fragment_make_appointment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentContainerView calendarContainer = view.findViewById(R.id.calendar_fragment_view);
        CalendarFragment calendarFragment = calendarContainer.getFragment();
        calendarFragment.addEvent();

        String businessId = ((MakeAppointmentActivity) requireActivity()).getBusinessId();
        System.out.println("businessId: " + businessId);
    }
}