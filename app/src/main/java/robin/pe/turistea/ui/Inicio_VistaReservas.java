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
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import robin.pe.turistea.R;
import robin.pe.turistea.Config;

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
            if (getActivity() instanceof robin.pe.turistea.MainActivity) {
                ((robin.pe.turistea.MainActivity) getActivity()).openDrawer();
            }
        });

        recyclerViewReservas = view.findViewById(R.id.recyclerViewReservas);
        recyclerViewReservas.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ReservasAdapter(listaReservas, this);
        recyclerViewReservas.setAdapter(adapter);

        loadReservesFromBackend();
    }

    private void loadReservesFromBackend() {
        new Thread(() -> {
            try {
                SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                String jwt = prefs.getString("jwt", "");

                String urlString = Config.BASE_URL + "/api/user-account/form_reserves";

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

                    String responseData = response.toString();
                    Log.d("Inicio_VistaReservas", "Respuesta del backend: " + responseData);

                    // **CORRECCIÓN**: Navegar a través del objeto para encontrar el array "rows"
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
                                Toast.makeText(getContext(), "No hay reservas disponibles", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                     if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error al cargar reservas: " + responseCode, Toast.LENGTH_SHORT).show());
                    }
                }
            } catch (Exception e) {
                Log.e("Inicio_VistaReservas", "Error en loadReservesFromBackend", e);
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error al procesar la respuesta del servidor", Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }

    @Override
    public void onItemClick(JSONObject item) {
        try {
            //El ID de la reserva es "id"
            int reserveId = item.getInt("id");
            Bundle bundle = new Bundle();
            bundle.putInt("reserve_id", reserveId);

            if (getView() != null) {
                Navigation.findNavController(getView()).navigate(R.id.action_navigation_inicio_VistaReservas_to_reservationDetail, bundle);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error al abrir el detalle", Toast.LENGTH_SHORT).show();
        }
    }
}
