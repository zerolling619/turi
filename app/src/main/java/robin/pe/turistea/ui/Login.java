package robin.pe.turistea.ui;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import robin.pe.turistea.R;
import robin.pe.turistea.databinding.FragmentLoginBinding;


public class Login extends Fragment {

    FragmentLoginBinding binding;
    View view;
    Context context;
    NavController navController;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate( inflater, container, false );
        return view = binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();
        navController = Navigation.findNavController( view );

        binding.tvOlvidastePasswordd.setOnClickListener( v -> navController.navigate( R.id.navigation_forget_password ) );
        binding.tvRegistrate.setOnClickListener( v -> navController.navigate( R.id.navigation_register ) );
        binding.btnIniciarSesion.setOnClickListener(v -> btnLoginClick() );

        binding.edtCorreo.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilCorreo.setError( null );
            }

            @Override public void afterTextChanged(Editable s) { }
        });

        binding.edtPasswordd.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilPasswordd.setError( null );
            }

            @Override public void afterTextChanged(Editable s) { }
        });

    }

    private void btnLoginClick() {
        String email = binding.edtCorreo.getText().toString().trim();
        String password = binding.edtPasswordd.getText().toString().trim();

        if (email.isEmpty()) {
            binding.tilCorreo.setError("Ingrese su correo");
            return;
        }
        if (password.isEmpty()) {
            binding.tilPasswordd.setError("Ingrese su contraseña");
            return;
        }

        // Toast de depuración
        android.widget.Toast.makeText(context, "Iniciando login...", android.widget.Toast.LENGTH_SHORT).show();
        new LoginTask(email, password).execute();
    }

    private class LoginTask extends android.os.AsyncTask<Void, Void, String> {
        private String email, password;
        private String errorMsg = "";

        LoginTask(String email, String password) {
            this.email = email;
            this.password = password;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                // Log 1: Iniciando petición
                android.util.Log.d("LoginTask", "Iniciando petición a: http://10.0.2.2:4001/api/signin");
                
                java.net.URL url = new java.net.URL("http://10.0.2.2:4001/api/signin");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");

                org.json.JSONObject jsonParam = new org.json.JSONObject();
                jsonParam.put("email", email);
                jsonParam.put("password", password);

                // Log 2: Datos a enviar
                android.util.Log.d("LoginTask", "Enviando datos: " + jsonParam.toString());

                java.io.OutputStream os = conn.getOutputStream();
                os.write(jsonParam.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                
                // Log 3: Código de respuesta
                android.util.Log.d("LoginTask", "Código de respuesta: " + responseCode);
                
                java.io.InputStream inputStream = (responseCode == java.net.HttpURLConnection.HTTP_OK) 
                    ? conn.getInputStream() : conn.getErrorStream();
                
                if (inputStream != null) {
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream));
                    String line;
                    StringBuilder response = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    // Log 4: Respuesta recibida
                    android.util.Log.d("LoginTask", "Respuesta recibida: " + response.toString());
                    return response.toString();
                }
                errorMsg = "Error de conexión: " + responseCode;
                android.util.Log.e("LoginTask", errorMsg);
            } catch (Exception e) {
                errorMsg = "Error: " + e.getMessage();
                android.util.Log.e("LoginTask", "Excepción: " + e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            
            // Log de depuración
            android.util.Log.d("LoginTask", "onPostExecute llamado. Result: " + result);
            android.util.Log.d("LoginTask", "ErrorMsg: " + errorMsg);
            
            if (result != null) {
                // Mostrar respuesta completa para diagnóstico
                android.widget.Toast.makeText(context, "Respuesta: " + result, android.widget.Toast.LENGTH_LONG).show();
                
                try {
                    org.json.JSONObject json = new org.json.JSONObject(result);
                    
                    // Verificar si hay token (múltiples variantes posibles)
                    boolean hasToken = json.has("accessToken") || json.has("token") || json.has("access_token") || 
                                     json.has("jwt") || json.has("JWT") || json.has("authToken");
                    
                    if (hasToken) {
                        // Login exitoso
                        android.widget.Toast.makeText(context, "Login exitoso!", android.widget.Toast.LENGTH_SHORT).show();
                        navController.navigate(R.id.action_navigation_login_to_navigation_profile);
                    } else {
                        // Buscar mensaje de error en diferentes campos
                        String msg = "";
                        if (json.has("message")) msg = json.getString("message");
                        else if (json.has("error")) msg = json.getString("error");
                        else if (json.has("msg")) msg = json.getString("msg");
                        else if (json.has("errorMessage")) msg = json.getString("errorMessage");
                        else msg = "Credenciales inválidas";
                        
                        showError(msg);
                    }
                } catch (Exception e) {
                    // Si no es JSON, verificar si es JWT directo
                    String jwtRegex = "^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_.+/=]*$";
                    if (result.matches(jwtRegex)) {
                        android.widget.Toast.makeText(context, "JWT detectado - Login exitoso!", android.widget.Toast.LENGTH_SHORT).show();
                        navController.navigate(R.id.action_navigation_login_to_navigation_profile);
                    } else {
                        showError("Respuesta inválida del servidor: " + result);
                    }
                }
            } else {
                // Mostrar el error específico
                String finalError = errorMsg.isEmpty() ? "Error desconocido" : errorMsg;
                android.widget.Toast.makeText(context, "Error: " + finalError, android.widget.Toast.LENGTH_LONG).show();
                showError(finalError);
            }
        }

        private void showError(String message) {
            // Error de credenciales: mostrar en ambos campos con mensajes específicos
            if (message.contains("credenciales") || message.contains("incorrecto") || message.contains("inválido")) {
                binding.tilCorreo.setError("Correo o contraseña incorrectos");
                binding.tilPasswordd.setError("Correo o contraseña incorrectos");
            }
            // Error de conexión: mostrar solo en email
            else if (message.contains("conexión") || message.contains("Error de conexión")) {
                binding.tilCorreo.setError("Error de conexión al servidor");
                binding.tilPasswordd.setError(null);
            }
            // Error de servidor: mostrar solo en email
            else if (message.contains("servidor") || message.contains("Respuesta inválida")) {
                binding.tilCorreo.setError("Error del servidor");
                binding.tilPasswordd.setError(null);
            }
            // Otros errores: mostrar en email
            else {
                binding.tilCorreo.setError(message);
                binding.tilPasswordd.setError(null);
            }
        }
    }

}
























