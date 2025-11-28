package robin.pe.turistea.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import robin.pe.turistea.Config;
import robin.pe.turistea.R;

public class PendingPay extends Fragment {

    private NavController navController;
    private Button btnUploadReceipt;
    private int reserveId = -1;

    // Forma moderna de manejar el resultado de una actividad (como seleccionar una imagen)
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        try {
                            // Convertir la imagen seleccionada a Base64 y enviarla
                            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            String base64Image = bitmapToBase64(bitmap);
                            uploadReceiptAndUpdateStatus(base64Image);
                        } catch (Exception e) {
                            Log.e("PendingPay", "Error al procesar la imagen", e);
                            Toast.makeText(getContext(), "Error al procesar la imagen.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    public PendingPay() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            reserveId = getArguments().getInt("reserve_id", -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pending_pay, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        // Asumo que tu botón para subir el comprobante tiene este ID en el XML
        btnUploadReceipt = view.findViewById(R.id.btnUploadReceipt);

        btnUploadReceipt.setOnClickListener(v -> {
            // Crear un intent para abrir la galería y seleccionar una imagen JPG
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/jpeg");
            imagePickerLauncher.launch(intent);
        });
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // Comprimir la imagen a formato JPG antes de codificar. 80 es un buen balance de calidad/tamaño.
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void uploadReceiptAndUpdateStatus(String base64Image) {
        if (reserveId == -1) {
            Toast.makeText(getContext(), "Error: ID de reserva no válido.", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                // **LA CORRECCIÓN ESTÁ AQUÍ**: URL estándar para actualizar un recurso
                URL url = new URL(Config.FORM_RESERVE_URL + "/" + reserveId + "/" + "pendingpayinprocess");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                // Añadir el token de autorización
                SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                String jwt = prefs.getString("jwt", "");
                if (!jwt.isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + jwt);
                }

                // Crear el cuerpo del JSON con la imagen y el nuevo estado
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("pay_img", base64Image);
                jsonParam.put("status_form", "pending_pay_in_process");

                Log.d("PendingPay", "Enviando JSON al backend: " + jsonParam.toString());

                // Enviar la petición
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonParam.toString().getBytes("UTF-8"));
                }

                int responseCode = conn.getResponseCode();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            Toast.makeText(getContext(), "Comprobante subido. Procesando pago...", Toast.LENGTH_SHORT).show();

                            // Navegar a la siguiente pantalla, pasándole el ID de la reserva
                            Bundle bundle = new Bundle();
                            bundle.putInt("reserve_id", reserveId);
                            navController.navigate(R.id.action_navigation_pendingPay_to_pendingPayInProcess, bundle);
                        } else {
                            Log.e("PendingPay", "Error al subir comprobante. Código: " + responseCode);
                            Toast.makeText(getContext(), "Error al subir el comprobante.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("PendingPay", "Error de conexión al subir el comprobante", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error de conexión.", Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }
}