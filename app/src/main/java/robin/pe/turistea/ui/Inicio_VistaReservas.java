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
    private List<JSONObject> listaReservasFiltradas = new ArrayList<>(); // Nueva lista filtrada

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
        adapter = new ReservasAdapter(listaReservasFiltradas, this);
        recyclerViewReservas.setAdapter(adapter);

        // Inicialmente, lista vacía hasta que cargue
        listaReservasFiltradas.clear();
        adapter.notifyDataSetChanged();

        View btnTodos = view.findViewById(R.id.btnStatusPending); // Ejemplo con btnStatusPending, repetir por cada botón
        View btnPendienteFirma = view.findViewById(R.id.btnStatusPendingSing);
        View btnPendientePago = view.findViewById(R.id.btnStatusPendingPay);
        View btnPagoProceso = view.findViewById(R.id.btnStatusPendingPayinProcess);
        View btnReservado = view.findViewById(R.id.btnStatusReserve);
        View btnEnProcesoViaje = view.findViewById(R.id.btnStatusInProcessTravel);
        View btnCompletado = view.findViewById(R.id.btnStatusCompleted);
        View btnRechazado = view.findViewById(R.id.btnStatusRejected);
        View btnConfirmado = view.findViewById(R.id.btnStatusConfirmed);
        View btnCancelado = view.findViewById(R.id.btnStatusCancelled);
        // Puedes agregar más referencias según tus botones

        btnTodos.setOnClickListener(v -> aplicarFiltroStatusForm("pending"));
        btnPendienteFirma.setOnClickListener(v -> aplicarFiltroStatusForm("pending_sign"));
        btnPendientePago.setOnClickListener(v -> aplicarFiltroStatusForm("pending_payment"));
        btnPagoProceso.setOnClickListener(v -> aplicarFiltroStatusForm("payment_in_process"));
        btnReservado.setOnClickListener(v -> aplicarFiltroStatusForm("reserved"));
        btnEnProcesoViaje.setOnClickListener(v -> aplicarFiltroStatusForm("travel_in_process"));
        btnCompletado.setOnClickListener(v -> aplicarFiltroStatusForm("completed"));
        btnRechazado.setOnClickListener(v -> aplicarFiltroStatusForm("rejected"));
        btnConfirmado.setOnClickListener(v -> aplicarFiltroStatusForm("confirmed"));
        btnCancelado.setOnClickListener(v -> aplicarFiltroStatusForm("cancelled"));

        loadReservesFromBackend();
    }

    private void aplicarFiltroStatusForm(String status) {
        listaReservasFiltradas.clear();
        for (JSONObject reserva : listaReservas) {
            String statusForm = reserva.optString("status_", "");
            if (statusForm.equalsIgnoreCase(status)) {
                listaReservasFiltradas.add(reserva);
            }
        }
        adapter.notifyDataSetChanged();
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
                    JSONObject responseObject = new JSONObject(responseData);
                    JSONObject dataObject = responseObject.getJSONObject("data");
                    JSONArray reservesArray = dataObject.getJSONArray("rows");
                    listaReservas.clear();
                    for (int i = 0; i < reservesArray.length(); i++) {
                        listaReservas.add(reservesArray.getJSONObject(i));
                    }
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            // Al cargar, mostrará todas las reservas inicialmente:
                            listaReservasFiltradas.clear();
                            listaReservasFiltradas.addAll(listaReservas);
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
