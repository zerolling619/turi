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
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import robin.pe.turistea.R;
import robin.pe.turistea.Config;
import robin.pe.turistea.models.RouteItemDetail;

public class Package_tourTracking extends Fragment {

    private ImageView imgBgPath;
    private NavController navController;
    private ImageView icBack;
    private TextView tvDetallesDestino;
    private TextView tvDetallesTracking;
    private TextView tvCalificacion;
    private TextView tvTemperatura;
    private TextView tvDias;
    private Button btnReservar;

    private int packageId;
    private String packageName;
    private String packageDescription;
    private String packageImage;
    private double packagePrice;
    private String packageLocation;
    private int packageDuration;
    private JSONArray packagesArray; // Almacena todos los paquetes del response

    public Package_tourTracking() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
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
        return inflater.inflate(R.layout.fragment_package_tour_tracking, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        navController = Navigation.findNavController(view);
        
        icBack = view.findViewById(R.id.IcBack);
        imgBgPath = view.findViewById(R.id.imgBgPath);
        tvDetallesDestino = view.findViewById(R.id.tvDetallesDestino);
        tvDetallesTracking = view.findViewById(R.id.tvDetallesTracking);
        btnReservar = view.findViewById(R.id.btnReservar);
        
        icBack.setOnClickListener(v -> {
            navController.popBackStack(R.id.navigation_inicio, false);
        });
        
        tvDetallesDestino.setOnClickListener(v -> {
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
        
        tvDetallesTracking.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Ya estás en Tracking", Toast.LENGTH_SHORT).show();
        });
        
        if (btnReservar != null) {
            btnReservar.setOnClickListener(v -> {
                try {
                    Bundle bundle = new Bundle();
                    bundle.putInt("package_id", packageId);
                    bundle.putString("package_name", packageName != null ? packageName : "");
                    bundle.putString("package_description", packageDescription != null ? packageDescription : "");
                    bundle.putString("package_image", packageImage != null ? packageImage : "");
                    bundle.putFloat("package_price", (float) packagePrice);
                    bundle.putString("package_location", packageLocation != null ? packageLocation : "");
                    bundle.putInt("package_duration", packageDuration);
                    bundle.putInt("package_max_personas", 10);
                    
                    navController.navigate(R.id.action_navigation_package_tourTracking_to_navigation_reservation, bundle);
                } catch (Exception e) {
                    Log.e("Package_tourTracking", "Error al navegar a reserva: " + e.getMessage(), e);
                }
            });
        }
        
        loadPackageData();
    }
    
