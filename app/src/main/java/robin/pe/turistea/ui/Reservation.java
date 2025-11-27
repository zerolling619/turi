package robin.pe.turistea.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.content.SharedPreferences;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.AdapterView;

import com.google.android.material.textfield.TextInputEditText;

import robin.pe.turistea.R;
import robin.pe.turistea.Config;

public class Reservation extends Fragment {

    private NavController navController;
    private RouteItem selectedRoute;
    private int packageIdFromResponse = 0;
    private TextInputEditText edtUbiEncuentro;

    public Reservation() {
        // Constructor vacío requerido
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getParentFragmentManager().setFragmentResultListener("location_request", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                String selectedLocation = bundle.getString("selected_location");
                if (edtUbiEncuentro != null && selectedLocation != null) {
                    edtUbiEncuentro.setText(selectedLocation);
                    Log.d("Reservation", "Ubicación recibida: " + selectedLocation);
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_reservation, container, false);

        TextInputEditText edtCantPers = view.findViewById(R.id.edtCantPers);
        TextInputEditText edtDni = view.findViewById(R.id.edtDni);
        TextInputEditText edtNombresyApelli = view.findViewById(R.id.edtNombresyApelli);
        TextInputEditText edtUbiTuristic = view.findViewById(R.id.edtUbiTuristic);
        edtUbiEncuentro = view.findViewById(R.id.edtUbiEncuentro);
        TextView tvDni = view.findViewById(R.id.tvDetallesDestino);
        TextView tvCarnet = view.findViewById(R.id.tvDetallesTracking);
        TextView tvFechaHora = view.findViewById(R.id.TvFechaHora);
        TextView tvPrecio = view.findViewById(R.id.TvPrecio);
        CheckBox cbTerramoza = view.findViewById(R.id.CbTerramoza);
        CheckBox cbGuia = view.findViewById(R.id.CbGuiaTuristic);
        Button btnRegisterReservation = view.findViewById(R.id.BtnRegisterReservation);
        Spinner spinnerRutas = view.findViewById(R.id.spinnerRutas);
        ImageView imLocation = view.findViewById(R.id.ImLocation);

        edtNombresyApelli.setHint("Ingrese su nombre completo");
        edtNombresyApelli.setEnabled(true);

        imLocation.setOnClickListener(v -> {
            if (navController != null) {
                navController.navigate(R.id.navigation_location);
            }
        });

        Bundle args = getArguments();
        int packageId = args != null ? args.getInt("package_id", 0) : 0;
        String packageName = args != null ? args.getString("package_name", "") : "";
        int packageMaxPersonas = args != null ? args.getInt("package_max_personas", 5) : 5;
        double packagePrice = args != null ? (double) args.getFloat("package_price", 0.0f) : 0.0;

        edtUbiTuristic.setText(packageName);
        edtUbiTuristic.setEnabled(false);
        if (tvPrecio != null) {
            tvPrecio.setText("S/ " + String.format("%.2f", packagePrice));
        }
        edtCantPers.setHint("Máx: " + packageMaxPersonas);

        ArrayList<RouteItem> rutasList = new ArrayList<>();
        if (packageId > 0) {
            loadRoutesFromBackend(packageId, rutasList, spinnerRutas, tvPrecio, packagePrice);
        }

        edtCantPers.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String text = edtCantPers.getText().toString();
                if (!text.isEmpty()) {
                    int val = Integer.parseInt(text);
                    if (val < 1) edtCantPers.setText("1");
                    else if (val > packageMaxPersonas) {
                        edtCantPers.setText(String.valueOf(packageMaxPersonas));
                        Toast.makeText(getContext(), "Máximo permitido: " + packageMaxPersonas, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        tvFechaHora.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(requireContext(), (dateView, y, m, d) -> {
                TimePickerDialog timePicker = new TimePickerDialog(requireContext(), (timeView, h, min) -> {
                    String fechaStr = String.format("%04d-%02d-%02d", y, m + 1, d);
                    String horaStr = String.format("%02d:%02d", h, min);
                    tvFechaHora.setText(fechaStr + " " + horaStr);
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                timePicker.show();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });

        final boolean[] esDni = {true};
        tvDni.setOnClickListener(v -> {
            tvDni.setBackgroundResource(R.drawable.fd_btn_gris_oscuro_bordes);
            tvCarnet.setBackgroundResource(R.drawable.fd_btn_gris_bordes);
            edtDni.setHint("Ingrese DNI");
            esDni[0] = true;
        });
        tvCarnet.setOnClickListener(v -> {
            tvCarnet.setBackgroundResource(R.drawable.fd_btn_gris_oscuro_bordes);
            tvDni.setBackgroundResource(R.drawable.fd_btn_gris_bordes);
            edtDni.setHint("Ingrese Carnet Ext");
            esDni[0] = false;
        });

        btnRegisterReservation.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && getView() != null) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }

            String doc = edtDni.getText().toString().trim();
            String nombreCompletoForm = edtNombresyApelli.getText().toString().trim();
            String fechaHora = tvFechaHora.getText().toString().trim();
            String cant = edtCantPers.getText().toString().trim();
            String meetingPoint = edtUbiEncuentro.getText().toString().trim();
            boolean terramoza = cbTerramoza.isChecked();
            boolean guia = cbGuia.isChecked();

            if (doc.isEmpty() || nombreCompletoForm.isEmpty() || fechaHora.isEmpty() || cant.isEmpty() || meetingPoint.isEmpty() || selectedRoute == null) {
                Toast.makeText(getContext(), "Completa todos los campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            int cantidad = Math.min(Integer.parseInt(cant), packageMaxPersonas);

            new Thread(() -> {
                try {
                    URL url = new URL(Config.FORM_RESERVE_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);

                    SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                    String jwt = prefs.getString("jwt", "");
                    if (!jwt.isEmpty()) conn.setRequestProperty("Authorization", "Bearer " + jwt);

                    if (selectedRoute == null) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Error: Debe seleccionar una ruta", Toast.LENGTH_LONG).show();
                        });
                        return;
                    }

                    JSONObject reservaJson = new JSONObject();
                    reservaJson.put("full_name", nombreCompletoForm);
                    reservaJson.put("type_document", esDni[0] ? "DNI" : "CARNET_EXT");
                    reservaJson.put("number_document", doc);
                    reservaJson.put("id_package", packageId);
                    reservaJson.put("id_route", selectedRoute.id);
                    int idRouterTracking = packageIdFromResponse > 0 ? packageIdFromResponse : packageId;
                    reservaJson.put("id_router_tracking", idRouterTracking);
                    reservaJson.put("date_reserve", fechaHora);
                    reservaJson.put("cant_people", cantidad);
                    reservaJson.put("meeting_point", meetingPoint);
                    reservaJson.put("guide", guia ? 1 : 0);
                    reservaJson.put("terrace", terramoza ? 1 : 0);
                    reservaJson.put("price_total", selectedRoute.price);
                    reservaJson.put("status_form", "pending");

                    String userIdStr = prefs.getString("user_id", "");
                    if (!userIdStr.isEmpty()) {
                        reservaJson.put("id_user", Integer.parseInt(userIdStr));
                    }

                    Log.d("Reservation", "JSON a enviar: " + reservaJson.toString());

                    OutputStream os = conn.getOutputStream();
                    os.write(reservaJson.toString().getBytes("UTF-8"));
                    os.flush();
                    os.close();

                    int respCode = conn.getResponseCode();
                    String responseMessage = conn.getResponseMessage();

                    requireActivity().runOnUiThread(() -> {
                        if (respCode == HttpURLConnection.HTTP_CREATED || respCode == HttpURLConnection.HTTP_OK) {
                            Toast.makeText(getContext(), "Reserva registrada correctamente.", Toast.LENGTH_SHORT).show();
                            if (getView() != null) {
                                Navigation.findNavController(getView()).navigate(R.id.action_navigation_reservation_to_navigation_registered_reservation);
                            }
                        } else {
                            Toast.makeText(getContext(), "Error " + respCode + ": " + responseMessage, Toast.LENGTH_LONG).show();
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Error de conexión. Verifica tu internet.", Toast.LENGTH_LONG).show()
                    );
                }
            }).start();
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
    }

    private void loadRoutesFromBackend(int packageId, ArrayList<RouteItem> rutasList, Spinner spinnerRutas, TextView tvPrecio, double packagePrice) {
        new Thread(() -> {
            try {
                URL url = new URL(Config.ROUTER_PACKAGES_URL(packageId));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                String jwt = prefs.getString("jwt", "");
                if (!jwt.isEmpty()) conn.setRequestProperty("Authorization", "Bearer " + jwt);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) response.append(line);
                    reader.close();

                    // **LA CORRECCIÓN ESTÁ AQUÍ**
                    parseRoutesJson(response.toString(), rutasList, spinnerRutas, tvPrecio, packagePrice);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void parseRoutesJson(String jsonResponse, ArrayList<RouteItem> rutasList, Spinner spinnerRutas, TextView tvPrecio, double packagePrice) {
        try {
            // La respuesta de la API es un array de paquetes.
            JSONArray packagesArray = new JSONArray(jsonResponse);
            rutasList.clear();

            // 1. Recorremos el array principal de paquetes.
            for (int i = 0; i < packagesArray.length(); i++) {
                JSONObject packageJson = packagesArray.getJSONObject(i);

                int id = packageJson.optInt("id");
                // 2. Obtenemos el TÍTULO PRINCIPAL de cada paquete.
                String title = packageJson.optString("title", "Ruta sin nombre");
                double price = packageJson.optDouble("price_route", packagePrice);

                // 3. Añadimos el título principal a la lista del spinner.
                if (id > 0 && !title.isEmpty()) {
                    rutasList.add(new RouteItem(id, title, price));
                }
            }

            if (getActivity() == null) return;

            // 4. Actualizamos el Spinner en el hilo principal.
            getActivity().runOnUiThread(() -> {
                ArrayAdapter<RouteItem> adapter = new ArrayAdapter<RouteItem>(requireContext(),
                        android.R.layout.simple_spinner_item, rutasList) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        view.setBackgroundResource(R.drawable.fd_btn_gris_oscuro_bordes);
                        int heightPx = (int) (40 * getResources().getDisplayMetrics().density);
                        int paddingPx = (int) (8 * getResources().getDisplayMetrics().density);
                        view.setMinimumHeight(heightPx);
                        view.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
                        ((TextView) view).setTextColor(getResources().getColor(android.R.color.black));
                        return view;
                    }

                    @Override
                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
                        View view = super.getDropDownView(position, convertView, parent);
                        view.setBackgroundResource(R.drawable.fd_btn_gris_bordes);
                        int heightPx = (int) (40 * getResources().getDisplayMetrics().density);
                        int paddingPx = (int) (8 * getResources().getDisplayMetrics().density);
                        view.setMinimumHeight(heightPx);
                        view.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
                        return view;
                    }
                };
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerRutas.setAdapter(adapter);

                spinnerRutas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedRoute = rutasList.get(position);
                        if (selectedRoute != null && tvPrecio != null) {
                            String precioTexto = "S/ " + String.format("%.2f", selectedRoute.price);
                            tvPrecio.setText(precioTexto);
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });

                if (!rutasList.isEmpty()) {
                    spinnerRutas.setSelection(0);
                }
            });

        } catch (Exception e) {
            Log.e("Reservation", "Error crítico al parsear las rutas", e);
        }
    }

    private static class RouteItem {
        int id;
        String title;
        double price;

        RouteItem(int id, String title, double price) {
            this.id = id;
            this.title = title;
            this.price = price;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}