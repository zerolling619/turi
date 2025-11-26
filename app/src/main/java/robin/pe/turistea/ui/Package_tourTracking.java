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

import robin.pe.turistea.R;
import robin.pe.turistea.Config;

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
        tvCalificacion = view.findViewById(R.id.TvCalificacion);
        tvTemperatura = view.findViewById(R.id.TvTemperatura);
        tvDias = view.findViewById(R.id.TvDias);
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
            // La respuesta es un array de paquetes.
            JSONArray packagesArray = new JSONArray(jsonResponse);
            
            // Usamos un StringBuilder para construir la lista de títulos.
            StringBuilder titlesText = new StringBuilder();

            // Recorrer cada objeto (paquete) en el array.
            for (int i = 0; i < packagesArray.length(); i++) {
                JSONObject packageJson = packagesArray.getJSONObject(i);
                
                // Obtener el título principal de este paquete.
                String title = packageJson.optString("title", "Título no disponible");
                
                // Añadirlo a nuestra lista, con un número.
                titlesText.append(i + 1).append(". ").append(title).append("\n\n");
            }
            
            String finalText = titlesText.toString().trim();

            // Actualizar la UI en el hilo principal.
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (getView() != null) {
                        TextView tvRutaContent = getView().findViewById(R.id.tvRutaContent);
                        if (tvRutaContent != null) {
                            if (finalText.isEmpty()) {
                                tvRutaContent.setText("No se encontraron títulos de paquetes.");
                            } else {
                                tvRutaContent.setText(finalText);
                            }
                        }
                    }
                });
            }
        } catch (JSONException e) {
            Log.e("Package_tourTracking", "Error al parsear JSON de paquetes: " + e.getMessage(), e);
        }
    }
}
