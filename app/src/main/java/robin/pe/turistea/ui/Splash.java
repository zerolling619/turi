package robin.pe.turistea.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import robin.pe.turistea.R;

public class Splash extends Fragment {

    private NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        // Un Handler para esperar unos segundos antes de navegar
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (getContext() == null) return;

            SharedPreferences prefs = getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            String jwt = prefs.getString("jwt", null);
            String userRole = prefs.getString("user_role", ""); // Obtener el rol

            // **LA LÓGICA ESTÁ AQUÍ**
            if (jwt != null && !jwt.isEmpty()) {
                // Si hay una sesión activa, decidir a dónde ir según el rol
                if ("driver".equalsIgnoreCase(userRole) || "guide".equalsIgnoreCase(userRole) || "terrace".equalsIgnoreCase(userRole)) {
                    // Si es un rol especial, ir a la vista de reservas
                    navController.navigate(R.id.action_splash_to_inicio_vista_reservas); // Necesitarás crear esta acción
                } else {
                    // Si es user, admin o un rol no especificado, ir al inicio normal
                    navController.navigate(R.id.action_splash_to_inicio);
                }
            } else {
                // Si no hay sesión, ir a la pantalla de "Start App" o Login
                navController.navigate(R.id.action_splash_to_start_app);
            }

        }, 2000); // 2 segundos de espera
    }
}