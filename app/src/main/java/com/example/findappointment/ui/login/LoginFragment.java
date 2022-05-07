package com.example.findappointment.ui.login;

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
import android.widget.EditText;

import com.example.findappointment.MainActivity;
import com.example.findappointment.RegisterActivity;
import com.example.findappointment.R;
import com.example.findappointment.Services;
import com.example.findappointment.services.Utility;

public class LoginFragment extends Fragment {

    private Services services;
    private EditText emailField;
    private EditText passwordField;
    private Button loginButton;
    private Button registerButton;
    private ActivityResultLauncher<Intent> launcher;

    private void goHome() {
        ((MainActivity) requireActivity()).getNavController().navigate(R.id.nav_home);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        services = ((MainActivity) requireActivity()).getServices();
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        // Intent data = result.getData();
                        goHome();
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        emailField = (EditText) view.findViewById(R.id.email_field);
        passwordField = (EditText) view.findViewById(R.id.password_field);
        loginButton = (Button) view.findViewById(R.id.login_button);
        registerButton = (Button) view.findViewById(R.id.register_button);
        emailField.setOnFocusChangeListener((elView, hasFocus) -> {
            if (hasFocus) {
                return;
            }
            String email = emailField.getText().toString();
            if (!services.getDatabase().getValidator().isLoginEmailValid(email)) {
                emailField.setError("Email is invalid");
            }
        });
        loginButton.setOnClickListener(parentView -> {
            // TODO: Remove
            emailField.setText("tania.c@gmail.com");
            passwordField.setText("abcdef");

            String email = emailField.getText().toString();
            String password = passwordField.getText().toString();

            try {
                services.getDatabase().loginUser(email, password);
                goHome();

            } catch (Exception e) {
                services.getUtility().showDialog(requireActivity(), Utility.DialogType.INFO,
                        e.getMessage());
            }
        });
        registerButton.setOnClickListener(parentView -> {
            Intent registerIntent = new Intent(requireActivity(), RegisterActivity.class);
            launcher.launch(registerIntent);
        });
    }
}