package robin.pe.turistea.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import robin.pe.turistea.R;
import robin.pe.turistea.Config;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.view.inputmethod.InputMethodManager;
import java.io.OutputStream;
import java.util.Calendar;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;

public class Reservation extends Fragment {

    private NavController navController;

    public Reservation() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reservation, container, false);
        
        // NO obtener NavController aquí, se obtendrá en onViewCreated o cuando se necesite

        // Obtener referencias a los campos
        TextInputEditText edtDni = view.findViewById(R.id.edtDni);
        TextInputEditText edtNombresyApelli = view.findViewById(R.id.edtNombresyApelli);
        TextInputEditText edtUbiTuristic = view.findViewById(R.id.edtUbiTuristic);
        TextInputEditText edtCantPers = view.findViewById(R.id.edtCantPers);
        TextView tvDni = view.findViewById(R.id.tvDetallesDestino); // DNI
        TextView tvCarnet = view.findViewById(R.id.tvDetallesTracking); // CARNET EXT
        TextView tvFechaHora = view.findViewById(R.id.TvFechaHora);
        TextView tvUbiTuristica = view.findViewById(R.id.TvLisRoute); // Rutas
        TextView tvPrecio = view.findViewById(R.id.TvPrecio);
        CheckBox cbTerramoza = view.findViewById(R.id.CbTerramoza);
        CheckBox cbGuia = view.findViewById(R.id.CbGuiaTuristic);
        Button btnRegisterReservation = view.findViewById(R.id.BtnRegisterReservation);

        // Campo de nombre completo editable - el usuario puede escribir su nombre
        // No se autocompleta, el usuario debe ingresarlo manualmente
        edtNombresyApelli.setHint("Ingrese su nombre completo");
        edtNombresyApelli.setEnabled(true);

        // Argumentos del paquete
        Bundle args = getArguments();
        int packageId = args != null ? args.getInt("package_id", 0) : 0;
        String packageName = args != null ? args.getString("package_name", "") : "";
        int packageMaxPersonas = args != null ? args.getInt("package_max_personas", 5) : 5;
        double packagePrice = args != null ? (double) args.getFloat("package_price", 0.0f) : 0.0;
        
        // Mostrar nombre del paquete en ubicación turística
        edtUbiTuristic.setText(packageName);
        edtUbiTuristic.setEnabled(false);
        
        // Mostrar precio del paquete
        tvPrecio.setText("S/ " + packagePrice);
        edtCantPers.setHint("Máx: " + packageMaxPersonas);
        
        // Cargar rutas del paquete desde el backend
        if (packageId > 0) {
            loadRoutesFromBackend(packageId, tvUbiTuristica);
        } else {
            tvUbiTuristica.setText("No hay rutas disponibles");
        }

        // Limitar campo cantidad de personas al máximo
        edtCantPers.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String text = edtCantPers.getText().toString();
                if (!text.isEmpty()) {
                    int val = Integer.parseInt(text);
                    if (val > packageMaxPersonas) {
                        edtCantPers.setText(String.valueOf(packageMaxPersonas));
                        Toast.makeText(getContext(), "Máximo permitido: " + packageMaxPersonas, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Lógica para seleccionar fecha y hora
        tvFechaHora.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(requireContext(), (dateView, y, m, d) -> {
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePicker = new TimePickerDialog(requireContext(), (timeView, h, min) -> {
                    String fechaStr = String.format("%04d-%02d-%02d", y, m+1, d);
                    String horaStr = String.format("%02d:%02d", h, min);
                    tvFechaHora.setText(fechaStr + " " + horaStr);
                }, hour, minute, true);
                timePicker.show();
            }, year, month, day);
            datePicker.show();
        });

        // Cambia entre DNI y Carnet
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

        // Botón Registrar Reserva
        btnRegisterReservation.setOnClickListener(v -> {
            // Ocultar teclado
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && getView() != null) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }

            // Validar campos básicos
            String doc = edtDni.getText().toString().trim();
            String nombreCompletoForm = edtNombresyApelli.getText().toString().trim();
            String rutaTur = packageName;
            String fechaHora = tvFechaHora.getText().toString().trim();
            String cant = edtCantPers.getText().toString().trim();
            boolean terramoza = cbTerramoza.isChecked();
            boolean guia = cbGuia.isChecked();
            
            if (doc.isEmpty() || nombreCompletoForm == null || nombreCompletoForm.isEmpty() || fechaHora.isEmpty() || cant.isEmpty()) {
                Toast.makeText(getContext(), "Completa todos los campos.", Toast.LENGTH_SHORT).show();
                return;
            }
            
            int cantidad = Math.min(Integer.parseInt(cant), packageMaxPersonas);

            // Separar nombre y apellido del nombre completo
            String[] nombreParts = nombreCompletoForm.split(" ", 2);
            String soloNombre = nombreParts.length > 0 && nombreParts[0] != null ? nombreParts[0].trim() : "";
            String soloApellido = nombreParts.length > 1 && nombreParts[1] != null ? nombreParts[1].trim() : "";

            // Enviar reserva al backend con estado "pending"
            new Thread(() -> {
                try {
                    android.util.Log.d("Reservation", "=== ENVIANDO RESERVA AL BACKEND ===");
                    
                    URL url = new URL(Config.FORM_RESERVE_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);
                    
                    // Obtener JWT del usuario autenticado
                    SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                    String jwt = prefs.getString("jwt", "");
                    
                    if (!jwt.isEmpty()) {
                        conn.setRequestProperty("Authorization", "Bearer " + jwt);
                        android.util.Log.d("Reservation", "JWT agregado al header");
                    } else {
                        android.util.Log.w("Reservation", "No hay JWT - usuario no autenticado");
                    }

                    // Crear JSON con todos los datos de la reserva según la estructura de la tabla form_reserve
                    JSONObject reservaJson = new JSONObject();
                    
                    // Campos según la estructura de la tabla
                    reservaJson.put("full_name", nombreCompletoForm); // Nombre completo
                    reservaJson.put("type_document", esDni[0] ? "DNI" : "CARNET_EXT");
                    reservaJson.put("number_document", doc);
                    reservaJson.put("id_package", packageId); // ID del paquete (no el nombre)
                    reservaJson.put("date_reserve", fechaHora); // Fecha y hora de reserva
                    reservaJson.put("cant_people", cantidad);
                    reservaJson.put("guide", guia ? 1 : 0); // tinyint(1) - 1 o 0
                    reservaJson.put("terrace", terramoza ? 1 : 0); // tinyint(1) - 1 o 0
                    reservaJson.put("price_total", packagePrice); // Precio total
                    reservaJson.put("status_form", "pending"); // Estado pendiente
                    
                    // id_user - el backend puede obtenerlo del JWT, pero lo intentamos obtener si está disponible
                    String userIdStr = prefs.getString("user_id", "");
                    if (!userIdStr.isEmpty()) {
                        try {
                            reservaJson.put("id_user", Integer.parseInt(userIdStr));
                        } catch (NumberFormatException e) {
                            android.util.Log.w("Reservation", "No se pudo parsear user_id: " + userIdStr);
                        }
                    }
                    // Si no hay user_id, el backend debería obtenerlo del JWT token
                    
                    android.util.Log.d("Reservation", "Datos a enviar: " + reservaJson.toString());

                    OutputStream os = conn.getOutputStream();
                    os.write(reservaJson.toString().getBytes("UTF-8"));
                    os.flush();
                    os.close();

                    int respCode = conn.getResponseCode();
                    android.util.Log.d("Reservation", "Código de respuesta: " + respCode);
                    
                    // Leer respuesta del servidor
                    String responseMessage = "";
                    if (respCode == HttpURLConnection.HTTP_CREATED || respCode == HttpURLConnection.HTTP_OK) {
                        java.io.InputStream inputStream = conn.getInputStream();
                        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        responseMessage = response.toString();
                        android.util.Log.d("Reservation", "✅ Reserva creada exitosamente: " + responseMessage);
                    } else {
                        // Leer mensaje de error
                        java.io.InputStream errorStream = conn.getErrorStream();
                        if (errorStream != null) {
                            java.io.BufferedReader errorReader = new java.io.BufferedReader(new java.io.InputStreamReader(errorStream));
                            StringBuilder errorResponse = new StringBuilder();
                            String line;
                            while ((line = errorReader.readLine()) != null) {
                                errorResponse.append(line);
                            }
                            errorReader.close();
                            responseMessage = errorResponse.toString();
                            android.util.Log.e("Reservation", "❌ Error " + respCode + ": " + responseMessage);
                        }
                    }
                    
                    final int finalRespCode = respCode;
                    final String finalResponseMessage = responseMessage;
                    
                    requireActivity().runOnUiThread(() -> {
                        if (finalRespCode == HttpURLConnection.HTTP_CREATED || finalRespCode == HttpURLConnection.HTTP_OK) {
                            Toast.makeText(getContext(), "Reserva registrada correctamente.", Toast.LENGTH_SHORT).show();
                            // Navegar al fragmento de reserva registrada
                            View rootView = getView();
                            if (rootView != null) {
                                try {
                                    NavController nav = Navigation.findNavController(rootView);
                                    nav.navigate(R.id.action_navigation_reservation_to_navigation_registered_reservation);
                                    android.util.Log.d("Reservation", "Navegando a fragment_registered_reservation");
                                } catch (Exception navEx) {
                                    android.util.Log.e("Reservation", "Error al navegar: " + navEx.getMessage(), navEx);
                                    Toast.makeText(getContext(), "Reserva creada pero error al navegar", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            android.util.Log.e("Reservation", "Error al registrar: " + finalResponseMessage);
                            Toast.makeText(getContext(), "Error al registrar reserva: " + finalResponseMessage, Toast.LENGTH_LONG).show();
                        }
                    });

                } catch (Exception e) {
                    android.util.Log.e("Reservation", "❌ Excepción al enviar reserva: " + e.getMessage(), e);
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
        // Inicializar NavController aquí, cuando la vista ya está completamente creada
        navController = Navigation.findNavController(view);
    }
    
    private void loadRoutesFromBackend(int packageId, TextView tvRoutes) {
        new Thread(() -> {
            try {
                android.util.Log.d("Reservation", "=== CARGANDO RUTAS DESDE BACKEND ===");
                android.util.Log.d("Reservation", "Package ID: " + packageId);
                
                java.net.URL url = new java.net.URL(Config.ROUTER_PACKAGES_URL + packageId);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/json");
                
                // Obtener JWT del usuario autenticado
                SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                String jwt = prefs.getString("jwt", "");
                
                if (!jwt.isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + jwt);
                }
                
                int responseCode = conn.getResponseCode();
                android.util.Log.d("Reservation", "Código de respuesta: " + responseCode);
                
                if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                    java.io.InputStream inputStream = conn.getInputStream();
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    String jsonResponse = response.toString();
                    android.util.Log.d("Reservation", "✅ Rutas recibidas");
                    
                    // Parsear y mostrar las rutas
                    parseRoutesJson(jsonResponse, tvRoutes);
                } else {
                    android.util.Log.e("Reservation", "❌ Error al cargar rutas: " + responseCode);
                    requireActivity().runOnUiThread(() -> 
                        tvRoutes.setText("Sin rutas")
                    );
                }
                
            } catch (Exception e) {
                android.util.Log.e("Reservation", "❌ Error al cargar rutas: " + e.getMessage(), e);
                requireActivity().runOnUiThread(() -> 
                    tvRoutes.setText("Error al cargar")
                );
            }
        }).start();
    }
    
    private void parseRoutesJson(String jsonResponse, TextView tvRoutes) {
        try {
            org.json.JSONObject packageJson = new org.json.JSONObject(jsonResponse);
            String routeJsonString = packageJson.optString("route_json", "");
            
            StringBuilder routesText = new StringBuilder();
            
            if (!routeJsonString.isEmpty()) {
                try {
                    org.json.JSONArray routesArray = new org.json.JSONArray(routeJsonString);
                    
                    // Mostrar solo las primeras 2-3 rutas de forma compacta
                    int maxRoutes = Math.min(routesArray.length(), 3);
                    for (int i = 0; i < maxRoutes; i++) {
                        if (routesArray.isNull(i)) continue;
                        
                        try {
                            org.json.JSONObject routeJson = routesArray.getJSONObject(i);
                            String routeTitle = routeJson.optString("title", "");
                            if (!routeTitle.isEmpty()) {
                                if (routesText.length() > 0) {
                                    routesText.append(", ");
                                }
                                routesText.append(routeTitle);
                            }
                        } catch (org.json.JSONException e) {
                            continue;
                        }
                    }
                    
                    if (routesArray.length() > maxRoutes) {
                        routesText.append("...");
                    }
                    
                } catch (org.json.JSONException e) {
                    android.util.Log.e("Reservation", "Error al parsear route_json: " + e.getMessage());
                    routesText.append("Rutas disponibles");
                }
            } else {
                routesText.append("Sin rutas");
            }
            
            String finalRoutesText = routesText != null ? routesText.toString().trim() : "";
            if (finalRoutesText == null || finalRoutesText.isEmpty()) {
                finalRoutesText = "Sin rutas";
            }
            
            // Actualizar UI en el hilo principal
            final String routesToShow = finalRoutesText; // Variable final para usar en el lambda
            requireActivity().runOnUiThread(() -> {
                if (tvRoutes != null) {
                    tvRoutes.setText(routesToShow);
                }
            });
            
        } catch (org.json.JSONException e) {
            android.util.Log.e("Reservation", "❌ Error al parsear respuesta: " + e.getMessage(), e);
            requireActivity().runOnUiThread(() -> 
                tvRoutes.setText("Error al cargar")
            );
        }
    }
}