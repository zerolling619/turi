package robin.pe.turistea.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
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
import robin.pe.turistea.MainActivity;

public class ReservationDetail extends Fragment {

    private int reserveId = -1;
    private String statusForm = ""; // **NUEVO**: Variable para el estado
    private NavController navController;

    // Vistas del layout
    private TextView tvUserName, tvPackageTitle, tvMeetingPoint, tvDeparturePoint, tvDate, tvStatus, tvGuideName, tvTerraceName, tvPeopleCount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            reserveId = getArguments().getInt("reserve_id", -1);
            // **NUEVO**: Recibir el estado del Bundle
            statusForm = getArguments().getString("status_form", ""); 
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reservation_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        tvUserName = view.findViewById(R.id.tvDetailUserName);
        tvPackageTitle = view.findViewById(R.id.tvDetailPackageTitle);
        tvMeetingPoint = view.findViewById(R.id.NombreEncuentro);
        tvDeparturePoint = view.findViewById(R.id.NombreSalida);
        tvDate = view.findViewById(R.id.tvDetailReserveDate);
        tvStatus = view.findViewById(R.id.NombreEstado);
        tvGuideName = view.findViewById(R.id.NombreGuide);
        tvTerraceName = view.findViewById(R.id.NombreTerrace);
        tvPeopleCount = view.findViewById(R.id.tvDetailPeopleCount);

        ImageView icMenuLateral = view.findViewById(R.id.IcMenuLateral);
        icMenuLateral.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openDrawer();
            }
        });

        ImageView icBack = view.findViewById(R.id.IcBack);
        icBack.setOnClickListener(v -> navController.navigateUp());

        if (reserveId != -1 && !statusForm.isEmpty()) {
            loadReservationDetails();
        } else {
            Toast.makeText(getContext(), "Error: No se recibieron los datos completos de la reserva.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadReservationDetails() {
        new Thread(() -> {
            try {
                SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                String jwt = prefs.getString("jwt", "");

                // **LA CORRECCIÓN ESTÁ AQUÍ**
                // Se construye la URL con el ID y el estado
                String urlString = Config.BASE_URL + "/api/user-account/form_reserves/" + reserveId + "/" + statusForm;
                Log.d("ReservationDetail", "Llamando a la URL: " + urlString);

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

                    JSONObject reservationData = new JSONObject(response.toString());

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> updateUi(reservationData));
                    }

                } else {
                    Log.e("ReservationDetail", "Error del servidor. Código: " + responseCode);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error al cargar detalles. Código: " + responseCode, Toast.LENGTH_SHORT).show());
                    }
                }
            } catch (Exception e) {
                Log.e("ReservationDetail", "Error en la conexión", e);
            }
        }).start();
    }

    private void updateUi(JSONObject data) {
        if (data == null) return;

        try {
            tvUserName.setText(data.optString("full_name", "N/A"));

            String date = data.optString("date_reserve", "");
            if (date.length() > 10) date = date.substring(0, 10);
            tvDate.setText(date);

            String status = data.optString("status_form", "").toUpperCase();
            tvStatus.setText(status);

            String peopleCount = String.valueOf(data.optInt("cant_people", 0));
            if(tvPeopleCount != null) tvPeopleCount.setText(peopleCount);

            if (data.has("package") && !data.isNull("package")) {
                JSONObject packageObj = data.getJSONObject("package");

                tvPackageTitle.setText(packageObj.optString("title", "N/A"));
                tvMeetingPoint.setText(packageObj.optString("address_initial", "N/A"));
                tvDeparturePoint.setText(packageObj.optString("address_final", "N/A"));

                String guideName = "No seleccionado";
                if (packageObj.has("guide") && !packageObj.isNull("guide")) {
                    JSONObject guideObj = packageObj.getJSONObject("guide");
                    guideName = guideObj.optString("name", "") + " " + guideObj.optString("lastname", "");
                }
                if (tvGuideName != null) tvGuideName.setText(guideName.trim());

                String terraceName = "No seleccionado";
                if (packageObj.has("terrace") && !packageObj.isNull("terrace")) {
                    JSONObject terraceObj = packageObj.getJSONObject("terrace");
                    terraceName = terraceObj.optString("name", "") + " " + terraceObj.optString("lastname", "");
                }
                if (tvTerraceName != null) tvTerraceName.setText(terraceName.trim());
            }

        } catch (Exception e) {
            Log.e("ReservationDetail", "Error al actualizar la UI", e);
            Toast.makeText(getContext(), "Error al mostrar los datos.", Toast.LENGTH_SHORT).show();
        }
    }
}
