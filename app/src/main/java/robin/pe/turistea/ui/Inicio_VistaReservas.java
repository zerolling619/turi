package robin.pe.turistea.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import robin.pe.turistea.R;
import robin.pe.turistea.Config;
import robin.pe.turistea.MainActivity;

public class Inicio_VistaReservas extends Fragment implements ReservasAdapter.OnItemClickListener {

    private ImageView icMenuLateral;
    private RecyclerView recyclerViewReservas;
    private ReservasAdapter adapter;
    private List<JSONObject> listaReservas = new ArrayList<>();

    public Inicio_VistaReservas() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inicio_vista_reservas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        icMenuLateral = view.findViewById(R.id.IcMenuLateral);
        icMenuLateral.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openDrawer();
            }
        });

        recyclerViewReservas = view.findViewById(R.id.recyclerViewReservas);
        recyclerViewReservas.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ReservasAdapter(listaReservas, this);
        recyclerViewReservas.setAdapter(adapter);

        // Asignar listeners a los botones de filtro
        view.findViewById(R.id.btnStatusPending).setOnClickListener(v -> loadReservesFromBackend("pending"));
        view.findViewById(R.id.btnStatusConfirmed).setOnClickListener(v -> loadReservesFromBackend("confirmed"));
        view.findViewById(R.id.btnStatusCompleted).setOnClickListener(v -> loadReservesFromBackend("completed"));
        view.findViewById(R.id.btnStatusCancelled).setOnClickListener(v -> loadReservesFromBackend("cancelled"));

        // Cargar las reservas pendientes por defecto al iniciar
        loadReservesFromBackend("pending");
    }

    private void loadReservesFromBackend(String status) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                String jwt = prefs.getString("jwt", "");

                // **LA CORRECCIÓN ESTÁ AQUÍ**
                // Se añaden los parámetros page=1 y state=1 que el backend espera
                String urlString = Config.BASE_URL + "/api/user-account/form_reserves?page=1&status=" + status + "&state=1";

                Log.d("Inicio_VistaReservas", "Llamando a la URL: " + urlString);

                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + jwt);

                int responseCode = conn.getResponseCode();
                
                InputStream inputStream = (responseCode == HttpURLConnection.HTTP_OK)
                        ? conn.getInputStream() : conn.getErrorStream();
                
                if (inputStream == null) {
                     if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error: No se recibió respuesta del servidor", Toast.LENGTH_SHORT).show());
                    }
                    return;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                String responseData = response.toString();
                Log.d("Inicio_VistaReservas", "Respuesta completa del backend: " + responseData);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    JSONObject responseObject = new JSONObject(responseData);
                    JSONObject dataObject = responseObject.getJSONObject("data");
                    JSONArray reservesArray = dataObject.getJSONArray("rows");

                    listaReservas.clear();
                    for (int i = 0; i < reservesArray.length(); i++) {
                        listaReservas.add(reservesArray.getJSONObject(i));
                    }

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            adapter.notifyDataSetChanged();
                            if (listaReservas.isEmpty()) {
                                Toast.makeText(getContext(), "No hay reservas con estado '" + status + "'", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                     if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error al cargar reservas. Código: " + responseCode, Toast.LENGTH_SHORT).show());
                    }
                }
            } catch (Exception e) {
                Log.e("Inicio_VistaReservas", "Error en loadReservesFromBackend", e);
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show());
                }
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }

    @Override
    public void onItemClick(JSONObject item) {
        try {
            int reserveId = item.getInt("id");
            String statusForm = item.getString("status_form"); // Obtener el estado

            Bundle bundle = new Bundle();
            bundle.putInt("reserve_id", reserveId);
            bundle.putString("status_form", statusForm); // **AÑADIR ESTA LÍNEA**

            if (getView() != null) {
                Navigation.findNavController(getView()).navigate(R.id.action_navigation_inicio_VistaReservas_to_reservationDetail, bundle);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error al abrir el detalle", Toast.LENGTH_SHORT).show();
        }
    }
}
