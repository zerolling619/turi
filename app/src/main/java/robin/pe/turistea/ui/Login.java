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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.content.Intent;


public class Login extends Fragment {

    FragmentLoginBinding binding;
    View view;
    Context context;
    NavController navController;

    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    // private CallbackManager callbackManager; // Facebook login (desactivado por ahora)

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Configurar Google Sign-In
        String webClientId = "556675201105-9vrhtsgv39lg0ho7kbnngak2tb4uhu8t.apps.googleusercontent.com";
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId) // Solicitar ID Token
                        .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
        // Configurar el launcher para Google Sign-In
        googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                handleGoogleSignInResult(task);
            }
        );
        // Configurar Facebook Login
        // callbackManager = CallbackManager.Factory.create(); // Facebook login desactivado
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();
        navController = Navigation.findNavController(view);

        binding.tvOlvidastePasswordd.setOnClickListener( v -> navController.navigate( R.id.navigation_forget_password ) );
        binding.tvRegistrate.setOnClickListener( v -> navController.navigate( R.id.navigation_register ) );
        binding.btnIniciarSesion.setOnClickListener(v -> btnLoginClick());
        requireView().findViewById(R.id.IcGoogle).setOnClickListener(v -> signInWithGoogle());
        // requireView().findViewById(R.id.IcFacebook).setOnClickListener(v -> signInWithFacebook()); // Desactivado Facebook Login en la vista

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
                            
                            // Hacer petición para obtener datos del usuario
                            fetchUserProfile(context, jwt, editor, email);
                        } else {
                            android.util.Log.e("Login", "JWT es null, no se puede guardar");
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
                    android.util.Log.e("Login", "Error al procesar respuesta del servidor: " + e.getMessage(), e);
                    android.util.Log.e("Login", "Respuesta del servidor que causó el error: " + result);
                    
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

    // Método para obtener los datos del usuario usando el JWT
    private void fetchUserProfile(Context context, String jwt, android.content.SharedPreferences.Editor editor, String email) {
        new Thread(() -> {
            try {
                android.util.Log.d("Login", "Obteniendo perfil del usuario con JWT: " + jwt);
                
                java.net.URL url = new java.net.URL("http://10.0.2.2:4001/api/user-account");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + jwt);
                conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/json");
                
                int responseCode = conn.getResponseCode();
                android.util.Log.d("Login", "Código de respuesta del perfil: " + responseCode);
                
                if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    android.util.Log.d("Login", "Perfil del usuario recibido: " + response.toString());
                    org.json.JSONObject userProfile = new org.json.JSONObject(response.toString());
                    
                    // Procesar los datos en el hilo principal
                    requireActivity().runOnUiThread(() -> {
                        try {
                            String userName = userProfile.optString("name", "");
                            String userLastName = userProfile.optString("lastname", "");
                            String userEmail = userProfile.optString("email", email);
                            String userCellphone = userProfile.optString("cellphone", "");
                            
                            android.util.Log.d("Login", "Datos del perfil - Nombre: '" + userName + "', Apellido: '" + userLastName + "', Email: '" + userEmail + "', Cellphone: '" + userCellphone + "'");
                            
                            // Combinar nombre y apellido
                            String fullName = "";
                            if (!userName.isEmpty() && !userLastName.isEmpty()) {
                                fullName = userName + " " + userLastName;
                            } else if (!userName.isEmpty()) {
                                fullName = userName;
                            } else if (!userLastName.isEmpty()) {
                                fullName = userLastName;
                            }
                            
                            // Si no se obtuvo nombre, usar el email de forma más presentable
                            if (fullName.isEmpty()) {
                                String emailUser = userEmail.split("@")[0];
                                fullName = emailUser.replaceAll("([a-z])([A-Z])", "$1 $2")
                                                   .replaceAll("([a-z])([0-9])", "$1 $2")
                                                   .replaceAll("([0-9])([a-z])", "$1 $2")
                                                   .replaceAll("_", " ")
                                                   .replaceAll("-", " ")
                                                   .toLowerCase();
                                
                                String[] words = fullName.split(" ");
                                StringBuilder capitalized = new StringBuilder();
                                for (String word : words) {
                                    if (word.length() > 0) {
                                        capitalized.append(Character.toUpperCase(word.charAt(0)));
                                        if (word.length() > 1) {
                                            capitalized.append(word.substring(1));
                                        }
                                        capitalized.append(" ");
                                    }
                                }
                                fullName = capitalized.toString().trim();
                            }
                            
                            // Guardar los datos del usuario
                            editor.putString("user_name", fullName);
                            editor.putString("user_email", userEmail);
                            editor.putString("user_cellphone", userCellphone);
                            editor.apply();
                            
                            android.util.Log.d("Login", "Perfil guardado - Nombre completo: " + fullName + ", Email: " + userEmail + ", Cellphone: " + userCellphone);
                            
                            // Actualizar el header del drawer si la actividad es MainActivity
                            if (getActivity() instanceof robin.pe.turistea.MainActivity) {
                                ((robin.pe.turistea.MainActivity) getActivity()).updateDrawerHeader();
                            }
                            
                        } catch (Exception e) {
                            android.util.Log.e("Login", "Error al procesar perfil del usuario: " + e.getMessage(), e);
                            // En caso de error, usar datos básicos
                            editor.putString("user_name", email.split("@")[0]);
                            editor.putString("user_email", email);
                            editor.putString("user_cellphone", "");
                            editor.apply();
                        }
                    });
                    
                } else {
                    android.util.Log.e("Login", "Error al obtener perfil: " + responseCode);
                    // En caso de error, usar datos básicos
                    requireActivity().runOnUiThread(() -> {
                        editor.putString("user_name", email.split("@")[0]);
                        editor.putString("user_email", email);
                        editor.putString("user_cellphone", "");
                        editor.apply();
                    });
                }
            } catch (Exception e) {
                android.util.Log.e("Login", "Error al obtener perfil del usuario: " + e.getMessage(), e);
                // En caso de error, usar datos básicos
                requireActivity().runOnUiThread(() -> {
                    editor.putString("user_name", email.split("@")[0]);
                    editor.putString("user_email", email);
                    editor.putString("user_cellphone", "");
                    editor.apply();
                });
            }
        }).start();
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }
    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Usuario autenticado con Google
            String email = account.getEmail();
            String displayName = account.getDisplayName();
            String googleId = account.getId();
            String idToken = account.getIdToken(); // Token para validar con backend
            
            android.util.Log.d("GoogleSignIn", "=== GOOGLE SIGN-IN EXITOSO ===");
            android.util.Log.d("GoogleSignIn", "Usuario: " + displayName + ", Email: " + email);
            android.util.Log.d("GoogleSignIn", "ID Token: " + (idToken != null ? "Recibido" : "NULL"));
            
            // Mostrar mensaje de carga
            android.widget.Toast.makeText(context, "Validando con el servidor...", android.widget.Toast.LENGTH_SHORT).show();
            
            // Dividir el nombre en name y lastname
            String name = "";
            String lastname = "";
            if (displayName != null && !displayName.isEmpty()) {
                String[] nameParts = displayName.split(" ", 2);
                name = nameParts[0];
                lastname = nameParts.length > 1 ? nameParts[1] : "";
            }
            
            // Llamar al backend usando el ID Token
            new GoogleSignInBackendTask(email, name, lastname, googleId, idToken, navController, context).execute();
            
        } catch (ApiException e) {
            android.util.Log.w("GoogleSignIn", "Error al iniciar sesión: " + e.getStatusCode(), e);
            android.widget.Toast.makeText(context, "Error al iniciar sesión con Google. Código: " + e.getStatusCode(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    
    // Tarea asíncrona para registrar/validar usuario de Google con el backend
    private static class GoogleSignInBackendTask extends android.os.AsyncTask<Void, Void, String> {
        private String email, name, lastname, googleId, idToken;
        private NavController navController;
        private Context context;
        
        GoogleSignInBackendTask(String email, String name, String lastname, String googleId, String idToken, NavController navController, Context context) {
            this.email = email;
            this.name = name;
            this.lastname = lastname;
            this.googleId = googleId;
            this.idToken = idToken;
            this.navController = navController;
            this.context = context;
        }
        
        @Override
        protected String doInBackground(Void... voids) {
            try {
                // PASO 1: Intentar login con Google ID Token (más seguro)
                if (idToken != null && !idToken.isEmpty()) {
                    android.util.Log.d("GoogleSignInBackend", "Intentando signin con Google ID Token");
                    java.net.URL url = new java.net.URL("http://10.0.2.2:4001/api/signin-google");
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    
                    org.json.JSONObject jsonParam = new org.json.JSONObject();
                    jsonParam.put("token", idToken);
                    
                    java.io.OutputStream os = conn.getOutputStream();
                    os.write(jsonParam.toString().getBytes("UTF-8"));
                    os.flush();
                    os.close();
                    
                    int responseCode = conn.getResponseCode();
                    android.util.Log.d("GoogleSignInBackend", "signin-google código: " + responseCode);
                    
                    if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        android.util.Log.d("GoogleSignInBackend", "signin-google exitoso: " + response.toString());
                        
                        // Usuario existe, obtener JWT del perfil
                        return "GOOGLE_VERIFIED:" + response.toString();
                    } else {
                        android.util.Log.w("GoogleSignInBackend", "signin-google falló, usuario no existe. Creando...");
                    }
                }
                
                // PASO 2: Si falla o no hay token, crear usuario con signup-social-network
                android.util.Log.d("GoogleSignInBackend", "Creando usuario en backend: " + email);
                java.net.URL url = new java.net.URL("http://10.0.2.2:4001/api/signup-social-network");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                
                org.json.JSONObject jsonParam = new org.json.JSONObject();
                jsonParam.put("email", email);
                jsonParam.put("name", name);
                jsonParam.put("lastname", lastname);
                jsonParam.put("password", "google_" + googleId);
                jsonParam.put("cellphone", "");
                jsonParam.put("sexo", "");
                jsonParam.put("dni", "");
                jsonParam.put("date_of_birth", "");
                jsonParam.put("origin", "google");
                
                android.util.Log.d("GoogleSignInBackend", "Datos enviados: " + jsonParam.toString());
                
                java.io.OutputStream os = conn.getOutputStream();
                os.write(jsonParam.toString().getBytes("UTF-8"));
                os.flush();
                os.close();
                
                int responseCode = conn.getResponseCode();
                android.util.Log.d("GoogleSignInBackend", "signup-social-network código: " + responseCode);
                
                java.io.InputStream inputStream = (responseCode == java.net.HttpURLConnection.HTTP_OK)
                        ? conn.getInputStream() : conn.getErrorStream();
                
                if (inputStream != null) {
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    android.util.Log.d("GoogleSignInBackend", "Respuesta: " + response.toString());
                    return response.toString();
                }
            } catch (Exception e) {
                android.util.Log.e("GoogleSignInBackend", "Error: " + e.getMessage(), e);
                return "ERROR:" + e.getMessage();
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(String result) {
            if (result != null && !result.startsWith("ERROR:")) {
                try {
                    // Caso especial: Usuario verificado con signin-google
                    if (result.startsWith("GOOGLE_VERIFIED:")) {
                        android.util.Log.d("GoogleSignInBackend", "Usuario verificado con Google");
                        android.content.SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                        android.content.SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("user_name", name + " " + lastname);
                        editor.putString("user_email", email);
                        editor.putString("jwt", "google_verified_" + googleId);
                        editor.apply();
                        
                        // Actualizar el header del drawer
                        if (context instanceof robin.pe.turistea.MainActivity) {
                            ((robin.pe.turistea.MainActivity) context).updateDrawerHeader();
                        }
                        
                        android.widget.Toast.makeText(context, "¡Bienvenido " + name + "!", android.widget.Toast.LENGTH_SHORT).show();
                        navController.navigate(R.id.action_navigation_login_to_navigation_inicio);
                        return;
                    }
                    
                    org.json.JSONObject json = new org.json.JSONObject(result);
                    
                    // Obtener el JWT (puede venir como JWT, jwt o token)
                    String jwt = null;
                    if (json.has("JWT")) jwt = json.getString("JWT");
                    else if (json.has("jwt")) jwt = json.getString("jwt");
                    else if (json.has("token")) jwt = json.getString("token");
                    
                    if (jwt != null && !jwt.isEmpty()) {
                        android.util.Log.d("GoogleSignUpBackend", "JWT recibido exitosamente");
                        
                        // Guardar datos en SharedPreferences
                        android.content.SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                        android.content.SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("user_name", name + " " + lastname);
                        editor.putString("user_email", email);
                        editor.putString("jwt", jwt);
                        editor.apply();
                        
                        // Actualizar el header del drawer
                        if (context instanceof robin.pe.turistea.MainActivity) {
                            ((robin.pe.turistea.MainActivity) context).updateDrawerHeader();
                        }
                        
                        android.widget.Toast.makeText(context, "¡Bienvenido " + name + "!", android.widget.Toast.LENGTH_SHORT).show();
                        
                        // Navegar al inicio
                        try {
                            navController.navigate(R.id.action_navigation_login_to_navigation_inicio);
                            android.util.Log.d("GoogleSignUpBackend", "Navegación exitosa");
                        } catch (Exception navEx) {
                            android.util.Log.e("GoogleSignUpBackend", "Error al navegar: " + navEx.getMessage());
                            android.widget.Toast.makeText(context, "Login exitoso. Por favor reinicia la app.", android.widget.Toast.LENGTH_LONG).show();
                        }
                    } else {
                        android.util.Log.e("GoogleSignUpBackend", "No se recibió JWT en la respuesta");
                        android.widget.Toast.makeText(context, "Error: No se recibió token de autenticación", android.widget.Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    android.util.Log.e("GoogleSignUpBackend", "Error al procesar respuesta: " + e.getMessage(), e);
                    android.widget.Toast.makeText(context, "Error al procesar respuesta del servidor", android.widget.Toast.LENGTH_LONG).show();
                }
            } else {
                android.util.Log.e("GoogleSignUpBackend", "Error en backend: " + result);
                android.widget.Toast.makeText(context, "Error al conectar con el servidor", android.widget.Toast.LENGTH_LONG).show();
            }
        }
    }
    // TODO: Facebook Login desactivado, para futuro sólo descomentar todo este bloque:
    /*
    private void signInWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, java.util.Arrays.asList("email", "public_profile"));
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                android.util.Log.d("FacebookLogin", "Inicio de sesión exitoso");
                com.facebook.GraphRequest request = com.facebook.GraphRequest.newMeRequest(accessToken, (object, response) -> {
                    try {
                        String email = object.optString("email", "");
                        String name = object.optString("name", "Usuario");
                        String id = object.optString("id", "");
                        android.util.Log.d("FacebookLogin", "Usuario: " + name + ", Email: " + email);
                        android.content.SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                        android.content.SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("user_name", name);
                        editor.putString("user_email", email);
                        editor.putString("jwt", "facebook_token_" + id); // Token temporal
                        editor.apply();
                        android.widget.Toast.makeText(context, "Inicio de sesión exitoso con Facebook", android.widget.Toast.LENGTH_SHORT).show();
                        navController.navigate(R.id.navigation_profile);
                    } catch (Exception e) {
                        android.util.Log.e("FacebookLogin", "Error al obtener datos del usuario", e);
                    }
                });
                android.os.Bundle parameters = new android.os.Bundle();
                parameters.putString("fields", "id,name,email");
                request.setParameters(parameters);
                request.executeAsync();
            }
            @Override
            public void onCancel() {
                android.util.Log.d("FacebookLogin", "Login cancelado");
                android.widget.Toast.makeText(context, "Login cancelado", android.widget.Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(FacebookException error) {
                android.util.Log.e("FacebookLogin", "Error al iniciar sesión", error);
                android.widget.Toast.makeText(context, "Error al iniciar sesión con Facebook", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (callbackManager != null) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
    */
}
























