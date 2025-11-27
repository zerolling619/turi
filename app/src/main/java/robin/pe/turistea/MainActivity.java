package robin.pe.turistea;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

    //public static Usuario usuario = null;

    private ActivityMainBinding binding;
    public DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TEMPORAL: Limpiar datos de SharedPreferences para resetear la app
        /* SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        android.util.Log.d("MainActivity", "SharedPreferences limpiado - Datos de usuario eliminados");
        Toast.makeText(this, "Datos limpiados - Inicia sesión de nuevo", Toast.LENGTH_LONG).show(); */
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        drawerLayout = findViewById(R.id.drawer_layout);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Configurar el Navigation Drawer
        navigationView = findViewById(R.id.nav_drawer);
        
        // Cargar datos del usuario en el header
        loadUserDataInHeader();
        
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                
                if (id == R.id.nav_inicio) {
                    navigateBasedOnRole();
                } else if (id == R.id.nav_profile) {
                    navController.navigate(R.id.navigation_profile);
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
        
        if (nameTextView != null) {
            nameTextView.setText(userName);
        }
        if (emailTextView != null) {
            emailTextView.setText(userEmail);
        }
        
        // Cargar imagen de perfil
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
        // Limpiar las preferencias compartidas
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        
        // Navegar al login
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        navController.navigate(R.id.navigation_login);
        
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
    }

    public void openDrawer() {
        if (drawerLayout != null) {
            drawerLayout.open();
        }
    }
    
    public void updateDrawerHeader() {
        loadUserDataInHeader();
    }
    
    public void navigateToLogin() {
        navController.navigate(R.id.navigation_login);
    }
    
    // Función para navegar según el rol del usuario
    private void navigateBasedOnRole() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userRole = prefs.getString("user_role", "user");
        
        android.util.Log.d("MainActivity", "=== NAVEGACIÓN BASADA EN ROL (MENÚ LATERAL) ===");
        android.util.Log.d("MainActivity", "Rol obtenido de SharedPreferences: '" + userRole + "'");
        
        if (userRole.equals("driver") || userRole.equals("guide") || userRole.equals("terrace")) {
            android.util.Log.d("MainActivity", "Navegando a vista de reservas (driver/guide/terrace)");
            navController.navigate(R.id.navigation_inicio_VistaReservas);
        } else {
            android.util.Log.d("MainActivity", "Navegando a vista normal de inicio (user/admin)");
            navController.navigate(R.id.navigation_inicio);
        }
    }

}
