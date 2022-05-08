package com.example.findappointment;

import static com.amulyakhare.textdrawable.util.ColorGenerator.MATERIAL;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.example.findappointment.data.User;
import com.example.findappointment.services.Utility;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.navigation.NavigationView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int HEADER_IMAGE_SIZE = 150;

    private AppBarConfiguration appBarConfiguration;
    private Services services;
    private NavigationView navigationView;
    private NavController navController;
    private DrawerLayout drawer;
    private ActivityResultLauncher<Intent> launcher;

    private void setupNotLogged() {
        navigationView.getMenu().clear();
        getMenuInflater().inflate(R.menu.logout_menu, navigationView.getMenu());
        navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.getMenu().getItem(2).setOnMenuItemClickListener(menuItem -> {
            launchRegisterActivity();
            return false;
        });
        TextView accountName = findViewById(R.id.header_account_name);
        accountName.setText(R.string.nav_header_title);
        ImageView imageFrame = findViewById(R.id.header_account_image);
        imageFrame.setImageResource(R.mipmap.ic_guest);
    }

    private void setupLogged() {
        navigationView.getMenu().clear();
        getMenuInflater().inflate(R.menu.login_user_menu, navigationView.getMenu());
        navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.getMenu().getItem(4).setOnMenuItemClickListener(menuItem -> {
            services.getDatabase().logout();
            services.getUtility().showToast(this, "Successfully logged out");
            goHome();
            hideNav();
            return false;
        });
        services.getDatabase().getSignedInUser().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User user = task.getResult();

                // Set name
                TextView accountName = findViewById(R.id.header_account_name);
                accountName.setText(String.format("%s %s",
                        user.getFirstName(), user.getLastName()));
                if (user.getFirstName().isEmpty() || user.getLastName().isEmpty()) {
                    return;
                }

                String initials = "" + user.getFirstName().toUpperCase().charAt(0) +
                        user.getLastName().toUpperCase().charAt(0);

                // Set default image
                TextDrawable drawable = TextDrawable.builder()
                        .beginConfig()
                        .width(HEADER_IMAGE_SIZE)
                        .height(HEADER_IMAGE_SIZE)
                        .endConfig()
                        .buildRound(initials, MATERIAL.getColor(initials));
                ImageView imageFrame = findViewById(R.id.header_account_image);
                imageFrame.setImageDrawable(drawable);
            } else {
                services.getUtility().showDialog(this, Utility.DialogType.ERROR,
                        task.getException().getMessage());
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        services = ((MainApplication) getApplication()).getServices();

        com.example.findappointment.databinding.ActivityMainBinding binding =
                com.example.findappointment.databinding.ActivityMainBinding
                        .inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        drawer = binding.drawerLayout;
        navigationView = binding.navView;

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.nav_login,
                R.id.nav_appointments,
                R.id.nav_account,
                R.id.nav_feedback)
                .setOpenableLayout(drawer)
                .build();

        navController = Navigation
                .findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI
                .setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        services.getDatabase().onAuthChanged(firebaseAuth -> {
            if (services.getDatabase().isUserLoggedIn()) {
                setupLogged();
            } else {
                setupNotLogged();
            }
        });

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        // Intent data = result.getData();
                        services.getUtility().showToast(this,
                                "Registered successfully");
                        goHome();
                        hideNav();
                    }
                });
    }

    public void hideNav() {
        drawer.closeDrawer(GravityCompat.START);
    }

    public void goHome() {
        navController.navigate(R.id.nav_home);
    }

    public void goLogin() {
        navController.navigate(R.id.nav_login);
    }

    public void launchRegisterActivity() {
        Intent registerIntent = new Intent(this, RegisterUserActivity.class);
        launcher.launch(registerIntent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation
                .findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @NonNull
    public Services getServices() {
        return services;
    }
}