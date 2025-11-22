package robin.pe.turistea.ui;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import androidx.navigation.Navigation;

import robin.pe.turistea.R;
import robin.pe.turistea.Config;

import org.json.JSONObject;
import android.util.Log;

public class New_password extends Fragment {

    private TextInputEditText NewPasswordd, edtRepetitPasswordd;
    private Button btnRestablecerPassword;
    private String correo;
    private String codeVerification;

    public New_password() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            correo = getArguments().getString("correo", "");
            codeVerification = getArguments().getString("code_verification", "");
            Log.d("NewPassword", "Argumentos recibidos - correo: " + correo + ", code: " + codeVerification);
        } else {
            Log.e("NewPassword", "No se recibieron argumentos!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_new_password, container, false);

        NewPasswordd = view.findViewById(R.id.NewPasswordd);
        edtRepetitPasswordd = view.findViewById(R.id.edtRepetitPasswordd);
        btnRestablecerPassword = view.findViewById(R.id.btnRestablecerPassword);

        btnRestablecerPassword.setOnClickListener(v -> {
            String pass1 = NewPasswordd.getText().toString();
            String pass2 = edtRepetitPasswordd.getText().toString();

            if (pass1.isEmpty() || pass2.isEmpty()) {
                Toast.makeText(getContext(), "Complete ambos campos", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!pass1.equals(pass2)) {
                Toast.makeText(getContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }
            if (pass1.length() < 6) {
                Toast.makeText(getContext(), "La nueva contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }
            if (correo == null || correo.isEmpty() || codeVerification == null || codeVerification.isEmpty()) {
                Toast.makeText(getContext(), "Error: Faltan datos de verificación. Vuelve a solicitar el código.", Toast.LENGTH_LONG).show();
                return;
            }

            btnRestablecerPassword.setEnabled(false);
            btnRestablecerPassword.setText("Procesando...");

            new Thread(() -> {
                try {
                    String endpoint = Config.getBaseUrl() + "/api/restore";

                    Log.d("NewPassword", "=== INICIANDO RESTAURACIÓN ===");
                    Log.d("NewPassword", "URL: " + endpoint);
                    Log.d("NewPassword", "Correo: " + correo);
                    Log.d("NewPassword", "Código: " + codeVerification);

                    java.net.URL url = new java.net.URL(endpoint);
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();

                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);

                    JSONObject body = new JSONObject();
                    body.put("email", correo);
                    body.put("new_password", pass1);
                    body.put("code_verification", codeVerification);

                    String jsonBody = body.toString();
                    Log.d("NewPassword", "JSON Body: " + jsonBody);

                    byte[] input = jsonBody.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    conn.setFixedLengthStreamingMode(input.length);

                    java.io.OutputStream os = conn.getOutputStream();
                    os.write(input, 0, input.length);
                    os.flush();
                    os.close();

                    int responseCode = conn.getResponseCode();
                    Log.d("NewPassword", "Código respuesta: " + responseCode);

                    java.io.InputStream is = (responseCode >= 200 && responseCode < 300)
                            ? conn.getInputStream() : conn.getErrorStream();

                    StringBuilder sb = new StringBuilder();
                    if (is != null) {
                        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is));
                        String line;
                        while ((line = reader.readLine()) != null) sb.append(line);
                        reader.close();
                    }

                    String responseStr = sb.toString();
                    Log.d("NewPassword", "Respuesta: " + responseStr);

                    requireActivity().runOnUiThread(() -> {
                        btnRestablecerPassword.setEnabled(true);
                        btnRestablecerPassword.setText("Restablecer Contraseña");

                        if (responseCode >= 200 && responseCode < 300) {
                            Toast.makeText(getContext(), "Contraseña cambiada correctamente", Toast.LENGTH_LONG).show();
                            Navigation.findNavController(view).navigate(R.id.action_new_password_to_navigation_login);
                        } else {
                            String msg = "No se pudo cambiar la contraseña";
                            try {
                                if (!responseStr.isEmpty()) {
                                    JSONObject jo = new JSONObject(responseStr);
                                    if (jo.has("message")) msg = jo.getString("message");
                                    else if (jo.has("error")) msg = jo.getString("error");
                                }
                            } catch (Exception e) {
                                Log.e("NewPassword", "Error parseando respuesta: " + e.getMessage());
                            }

                            Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                            Log.e("NewPassword", "Error al cambiar contraseña: " + msg);
                        }
                    });

                } catch (Exception e) {
                    Log.e("NewPassword", "Error en la conexión", e);
                    requireActivity().runOnUiThread(() -> {
                        btnRestablecerPassword.setEnabled(true);
                        btnRestablecerPassword.setText("Restablecer Contraseña");
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }).start();
        });

        return view;
    }
}