    private void loadPackageData() {
        if (tvDias != null) {
            tvDias.setText(packageDuration + " Días");
        }
        
        if (tvCalificacion != null) {
            tvCalificacion.setText("4.5");
        }
        
        if (tvTemperatura != null) {
            tvTemperatura.setText("25°C");
        }
        
        View rootView = getView();
        if (rootView != null) {
            TextView tvDestinoNombre = rootView.findViewById(R.id.tvDestinoNombre);
            if (tvDestinoNombre != null) {
                tvDestinoNombre.setText(packageName);
            }
        }
        
        loadRoutesFromBackend();

        if (imgBgPath != null && getContext() != null) {
            String imageUrl = packageImage;
            
            if (imageUrl != null && (imageUrl.contains("localhost") || imageUrl.contains("127.0.0.1"))) {
                imageUrl = imageUrl.replace("localhost", "10.0.2.2").replace("127.0.0.1", "10.0.2.2");
            }

            Log.d("Package_tourTracking", "Cargando imagen desde URL: " + imageUrl);

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(getContext())
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.fd_celeste_degrade)
                    .error(R.drawable.fd_celeste_degrade)
                    .into(imgBgPath);
            } else {
                Glide.with(getContext()).load(R.drawable.fd_celeste_degrade).centerCrop().into(imgBgPath);
            }
        }
    }
    
    private void loadRoutesFromBackend() {
        new Thread(() -> {
            try {
                URL url = new URL(Config.ROUTER_PACKAGES_URL(packageId) );
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                
                SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                String jwt = prefs.getString("jwt", "");
                
                if (!jwt.isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + jwt);
                }
       
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    parseRoutesJson(response.toString());
                } else {
                    Log.e("Package_tourTracking", "Error en la respuesta del servidor: " + responseCode);
                    if(getActivity() != null) getActivity().runOnUiThread(() -> 
                        Toast.makeText(getContext(), "No se pudieron cargar las rutas", Toast.LENGTH_SHORT).show());
                }
                
            } catch (Exception e) {
                Log.e("Package_tourTracking", "Error de conexión al cargar rutas", e);
                e.printStackTrace();
                
                if(getActivity() != null) getActivity().runOnUiThread(() -> 
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

  private void parseRoutesJson(String jsonResponse) {
        try {
            // La respuesta es un array de paquetes - guardamos TODO el array
            packagesArray = new JSONArray(jsonResponse);
            
            Log.d("Package_tourTracking", "Paquetes recibidos: " + packagesArray.length());
            
            // Construir Spannable con cada paquete y un botón [Ver]
            SpannableStringBuilder spannableBuilder = new SpannableStringBuilder();
            for (int i = 0; i < packagesArray.length(); i++) {
                JSONObject packageJson = packagesArray.getJSONObject(i);
                String title = packageJson.optString("title", "Título no disponible");
                // Prefijo con numeración
                String linePrefix = (i + 1) + ". " + title + " ";
                spannableBuilder.append(linePrefix);
                int startVer = spannableBuilder.length();
                String verLabel = "[Ver]";
                spannableBuilder.append(verLabel);
                int endVer = spannableBuilder.length();
                final int indexForClick = i;
                spannableBuilder.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        try {
                            JSONObject targetPackage = packagesArray.getJSONObject(indexForClick);
                            String routeJsonString = targetPackage.optString("route_json", "");
                            String packageTitle = targetPackage.optString("title", "Paquete");
                            Log.d("Package_tourTracking", "Click paquete index=" + indexForClick + " title=" + packageTitle);
                            if (!routeJsonString.isEmpty()) {
                                showRouteDetailsModal(routeJsonString, packageTitle);
                            } else {
                                Toast.makeText(getContext(), "Este paquete no tiene rutas definidas", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("Package_tourTracking", "Error al abrir rutas (span)", e);
                            Toast.makeText(getContext(), "Error al cargar rutas", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, startVer, endVer, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableBuilder.append("\n\n");
            }
            
            // Actualizar la UI en el hilo principal.
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (getView() != null) {
                        TextView tvRutaContent = getView().findViewById(R.id.tvRutaContent);
                        if (tvRutaContent != null) {
                            if (packagesArray.length() == 0) {
                                tvRutaContent.setText("No se encontraron paquetes disponibles.");
                            } else {
                                tvRutaContent.setText(spannableBuilder);
                                tvRutaContent.setMovementMethod(LinkMovementMethod.getInstance());
                            }
                            tvRutaContent.setTextColor(getResources().getColor(R.color.white));
                        }
                    }
                });
            }
        } catch (JSONException e) {
            Log.e("Package_tourTracking", "Error al parsear JSON de paquetes: " + e.getMessage(), e);
        }
    }
    
    
    private void showPackageSelector() {
        if (packagesArray == null || packagesArray.length() == 0) {
            Toast.makeText(getContext(), "No hay paquetes disponibles", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Crear array de títulos para el diálogo
            String[] packageTitles = new String[packagesArray.length()];
            for (int i = 0; i < packagesArray.length(); i++) {
                JSONObject pkg = packagesArray.getJSONObject(i);
                String title = pkg.optString("title", "Sin título");
                packageTitles[i] = (i + 1) + ". " + title;
            }
            
            // Mostrar diálogo de selección
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
            builder.setTitle("Selecciona un paquete para ver sus rutas");
            builder.setItems(packageTitles, (dialog, position) -> {
                try {
                    JSONObject selectedPackage = packagesArray.getJSONObject(position);
                    String routeJsonString = selectedPackage.optString("route_json", "");
                    String packageTitle = selectedPackage.optString("title", "Paquete");
                    int selectedPackageId = selectedPackage.optInt("id", 0);
                    
                    Log.d("Package_tourTracking", "Paquete seleccionado ID: " + selectedPackageId + ", título: " + packageTitle);
                    Log.d("Package_tourTracking", "route_json: " + routeJsonString);
                    
                    if (!routeJsonString.isEmpty()) {
                        showRouteDetailsModal(routeJsonString, packageTitle);
                    } else {
                        Toast.makeText(getContext(), "Este paquete no tiene rutas definidas", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e("Package_tourTracking", "Error al obtener paquete seleccionado: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Error al cargar las rutas", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Cancelar", null);
            builder.show();
            
        } catch (JSONException e) {
            Log.e("Package_tourTracking", "Error al crear selector: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error al mostrar opciones", Toast.LENGTH_SHORT).show();
        }
    }

    private void showRouteDetailsModal(String routeJsonString, String packageTitle) {
        try {
            JSONArray routesArray = new JSONArray(routeJsonString);
            ArrayList<RouteItemDetail> routesList = new ArrayList<>();

            for (int i = 0; i < routesArray.length(); i++) {
                JSONObject routeObj = routesArray.getJSONObject(i);

                int id = routeObj.optInt("id", 0);
                int index = routeObj.optInt("index", i);
                String title = routeObj.optString("title", "Sin título");
                String description = routeObj.optString("description", "Sin descripción");
                String bgImage = routeObj.optString("bg_image", "");
                String bgImageKey = routeObj.optString("bg_image_key", "");
                String bgImageSize = routeObj.optString("bg_image_size", "");

                RouteItemDetail route = new RouteItemDetail(id, index, title, description, bgImage, bgImageKey, bgImageSize);
                routesList.add(route);
            }

            // Buscar el paquete correspondiente en packagesArray para extraer datos del driver
            String driverName = "-";
            String driverPlate = "-";
            String driverCar = "-";
            if (packagesArray != null) {
                for (int i = 0; i < packagesArray.length(); i++) {
                    JSONObject pkg = packagesArray.getJSONObject(i);
                    if (pkg.optString("title", "Paquete").equals(packageTitle)) {
                        JSONObject driver = pkg.optJSONObject("package") != null ? pkg.optJSONObject("package").optJSONObject("driver") : null;
                        if (driver != null) {
                            driverName = driver.optString("name", "-") + " " + driver.optString("lastname", "-");
                            driverPlate = driver.optString("number_plate", "-");
                            driverCar = driver.optString("brand_car", "-") + " " + driver.optString("model_car", "-");
                        }
                        break;
                    }
                }
            }

            // Crear y mostrar el dialog
            RouteDetailsDialog dialog = RouteDetailsDialog.newInstance(routesList, packageTitle, driverName, driverPlate, driverCar);
            dialog.show(getChildFragmentManager(), "RouteDetailsDialog");

        } catch (JSONException e) {
            Log.e("Package_tourTracking", "Error al parsear route_json: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error al cargar detalles de rutas", Toast.LENGTH_SHORT).show();
        }
    }
}
