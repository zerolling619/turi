package robin.pe.turistea.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import robin.pe.turistea.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
        
        // Leer el JWT guardado en las preferencias compartidas
        SharedPreferences prefs = getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String jwt = prefs.getString("jwt", null);
        if (jwt != null) {
            new FetchProfileTask(view, jwt).execute();
        } else {
            // Si no hay JWT, mostrar mensaje de error en los campos
            TextView tvNombreApellido = view.findViewById(R.id.tvNombreApellido);
            TextView tvEmail = view.findViewById(R.id.textViewEmailValue);
            TextView tvTelefono = view.findViewById(R.id.textViewTelefonoValue);
            if (tvNombreApellido != null) tvNombreApellido.setText("No autenticado");
            if (tvEmail != null) tvEmail.setText("-");
            if (tvTelefono != null) tvTelefono.setText("-");
        }
        return view;
    }

    // Tarea asíncrona para obtener los datos del usuario desde el backend
    private static class FetchProfileTask extends AsyncTask<Void, Void, org.json.JSONObject> {
        private View view;
        private String jwt;
        FetchProfileTask(View view, String jwt) {
            this.view = view;
            this.jwt = jwt;
        }
        @Override
        protected org.json.JSONObject doInBackground(Void... voids) {
            try {
                Log.d("Perfil", "Iniciando petición a: http://10.0.2.2:4001/api/user-account");
                Log.d("Perfil", "JWT usado: " + jwt);
                
                // Validar que el JWT no esté vacío
                if (jwt == null || jwt.trim().isEmpty()) {
                    Log.e("Perfil", "JWT es null o vacío");
                    return null;
                }
                
                java.net.URL url = new java.net.URL("http://10.0.2.2:4001/api/user-account");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + jwt);
                conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/json");
                
                int responseCode = conn.getResponseCode();
                Log.d("Perfil", "Código de respuesta: " + responseCode);
                
                if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    Log.d("Perfil", "Respuesta recibida: " + response.toString());
                    return new org.json.JSONObject(response.toString());
                } else {
                    // Leer el mensaje de error
                    java.io.BufferedReader errorReader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getErrorStream()));
                    StringBuilder errorResponse = new StringBuilder();
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        errorResponse.append(errorLine);
                    }
                    errorReader.close();
                    Log.e("Perfil", "Error en la petición: " + responseCode + " - " + errorResponse.toString());
                }
            } catch (Exception e) {
                Log.e("Perfil", "Excepción al obtener datos: " + e.getMessage(), e);
            }
            return null;
        }
        @Override
        protected void onPostExecute(org.json.JSONObject user) {
            TextView tvNombreApellido = view.findViewById(R.id.tvNombreApellido);
            TextView tvEmail = view.findViewById(R.id.textViewEmailValue);
            TextView tvTelefono = view.findViewById(R.id.textViewTelefonoValue);
            
            if (user != null) {
                try {
                    Log.d("Perfil", "Datos del usuario recibidos: " + user.toString());
                    
                    String nombre = user.optString("name", "");
                    String apellido = user.optString("lastname", "");
                    String email = user.optString("email", "");
                    String dni = user.optString("dni", "");
                    String sexo = user.optString("sexo", "");
                    
                    Log.d("Perfil", "Nombre: " + nombre + ", Apellido: " + apellido + ", Email: " + email);
                    
                    // Mostrar los datos en los TextView
                    if (tvNombreApellido != null) {
                        String nombreCompleto = (nombre + " " + apellido).trim();
                        tvNombreApellido.setText(nombreCompleto.isEmpty() ? "Usuario" : nombreCompleto);
                    }
                    if (tvEmail != null) tvEmail.setText(email.isEmpty() ? "-" : email);
                    // Como el backend no devuelve cellphone, mostramos DNI o sexo si lo prefieres
                    if (tvTelefono != null) {
                        String infoExtra = dni.isEmpty() || dni.equals("null") ? 
                            (sexo.isEmpty() ? "-" : sexo) : dni;
                        tvTelefono.setText(infoExtra);
                    }
                    
                } catch (Exception e) {
                    Log.e("Perfil", "Error al mostrar datos: " + e.getMessage(), e);
                    if (tvNombreApellido != null) tvNombreApellido.setText("Error al mostrar datos");
                    if (tvEmail != null) tvEmail.setText("-");
                    if (tvTelefono != null) tvTelefono.setText("-");
                }
            } else {
                Log.e("Perfil", "No se recibieron datos del usuario");
                // Si no se pudo obtener el usuario, mostrar mensaje
                if (tvNombreApellido != null) tvNombreApellido.setText("No se pudo obtener el perfil");
                if (tvEmail != null) tvEmail.setText("-");
                if (tvTelefono != null) tvTelefono.setText("-");
            }
        }
    }
}