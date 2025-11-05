package robin.pe.turistea.ui;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import robin.pe.turistea.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.TextView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class Profile extends Fragment {

    private NavController navController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Interceptar el botón "Atrás" para navegar según el rol
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateBasedOnRole();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        return view;
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Inicializar NavController después de que la vista esté creada
        navController = Navigation.findNavController(view);

        // Configurar el clic en el icono del menú lateral
        android.widget.ImageView icMenuLateral = view.findViewById(R.id.IcMenuLateral);
        if (icMenuLateral != null) {
            icMenuLateral.setOnClickListener(v -> {
                if (getActivity() instanceof robin.pe.turistea.MainActivity) {
                    ((robin.pe.turistea.MainActivity) getActivity()).openDrawer();
                }
            });
        }

        // Cargar datos del usuario desde SharedPreferences
        SharedPreferences prefs = getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userName = prefs.getString("user_name", "");
        String userEmail = prefs.getString("user_email", "");
        String userCellphone = prefs.getString("user_cellphone", "");
        String userImagePath = prefs.getString("user_image_path", "");

        Log.d("Profile", "Datos cargados desde SharedPreferences:");
        Log.d("Profile", "user_name: '" + userName + "'");
        Log.d("Profile", "user_email: '" + userEmail + "'");
        Log.d("Profile", "user_cellphone: '" + userCellphone + "'");
        Log.d("Profile", "user_image_path: '" + userImagePath + "'");

        // Obtener referencias a los TextView e ImageView
        TextView tvNombreApellido = view.findViewById(R.id.tvNombreApellido);
        TextView tvEmail = view.findViewById(R.id.textViewEmailValue);
        TextView tvTelefono = view.findViewById(R.id.textViewTelefonoValue);
        android.widget.ImageView imgProfile = view.findViewById(R.id.ImgProfile);

        Log.d("Profile", "TextView encontrados:");
        Log.d("Profile", "tvNombreApellido: " + (tvNombreApellido != null ? "OK" : "NULL"));
        Log.d("Profile", "tvEmail: " + (tvEmail != null ? "OK" : "NULL"));
        Log.d("Profile", "tvTelefono: " + (tvTelefono != null ? "OK" : "NULL"));
        Log.d("Profile", "imgProfile: " + (imgProfile != null ? "OK" : "NULL"));

        // Mostrar los datos guardados
        if (tvNombreApellido != null) {
            String displayName = userName.isEmpty() ? "Usuario" : userName;
            tvNombreApellido.setText(displayName);
            Log.d("Profile", "Texto establecido en tvNombreApellido: '" + displayName + "'");
        }
        if (tvEmail != null) {
            String displayEmail = userEmail.isEmpty() ? "-" : userEmail;
            tvEmail.setText(displayEmail);
            Log.d("Profile", "Texto establecido en tvEmail: '" + displayEmail + "'");
        }
        if (tvTelefono != null) {
            String displayPhone = userCellphone.isEmpty() ? "-" : userCellphone;
            tvTelefono.setText(displayPhone);
            Log.d("Profile", "Texto establecido en tvTelefono: '" + displayPhone + "'");
        }

        // Cargar imagen de perfil
        if (imgProfile != null && !userImagePath.isEmpty()) {
            com.bumptech.glide.Glide.with(this)
                .load(userImagePath)
                .circleCrop()
                .placeholder(R.drawable.fd_blanco_circulo)
                .error(R.drawable.fd_blanco_circulo)
                .into(imgProfile);
            Log.d("Profile", "Imagen de perfil cargada desde: " + userImagePath);
        } else {
            Log.d("Profile", "No hay imagen de perfil para cargar");
        }

        // Configurar el botón de cerrar sesión
        android.widget.Button btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);
        if (btnCerrarSesion != null) {
            btnCerrarSesion.setOnClickListener(v -> {
                Log.d("Profile", "Botón cerrar sesión presionado");
                cerrarSesion();
            });
        } else {
            Log.e("Profile", "No se encontró el botón btnCerrarSesion");
        }
    }

    private void cerrarSesion() {
        // Mostrar diálogo de confirmación
        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que quieres cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    Log.d("Profile", "Usuario confirmó cerrar sesión");

                    // Limpiar SharedPreferences
                    SharedPreferences prefs = getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                    prefs.edit().clear().apply();

                    Log.d("Profile", "Datos de usuario eliminados de SharedPreferences");

                    // Mostrar mensaje de confirmación
                    android.widget.Toast.makeText(getContext(), "Sesión cerrada", android.widget.Toast.LENGTH_SHORT).show();

                    // Navegar al fragmento de login
                    if (getActivity() instanceof robin.pe.turistea.MainActivity) {
                        ((robin.pe.turistea.MainActivity) getActivity()).navigateToLogin();
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    Log.d("Profile", "Usuario canceló cerrar sesión");
                    dialog.dismiss();
                })
                .show();
    }
    
    // Función para navegar según el rol del usuario
    private void navigateBasedOnRole() {
        SharedPreferences prefs = getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userRole = prefs.getString("user_role", "user");
        
        android.util.Log.d("Profile", "=== NAVEGACIÓN BASADA EN ROL (DESDE PERFIL) ===");
        android.util.Log.d("Profile", "Rol obtenido de SharedPreferences: '" + userRole + "'");
        
        if (userRole.equals("driver") || userRole.equals("guide")) {
            android.util.Log.d("Profile", "Navegando a vista de reservas (driver/guide)");
            navController.navigate(R.id.navigation_inicio_VistaReservas);
        } else {
            android.util.Log.d("Profile", "Navegando a vista normal de inicio (user/admin)");
            navController.navigate(R.id.navigation_inicio);
        }
    }

}