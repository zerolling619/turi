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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import robin.pe.turistea.MainActivity;
import robin.pe.turistea.R;

public class Inicio extends Fragment {

    private ImageView icMenuLateral;
    private ImageView icLocation;
    private TextView tvLocationText;
    
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;
    private NavController navController;
    private Geocoder geocoder;
    private String selectedLocation = "";

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inicio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        navController = Navigation.findNavController(view);
        
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
        
        // Configurar clicks en los carruseles
        setupCarouselClicks(view);
        
        // Cargar ubicación guardada si existe
        loadSavedLocation();
    }
    
    private void setupCarouselClicks(View view) {
        // Carrusel horizontal
        view.findViewById(R.id.cardPlace1).setOnClickListener(v -> 
            showPlaceInfo("Tarapoto", "La ciudad de las palmeras, conocida por sus paisajes tropicales y cataratas"));
        view.findViewById(R.id.cardPlace2).setOnClickListener(v -> 
            showPlaceInfo("Cusco", "Ciudad imperial, puerta de entrada a Machu Picchu"));
        view.findViewById(R.id.cardPlace3).setOnClickListener(v -> 
            showPlaceInfo("Arequipa", "La ciudad blanca, conocida por su arquitectura colonial"));
        
        // Carrusel vertical (Popular)
        view.findViewById(R.id.cardPopular1).setOnClickListener(v -> 
            showPlaceInfo("Tarapoto", "Destino turístico popular en la selva peruana"));
        view.findViewById(R.id.cardPopular2).setOnClickListener(v -> 
            showPlaceInfo("Huaraz", "Capital del andinismo peruano, puerta a la Cordillera Blanca"));
        view.findViewById(R.id.cardPopular4).setOnClickListener(v -> 
            showPlaceInfo("Piura", "Tierra del sol eterno y hermosas playas del norte"));
        view.findViewById(R.id.cardPopular5).setOnClickListener(v -> 
            showPlaceInfo("Iquitos", "Capital de la Amazonía peruana, rodeada de naturaleza"));
    }
    
    private void showPlaceInfo(String placeName, String description) {
        Toast.makeText(getContext(), placeName + ": " + description, Toast.LENGTH_LONG).show();
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
    
    private void showLocationSelectionDialog() {
        // Crear lista de ciudades turísticas comunes en Perú
        String[] ciudades = {
            "Lima", "Cusco", "Arequipa", "Trujillo", "Chiclayo",
            "Piura", "Iquitos", "Huancayo", "Tacna", "Puno",
            "Tarapoto", "Ayacucho", "Cajamarca", "Huaraz", "Paracas"
        };
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Selecciona tu ciudad");
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), 
            android.R.layout.select_dialog_singlechoice, ciudades);
        
        builder.setAdapter(adapter, (dialog, which) -> {
            selectedLocation = ciudades[which];
            updateLocationDisplay(selectedLocation);
            saveLocation(selectedLocation);
            Toast.makeText(getContext(), "Ubicación seleccionada: " + selectedLocation, Toast.LENGTH_SHORT).show();
        });
        
        builder.setNeutralButton("Escribir otra ciudad", (dialog, which) -> {
            showCustomLocationDialog();
        });
        
        builder.setNegativeButton("Cancelar", null);
        builder.show();
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
}