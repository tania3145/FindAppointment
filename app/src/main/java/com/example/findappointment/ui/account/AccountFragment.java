package com.example.findappointment.ui.account;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.findappointment.MainApplication;
import com.example.findappointment.R;
import com.example.findappointment.RegisterBusinessActivity;
import com.example.findappointment.Services;

public class AccountFragment extends Fragment {
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

        Button button = view.findViewById(R.id.button);
        button.setOnClickListener(elView -> {
            Intent registerIntent = new Intent(requireContext(),
                    RegisterBusinessActivity.class);
            launcher.launch(registerIntent);
        });
    }
}