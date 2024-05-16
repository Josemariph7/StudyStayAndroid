package com.example.studystayandroid.view;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.studystayandroid.R;
import com.google.android.material.navigation.NavigationView;

public class DashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Configurar el evento para abrir/cerrar el Navigation Drawer
        setupDrawerContent();
    }

    private void setupDrawerContent() {
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    // Manejar los clics en los elementos del menú
                    menuItem.setChecked(true);
                    // Cerrar el Navigation Drawer al hacer clic en un elemento
                    drawerLayout.closeDrawers();
                    return true;
                }
        );
    }
}
