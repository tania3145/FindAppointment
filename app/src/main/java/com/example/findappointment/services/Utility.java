package com.example.findappointment.services;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.findappointment.R;

public class Utility {

    public enum DialogType {
        INFO, WARNING, ERROR
    }

    private Application application;

    public Utility(@NonNull Application application) {
        this.application = application;
    }

    private void showInfoDialog(Activity activity, DialogType type, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        switch (type) {
            case INFO: alertDialog.setTitle("Info"); break;
            case WARNING: alertDialog.setTitle("Warning"); break;
            case ERROR: alertDialog.setTitle("Error"); break;
            default: break;
        }
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Ok",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    public void showDialog(Activity activity, DialogType type, String message) {
        if (type == DialogType.INFO) {
            Log.i(application.getResources().getString(R.string.app_tag), message);
        } else if (type == DialogType.WARNING) {
            Log.w(application.getResources().getString(R.string.app_tag), message);
        }
        showInfoDialog(activity, type, message);
    }

    public void showToast(Activity activity, String message) {
        showToast(activity, message, Toast.LENGTH_LONG);
    }

    public void showToast(Activity activity, String message, int duration) {
        Toast.makeText(activity, message, duration).show();
    }
}
