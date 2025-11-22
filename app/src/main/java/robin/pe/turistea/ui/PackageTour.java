package robin.pe.turistea.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import robin.pe.turistea.R;

public class PackageTour extends Fragment {

    private NavController navController;
    private ImageView icBack;
    private TextView tvDetallesDestino;
    private TextView tvDetallesTracking;
    private TextView tvCalificacion;
    private TextView tvTemperatura;
    private TextView tvDias;
    private TextView tvDestino;
    private TextView tvCiudadPais;
    private TextView tvDescripcion;
    private Button btnReservar;
    
    // Datos del paquete
    private int packageId;
    private String packageName;
    private String packageDescription;
    private String packageImage;
    private double packagePrice;
    private String packageLocation;
    private int packageDuration;

    public PackageTour() {
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_package_tour, container, false);
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
            android.util.Log.d("PackageTour", "Navegando de vuelta al inicio");
            // Limpiar el back stack y navegar al inicio
            navController.popBackStack(R.id.navigation_inicio, false);
        });
        
        // Configurar botón de Detalles (ya está seleccionado por defecto)
        tvDetallesDestino.setOnClickListener(v -> {
            // Ya estamos en detalles, no hacer nada
            Toast.makeText(getContext(), "Ya estás en Detalles", Toast.LENGTH_SHORT).show();
        });
        
        // Configurar botón de Tracking
        tvDetallesTracking.setOnClickListener(v -> {
            // Navegar a tracking con los mismos datos
            // Se obtiene del bundle los datos del paquete en Inicio 
            // Obtine los datos del paquete y los manda a la actividad Package_tourTracking
            Bundle bundle = new Bundle();
            bundle.putInt("package_id", packageId);
            bundle.putString("package_name", packageName);
            bundle.putString("package_description", packageDescription);
            bundle.putString("package_image", packageImage);
            bundle.putDouble("package_price", packagePrice);
            bundle.putString("package_location", packageLocation);
            bundle.putInt("package_duration", packageDuration);
            
            navController.navigate(R.id.action_navigation_packageTour_to_navigation_package_tourTracking, bundle);
        });
        
        // Configurar botón de reservar
        if (btnReservar != null) {
            android.util.Log.d("PackageTour", "Botón reservar encontrado, configurando listener");
            btnReservar.setOnClickListener(v -> {
                try {
                    android.util.Log.d("PackageTour", "Botón reservar presionado");
                    
                    if (navController == null) {
                        android.util.Log.e("PackageTour", "navController es null");
                        Toast.makeText(getContext(), "Error de navegación", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Crear bundle con los datos del paquete para pasar a Reservation
                    // Se obtiene del Inicio.java
                    Bundle bundle = new Bundle();
                    bundle.putInt("package_id", packageId);
                    bundle.putString("package_name", packageName != null ? packageName : "");
                    bundle.putString("package_description", packageDescription != null ? packageDescription : "");
                    bundle.putString("package_image", packageImage != null ? packageImage : "");
                    bundle.putFloat("package_price", (float) packagePrice);
                    bundle.putString("package_location", packageLocation != null ? packageLocation : "");
                    bundle.putInt("package_duration", packageDuration);
                    bundle.putInt("package_max_personas", 10); // Máximo por defecto, ajustar según tu lógica
                    
                    android.util.Log.d("PackageTour", "Navegando a reserva con datos: " + packageName);
                    navController.navigate(R.id.action_navigation_packageTour_to_navigation_reservation, bundle);
                } catch (Exception e) {
                    android.util.Log.e("PackageTour", "Error al navegar a reserva: " + e.getMessage(), e);
                    e.printStackTrace();
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error al abrir el formulario de reserva", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            android.util.Log.e("PackageTour", "btnReservar es null - el botón no se encontró en el layout");
        }
        
        // Cargar datos del paquete
        loadPackageData();
    }
    
    private void loadPackageData() {
        // Actualizar los TextViews con los datos del paquete
        if (tvDias != null) {
            tvDias.setText(packageDuration + " Días");
        }
        
        // TODO: Obtener más datos como calificación y temperatura desde el backend
        // Por ahora usar valores por defecto
        if (tvCalificacion != null) {
            tvCalificacion.setText("4.5");
        }
        
        if (tvTemperatura != null) {
            tvTemperatura.setText("25°C");
        }
        
        // Buscar los TextViews de destino y descripción en el layout
        View rootView = getView();
        if (rootView != null) {
            // Actualizar nombre del destino
            TextView tvDestinoNombre = rootView.findViewById(R.id.tvDestinoNombre);
            if (tvDestinoNombre != null) {
                tvDestinoNombre.setText(packageName);
            }
            
            // Actualizar ciudad y país
            TextView tvCiudadPais = rootView.findViewById(R.id.tvCiudadPais);
            if (tvCiudadPais != null) {
                tvCiudadPais.setText(packageLocation + ", Perú");
            }
            
            // Actualizar descripción
            TextView tvDescripcionContent = rootView.findViewById(R.id.tvDescripcionContent);
            if (tvDescripcionContent != null) {
                tvDescripcionContent.setText(packageDescription);
            }
        }
        
        android.util.Log.d("PackageTour", "Datos cargados: " + packageName + " - " + packageLocation + " - " + packageDuration + " días");
    }
}