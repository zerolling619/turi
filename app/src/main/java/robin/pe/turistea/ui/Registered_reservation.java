package robin.pe.turistea.ui;

import android.content.Context;
import android.content.SharedPreferences;import android.os.Bundle;
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

import org.json.JSONObject; // Importante

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream; // Importante
import java.net.HttpURLConnection;
import java.net.URL;

import robin.pe.turistea.Config;
import robin.pe.turistea.R;

public class Registered_reservation extends Fragment {

    private NavController navController;
    private int reserveId = -1;

    public Registered_reservation() {
        // Required empty public constructor
    }

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
        return inflater.inflate(R.layout.fragment_registered_reservation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        Button btnSeeContract = view.findViewById(R.id.BtnSeeContract);
        btnSeeContract.setOnClickListener(v -> {
            if (reserveId != -1) {
                updateReservationStatus();
            } else {
                Toast.makeText(getContext(), "Error: No se encontró el ID de la reserva.", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnInicio = view.findViewById(R.id.BtnInicio);
        btnInicio.setOnClickListener(v -> {
            navController.navigate(R.id.navigation_inicio);
        });

        TextView tvMensaje = view.findViewById(R.id.TvGenerated);
        tvMensaje.setText("Estamos generando el contrato\n" +
                "porfavor validaremos los documentos y te\n" +
                "entregaremos un contrato de afiliación con el\n" +
                "conductor.");
    }

    private void updateReservationStatus() {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                String newStatus = "pending_sing";
                String status = "pending_sign";
                String urlString = Config.FORM_RESERVE_URL + "/" + reserveId + "/" + newStatus;
                Log.d("Registered_reservation", "Actualizando estado. URL: " + urlString);

                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Accept", "application/json");
                // **LA CORRECCIÓN ESTÁ AQUÍ**
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true); // Habilitar el envío de un cuerpo

                SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                String jwt = prefs.getString("jwt", "");
                if (!jwt.isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + jwt);
                }

                // 1. Crear el objeto JSON con el nuevo estado
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("status_form", status);

                // 2. Enviar el JSON en el cuerpo de la petición
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonParam.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                // --- FIN DE LA CORRECCIÓN ---


                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Estado actualizado. Abriendo contrato...", Toast.LENGTH_SHORT).show();
                            Bundle bundle = new Bundle();
                            bundle.putInt("reserve_id", reserveId);
                            navController.navigate(R.id.action_navigation_registered_reservation_to_navigation_signing_process, bundle);
                        });
                    }
                } else {
                    String errorResponse = "Error desconocido";
                    InputStream errorStream = conn.getErrorStream();
                    if (errorStream != null) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        errorResponse = response.toString();
                    }
                    Log.e("Registered_reservation", "Error " + responseCode + ": " + errorResponse);

                    final String finalErrorResponse = errorResponse;
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Error del servidor: " + finalErrorResponse, Toast.LENGTH_LONG).show();
                        });
                    }
                }
            } catch (Exception e) {
                Log.e("Registered_reservation", "Error en la conexión al actualizar estado", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error de conexión.", Toast.LENGTH_SHORT).show();
                    });
                }
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }
}