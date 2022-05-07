package com.example.findappointment;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.findappointment.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private Services services;
    private NavigationView navigationView;
    private NavController navController;

    private void setupNotLogged() {
        navigationView.getMenu().clear();
        getMenuInflater().inflate(R.menu.logout_menu, navigationView.getMenu());
        navigationView.getMenu().getItem(0).setChecked(true);
    }

    private void setupLogged() {
        navigationView.getMenu().clear();
        getMenuInflater().inflate(R.menu.login_user_menu, navigationView.getMenu());
        navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.getMenu().getItem(4).setOnMenuItemClickListener(menuItem -> {
            services.getDatabase().logout();
            return true;
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        services = ((MainApplication) getApplication()).getServices();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
//        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        DrawerLayout drawer = binding.drawerLayout;
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

    @NonNull
    public NavController getNavController() {
        return navController;
    }
}