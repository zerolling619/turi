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


public class Profile extends Fragment {



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
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
        
        Log.d("Profile", "Datos cargados desde SharedPreferences:");
        Log.d("Profile", "user_name: '" + userName + "'");
        Log.d("Profile", "user_email: '" + userEmail + "'");
        Log.d("Profile", "user_cellphone: '" + userCellphone + "'");
        
        // Obtener referencias a los TextView
        TextView tvNombreApellido = view.findViewById(R.id.tvNombreApellido);
        TextView tvEmail = view.findViewById(R.id.textViewEmailValue);
        TextView tvTelefono = view.findViewById(R.id.textViewTelefonoValue);
        
        Log.d("Profile", "TextView encontrados:");
        Log.d("Profile", "tvNombreApellido: " + (tvNombreApellido != null ? "OK" : "NULL"));
        Log.d("Profile", "tvEmail: " + (tvEmail != null ? "OK" : "NULL"));
        Log.d("Profile", "tvTelefono: " + (tvTelefono != null ? "OK" : "NULL"));
        
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
        
        return view;
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

}