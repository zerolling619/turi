package robin.pe.turistea.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import robin.pe.turistea.R;
import robin.pe.turistea.Config;

public class Change_passwordd extends Fragment {

    private com.google.android.material.textfield.TextInputEditText edtCurrent;
    private com.google.android.material.textfield.TextInputEditText edtNew;
    private com.google.android.material.textfield.TextInputEditText edtRepeat;
    private Button btnSubmit;


    public Change_passwordd() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_passwordd, container, false);
        edtCurrent = view.findViewById(R.id.ActualPasswordd);
        edtNew = view.findViewById(R.id.NewPasswordd);
        edtRepeat = view.findViewById(R.id.edtRepetitPasswordd);
        btnSubmit = view.findViewById(R.id.btnIniciarSesion);

        // Configurar botón de volver
        android.widget.ImageView icBack = view.findViewById(R.id.IcBack);
        if (icBack != null) {
            icBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> attemptChangePassword());
        }

        return view;
    }

    private void attemptChangePassword() {
        String current = edtCurrent != null && edtCurrent.getText() != null ? edtCurrent.getText().toString().trim() : "";
        String newPass = edtNew != null && edtNew.getText() != null ? edtNew.getText().toString().trim() : "";
        String repeat = edtRepeat != null && edtRepeat.getText() != null ? edtRepeat.getText().toString().trim() : "";

        if (current.isEmpty()) {
            Toast.makeText(getContext(), "Ingresa tu contraseña actual", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPass.isEmpty()) {
            Toast.makeText(getContext(), "Ingresa la nueva contraseña", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPass.length() < 6) {
            Toast.makeText(getContext(), "La nueva contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPass.equals(repeat)) {
            Toast.makeText(getContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE);
        String jwt = prefs.getString("jwt", null);
        if (jwt == null || jwt.isEmpty()) {
            Toast.makeText(getContext(), "Sesión no válida. Inicia sesión nuevamente.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Procesando...");

        new Thread(() -> {
            try {
                org.json.JSONObject body = new org.json.JSONObject();
                body.put("current_password", current);
                body.put("new_password", newPass);

                // Usar getBaseUrl() para detectar automáticamente emulador o dispositivo real
                String changePasswordUrl = Config.getBaseUrl() + "/api/user-account/updatepassword";
                android.util.Log.d("ChangePassword", "=== INICIANDO CAMBIO DE CONTRASEÑA ===");
                android.util.Log.d("ChangePassword", "URL: " + changePasswordUrl);
                android.util.Log.d("ChangePassword", "Método: PUT");
                android.util.Log.d("ChangePassword", "JWT: " + (jwt != null ? jwt.substring(0, Math.min(20, jwt.length())) + "..." : "NULL"));

                java.net.URL url = new java.net.URL(changePasswordUrl);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                
                // Configurar método PUT
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Authorization", "Bearer " + jwt);
                conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                
                // Workaround para PUT requests en HttpURLConnection
                conn.setFixedLengthStreamingMode(0);

                String jsonBody = body.toString();
                android.util.Log.d("ChangePassword", "Request body: " + jsonBody);
                
                // Escribir el body después de configurar FixedLengthStreamingMode
                byte[] input = jsonBody.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                conn.setFixedLengthStreamingMode(input.length);

                java.io.OutputStream os = conn.getOutputStream();
                os.write(input, 0, input.length);
                os.flush();
                os.close();

                int code = conn.getResponseCode();
                android.util.Log.d("ChangePassword", "Response code: " + code);

                String responseText;
                try {
                    java.io.InputStream inputStream = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
                    if (inputStream != null) {
                        java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) sb.append(line);
                        br.close();
                        responseText = sb.toString();
                    } else {
                        responseText = "";
                    }
                    android.util.Log.d("ChangePassword", "Response: " + responseText);
                } catch (Exception e) {
                    android.util.Log.e("ChangePassword", "Error reading response: " + e.getMessage(), e);
                    responseText = "";
                }

                final int finalCode = code;
                final String finalResponse = responseText;
                requireActivity().runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Restablecer Contraseña");

                    if (finalCode == java.net.HttpURLConnection.HTTP_OK || finalCode == 204) {
                        Toast.makeText(getContext(), "Contraseña actualizada correctamente", Toast.LENGTH_LONG).show();
                        // Volver atrás
                        requireActivity().onBackPressed();
                    } else if (finalCode == 400 || finalCode == 401) {
                        // Posible contraseña actual incorrecta
                        String msg = "No se pudo actualizar. Verifica tu contraseña actual.";
                        try {
                            if (finalResponse != null && !finalResponse.isEmpty()) {
                                org.json.JSONObject j = new org.json.JSONObject(finalResponse);
                                if (j.has("message")) msg = j.optString("message", msg);
                                else if (j.has("error")) msg = j.optString("error", msg);
                                else if (j.has("msg")) msg = j.optString("msg", msg);
                            }
                        } catch (Exception ignored) { }
                        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                    } else {
                        String msg = "Error del servidor (" + finalCode + ")";
                        try {
                            if (finalResponse != null && !finalResponse.isEmpty()) {
                                org.json.JSONObject j = new org.json.JSONObject(finalResponse);
                                if (j.has("message")) msg = j.optString("message", msg);
                                else if (j.has("error")) msg = j.optString("error", msg);
                                else if (j.has("msg")) msg = j.optString("msg", msg);
                            }
                        } catch (Exception ignored) { }
                        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                    }
                });

            } catch (java.net.SocketTimeoutException e) {
                android.util.Log.e("ChangePassword", "Timeout: " + e.getMessage(), e);
                requireActivity().runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Restablecer Contraseña");
                    Toast.makeText(getContext(), "Tiempo de espera agotado. Verifica tu conexión.", Toast.LENGTH_LONG).show();
                });
            } catch (java.net.UnknownHostException e) {
                android.util.Log.e("ChangePassword", "Host desconocido: " + e.getMessage(), e);
                requireActivity().runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Restablecer Contraseña");
                    Toast.makeText(getContext(), "No se pudo conectar al servidor. Verifica la configuración.", Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                android.util.Log.e("ChangePassword", "Error: " + e.getMessage(), e);
                requireActivity().runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Restablecer Contraseña");
                    Toast.makeText(getContext(), "Error de conexión: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}