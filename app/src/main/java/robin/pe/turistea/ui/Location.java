package robin.pe.turistea.ui;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import robin.pe.turistea.R;

public class Location extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ImageView icBack;
    private Button btnCurrentLocation;
    private Button btnConfirmLocation;
    private TextView tvSelectedLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private NavController navController;
    private Geocoder geocoder;
    
    private LatLng selectedLatLng;
    private String selectedAddress = "";
    // Centro de Lima, Perú (Plaza Mayor)
    private static final LatLng DEFAULT_LOCATION = new LatLng(-12.0464, -77.0428);

    public Location() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        navController = Navigation.findNavController(view);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        geocoder = new Geocoder(requireContext(), Locale.getDefault());
        
        // Inicializar vistas
        icBack = view.findViewById(R.id.IcBack);
        btnCurrentLocation = view.findViewById(R.id.btnCurrentLocation);
        btnConfirmLocation = view.findViewById(R.id.btnConfirmLocation);
        tvSelectedLocation = view.findViewById(R.id.TvSelectedLocation);
        
        // Configurar el botón de volver
        icBack.setOnClickListener(v -> navController.navigateUp());
        
        // Configurar el botón de ubicación actual
        btnCurrentLocation.setOnClickListener(v -> moveToCurrentLocation());
        
        // Configurar el botón de confirmar
        btnConfirmLocation.setOnClickListener(v -> confirmLocation());
        
        // Inicializar el mapa de forma programática
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.ImMapa, mapFragment)
                .commit();
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        
        // Configurar el mapa
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        
        // Establecer ubicación inicial (Centro de Lima)
        selectedLatLng = DEFAULT_LOCATION;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 14));
        
        // Obtener dirección inicial
        getAddressFromLocation(selectedLatLng);
        
        // Escuchar cuando el usuario mueva el mapa
        mMap.setOnCameraIdleListener(() -> {
            selectedLatLng = mMap.getCameraPosition().target;
            getAddressFromLocation(selectedLatLng);
        });
        
        // Intentar obtener ubicación actual si hay permisos
        if (checkLocationPermissions()) {
            try {
                mMap.setMyLocationEnabled(true);
                moveToCurrentLocation();
            } catch (SecurityException e) {
                Toast.makeText(getContext(), "Error al acceder a la ubicación", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private boolean checkLocationPermissions() {
        return ContextCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
               ContextCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    
    private void moveToCurrentLocation() {
        // Mover al centro de Lima
        if (mMap != null) {
            selectedLatLng = DEFAULT_LOCATION;
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 15));
            getAddressFromLocation(selectedLatLng);
            Toast.makeText(getContext(), "Ubicación establecida en Lima Centro", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void getAddressFromLocation(LatLng latLng) {
        // Ejecutar en un hilo separado para no bloquear la UI
        new Thread(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    
                    // Construir dirección completa
                    StringBuilder fullAddress = new StringBuilder();
                    
                    // Dirección de calle
                    if (address.getThoroughfare() != null) {
                        fullAddress.append(address.getThoroughfare());
                    }
                    
                    // Número de calle
                    if (address.getSubThoroughfare() != null) {
                        if (fullAddress.length() > 0) fullAddress.append(" ");
                        fullAddress.append(address.getSubThoroughfare());
                    }
                    
                    // Localidad/Ciudad
                    if (address.getLocality() != null) {
                        if (fullAddress.length() > 0) fullAddress.append(", ");
                        fullAddress.append(address.getLocality());
                    }
                    
                    // Departamento/Región
                    if (address.getAdminArea() != null) {
                        if (fullAddress.length() > 0) fullAddress.append(", ");
                        fullAddress.append(address.getAdminArea());
                    }
                    
                    // País
                    if (address.getCountryName() != null) {
                        if (fullAddress.length() > 0) fullAddress.append(", ");
                        fullAddress.append(address.getCountryName());
                    }
                    
                    selectedAddress = fullAddress.toString();
                    
                    // Si la dirección está vacía, mostrar coordenadas
                    if (selectedAddress.isEmpty()) {
                        selectedAddress = String.format("Lat: %.4f, Lng: %.4f", 
                            latLng.latitude, latLng.longitude);
                    }
                } else {
                    // Si no se encuentra dirección, usar coordenadas
                    selectedAddress = String.format("Lat: %.4f, Lng: %.4f", 
                        latLng.latitude, latLng.longitude);
                }
                
                // Actualizar UI en el hilo principal
                requireActivity().runOnUiThread(() -> {
                    if (tvSelectedLocation != null) {
                        tvSelectedLocation.setText(selectedAddress);
                    }
                });
                
            } catch (IOException e) {
                // Error al obtener dirección, usar coordenadas
                selectedAddress = String.format("Lat: %.4f, Lng: %.4f", 
                    latLng.latitude, latLng.longitude);
                
                requireActivity().runOnUiThread(() -> {
                    if (tvSelectedLocation != null) {
                        tvSelectedLocation.setText(selectedAddress);
                    }
                    Toast.makeText(getContext(), "Error al obtener dirección", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    private void confirmLocation() {
        if (selectedLatLng != null) {
            // Guardar la ubicación seleccionada con la dirección
            SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            prefs.edit()
                .putString("selected_location", selectedAddress.isEmpty() ? 
                    String.format("Lat: %.4f, Lng: %.4f", selectedLatLng.latitude, selectedLatLng.longitude) : 
                    selectedAddress)
                .putFloat("latitude", (float) selectedLatLng.latitude)
                .putFloat("longitude", (float) selectedLatLng.longitude)
                .apply();
            
            Toast.makeText(getContext(), "Ubicación guardada", Toast.LENGTH_SHORT).show();
            
            // Volver al fragment anterior
            navController.navigateUp();
        }
    }
}

