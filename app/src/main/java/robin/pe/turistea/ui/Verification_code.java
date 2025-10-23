package robin.pe.turistea.ui;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import robin.pe.turistea.R;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class Verification_code extends Fragment {
    private String correo;
    private NavController navController;
    private EditText tvCodigo1, tvCodigo2, tvCodigo3, tvCodigo4;
    private String codigoCompleto = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtener el correo de los argumentos
        if (getArguments() != null) {
            correo = getArguments().getString("correo", "");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_verification_code, container, false);
        
        // Obtener referencias a los TextView del código
        tvCodigo1 = view.findViewById(R.id.tvCodigo1);
        tvCodigo2 = view.findViewById(R.id.tvCodigo2);
        tvCodigo3 = view.findViewById(R.id.tvCodigo3);
        tvCodigo4 = view.findViewById(R.id.tvCodigo4);
        
        // Mostrar el correo en el TextView correspondiente
        TextView tvCorreo = view.findViewById(R.id.tvCorreoVerificacion);
        if (tvCorreo != null) {
            tvCorreo.setText("Enviar a " + correo);
        }
        
        // Configurar los TextView para que funcionen como campos de entrada
        configurarCamposCodigo();
        
        // Botón para enviar el código
        Button btnEnviar = view.findViewById(R.id.btnIniciarSesion);
        btnEnviar.setOnClickListener(v -> {
            if (codigoCompleto.length() == 4) {
                if (navController != null) {
                    new VerificarCodigoTask(correo, codigoCompleto, navController, getContext()).execute();
                } else {
                    Toast.makeText(getContext(), "Error de navegación", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Ingresa el código completo de 4 dígitos", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Configurar reenviar código
        TextView tvReenviar = view.findViewById(R.id.tvReenviar);
        tvReenviar.setOnClickListener(v -> {
            // Aquí puedes implementar la lógica para reenviar el código
            Toast.makeText(getContext(), "Reenviando código...", Toast.LENGTH_SHORT).show();
        });
        
        return view;
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Inicializar NavController de manera segura
        try {
            navController = Navigation.findNavController(view);
        } catch (Exception e) {
            android.util.Log.e("Verification_code", "Error al inicializar NavController: " + e.getMessage());
        }
    }
    
    private void configurarCamposCodigo() {
        // Configurar auto-avance entre campos
        setupEditText(tvCodigo1, null, tvCodigo2);
        setupEditText(tvCodigo2, tvCodigo1, tvCodigo3);
        setupEditText(tvCodigo3, tvCodigo2, tvCodigo4);
        setupEditText(tvCodigo4, tvCodigo3, null);
        
        // Poner foco en el primer campo
        tvCodigo1.requestFocus();
    }
    
    private void setupEditText(EditText currentField, EditText previousField, EditText nextField) {
        currentField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Si se ingresó un dígito, avanzar al siguiente campo
                if (s.length() == 1 && nextField != null) {
                    nextField.requestFocus();
                }
                
                // Actualizar código completo
                actualizarCodigoCompleto();
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                // Si está vacío y se presiona backspace, retroceder al campo anterior
                if (s.length() == 0 && previousField != null) {
                    previousField.requestFocus();
                }
            }
        });
        
        // Permitir que al hacer clic seleccione todo el texto
        currentField.setOnClickListener(v -> currentField.selectAll());
    }
    
    private void actualizarCodigoCompleto() {
        codigoCompleto = tvCodigo1.getText().toString() +
                        tvCodigo2.getText().toString() +
                        tvCodigo3.getText().toString() +
                        tvCodigo4.getText().toString();
        
        android.util.Log.d("Verification", "Código actual: " + codigoCompleto + " (length=" + codigoCompleto.length() + ")");
    }

    // Tarea asíncrona para verificar el código
    private static class VerificarCodigoTask extends AsyncTask<Void, Void, String> {
        private String correo, codigo;
        private NavController navController;
        private Context context;
        VerificarCodigoTask(String correo, String codigo, NavController navController, Context context) {
            this.correo = correo;
            this.codigo = codigo;
            this.navController = navController;
            this.context = context;
        }
        @Override
        protected String doInBackground(Void... voids) {
            try {
                java.net.URL url = new java.net.URL("http://10.0.2.2:4001/api/active/verifycode");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                org.json.JSONObject jsonParam = new org.json.JSONObject();
                jsonParam.put("email", correo);
                jsonParam.put("code_verification", codigo);
                java.io.OutputStream os = conn.getOutputStream();
                os.write(jsonParam.toString().getBytes("UTF-8"));
                os.flush();
                os.close();
                int responseCode = conn.getResponseCode();
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
                    return response.toString();
                }
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    // Intentar parsear como JSON para verificar si hay mensaje de éxito
                    org.json.JSONObject json = new org.json.JSONObject(result);
                    if (json.has("message") && json.getString("message").contains("correcto")) {
                        // Verificación exitosa
                        Toast.makeText(context, "¡Cuenta verificada correctamente!", Toast.LENGTH_LONG).show();
                        navController.navigate(R.id.navigation_login);
                    } else {
                        // Error en la verificación
                        String errorMsg = json.has("message") ? json.getString("message") : "Código incorrecto";
                        Toast.makeText(context, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    // Si no es JSON, verificar si contiene palabras de éxito
                    if (result.contains("correcto") || result.contains("éxito") || result.contains("verificaci") || result.contains("success")) {
                        Toast.makeText(context, "¡Cuenta verificada correctamente!", Toast.LENGTH_LONG).show();
                        navController.navigate(R.id.navigation_login);
                    } else {
                        Toast.makeText(context, "Código incorrecto o error: " + result, Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                Toast.makeText(context, "Error de conexión. Intenta nuevamente.", Toast.LENGTH_LONG).show();
            }
        }
    }
}