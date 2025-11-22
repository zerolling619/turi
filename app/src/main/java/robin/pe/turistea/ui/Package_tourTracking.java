package robin.pe.turistea.ui;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import robin.pe.turistea.R;
import robin.pe.turistea.Config;

public class Package_tourTracking extends Fragment {

    private NavController navController;
    private ImageView icBack;
    private TextView tvDetallesDestino;
    private TextView tvDetallesTracking;
    private TextView tvCalificacion;
    private TextView tvTemperatura;
    private TextView tvDias;
    private Button btnReservar;
    
    // Datos del paquete
    private int packageId;
    private String packageName;
    private String packageDescription;
    private String packageImage;
    private double packagePrice;
    private String packageLocation;
    private int packageDuration;

    public Package_tourTracking() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Obtener los datos del bundle
        if (getArguments() != null) {
            packageId = getArguments().getInt("package_id", 0);
            packageName = getArguments().getString("package_name", "");
            packageDescription = getArguments().getString("package_description", "");
            packageImage = getArguments().getString("package_image", "");
            packagePrice = getArguments().getDouble("package_price", 0.0);
            packageLocation = getArguments().getString("package_location", "");
            packageDuration = getArguments().getInt("package_duration", 1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // 1. Inflar la vista
        View view = inflater.inflate(R.layout.fragment_package_tour_tracking, container, false);

        // 4. Retornar la vista
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        navController = Navigation.findNavController(view);
        
        // Inicializar vistas
        icBack = view.findViewById(R.id.IcBack);
        tvDetallesDestino = view.findViewById(R.id.tvDetallesDestino);
        tvDetallesTracking = view.findViewById(R.id.tvDetallesTracking);
        tvCalificacion = view.findViewById(R.id.TvCalificacion);
        tvTemperatura = view.findViewById(R.id.TvTemperatura);
        tvDias = view.findViewById(R.id.TvDias);
        btnReservar = view.findViewById(R.id.btnReservar);
        
        // Configurar botón de atrás - navegar al inicio
        icBack.setOnClickListener(v -> {
            android.util.Log.d("Package_tourTracking", "Navegando de vuelta al inicio");
            // Limpiar el back stack y navegar al inicio
            navController.popBackStack(R.id.navigation_inicio, false);
        });
        
        // Configurar botón de Detalles
        tvDetallesDestino.setOnClickListener(v -> {
            // Navegar de vuelta a detalles con los mismos datos
            Bundle bundle = new Bundle();
            bundle.putInt("package_id", packageId);
            bundle.putString("package_name", packageName);
            bundle.putString("package_description", packageDescription);
            bundle.putString("package_image", packageImage);
            bundle.putDouble("package_price", packagePrice);
            bundle.putString("package_location", packageLocation);
            bundle.putInt("package_duration", packageDuration);
            
            navController.navigate(R.id.action_navigation_package_tourTracking_to_navigation_packageTour, bundle);
        });
        
        // Configurar botón de Tracking (ya está seleccionado)
        tvDetallesTracking.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Ya estás en Tracking", Toast.LENGTH_SHORT).show();
        });
        
