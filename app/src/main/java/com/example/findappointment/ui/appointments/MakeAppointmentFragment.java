package com.example.findappointment.ui.appointments;

import android.app.Activity;
import android.app.ProgressDialog;
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
import com.example.findappointment.data.Appointment;
import com.example.findappointment.data.Business;
import com.example.findappointment.services.Utility;
import com.example.findappointment.ui.calendar.CalendarFragment;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MakeAppointmentFragment extends Fragment {
    private Services services;

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

        String userId = ((MakeAppointmentActivity) requireActivity()).getUserId();
        String businessId = ((MakeAppointmentActivity) requireActivity()).getBusinessId();

        FragmentContainerView calendarContainer = view.findViewById(R.id.calendar_fragment_view);
        CalendarFragment calendarFragment = calendarContainer.getFragment();
        calendarFragment.addOnEventClickListener(event -> {
            ProgressDialog pd = new ProgressDialog(requireActivity());
            pd.setMessage("Loading ...");
            pd.show();
            services.getDatabase().registerAppointment(
                    userId, businessId, event.getDay(), event.getHour()
            ).addOnCompleteListener(task -> {
                pd.dismiss();
                if (!task.isSuccessful()) {
                    services.getUtility().showDialog(requireActivity(),
                            Utility.DialogType.INFO, task.getException().getMessage());
                    return;
                }

                services.getUtility().showToast(requireActivity(),
                        "Successfully added appointment.");
                requireActivity().setResult(Activity.RESULT_OK);
                requireActivity().finish();
            });
        });
        calendarFragment.setAvailability(8, 17);
        calendarFragment.addDefaultDayEvent(new CalendarFragment.Event("Reserve slot"));
        services.getDatabase().getBusiness(businessId).addOnCompleteListener(business -> {
            services.getDatabase().getAppointments(business.getResult().getAppointments())
                    .addOnCompleteListener(appointments -> {
                        List<CalendarFragment.Event> events = new ArrayList<>();
                        for (Appointment app : appointments.getResult()) {
                            Date d = app.getTime().toDate();
                            CalendarDay day = CalendarDay.from(d);
                            int hour = d.getHours();
                            events.add(new CalendarFragment.Event(day, hour,
                                    "Reserved", false));
                        }
                        calendarFragment.addEvents(events);
                    });
        });
    }
}