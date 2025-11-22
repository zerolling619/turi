package robin.pe.turistea.ui;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import robin.pe.turistea.R;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.navigation.Navigation;
import org.json.JSONObject;
import android.widget.TextView;

public class Password_change_code extends Fragment {

    private EditText tvCodigo1, tvCodigo2, tvCodigo3, tvCodigo4;
    private Button btnIniciarSesion;
    private String correo;
    private TextView tvCorreoVerificacion;
    private TextView tvReenviar;

    public Password_change_code() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            correo = getArguments().getString("correo", "");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_password_change_code, container, false);

        tvCodigo1 = view.findViewById(R.id.tvCodigo1);
        tvCodigo2 = view.findViewById(R.id.tvCodigo2);
        tvCodigo3 = view.findViewById(R.id.tvCodigo3);
        tvCodigo4 = view.findViewById(R.id.tvCodigo4);
        btnIniciarSesion = view.findViewById(R.id.btnIniciarSesion);
        tvCorreoVerificacion = view.findViewById(R.id.tvCorreoVerificacion);
        tvReenviar = view.findViewById(R.id.tvReenviar);

        if (!correo.isEmpty()) {
            tvCorreoVerificacion.setText("Enviar a " + correo);
        }

        // ACTIVAR AUTO-AVANCE DE LOS CAMPOS
        configurarCamposCodigo();

        btnIniciarSesion.setOnClickListener(v -> {
            String code = tvCodigo1.getText().toString()
                    + tvCodigo2.getText().toString()
                    + tvCodigo3.getText().toString()
                    + tvCodigo4.getText().toString();

            if (code.length() != 4) {
                Toast.makeText(getContext(), "Ingrese el código de 4 dígitos", Toast.LENGTH_SHORT).show();
                return;
            }

            btnIniciarSesion.setEnabled(false);
            btnIniciarSesion.setText("Validando...");

            new Thread(() -> {
                try {
                    String baseUrl = robin.pe.turistea.Config.getBaseUrl();
                    java.net.URL url = new java.net.URL(baseUrl + "/api/active/verifycode");
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setDoOutput(true);

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("email", correo);
                    jsonParam.put("code_verification", code);

                    java.io.OutputStream os = conn.getOutputStream();
                    os.write(jsonParam.toString().getBytes("UTF-8"));
                    os.flush();
                    os.close();

                    int responseCode = conn.getResponseCode();

                    java.io.InputStream is = (responseCode >= 200 && responseCode < 300)
                            ? conn.getInputStream()
                            : conn.getErrorStream();

                    StringBuilder sb = new StringBuilder();
                    if (is != null) {
                        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is));
                        String line;
                        while ((line = reader.readLine()) != null)
                            sb.append(line);
                        reader.close();
                    }

                    String responseStr = sb.toString();

                    requireActivity().runOnUiThread(() -> {
                        btnIniciarSesion.setEnabled(true);
                        btnIniciarSesion.setText("Enviar");

                        if (responseCode >= 200 && responseCode < 300) {
                            Toast.makeText(getContext(), "Código verificado", Toast.LENGTH_SHORT).show();

                            Bundle args = new Bundle();
                            args.putString("correo", correo);
                            args.putString("code_verification", code);

                            Navigation.findNavController(view)
                                    .navigate(R.id.action_password_change_code_to_new_password, args);

                        } else {
                            String msg = "El código es incorrecto";
                            try {
                                JSONObject jo = new JSONObject(responseStr);
                                if (jo.has("message"))
                                    msg = jo.getString("message");
                            } catch (Exception ignored) {}

                            Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                        }
                    });

                } catch (Exception e) {
                    requireActivity().runOnUiThread(() -> {
                        btnIniciarSesion.setEnabled(true);
                        btnIniciarSesion.setText("Enviar");
                        Toast.makeText(getContext(), "Error de conexión: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }).start();
        });

        tvReenviar.setOnClickListener(v -> {
            tvReenviar.setEnabled(false);
            tvReenviar.setText("Reenviando...");

            new Thread(() -> {
                try {
                    String baseUrl = robin.pe.turistea.Config.getBaseUrl();
                    java.net.URL url = new java.net.URL(baseUrl + "/api/sendcodeverification");
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();

                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setDoOutput(true);

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("email", correo);

                    java.io.OutputStream os = conn.getOutputStream();
                    os.write(jsonParam.toString().getBytes("UTF-8"));
                    os.flush();
                    os.close();

                    int responseCode = conn.getResponseCode();

                    requireActivity().runOnUiThread(() -> {
                        tvReenviar.setEnabled(true);
                        tvReenviar.setText("Reenviar");

                        if (responseCode >= 200 && responseCode < 300) {
                            Toast.makeText(getContext(), "Código reenviado a: " + correo, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "No se pudo reenviar el código", Toast.LENGTH_LONG).show();
                        }
                    });

                } catch (Exception e) {
                    requireActivity().runOnUiThread(() -> {
                        tvReenviar.setEnabled(true);
                        tvReenviar.setText("Reenviar");
                        Toast.makeText(getContext(), "Error de conexión: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }).start();
        });

        return view;
    }

    private void configurarCamposCodigo() {
        setupEditText(tvCodigo1, null, tvCodigo2);
        setupEditText(tvCodigo2, tvCodigo1, tvCodigo3);
        setupEditText(tvCodigo3, tvCodigo2, tvCodigo4);
        setupEditText(tvCodigo4, tvCodigo3, null);

        tvCodigo1.requestFocus();
    }

    private void setupEditText(EditText currentField, EditText previousField, EditText nextField) {
        currentField.addTextChangedListener(new android.text.TextWatcher() {

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1 && nextField != null) {
                    nextField.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                if (s.length() == 0 && previousField != null) {
                    previousField.requestFocus();
                }
            }
        });

        currentField.setOnClickListener(v -> currentField.selectAll());
    }
}
