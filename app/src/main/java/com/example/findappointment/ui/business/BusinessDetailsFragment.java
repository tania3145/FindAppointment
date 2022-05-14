package com.example.findappointment.ui.business;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.findappointment.BusinessDetailsActivity;
import com.example.findappointment.MainApplication;
import com.example.findappointment.R;
import com.example.findappointment.Services;
import com.example.findappointment.data.Appointment;
import com.example.findappointment.data.Business;
import com.example.findappointment.data.User;
import com.example.findappointment.services.Utility;
import com.example.findappointment.ui.calendar.CalendarFragment;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
            } else {
                services.getUtility().showDialog(requireActivity(), Utility.DialogType.ERROR,
                        task.getException().getMessage());
                requireActivity().finish();
            }
        }).continueWith(task -> {
            FragmentContainerView calendarContainer = view.findViewById(R.id.calendar_fragment_view);
            if (!services.getDatabase().isUserLoggedIn()) {
                calendarContainer.setVisibility(View.GONE);
                return null;
            }
            services.getDatabase().getSignedInUser().addOnSuccessListener(loggedUser -> {
                if (!task.getResult().getOwner().equals(loggedUser.getId())) {
                    calendarContainer.setVisibility(View.GONE);
                    return;
                }
                ProgressDialog pd = new ProgressDialog(requireActivity());
                pd.setMessage("Loading ...");
                pd.show();
                CalendarFragment calendarFragment = calendarContainer.getFragment();
                calendarFragment.addOnEventClickListener(event -> {
                });
                calendarFragment.setAvailability(8, 17);
                services.getDatabase().getAppointments(task.getResult().getAppointments())
                        .addOnSuccessListener(appointments -> {
                            List<CalendarFragment.Event> events = new ArrayList<>();
                            List<Task<CalendarFragment.Event>> tasks = new ArrayList<>();
                            for (Appointment app : appointments) {
                                TaskCompletionSource<CalendarFragment.Event> taskSource =
                                        new TaskCompletionSource<>();
                                services.getDatabase().getUser(app.getUserId())
                                        .addOnSuccessListener(user -> {
                                            Date d = app.getTime().toDate();
                                            CalendarDay day = CalendarDay.from(d);
                                            int hour = d.getHours();
                                            CalendarFragment.Event e = new CalendarFragment.Event(day, hour,
                                                    user.getFirstName() + " " + user.getLastName());
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
            return null;
        });
    }
}