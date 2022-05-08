package com.example.findappointment.ui.register;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.findappointment.R;
import com.example.findappointment.RegisterActivity;
import com.example.findappointment.Services;
import com.example.findappointment.services.Utility;

public class RegisterUserDetailsFragment extends Fragment {

    private EditText firstNameField;
    private EditText lastNameField;
    private EditText emailField;
    private EditText passwordField;
    private EditText confirmPasswordField;
    private Services services;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        services = ((RegisterActivity) requireActivity()).getServices();
        return inflater.inflate(R.layout.fragment_register_user_details, container, false);
    }

    private void verifyFirstName() {
        String name = firstNameField.getText().toString();
        if (!services.getDatabase().getValidator().isNameValid(name)) {
            firstNameField.setError("First name cannot be empty");
        }
    }

    private void verifyLastName() {
        String name = lastNameField.getText().toString();
        if (!services.getDatabase().getValidator().isNameValid(name)) {
            lastNameField.setError("Last name cannot be empty");
        }
    }

    private void verifyEmail() {
        String email = emailField.getText().toString();
        if (!services.getDatabase().getValidator().isLoginEmailValid(email)) {
            emailField.setError("Email is invalid");
        }
    }

    private void verifyPassword() {
        String password = passwordField.getText().toString();
        if (!services.getDatabase().getValidator().isPasswordValid(password)) {
            passwordField.setError("Password must be at least 4 characters");
        }
    }

    private void verifyConfirmPassword() {
        String password = passwordField.getText().toString();
        String confirmPassword = confirmPasswordField.getText().toString();
        if (!services.getDatabase().getValidator()
                .isConfirmPasswordValid(password, confirmPassword)) {
            confirmPasswordField.setError("Passwords must match");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firstNameField = (EditText) view.findViewById(R.id.first_name_field);
        lastNameField = (EditText) view.findViewById(R.id.last_name_field);
        emailField = (EditText) view.findViewById(R.id.email_field);
        passwordField = (EditText) view.findViewById(R.id.password_field);
        confirmPasswordField = (EditText) view.findViewById(R.id.confirm_password_field);

        firstNameField.setOnFocusChangeListener((elView, hasFocus) -> {
            if (hasFocus) {
                return;
            }
            verifyFirstName();
        });

        lastNameField.setOnFocusChangeListener((elView, hasFocus) -> {
            if (hasFocus) {
                return;
            }
            verifyLastName();
        });

        emailField.setOnFocusChangeListener((elView, hasFocus) -> {
            if (hasFocus) {
                return;
            }
            verifyEmail();
        });

        passwordField.setOnFocusChangeListener((elView, hasFocus) -> {
            if (hasFocus) {
                return;
            }
            verifyPassword();
        });

        confirmPasswordField.setOnFocusChangeListener((elView, hasFocus) -> {
            if (hasFocus) {
                return;
            }
            verifyConfirmPassword();
        });

        // TODO: Remove
        firstNameField.setText("Tania");
        lastNameField.setText("C");
        emailField.setText("tania.c@gmail.com");
        passwordField.setText("abcdef");
        confirmPasswordField.setText("abcdef");

        Button registerButton = view.findViewById(R.id.register_button);
        registerButton.setOnClickListener(elView -> {
            ProgressDialog pd = new ProgressDialog(requireActivity());
            pd.setMessage("Loading ...");
            pd.show();
            verifyFirstName();
            verifyLastName();
            verifyEmail();
            verifyPassword();
            verifyConfirmPassword();
            services.getDatabase().registerUser(firstNameField.getText().toString(),
                    lastNameField.getText().toString(), emailField.getText().toString(),
                    passwordField.getText().toString(), confirmPasswordField.getText().toString())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            pd.dismiss();
                            requireActivity().setResult(Activity.RESULT_OK);
                            requireActivity().finish();
                        } else {
                            pd.dismiss();
                            services.getUtility().showDialog(requireActivity(),
                                    Utility.DialogType.INFO, task.getException().getMessage());
                        }
                    });
        });
    }
}