package com.example.findappointment.ui.account;

import static com.amulyakhare.textdrawable.util.ColorGenerator.MATERIAL;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.example.findappointment.MainActivity;
import com.example.findappointment.MainApplication;
import com.example.findappointment.R;
import com.example.findappointment.RegisterBusinessActivity;
import com.example.findappointment.Services;
import com.example.findappointment.data.Business;
import com.example.findappointment.data.User;
import com.example.findappointment.services.Database;
import com.example.findappointment.services.Utility;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

public class AccountFragment extends Fragment {
    private static final int HEADER_IMAGE_SIZE = 150;
    private Services services;
    private ActivityResultLauncher<Intent> launcher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        services = ((MainApplication) requireActivity().getApplication()).getServices();
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        services.getUtility().showToast(requireActivity(),
                                "Successfully added business.");
                    } else {
                        services.getUtility().showToast(requireActivity(),
                                "Couldn't add business.");
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button button = view.findViewById(R.id.create_business);
        button.setOnClickListener(elView -> {
            Intent registerIntent = new Intent(requireContext(),
                    RegisterBusinessActivity.class);
            launcher.launch(registerIntent);
        });

        services.getDatabase().getSignedInUser()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        services.getDatabase().subscribeToUser(task.getResult().getId(),
                                (value, error) -> {
                            if (error != null || value == null) {
                                services.getUtility().showDialog(requireActivity(),
                                        Utility.DialogType.INFO,
                                        error.getMessage());
                                return;
                            }
                            if (!value.getFirstName().isEmpty() && !value.getLastName().isEmpty()) {
                                String initials = "" + value.getFirstName().toUpperCase().charAt(0) +
                                        value.getLastName().toUpperCase().charAt(0);

                                // Set default image
                                TextDrawable drawable = TextDrawable.builder()
                                        .beginConfig()
                                        .width(HEADER_IMAGE_SIZE)
                                        .height(HEADER_IMAGE_SIZE)
                                        .endConfig()
                                        .buildRound(initials, MATERIAL.getColor(initials));
                                ImageView imageFrame = view.findViewById(R.id.account_image);
                                imageFrame.setImageDrawable(drawable);
                            }

                            TextView firstName = view.findViewById(R.id.first_name_text_input);
                            firstName.setText(value.getFirstName());

                            TextView lastName = view.findViewById(R.id.last_name_text_input);
                            lastName.setText(value.getLastName());

                            TextView email = view.findViewById(R.id.email_text_input);
                            email.setText(value.getEmail());

                            LinearLayout l = view.findViewById(R.id.businesses);

                            if (value.getBusinesses().isEmpty()) {
                                TextView noBusinessesText = new TextView(requireContext());
                                noBusinessesText.setText("You do not own any businesses");
                                noBusinessesText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                l.addView(noBusinessesText);
                                return;
                            }

                            List<Task<Business>> tasks = new ArrayList<>();
                            for (String businessId : value.getBusinesses()) {
                                tasks.add(services.getDatabase().getBusiness(businessId));
                            }
                            Tasks.whenAllComplete(tasks)
                                    .addOnCompleteListener(businessTasks -> {
                                        if (businessTasks.isSuccessful()) {
                                            for (Task<?> businessTask : businessTasks.getResult()) {
                                                if (!businessTask.isSuccessful() ||
                                                        !(businessTask.getResult() instanceof Business)) {
                                                    Log.e(getString(R.string.app_tag),
                                                            businessTask.getException().getMessage());
                                                    continue;
                                                }
                                                Business business = ((Business) businessTask.getResult());

                                                LinearLayout businessLayout = new LinearLayout(requireContext());
                                                businessLayout.setOrientation(LinearLayout.HORIZONTAL);
                                                TextView businessName = new TextView(requireContext());
                                                businessName.setText(business.getName());
                                                Button viewButton = new Button(requireContext());
                                                viewButton.setText("View");
                                                businessLayout.addView(businessName);
                                                businessLayout.addView(viewButton);
                                                l.addView(businessLayout);
                                            }
                                        } else {
                                            Log.e(getString(R.string.app_tag),
                                                    businessTasks.getException().getMessage());
                                        }
                                    });
                        });
                    } else {
                        ((MainActivity) requireActivity()).goLogin();
                    }
                });
    }
}