package com.example.findappointment.ui.home;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.findappointment.R;
import com.example.findappointment.Services;
import com.example.findappointment.data.Business;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HomeViewModel extends ViewModel {

    @NonNull
    private final Services services;
    private final MutableLiveData<List<Business>> observableBusinesses;

    public HomeViewModel(@NotNull Services services) {
        this.services = services;
        observableBusinesses = new MutableLiveData<>();
        this.services.getDatabase().getBusinesses(businesses -> {
            observableBusinesses.setValue(businesses);
            return null;
        }, error -> {
            Log.e(getServices().getApplication().getResources().getString(R.string.app_tag),
                    error.getMessage());
            return null;
        });
    }

    public LiveData<List<Business>> getBusinesses() {
        return observableBusinesses;
    }

    @NonNull
    public Services getServices() {
        return services;
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        @NonNull
        private final Services services;

        public Factory(@NonNull Services services) {
            this.services = services;
        }

        @SuppressWarnings("unchecked")
        @Override
        @NonNull
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new HomeViewModel(services);
        }
    }
}