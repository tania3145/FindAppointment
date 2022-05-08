package com.example.findappointment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.findappointment.databinding.ActivityMainBinding;
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
    }

    private void setupLogged() {
        navigationView.getMenu().clear();
        getMenuInflater().inflate(R.menu.login_user_menu, navigationView.getMenu());
        navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.getMenu().getItem(4).setOnMenuItemClickListener(menuItem -> {
            services.getDatabase().logout();
            drawer.closeDrawer(GravityCompat.START);
            services.getUtility().showToast(this, "Successfully logged out");
            return false;
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        services = ((MainApplication) getApplication()).getServices();

        com.example.findappointment.databinding.ActivityMainBinding binding =
                ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        drawer = binding.drawerLayout;
        navigationView = binding.navView;

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_login, R.id.nav_register)
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
                    }
                });
    }

    public void goHome() {
        navController.navigate(R.id.nav_home);
    }

    public void goLogin() {
        navController.navigate(R.id.nav_login);
    }

    public void launchRegisterActivity() {
        Intent registerIntent = new Intent(this, RegisterActivity.class);
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