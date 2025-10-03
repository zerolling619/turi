package robin.pe.turistea;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import robin.pe.turistea.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    //public static Usuario usuario = null;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

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

}