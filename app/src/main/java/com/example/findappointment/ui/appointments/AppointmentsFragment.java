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

import com.example.findappointment.MainApplication;
import com.example.findappointment.R;
import com.example.findappointment.Services;
import com.example.findappointment.data.Appointment;
import com.example.findappointment.data.Business;
import com.example.findappointment.services.Utility;
import com.example.findappointment.ui.calendar.CalendarFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.events.Event;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AppointmentsFragment extends Fragment {
    private Services services;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        services = ((MainApplication) requireActivity().getApplication()).getServices();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_appointments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ProgressDialog pd = new ProgressDialog(requireActivity());
        pd.setMessage("Loading ...");
        pd.show();
        FragmentContainerView calendarContainer = view.findViewById(R.id.calendar_fragment_view);
        CalendarFragment calendarFragment = calendarContainer.getFragment();
        calendarFragment.addOnEventClickListener(event -> {
            // TODO
        });
        calendarFragment.setAvailability(0, 23);
        services.getDatabase().getSignedInUser().addOnSuccessListener(user -> {
            services.getDatabase().getAppointments(user.getAppointments())
                    .addOnSuccessListener(appointments -> {
                        List<CalendarFragment.Event> events = new ArrayList<>();
                        List<Task<CalendarFragment.Event>> tasks = new ArrayList<>();
                        for (Appointment app : appointments) {
                            TaskCompletionSource<CalendarFragment.Event> taskSource =
                                    new TaskCompletionSource<>();
                            services.getDatabase().getBusiness(app.getBusinessId())
                                .addOnSuccessListener(business -> {
                                    Date d = app.getTime().toDate();
                                    CalendarDay day = CalendarDay.from(d);
                                    int hour = d.getHours();
                                    CalendarFragment.Event e = new CalendarFragment.Event(day, hour,
                                            business.getName());
                                    taskSource.setResult(e);
                                });
                            tasks.add(taskSource.getTask());
                        }
                        Tasks.whenAllComplete(tasks)
                            .addOnSuccessListener(eventTasks -> {
                                for (Task<?> eventTask : eventTasks) {
                                    events.add((CalendarFragment.Event) eventTask.getResult());
                                }
                                calendarFragment.addEvents(events);
                                pd.dismiss();
                            });
                    });
        });
    }
}