package robin.pe.turistea.ui;

import android.Manifest;
import android.content.Context;
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

import android.util.Log;
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
    private static final LatLng DEFAULT_LOCATION = new LatLng(-12.0464, -77.0428);

    public Location() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        navController = Navigation.findNavController(view);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        geocoder = new Geocoder(requireContext(), Locale.getDefault());
        
        icBack = view.findViewById(R.id.IcBack);
        btnCurrentLocation = view.findViewById(R.id.btnCurrentLocation);
        btnConfirmLocation = view.findViewById(R.id.btnConfirmLocation);
        tvSelectedLocation = view.findViewById(R.id.TvSelectedLocation);
        
        icBack.setOnClickListener(v -> navController.navigateUp());
        btnCurrentLocation.setOnClickListener(v -> moveToCurrentLocation());
        btnConfirmLocation.setOnClickListener(v -> confirmLocation());
        
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.ImMapa, mapFragment)
                .commit();
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 14));
        getAddressFromLocation(DEFAULT_LOCATION);
        
        mMap.setOnCameraIdleListener(() -> {
            selectedLatLng = mMap.getCameraPosition().target;
            getAddressFromLocation(selectedLatLng);
        });
        
        if (checkLocationPermissions()) {
            try {
                mMap.setMyLocationEnabled(true);
            } catch (SecurityException e) {
                Log.e("Location", "Error de seguridad al habilitar la ubicación.", e);
            }
        }
    }
    
    private boolean checkLocationPermissions() {
        return ContextCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    
    private void moveToCurrentLocation() {
         if (mMap != null && checkLocationPermissions()) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                    getAddressFromLocation(currentLocation);
                } else {
                    Toast.makeText(getContext(), "No se pudo obtener la ubicación actual.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "Permiso de ubicación no concedido.", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void getAddressFromLocation(LatLng latLng) {
        new Thread(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    
                    StringBuilder addressBuilder = new StringBuilder();

                    // Obtener la avenida/calle
                    String thoroughfare = address.getThoroughfare();
                    if (thoroughfare != null && !thoroughfare.isEmpty()) {
                        addressBuilder.append(thoroughfare);
                    }

                    // **NUEVO**: Obtener el número de la calle
                    String subThoroughfare = address.getSubThoroughfare();
                    if (subThoroughfare != null && !subThoroughfare.isEmpty()) {
                        if (addressBuilder.length() > 0) {
                            addressBuilder.append(" ");
                        }
                        addressBuilder.append(subThoroughfare);
                    }

                    // Obtener el distrito/localidad
                    String locality = address.getLocality();
                    if (locality != null && !locality.isEmpty()) {
                        if (addressBuilder.length() > 0) {
                            addressBuilder.append(", ");
                        }
                        addressBuilder.append(locality);
                    }

                    if (addressBuilder.length() > 0) {
                        selectedAddress = addressBuilder.toString();
                    } else {
                        selectedAddress = address.getAddressLine(0);
                    }

                } else {
                    selectedAddress = String.format(Locale.getDefault(), "Lat: %.4f, Lng: %.4f", latLng.latitude, latLng.longitude);
                }
                
                if (selectedAddress == null || selectedAddress.isEmpty()) {
                     selectedAddress = String.format(Locale.getDefault(), "Lat: %.4f, Lng: %.4f", latLng.latitude, latLng.longitude);
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (tvSelectedLocation != null) {
                            tvSelectedLocation.setText(selectedAddress);
                        }
                    });
                }
            } catch (IOException e) {
                Log.e("Location", "Error al obtener la dirección", e);
            }
        }).start();
    }
    
    private void confirmLocation() {
        if (selectedAddress != null && !selectedAddress.isEmpty()) {
            Bundle result = new Bundle();
            result.putString("selected_location", selectedAddress);
            getParentFragmentManager().setFragmentResult("location_request", result);
            
            Log.d("Location", "Enviando ubicación: " + selectedAddress);
            Toast.makeText(getContext(), "Ubicación confirmada", Toast.LENGTH_SHORT).show();
            
            navController.popBackStack();
        } else {
            Toast.makeText(getContext(), "No se ha seleccionado una ubicación válida", Toast.LENGTH_SHORT).show();
        }
    }
}
