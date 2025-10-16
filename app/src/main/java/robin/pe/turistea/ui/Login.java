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
                        String jwt = null;
                        if (json.has("JWT")) jwt = json.getString("JWT");
                        else if (json.has("jwt")) jwt = json.getString("jwt");
                        else if (json.has("token")) jwt = json.getString("token");
                        else if (json.has("accessToken")) jwt = json.getString("accessToken");
                        else if (json.has("access_token")) jwt = json.getString("access_token");
                        else if (json.has("authToken")) jwt = json.getString("authToken");
                        
                        // Guardar JWT y datos del usuario
                        android.content.SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                        android.content.SharedPreferences.Editor editor = prefs.edit();
                        
                        if (jwt != null) {
                            editor.putString("jwt", jwt);
                            android.util.Log.d("Login", "JWT guardado: " + jwt);
                        } else {
                            android.util.Log.e("Login", "JWT es null, no se puede guardar");
                        }
                        
                        // Guardar datos del usuario
                        String userName = "";
                        String userEmail = email; // Usar el email ingresado
                        
                        // Intentar obtener el nombre del usuario desde diferentes campos posibles
                        if (json.has("user")) {
                            org.json.JSONObject user = json.getJSONObject("user");
                            if (user.has("name")) userName = user.getString("name");
                            else if (user.has("nombre")) userName = user.getString("nombre");
                            else if (user.has("username")) userName = user.getString("username");
                            else if (user.has("fullName")) userName = user.getString("fullName");
                            
                            if (user.has("email")) userEmail = user.getString("email");
                            else if (user.has("correo")) userEmail = user.getString("correo");
                        } else if (json.has("name")) {
                            userName = json.getString("name");
                        } else if (json.has("nombre")) {
                            userName = json.getString("nombre");
                        } else if (json.has("username")) {
                            userName = json.getString("username");
                        }
                        
                        // Si no se obtuvo nombre, usar el email
                        if (userName.isEmpty()) {
                            userName = userEmail.split("@")[0];
                        }
                        
                        editor.putString("user_name", userName);
                        editor.putString("user_email", userEmail);
                        editor.apply();
                        
                        android.util.Log.d("Login", "Datos guardados - Nombre: " + userName + ", Email: " + userEmail);
                        
                        // Actualizar el header del drawer si la actividad es MainActivity
                        if (getActivity() instanceof robin.pe.turistea.MainActivity) {
                            ((robin.pe.turistea.MainActivity) getActivity()).updateDrawerHeader();
                        }
                        
                        android.widget.Toast.makeText(context, "Login exitoso!", android.widget.Toast.LENGTH_SHORT).show();
                        navController.navigate(R.id.action_navigation_login_to_navigation_inicio);
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
                        // Si la respuesta es el JWT directo
                        android.content.SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                        android.content.SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("jwt", result);
                        
                        // Guardar datos básicos del usuario
                        String userName = email.split("@")[0];
                        editor.putString("user_name", userName);
                        editor.putString("user_email", email);
                        editor.apply();
                        
                        android.util.Log.d("Login", "JWT directo guardado: " + result);
                        
                        // Actualizar el header del drawer si la actividad es MainActivity
                        if (getActivity() instanceof robin.pe.turistea.MainActivity) {
                            ((robin.pe.turistea.MainActivity) getActivity()).updateDrawerHeader();
                        }
                        
                        android.widget.Toast.makeText(context, "JWT detectado - Login exitoso!", android.widget.Toast.LENGTH_SHORT).show();
                        navController.navigate(R.id.action_navigation_login_to_navigation_inicio);
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
























