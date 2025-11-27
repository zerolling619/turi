package robin.pe.turistea.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import robin.pe.turistea.R;
import robin.pe.turistea.databinding.FragmentSplashBinding;

public class Splash extends Fragment {

    private static final long SPLASH_DELAY = 2000; // 2 segundos

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // No es necesario usar View Binding aquí, un layout simple es suficiente.
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Después del retraso, decidir a dónde navegar.
            checkSessionAndNavigate();
        }, SPLASH_DELAY);
    }

    private void checkSessionAndNavigate() {
        if (getContext() == null) {
            return; // Evita errores si el fragmento se destruye.
        }

        SharedPreferences prefs = getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String jwt = prefs.getString("jwt", null);

        NavController navController = Navigation.findNavController(requireView());

        if (jwt != null && !jwt.isEmpty()) {
            // Si hay un token, el usuario ya ha iniciado sesión. Ir al inicio.
            navController.navigate(R.id.action_splash_to_inicio);
        } else {
            // Si no hay token, ir al flujo normal de bienvenida/login.
            navController.navigate(R.id.action_splash_to_start_app);
        }
    }
}
