package robin.pe.turistea.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import robin.pe.turistea.R;
import robin.pe.turistea.Config;

public class ReservationDetail extends Fragment {

    private int reserveId = -1;

    // Vistas del layout
    private TextView tvPackageTitle;
    private TextView tvUserName;
    private TextView tvUserDocument;
    private TextView tvReserveDate;
    private TextView tvPeopleCount;
    private TextView tvTotalPrice;
    private TextView tvStatus;
    private ImageView icMenuLateral;

    public ReservationDetail() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Recibir el ID de la reserva enviado desde el fragmento anterior
            reserveId = getArguments().getInt("reserve_id", -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reservation_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // **IMPORTANTE**: Asegúrate de que los IDs de abajo coincidan con los de tu XML.
        tvPackageTitle = view.findViewById(R.id.tvDetailPackageTitle); 
        tvUserName = view.findViewById(R.id.tvDetailUserName); 
        //tvUserDocument = view.findViewById(R.id.tvDetailUserDocument); //esto no
        tvReserveDate = view.findViewById(R.id.tvDetailReserveDate); 
        tvPeopleCount = view.findViewById(R.id.tvDetailPeopleCount); 
        //tvTotalPrice = view.findViewById(R.id.tvDetailTotalPrice); //esto no
        //tvStatus = view.findViewById(R.id.tvDetailStatus); //esto no

        icMenuLateral = view.findViewById(R.id.IcMenuLateral);
        if (icMenuLateral != null) {
            icMenuLateral.setOnClickListener(v -> {
                if (getActivity() instanceof robin.pe.turistea.MainActivity) {
                    ((robin.pe.turistea.MainActivity) getActivity()).openDrawer();
                }
            });
        }
        
        if (reserveId != -1) {
            Log.d("ReservationDetail", "ID de reserva recibido: " + reserveId);
            loadReservationDetails();
        } else {
            Log.e("ReservationDetail", "No se recibió un ID de reserva válido.");
            Toast.makeText(getContext(), "No se pudo cargar el detalle.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadReservationDetails() {
        new Thread(() -> {
            try {
                SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                String jwt = prefs.getString("jwt", "");

                // Ruta para obtener el detalle de una reserva específica
                String urlString = Config.BASE_URL + "/api/form_reserves/" + reserveId; 
                URL url = new URL(urlString);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + jwt);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    Log.d("ReservationDetail", "Respuesta del backend: " + response.toString());
                    
                    JSONObject responseObject = new JSONObject(response.toString());
                    JSONObject reservationData = responseObject.optJSONObject("data");
                    if (reservationData == null) {
                       reservationData = responseObject;
                    }

                    final JSONObject finalReservationData = reservationData;
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> updateUi(finalReservationData));
                    }

                } else {
                    Log.e("ReservationDetail", "Error al cargar detalles. Código: " + responseCode);
                     if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error al cargar detalles: " + responseCode, Toast.LENGTH_SHORT).show());
                    }
                }
            } catch (Exception e) {
                Log.e("ReservationDetail", "Error en loadReservationDetails", e);
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }
    
    private void updateUi(JSONObject data) {
        if (data == null) {
            Toast.makeText(getContext(), "No se recibieron datos de la reserva", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            String packageName = "No disponible";
            if (data.has("package") && !data.isNull("package")) {
                packageName = data.getJSONObject("package").optString("title", "No disponible");
            }
            tvPackageTitle.setText(packageName);

            tvUserName.setText(data.optString("full_name", "N/A"));
            tvUserDocument.setText(data.optString("type_document", "") + " - " + data.optString("number_document", "N/A"));
            tvReserveDate.setText("Fecha: " + data.optString("date_reserve", "N/A").substring(0, 10)); // Mostrar solo la fecha
            tvPeopleCount.setText("Personas: " + data.optInt("cant_people", 0));
            //tvTotalPrice.setText("Total: S/ " + data.optString("price_total", "0.00"));
            //tvStatus.setText(data.optString("status_form", "DESCONOCIDO").toUpperCase());

        } catch(Exception e) {
            Log.e("ReservationDetail", "Error al actualizar la UI", e);
            Toast.makeText(getContext(), "Error al mostrar los datos.", Toast.LENGTH_SHORT).show();
        }
    }
}