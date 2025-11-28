package robin.pe.turistea;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import robin.pe.turistea.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    public DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        drawerLayout = findViewById(R.id.drawer_layout);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        navigationView = findViewById(R.id.nav_drawer);

        // Cargar datos del usuario y actualizar visibilidad del menú
        updateDrawerHeader(); // Este método ahora también se encarga de la visibilidad

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_inicio) {
                    navigateBasedOnRole();
                } else if (id == R.id.nav_profile) {
                    navController.navigate(R.id.navigation_profile);
                } else if (id == R.id.navigation_inicio_VistaReservas) { // **AÑADIDO**
                    navController.navigate(R.id.navigation_inicio_VistaReservas);
                } else if (id == R.id.nav_settings) {
                    navController.navigate(R.id.navigation_settings);
                } else if (id == R.id.nav_help) {
                    Toast.makeText(MainActivity.this, "Ayuda - Próximamente", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_logout) {
                    handleLogout();
                }

                drawerLayout.closeDrawers();
                return true;
            }
        });

        navController.addOnDestinationChangedListener( (navController1, navDestination, bundle) ->  {
            navView.setVisibility(View.VISIBLE);
            int id = navDestination.getId();
            if ( id == R.id.navigation_splash ||
                    id == R.id.navigation_login ||
                    id == R.id.navigation_register ||
                    id == R.id.navigation_verification_code )
                navView.setVisibility( View.INVISIBLE );
        } );
    }

    private void loadUserDataInHeader() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userName = prefs.getString("user_name", "Usuario");
        String userEmail = prefs.getString("user_email", "correo@ejemplo.com");
        String userImagePath = prefs.getString("user_image_path", "");

        View headerView = navigationView.getHeaderView(0);
        TextView nameTextView = headerView.findViewById(R.id.nav_header_name);
        TextView emailTextView = headerView.findViewById(R.id.nav_header_email);
        android.widget.ImageView imgProfile = headerView.findViewById(R.id.ImgProfile);

        if (nameTextView != null) nameTextView.setText(userName);
        if (emailTextView != null) emailTextView.setText(userEmail);

        if (imgProfile != null && !userImagePath.isEmpty()) {
            com.bumptech.glide.Glide.with(this)
                    .load(userImagePath)
                    .circleCrop()
                    .placeholder(R.drawable.fd_blanco_circulo)
                    .error(R.drawable.fd_blanco_circulo)
                    .into(imgProfile);
        }
    }

    private void handleLogout() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        navController.navigate(R.id.navigation_login);

        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
    }

    // **NUEVO MÉTODO** para controlar la visibilidad de los ítems del menú
    private void updateDrawerMenuVisibility() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userRole = prefs.getString("user_role", ""); // Default a vacío si no hay rol

        Menu menu = navigationView.getMenu();
        MenuItem misReservasItem = menu.findItem(R.id.navigation_inicio_VistaReservas);

        if (misReservasItem != null) {
            // El ítem solo es visible si el rol es "user" o "admin"
            boolean isVisible = "user".equalsIgnoreCase(userRole) || "admin".equalsIgnoreCase(userRole);
            misReservasItem.setVisible(isVisible);
            android.util.Log.d("MainActivity", "Visibilidad 'Mis Reservas' -> " + isVisible + " (Rol: " + userRole + ")");
        }
    }

    public void openDrawer() {
        if (drawerLayout != null) {
            drawerLayout.open();
        }
    }

    // Este mtodo ahora actualiza tanto el header como la visibilidad del menú
    public void updateDrawerHeader() {
        loadUserDataInHeader();
        updateDrawerMenuVisibility();
    }

    public void navigateToLogin() {
        navController.navigate(R.id.navigation_login);
    }

    private void navigateBasedOnRole() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userRole = prefs.getString("user_role", "user");

        android.util.Log.d("MainActivity", "Navegación desde menú lateral. Rol: '" + userRole + "'");

        if (userRole.equals("driver") || userRole.equals("guide") || userRole.equals("terrace")) {
            navController.navigate(R.id.navigation_inicio_VistaReservas);
        } else {
            navController.navigate(R.id.navigation_inicio);
        }
    }
}