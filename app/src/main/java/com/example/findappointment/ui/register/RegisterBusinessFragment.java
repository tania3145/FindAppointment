package com.example.findappointment.ui.register;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.findappointment.R;

public class RegisterBusinessFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register_business, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button noButton = view.findViewById(R.id.no_button);
        noButton.setOnClickListener(elView -> {
            requireActivity().setResult(Activity.RESULT_OK);
            requireActivity().finish();
        });
        Button yesButton = view.findViewById(R.id.yes_button);
        yesButton.setOnClickListener(elView -> {
            RegisterBusinessDetailsFragment fragment = new RegisterBusinessDetailsFragment();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.register_content_view, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }
}