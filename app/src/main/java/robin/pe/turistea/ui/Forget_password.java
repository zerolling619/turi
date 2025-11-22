package robin.pe.turistea.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import robin.pe.turistea.R;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.navigation.Navigation;
import android.content.Context;
import org.json.JSONObject;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;


public class Forget_password extends Fragment {

    private EditText edtCorreo;
    private Button btnEnviarCodigo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forget_password, container, false);
        edtCorreo = view.findViewById(R.id.edtCorreoOlvidado);
        btnEnviarCodigo = view.findViewById(R.id.btnEnviarCodigo);

        ImageView icBack = view.findViewById(R.id.IcBack);
        icBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();  // Retrocede en la navegación
            }
        });

        btnEnviarCodigo.setOnClickListener(v -> {
            edtCorreo.setError(null);
            String correo = edtCorreo.getText().toString().trim();
            if (correo.isEmpty()) {
                edtCorreo.setError("Ingrese su correo");
                edtCorreo.requestFocus();
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(edtCorreo, InputMethodManager.SHOW_IMPLICIT);
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                edtCorreo.setError("Correo inválido");
                edtCorreo.requestFocus();
                return;
            }
            btnEnviarCodigo.setEnabled(false);
            btnEnviarCodigo.setText("Enviando...");
            new Thread(() -> {
                String responseStr = "";
                int responseCode = -1;
                try {
                    String baseUrl = robin.pe.turistea.Config.getBaseUrl();
                    // Endpoint correcto para ENVIAR el código de verificación al correo
                    String endpoint = baseUrl + "/api/sendcodeverification";
                    java.net.URL url = new java.net.URL(endpoint);
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("email", correo);
                    java.io.OutputStream os = conn.getOutputStream();
                    os.write(jsonParam.toString().getBytes("UTF-8"));
                    os.flush(); os.close();
                    responseCode = conn.getResponseCode();
                    java.io.InputStream is = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();
                    StringBuilder sb = new StringBuilder();
                    if (is != null) {
                        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is));
                        String line;
                        while ((line = reader.readLine()) != null) sb.append(line);
                        reader.close();
                    }
                    responseStr = sb.toString();
                    Log.d("ForgetPassword", "[ENVIAR CÓDIGO] Respuesta: " + responseStr + " | Código: " + responseCode);
                } catch (Exception e) {
                    responseStr = e.getMessage();
                    Log.e("ForgetPassword", "Error en envío código", e);
                }
                String msgFinal = "No se pudo enviar el código";
                boolean exito = false;
                try {
                    // Si el código de respuesta es exitoso (200-299), considerar éxito
                    if (responseCode >= 200 && responseCode < 300) {
                        exito = true;
                        if (responseStr != null && !responseStr.isEmpty()) {
                            if (responseStr.startsWith("{")) {
                                JSONObject jo = new JSONObject(responseStr);
                                if (jo.has("message")) {
                                    msgFinal = jo.getString("message");
                                    // Si el mensaje contiene palabras de error, no es éxito
                                    String msgLower = msgFinal.toLowerCase();
                                    if (msgLower.contains("error") || msgLower.contains("fallo") || 
                                        msgLower.contains("no se pudo") || msgLower.contains("invalid")) {
                                        exito = false;
                                    }
                                } else if (jo.has("error")) {
                                    msgFinal = jo.getString("error");
                                    exito = false;
                                } else {
                                    msgFinal = "Código enviado correctamente";
                                }
                            } else {
                                // Respuesta de texto plano
                                String respLower = responseStr.toLowerCase();
                                if (respLower.contains("error") || respLower.contains("fallo") || 
                                    respLower.contains("no se pudo")) {
                                    exito = false;
                                    msgFinal = responseStr;
                                } else {
                                    msgFinal = "Código enviado correctamente";
                                }
                            }
                        } else {
                            msgFinal = "Código enviado correctamente";
                        }
                    } else {
                        // Código de error del servidor
                        if (responseStr != null && !responseStr.isEmpty()) {
                            if (responseStr.startsWith("{")) {
                                JSONObject jo = new JSONObject(responseStr);
                                if (jo.has("message")) msgFinal = jo.getString("message");
                                else if (jo.has("error")) msgFinal = jo.getString("error");
                                else msgFinal = "Error del servidor: " + responseCode;
                            } else {
                                msgFinal = responseStr;
                            }
                        } else {
                            msgFinal = "Error del servidor: " + responseCode;
                        }
                    }
                } catch(Exception e) { 
                    Log.e("ForgetPassword", "Error parseando respuesta", e);
                    if (responseCode >= 200 && responseCode < 300) {
                        exito = true;
                        msgFinal = "Código enviado correctamente";
                    }
                }
                final boolean resultado = exito;
                final String mensajeMostrar = msgFinal;
                requireActivity().runOnUiThread(() -> {
                    btnEnviarCodigo.setEnabled(true);
                    btnEnviarCodigo.setText("Enviar código");
                    if (resultado) {
                        Toast.makeText(getContext(), mensajeMostrar, Toast.LENGTH_SHORT).show();
                        android.os.Bundle args = new android.os.Bundle();
                        args.putString("correo", correo);
                        Navigation.findNavController(getView()).navigate(R.id.action_navigation_forget_password_to_password_change_code, args);
                    } else {
                        Toast.makeText(getContext(), mensajeMostrar, Toast.LENGTH_LONG).show();
                        edtCorreo.setError(mensajeMostrar);
                        edtCorreo.requestFocus();
                    }
                });
            }).start();
        });
        return view;
    }
}