        // Configurar botón de reservar
        if (btnReservar != null) {
            android.util.Log.d("Package_tourTracking", "Botón reservar encontrado, configurando listener");
            btnReservar.setOnClickListener(v -> {
                try {
                    android.util.Log.d("Package_tourTracking", "Botón reservar presionado");
                    
                    if (navController == null) {
                        android.util.Log.e("Package_tourTracking", "navController es null");
                        Toast.makeText(getContext(), "Error de navegación", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Crear bundle con los datos del paquete para pasar a Reservation
                    Bundle bundle = new Bundle();
                    bundle.putInt("package_id", packageId);
                    bundle.putString("package_name", packageName != null ? packageName : "");
                    bundle.putString("package_description", packageDescription != null ? packageDescription : "");
                    bundle.putString("package_image", packageImage != null ? packageImage : "");
                    bundle.putFloat("package_price", (float) packagePrice);
                    bundle.putString("package_location", packageLocation != null ? packageLocation : "");
                    bundle.putInt("package_duration", packageDuration);
                    bundle.putInt("package_max_personas", 10); // Máximo por defecto, ajustar según tu lógica
                    
                    android.util.Log.d("Package_tourTracking", "Navegando a reserva con datos: " + packageName);
                    navController.navigate(R.id.action_navigation_package_tourTracking_to_navigation_reservation, bundle);
                } catch (Exception e) {
                    android.util.Log.e("Package_tourTracking", "Error al navegar a reserva: " + e.getMessage(), e);
                    e.printStackTrace();
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error al abrir el formulario de reserva", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            android.util.Log.e("Package_tourTracking", "btnReservar es null - el botón no se encontró en el layout");
        }
        
        // Cargar datos del paquete
        loadPackageData();
    }
    
    private void loadPackageData() {
        // Actualizar los TextViews con los datos del paquete
        if (tvDias != null) {
            tvDias.setText(packageDuration + " Días");
        }
        
        if (tvCalificacion != null) {
            tvCalificacion.setText("4.5");
        }
        
        if (tvTemperatura != null) {
            tvTemperatura.setText("25°C");
        }
        
        // Actualizar nombre del destino y ubicación
        View rootView = getView();
        if (rootView != null) {
            TextView tvDestinoNombre = rootView.findViewById(R.id.tvDestinoNombre);
            if (tvDestinoNombre != null) {
                tvDestinoNombre.setText(packageName);
            }
            
            TextView tvCiudadPais = rootView.findViewById(R.id.tvCiudadPais);
            if (tvCiudadPais != null) {
                tvCiudadPais.setText(packageLocation + ", Perú");
            }
        }
        
        // Cargar rutas desde el backend
        loadRoutesFromBackend();
        
        android.util.Log.d("Package_tourTracking", "Datos de tracking cargados para: " + packageName);
    }
    
    private void loadRoutesFromBackend() {
        new Thread(() -> {
            try {
                android.util.Log.d("Package_tourTracking", "=== CARGANDO RUTAS DESDE BACKEND ===");
                android.util.Log.d("Package_tourTracking", "Package ID: " + packageId);
                
                java.net.URL url = new java.net.URL(Config.ROUTER_PACKAGES_URL(packageId) );
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/json");
                
                // Obtener JWT del usuario autenticado
                SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                String jwt = prefs.getString("jwt", "");
                
                android.util.Log.d("Package_tourTracking", "JWT encontrado: " + (!jwt.isEmpty() ? "SÍ" : "NO"));
                
                if (!jwt.isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + jwt);
                }
                
                int responseCode = conn.getResponseCode();
                android.util.Log.d("Package_tourTracking", "Código de respuesta: " + responseCode);
                
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
                    android.util.Log.d("Package_tourTracking", "✅ Rutas recibidas: " + jsonResponse.substring(0, Math.min(200, jsonResponse.length())) + "...");
                    
                    // Parsear y mostrar las rutas
                    parseRoutesJson(jsonResponse);
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
                        android.util.Log.e("Package_tourTracking", "❌ Error " + responseCode + ": " + errorResponse.toString());
                    }
                    
                    requireActivity().runOnUiThread(() -> 
                        Toast.makeText(getContext(), "No se pudieron cargar las rutas", Toast.LENGTH_SHORT).show()
                    );
                }
                
            } catch (Exception e) {
                android.util.Log.e("Package_tourTracking", "❌ Error al cargar rutas: " + e.getMessage(), e);
                requireActivity().runOnUiThread(() -> 
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
    
    private void parseRoutesJson(String jsonResponse) {
        try {
            // El backend devuelve un objeto JSON, no un array
            org.json.JSONObject packageJson = new org.json.JSONObject(jsonResponse);
            
            android.util.Log.d("Package_tourTracking", "Paquete recibido: " + packageJson.optString("title", "Sin título"));
            
            // Obtener el campo route_json que es un String
            String routeJsonString = packageJson.optString("route_json", "");
            
            android.util.Log.d("Package_tourTracking", "route_json encontrado: " + (!routeJsonString.isEmpty() ? "SÍ" : "NO"));
            android.util.Log.d("Package_tourTracking", "route_json: " + routeJsonString.substring(0, Math.min(100, routeJsonString.length())) + "...");
            
            StringBuilder routesText = new StringBuilder();
            int routeCounter = 1;
            
            if (!routeJsonString.isEmpty()) {
                try {
                    // Parsear el JSON string
                    org.json.JSONArray routesArray = new org.json.JSONArray(routeJsonString);
                    
                    android.util.Log.d("Package_tourTracking", "Rutas parseadas: " + routesArray.length());
                    
                    for (int j = 0; j < routesArray.length(); j++) {
                        // Verificar si el elemento no es null
                        if (routesArray.isNull(j)) {
                            android.util.Log.w("Package_tourTracking", "Ruta " + (j + 1) + " es null, saltando...");
                            continue;
                        }
                        
                        try {
                            org.json.JSONObject routeJson = routesArray.getJSONObject(j);
                            
                            // Obtener solo el título de cada ruta
                            String routeTitle = routeJson.optString("title", "Ruta " + routeCounter);
                            
                            routesText.append(routeCounter).append(". ").append(routeTitle);
                            routesText.append("\n\n");
                            
                            android.util.Log.d("Package_tourTracking", "Ruta " + routeCounter + ": " + routeTitle);
                            routeCounter++;
                            
                        } catch (org.json.JSONException e) {
                            android.util.Log.w("Package_tourTracking", "Error al parsear ruta " + (j + 1) + ": " + e.getMessage());
                            // Continuar con la siguiente ruta
                            continue;
                        }
                    }
                    
                } catch (org.json.JSONException e) {
                    android.util.Log.e("Package_tourTracking", "❌ Error al parsear route_json: " + e.getMessage());
                    // Si no se puede parsear, mostrar el string tal como está
                    routesText.append("1. Rutas del paquete\n");
                    routesText.append("   ").append(routeJsonString);
                    routesText.append("\n\n");
                }
            } else {
                android.util.Log.w("Package_tourTracking", "No hay route_json en el paquete");
                routesText.append("No hay rutas disponibles para este paquete");
            }
            
            // Actualizar UI con las rutas
            String finalRoutesText = routesText.toString().trim();
            requireActivity().runOnUiThread(() -> {
                View rootView = getView();
                if (rootView != null) {
                    TextView tvRutaContent = rootView.findViewById(R.id.tvRutaContent);
                    if (tvRutaContent != null) {
                        tvRutaContent.setText(finalRoutesText.isEmpty() ? "No hay rutas disponibles" : finalRoutesText);
                    } else {
                        android.util.Log.w("Package_tourTracking", "TextView tvRutaContent no encontrado");
                    }
                }
            });
            
        } catch (org.json.JSONException e) {
            android.util.Log.e("Package_tourTracking", "❌ Error al parsear respuesta principal: " + e.getMessage(), e);
            android.util.Log.e("Package_tourTracking", "JSON recibido: " + jsonResponse);
            
            // Mostrar mensaje de error en la UI
            requireActivity().runOnUiThread(() -> {
                View rootView = getView();
                if (rootView != null) {
                    TextView tvRutaContent = rootView.findViewById(R.id.tvRutaContent);
                    if (tvRutaContent != null) {
                        tvRutaContent.setText("Error al cargar las rutas");
                    }
                }
            });
        }
    }
}