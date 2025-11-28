package robin.pe.turistea.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import robin.pe.turistea.MainActivity;
import robin.pe.turistea.R;
import robin.pe.turistea.Config;
import robin.pe.turistea.models.TourPackage;

public class Inicio extends Fragment {

    private ImageView icMenuLateral;
    private ImageView icLocation;
    private TextView tvLocationText;
    private android.widget.LinearLayout linearListaPaquetes;
    private LinearLayout linearListaPaquetesVertical;

    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;
    private NavController navController;
    private Geocoder geocoder;
    private String selectedLocation = "";
    private java.util.ArrayList<TourPackage> packageList = new java.util.ArrayList<>();

    public Inicio() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar el cliente de ubicación y geocoder
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        geocoder = new Geocoder(requireContext(), Locale.getDefault());

        // Registrar el launcher para solicitar permisos
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean fineLocationGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                    Boolean coarseLocationGranted = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);

                    if (fineLocationGranted != null && fineLocationGranted) {
                        // Permiso concedido, obtener ubicación
                        getCurrentLocation();
                    } else if (coarseLocationGranted != null && coarseLocationGranted) {
                        // Permiso de ubicación aproximada concedido
                        getCurrentLocation();
                    } else {
                        // Permiso denegado, navegar a selección manual
                        Toast.makeText(getContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
                        navController.navigate(R.id.action_navigation_inicio_to_navigation_location);
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inicio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        // Referencias a las vistas del layout
        LinearLayout filtroMenu = view.findViewById(R.id.linearFiltroMenu);
        TextView tvFiltro = view.findViewById(R.id.tvFiltro);
        // Acción al presionar el texto o la flecha
        filtroMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), v);
            //Agregar filtros
            popup.getMenu().add("Popular");
            popup.getMenu().add("Económico");

            popup.setOnMenuItemClickListener(item -> {
                String opcion = item.getTitle().toString();
                tvFiltro.setText(opcion);
                return true;
            });
            popup.show();
        });
        // Configurar el botón del menú lateral
        icMenuLateral = view.findViewById(R.id.IcMenuLateral);
        icMenuLateral.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openDrawer();
            }
        });
        // Configurar el botón de ubicación
        icLocation = view.findViewById(R.id.ImLocation);
        tvLocationText = view.findViewById(R.id.TvLocation);

        icLocation.setOnClickListener(v -> handleLocationClick());
        // Obtener referencia al contenedor de paquetes
        linearListaPaquetes = view.findViewById(R.id.LinearListaPaquetes);
        // Obtener referencia al contenedor vertical
        linearListaPaquetesVertical = view.findViewById(R.id.LinearListaPaquetesVertical);
        // Referencias nuevas: búsqueda
        EditText edtSearch = view.findViewById(R.id.edtSearch);
        ImageView btnSearch = view.findViewById(R.id.btnSearch);
        ImageView btnClean = view.findViewById(R.id.btnClean);

        btnSearch.setOnClickListener(v -> {
            String texto = edtSearch.getText().toString().trim().toLowerCase();
            filtrarYMostrarPaquetes(texto);
        });
        btnClean.setOnClickListener(v -> {
            edtSearch.setText("");
            displayPackages();
        });
        // Cargar ubicación guardada si existe
        loadSavedLocation();
        // Cargar paquetes desde el backend (se mostrarán solo los paquetes de la DB)
        loadPackagesFromBackend();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recargar la ubicación cuando se regresa del fragment de mapa
        loadSavedLocation();
    }

    private void handleLocationClick() {
        // Verificar si la ubicación está habilitada
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGpsEnabled && !isNetworkEnabled) {
            // Ubicación deshabilitada, mostrar diálogo
            showEnableLocationDialog();
        } else {
            // Verificar permisos
            if (checkLocationPermissions()) {
                // Permisos concedidos, mostrar opciones
                showLocationOptionsDialog();
            } else {
                // Solicitar permisos
                requestLocationPermissions();
            }
        }
    }

    private boolean checkLocationPermissions() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermissions() {
        locationPermissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void showEnableLocationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Activar ubicación")
                .setMessage("Para usar esta función, debes activar la precisión de ubicación en tu dispositivo.")
                .setPositiveButton("Configuración", (dialog, which) -> {
                    // Abrir configuración de ubicación
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Seleccionar manualmente", (dialog, which) -> {
                    // Navegar al fragment de selección de ubicación con mapa
                    navController.navigate(R.id.action_navigation_inicio_to_navigation_location);
                })
                .setNeutralButton("Cancelar", null)
                .show();
    }

    private void showLocationOptionsDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Seleccionar ubicación")
                .setMessage("¿Cómo deseas configurar tu ubicación?")
                .setPositiveButton("Usar mi ubicación actual", (dialog, which) -> {
                    getCurrentLocation();
                })
                .setNegativeButton("Seleccionar manualmente", (dialog, which) -> {
                    // Navegar al fragment de selección de ubicación con mapa
                    navController.navigate(R.id.action_navigation_inicio_to_navigation_location);
                })
                .setNeutralButton("Cancelar", null)
                .show();
    }

    private void getCurrentLocation() {
        if (!checkLocationPermissions()) {
            Toast.makeText(getContext(), "Se necesitan permisos de ubicación", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            // Ubicación obtenida, convertir a dirección
                            getAddressFromLocation(location);
                        } else {
                            Toast.makeText(getContext(), "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show();
                            navController.navigate(R.id.action_navigation_inicio_to_navigation_location);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error al obtener ubicación", Toast.LENGTH_SHORT).show();
                        navController.navigate(R.id.action_navigation_inicio_to_navigation_location);
                    });
        } catch (SecurityException e) {
            Toast.makeText(getContext(), "Error de permisos", Toast.LENGTH_SHORT).show();
        }
    }

    private void showCustomLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Escribe tu ciudad");

        final EditText input = new EditText(requireContext());
        input.setHint("Nombre de la ciudad");
        input.setPadding(50, 30, 50, 30);
        builder.setView(input);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String ciudad = input.getText().toString().trim();
            if (!ciudad.isEmpty()) {
                selectedLocation = ciudad;
                updateLocationDisplay(selectedLocation);
                saveLocation(selectedLocation);
                Toast.makeText(getContext(), "Ubicación guardada: " + selectedLocation, Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void updateLocationDisplay(String location) {
        if (tvLocationText != null) {
            tvLocationText.setText(location);
        }
    }

    private void saveLocation(String location) {
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        prefs.edit().putString("selected_location", location).apply();
    }

    private void getAddressFromLocation(Location location) {
        // Ejecutar en un hilo separado para no bloquear la UI
        new Thread(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(),
                        1
                );

                String addressStr;
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);

                    // Construir dirección de forma más compacta
                    StringBuilder shortAddress = new StringBuilder();

                    // Ciudad
                    if (address.getLocality() != null) {
                        shortAddress.append(address.getLocality());
                    }

                    // Región/Departamento
                    if (address.getAdminArea() != null) {
                        if (shortAddress.length() > 0) shortAddress.append(", ");
                        shortAddress.append(address.getAdminArea());
                    }

                    addressStr = shortAddress.toString();

                    // Si está vacío, usar coordenadas
                    if (addressStr.isEmpty()) {
                        addressStr = String.format("Lat: %.4f, Lng: %.4f",
                                location.getLatitude(), location.getLongitude());
                    }
                } else {
                    // Si no se encuentra dirección, usar coordenadas
                    addressStr = String.format("Lat: %.4f, Lng: %.4f",
                            location.getLatitude(), location.getLongitude());
                }
                String finalAddress = addressStr;
                // Actualizar UI en el hilo principal
                requireActivity().runOnUiThread(() -> {
                    selectedLocation = finalAddress;
                    updateLocationDisplay(selectedLocation);
                    saveLocation(selectedLocation);
                    Toast.makeText(getContext(), "Ubicación actual obtenida", Toast.LENGTH_SHORT).show();
                });
            } catch (IOException e) {
                // Error al obtener dirección, usar coordenadas
                String locationStr = String.format("Lat: %.4f, Lng: %.4f",
                        location.getLatitude(), location.getLongitude());
                requireActivity().runOnUiThread(() -> {
                    selectedLocation = locationStr;
                    updateLocationDisplay(selectedLocation);
                    saveLocation(selectedLocation);
                    Toast.makeText(getContext(), "Ubicación obtenida", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void loadSavedLocation() {
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        selectedLocation = prefs.getString("selected_location", "");
        if (!selectedLocation.isEmpty() && tvLocationText != null) {
            updateLocationDisplay(selectedLocation);
        }
    }

    private void loadPackagesFromBackend() {
        new Thread(() -> {
            try {
                android.util.Log.d("Inicio", "=== CARGANDO PAQUETES DESDE BACKEND ===");
                java.net.URL url = new java.net.URL(Config.PACKAGES_URL);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/json");

                // Obtener JWT del usuario autenticado
                SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                String jwt = prefs.getString("jwt", "");

                android.util.Log.d("Inicio", "JWT encontrado: " + (!jwt.isEmpty() ? "SÍ (longitud: " + jwt.length() + ")" : "NO"));

                if (!jwt.isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + jwt);
                    android.util.Log.d("Inicio", "Header Authorization agregado: Bearer " + jwt.substring(0, Math.min(20, jwt.length())) + "...");
                } else {
                    android.util.Log.w("Inicio", "No hay JWT - El usuario no ha iniciado sesión");
                }

                int responseCode = conn.getResponseCode();
                android.util.Log.d("Inicio", "Código de respuesta del servidor: " + responseCode);

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
                    android.util.Log.d("Inicio", "Respuesta exitosa del servidor");
                    android.util.Log.d("Inicio", "JSON recibido: " + jsonResponse.substring(0, Math.min(200, jsonResponse.length())) + "...");

                    // Parsear JSON
                    parsePackagesJson(jsonResponse);
                } else {
                    // Leer mensaje de error del servidor
                    java.io.InputStream errorStream = conn.getErrorStream();
                    if (errorStream != null) {
                        java.io.BufferedReader errorReader = new java.io.BufferedReader(new java.io.InputStreamReader(errorStream));
                        StringBuilder errorResponse = new StringBuilder();
                        String line;
                        while ((line = errorReader.readLine()) != null) {
                            errorResponse.append(line);
                        }
                        errorReader.close();
                        android.util.Log.e("Inicio", "❌ Error " + responseCode + " - Respuesta del servidor: " + errorResponse.toString());
                    } else {
                        android.util.Log.e("Inicio", "❌ Error al cargar paquetes - Código: " + responseCode);
                    }
                }

            } catch (Exception e) {
                android.util.Log.e("Inicio", "Error al cargar paquetes: " + e.getMessage(), e);
            }
        }).start();
    }
    private void parsePackagesJson(String jsonResponse) {
        try {
            org.json.JSONArray packagesArray;
            // El backend devuelve un array directamente
            try {
                packagesArray = new org.json.JSONArray(jsonResponse);
            } catch (org.json.JSONException e) {
                // Si no es un array, intentar como objeto con "packages" o "data"
                org.json.JSONObject jsonObject = new org.json.JSONObject(jsonResponse);
                if (jsonObject.has("packages")) {
                    packagesArray = jsonObject.getJSONArray("packages");
                } else if (jsonObject.has("data")) {
                    packagesArray = jsonObject.getJSONArray("data");
                } else {
                    throw new org.json.JSONException("No se encontró array de paquetes");
                }
            }

            packageList.clear();

            for (int i = 0; i < packagesArray.length(); i++) {
                org.json.JSONObject packageJson = packagesArray.getJSONObject(i);

                TourPackage pkg = new TourPackage();

                pkg.setId(packageJson.optInt("id", 0));
                pkg.setName(packageJson.optString("title", "Paquete"));  // "title" en el backend
                pkg.setDescription(packageJson.optString("description", "Sin descripción"));

                try {
                    Thread.sleep(1000);  // Simula tiempo de carga (quita esto en producción)
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                pkg.setImage(packageJson.optString("path_bg", ""));
                // Construir ubicación desde name_district, name_province, name_region
                String district = packageJson.optString("name_district", "");
                String province = packageJson.optString("name_province", "");
                String region = packageJson.optString("name_region", "");
                String location = region + ", " + province + ", " + district;

                pkg.setLocation(location);

                // Valores por defecto para campos que no están en el backend
                pkg.setPrice(0.0);  // No hay precio en el backend
                pkg.setDuration(3);  // Duración por defecto: 3 días
                pkg.setMaxPeople(packageJson.optInt("quantity_person", 1));

                packageList.add(pkg);

                android.util.Log.d("Inicio", "Paquete parseado: " + pkg.getName() + " - " + location);
            }

            android.util.Log.d("Inicio", "✅ Total paquetes cargados: " + packageList.size());

            // Actualizar UI en el hilo principal
            requireActivity().runOnUiThread(() -> displayPackages());
            requireActivity().runOnUiThread(() -> displayPackagesVertical());

        } catch (org.json.JSONException e) {
            android.util.Log.e("Inicio", "❌ Error al parsear JSON de paquetes: " + e.getMessage(), e);
            android.util.Log.e("Inicio", "JSON recibido: " + jsonResponse);
        }
    }



    private void displayPackages() {
        if (linearListaPaquetes == null) {
            android.util.Log.e("Inicio", "linearListaPaquetes es null");
            return;
        }

        // Limpiar el contenedor (excepto los elementos fijos si los hay)
        linearListaPaquetes.removeAllViews();

        android.util.Log.d("Inicio", "Mostrando " + packageList.size() + " paquetes");

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (TourPackage pkg : packageList) {
            // Inflate custom item layout for each package
            View itemView = inflater.inflate(R.layout.item_paquete, linearListaPaquetes, false);

            // Set layout params for spacing
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(convertDpToPx(200), convertDpToPx(210));
            lp.setMargins(0, 0, convertDpToPx(24), 0);
            itemView.setLayoutParams(lp);

            ImageView imageView = itemView.findViewById(R.id.imgPaquete);
            TextView textView = itemView.findViewById(R.id.tvTituloPaquete);
            ImageView ivHeart = itemView.findViewById(R.id.ivHeart);
            View flHeart = itemView.findViewById(R.id.flHeart);

            // Cargar imagen con Glide
            if (!pkg.getImage().isEmpty()) {
                com.bumptech.glide.Glide.with(this)
                        .load(pkg.getImage())
                        .placeholder(R.mipmap.ic_tarapoto_foreground)
                        .error(R.mipmap.ic_tarapoto_foreground)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.mipmap.ic_tarapoto_foreground);
            }

            // Set title
            textView.setText(pkg.getName());

            // Click events
            itemView.setOnClickListener(v -> showPackageDetails(pkg));
            flHeart.setOnClickListener(v -> {
                // Toggle visual feedback for favorite (simple example)
                ivHeart.setAlpha(ivHeart.getAlpha() == 1f ? 0.6f : 1f);
            });

            linearListaPaquetes.addView(itemView);
        }
    }

    private void showPackageDetails(TourPackage pkg) {
        // Obtine los datos del paquete y los manda a la actividad PackageTour
        // Navegar a fragment_package_tour con los datos del paquete
        Bundle bundle = new Bundle();
        bundle.putInt("package_id", pkg.getId());
        bundle.putString("package_name", pkg.getName());
        bundle.putString("package_description", pkg.getDescription());
        bundle.putString("package_image", pkg.getImage());
        bundle.putInt("package_max_personas", pkg.getMaxPeople());
        android.util.Log.d("Inicio", "Enviando max_personas: " + pkg.getMaxPeople());
        bundle.putDouble("package_price", pkg.getPrice());
        bundle.putString("package_location", pkg.getLocation());
        bundle.putInt("package_duration", pkg.getDuration());

        navController.navigate(R.id.action_navigation_inicio_to_navigation_packageTour, bundle);
    }

    private int convertDpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
    // Filtra los paquetes por nombre (ignora mayúsculas/minúsculas) y los muestra en el carrusel
    private void filtrarYMostrarPaquetes(String filtro) {
        if (linearListaPaquetes == null) return;
        if (filtro.isEmpty()) {
            displayPackages();
            return;
        }

        java.util.ArrayList<TourPackage> paquetesFiltrados = new java.util.ArrayList<>();
        for (TourPackage pkg : packageList) {
            if (pkg.getName() != null && pkg.getName().toLowerCase().contains(filtro)) {
                paquetesFiltrados.add(pkg);
            }
        }

        // Muestra sólo los paquetes filtrados
        linearListaPaquetes.removeAllViews();
        for (TourPackage pkg : paquetesFiltrados) {
            // Inflate our item layout and set data
            View itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_paquete, linearListaPaquetes, false);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(convertDpToPx(200), convertDpToPx(210));
            lp.setMargins(0, 0, convertDpToPx(24), 0);
            itemView.setLayoutParams(lp);

            ImageView imageView = itemView.findViewById(R.id.imgPaquete);
            TextView textView = itemView.findViewById(R.id.tvTituloPaquete);
            View flHeart = itemView.findViewById(R.id.flHeart);

            if (!pkg.getImage().isEmpty()) {
                com.bumptech.glide.Glide.with(this)
                        .load(pkg.getImage())
                        .placeholder(R.mipmap.ic_tarapoto_foreground)
                        .error(R.mipmap.ic_tarapoto_foreground)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.mipmap.ic_tarapoto_foreground);
            }

            textView.setText(pkg.getName());
            itemView.setOnClickListener(v -> showPackageDetails(pkg));
            linearListaPaquetes.addView(itemView);
        }
    }

    //  Metodo para mostrar paquetes en vertical
    private void displayPackagesVertical() {
        if (linearListaPaquetesVertical == null) {
            android.util.Log.e("Inicio", "linearListaPaquetesVertical es null");
            return;
        }

        // Limpiar el contenedor
        linearListaPaquetesVertical.removeAllViews();

        android.util.Log.d("Inicio", "Mostrando " + packageList.size() + " paquetes en vertical");

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (TourPackage pkg : packageList) {
            // Inflate item vertical
            View itemView = inflater.inflate(R.layout.item_paquete_vertical, linearListaPaquetesVertical, false);

            // Layout params para vertical (ancho completo, alto wrap_content)
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(0, 0, 0, convertDpToPx(16)); // Margen inferior para separación
            itemView.setLayoutParams(lp);

            ImageView imageView = itemView.findViewById(R.id.imgPaqueteVertical);
            TextView textView = itemView.findViewById(R.id.tvTituloPaqueteVertical);

            // Cargar imagen con Glide
            if (!pkg.getImage().isEmpty()) {
                com.bumptech.glide.Glide.with(this)
                        .load(pkg.getImage())
                        .placeholder(R.mipmap.ic_tarapoto_foreground)
                        .error(R.mipmap.ic_tarapoto_foreground)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.mipmap.ic_tarapoto_foreground);
            }
            // Set title
            textView.setText(pkg.getName());
            // Click events
            itemView.setOnClickListener(v -> showPackageDetails(pkg));
            linearListaPaquetesVertical.addView(itemView);
        }
    }

    // Metodo para filtrar y mostrar en vertical
    private void filtrarYMostrarPaquetesVertical(String filtro) {
        if (linearListaPaquetesVertical == null) return;
        if (filtro.isEmpty()) {
            displayPackagesVertical();
            return;
        }

        java.util.ArrayList<TourPackage> paquetesFiltrados = new java.util.ArrayList<>();
        for (TourPackage pkg : packageList) {
            if (pkg.getName() != null && pkg.getName().toLowerCase().contains(filtro)) {
                paquetesFiltrados.add(pkg);
            }
        }

        // Limpiar y mostrar filtrados en vertical
        linearListaPaquetesVertical.removeAllViews();
        for (TourPackage pkg : paquetesFiltrados) {
            View itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_paquete_vertical, linearListaPaquetesVertical, false);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(0, 0, 0, convertDpToPx(16));
            itemView.setLayoutParams(lp);
            ImageView imageView = itemView.findViewById(R.id.imgPaqueteVertical);
            TextView textView = itemView.findViewById(R.id.tvTituloPaqueteVertical);

            if (!pkg.getImage().isEmpty()) {
                com.bumptech.glide.Glide.with(this)
                        .load(pkg.getImage())
                        .placeholder(R.mipmap.ic_tarapoto_foreground)
                        .error(R.mipmap.ic_tarapoto_foreground)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.mipmap.ic_tarapoto_foreground);
            }
            textView.setText(pkg.getName());
            itemView.setOnClickListener(v -> showPackageDetails(pkg));
            linearListaPaquetesVertical.addView(itemView);
        }
    }
}
