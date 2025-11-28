package robin.pe.turistea.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import robin.pe.turistea.Config;
import robin.pe.turistea.R;

public class Signing_process extends Fragment {

    private NavController navController;
    private SignatureView signatureView;
    private int reserveId = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            reserveId = getArguments().getInt("reserve_id", -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signing_process, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        signatureView = view.findViewById(R.id.signatureView);
        Button btnSignature = view.findViewById(R.id.BtnSignature);
        Button btnClear = view.findViewById(R.id.BtnClear);
        TextView tvContrato = view.findViewById(R.id.TvContrato);

        tvContrato.setText("Yo, [Nombre del Usuario], acepto los términos y condiciones del servicio de transporte turístico para el paquete [Nombre del Paquete] en la fecha [Fecha de Reserva]. Me comprometo a seguir las indicaciones del guía y respetar las normas de seguridad. Entiendo que la firma digital de este documento tiene la misma validez que una firma manuscrita.");

        btnClear.setOnClickListener(v -> signatureView.clear());

        btnSignature.setOnClickListener(v -> {
            Bitmap signatureBitmap = signatureView.getSignatureBitmap();
            if (signatureBitmap == null) {
                Toast.makeText(getContext(), "Por favor, realiza tu firma", Toast.LENGTH_SHORT).show();
                return;
            }
            String base64Signature = bitmapToBase64(signatureBitmap);
            sendSignatureAndUpdateStatus(base64Signature);
        });
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void sendSignatureAndUpdateStatus(String base64Signature) {
        if (reserveId == -1) {
            Toast.makeText(getContext(), "Error: ID de reserva no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                // **LA CORRECCIÓN ESTÁ AQUÍ**: Una sola llamada a la API
                URL url = new URL(Config.FORM_RESERVE_URL + "/" + reserveId + "/" + "pending_pay");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                String jwt = prefs.getString("jwt", "");
                if (!jwt.isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + jwt);
                }

                // 1. Crear el JSON con AMBOS datos: la firma y el nuevo estado
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("sign_img", base64Signature); // Corregido a sign_img como pediste
                jsonParam.put("status_form", "pending_pay");

                Log.d("SigningProcess", "Enviando JSON al backend: " + jsonParam.toString());

                // 2. Enviar la petición
                conn.setDoOutput(true);
                try(OutputStream os = conn.getOutputStream()) {
                    os.write(jsonParam.toString().getBytes("UTF-8"));
                }

                int responseCode = conn.getResponseCode();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            Toast.makeText(getContext(), "Firma guardada. Procediendo al pago...", Toast.LENGTH_SHORT).show();

                            // 3. Navegar a la siguiente pantalla
                            Bundle bundle = new Bundle();
                            bundle.putInt("reserve_id", reserveId);
                            navController.navigate(R.id.action_signing_process_to_pending_pay, bundle);
                        } else {
                            Log.e("SigningProcess", "Error al actualizar. Código: " + responseCode);
                            Toast.makeText(getContext(), "Error al guardar la firma y actualizar estado.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            } catch (Exception e) {
                Log.e("SigningProcess", "Error de conexión", e);
                if(getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error de conexión.", Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }
}