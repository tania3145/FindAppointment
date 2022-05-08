package com.example.findappointment.ui.login;

import android.app.Activity;
import android.app.ProgressDialog;
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
import android.widget.Spinner;

import com.example.findappointment.MainActivity;
import com.example.findappointment.RegisterActivity;
import com.example.findappointment.R;
import com.example.findappointment.Services;
import com.example.findappointment.data.Business;
import com.example.findappointment.services.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class LoginFragment extends Fragment {

    private Services services;
    private EditText emailField;
    private EditText passwordField;
    private Button loginButton;
    private Button registerButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        services = ((MainActivity) requireActivity()).getServices();
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
        // TODO: Remove
        emailField.setText("tania.c@gmail.com");
        passwordField.setText("abcdef");
        loginButton.setOnClickListener(parentView -> {
            String email = emailField.getText().toString();
            String password = passwordField.getText().toString();

            ProgressDialog pd = new ProgressDialog(requireActivity());
            pd.setMessage("Loading ...");
            pd.show();
            services.getDatabase().loginUser(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            services.getUtility().showToast(requireActivity(),
                                    "Successfully logged in");
                            ((MainActivity) requireActivity()).goHome();
                            pd.dismiss();
                        } else {
                            services.getUtility()
                                    .showDialog(requireActivity(), Utility.DialogType.INFO,
                                            task.getException().getMessage());
                            pd.dismiss();
                        }
                    });
        });
        registerButton.setOnClickListener(parentView -> {
            ((MainActivity) requireActivity()).launchRegisterActivity();
        });
    }
}