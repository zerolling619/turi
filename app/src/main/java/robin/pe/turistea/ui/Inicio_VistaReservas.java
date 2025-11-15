package robin.pe.turistea.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.navigation.Navigation;
import robin.pe.turistea.R;
import robin.pe.turistea.Config;

public class Inicio_VistaReservas extends Fragment {

    private ImageView icMenuLateral;
    private LinearLayout containerReservas;

    public Inicio_VistaReservas() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inicio_vista_reservas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Configurar el botón del menú lateral
        icMenuLateral = view.findViewById(R.id.IcMenuLateral);
        if (icMenuLateral != null) {
            icMenuLateral.setOnClickListener(v -> {
                if (getActivity() instanceof robin.pe.turistea.MainActivity) {
                    ((robin.pe.turistea.MainActivity) getActivity()).openDrawer();
                }
            });
        }

        // Obtener el contenedor de reservas (el LinearLayout que contiene RvReservar)
        View parentView = view.findViewById(R.id.RvReservar);
        if (parentView != null) {
            ViewGroup parent = (ViewGroup) parentView.getParent();
            if (parent instanceof LinearLayout) {
                containerReservas = (LinearLayout) parent;
                android.util.Log.d("Inicio_VistaReservas", "Contenedor encontrado: LinearLayout padre de RvReservar");
            } else {
                android.util.Log.e("Inicio_VistaReservas", "El padre de RvReservar no es un LinearLayout");
            }
        }
        
        if (containerReservas == null) {
            android.util.Log.e("Inicio_VistaReservas", "No se encontró el contenedor de reservas");
            // Intentar buscar recursivamente
            containerReservas = findLinearLayoutContainer((ViewGroup) view);
        }
        
        if (containerReservas == null) {
            android.util.Log.e("Inicio_VistaReservas", "ERROR: No se pudo encontrar el contenedor de reservas");
        } else {
            android.util.Log.d("Inicio_VistaReservas", "Contenedor de reservas encontrado correctamente");
        }

        // Cargar reservas desde el backend
        loadReservesFromBackend(view);
    }
    
    private void loadReservesFromBackend(View rootView) {
        new Thread(() -> {
            try {
                android.util.Log.d("Inicio_VistaReservas", "=== CARGANDO RESERVAS DESDE BACKEND ===");
                
                // Obtener el rol del usuario para filtrar las reservas
                SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                String userRole = prefs.getString("user_role", "user");
                String jwt = prefs.getString("jwt", "");
                
                android.util.Log.d("Inicio_VistaReservas", "Rol del usuario: " + userRole);
                
                // Construir URL con filtro por estado según el rol
                String urlString = Config.FORM_RESERVES_LIST_URL;
                if (userRole.equals("driver") || userRole.equals("guide")) {
                    // Para drivers y guides, mostrar reservas pendientes asignadas
                    urlString += "?status=pending";
                } else {
                    // Para usuarios normales, mostrar sus propias reservas
                    urlString += "?status=pending";
                }
                
                java.net.URL url = new java.net.URL(urlString);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/json");
                
                if (!jwt.isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + jwt);
                }
                
                int responseCode = conn.getResponseCode();
                android.util.Log.d("Inicio_VistaReservas", "Código de respuesta: " + responseCode);
                
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
                    android.util.Log.d("Inicio_VistaReservas", "✅ Reservas recibidas: " + jsonResponse.substring(0, Math.min(200, jsonResponse.length())) + "...");
                    
                    // Parsear y mostrar las reservas
                    parseAndDisplayReserves(jsonResponse, rootView);
                } else {
                    android.util.Log.e("Inicio_VistaReservas", "❌ Error al cargar reservas: " + responseCode);
                    requireActivity().runOnUiThread(() -> 
                        Toast.makeText(getContext(), "No se pudieron cargar las reservas", Toast.LENGTH_SHORT).show()
                    );
                }
                
            } catch (Exception e) {
                android.util.Log.e("Inicio_VistaReservas", "❌ Error al cargar reservas: " + e.getMessage(), e);
                requireActivity().runOnUiThread(() -> 
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
    
    private void parseAndDisplayReserves(String jsonResponse, View rootView) {
        try {
            org.json.JSONArray reservesArray = new org.json.JSONArray(jsonResponse);
            
            android.util.Log.d("Inicio_VistaReservas", "Total reservas recibidas: " + reservesArray.length());
            
            requireActivity().runOnUiThread(() -> {
                // Limpiar el contenedor (excepto el primer elemento si es el template)
                if (containerReservas == null) {
                    android.util.Log.e("Inicio_VistaReservas", "containerReservas es null, no se pueden mostrar las reservas");
                    Toast.makeText(getContext(), "Error al mostrar reservas", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Obtener el template original y ocultarlo o eliminarlo
                RelativeLayout templateReserva = rootView.findViewById(R.id.RvReservar);
                if (templateReserva != null) {
                    // Ocultar el template original en lugar de eliminarlo
                    templateReserva.setVisibility(View.GONE);
                }
                
                // Limpiar el contenedor
                containerReservas.removeAllViews();
                
                // Mostrar cada reserva
                for (int i = 0; i < reservesArray.length(); i++) {
                    try {
                        org.json.JSONObject reserve = reservesArray.getJSONObject(i);
                        createReserveCard(reserve, containerReservas, rootView);
                    } catch (org.json.JSONException e) {
                        android.util.Log.e("Inicio_VistaReservas", "Error al parsear reserva " + i + ": " + e.getMessage());
                    }
                }
                
                if (reservesArray.length() == 0) {
                    TextView emptyText = new TextView(getContext());
                    emptyText.setText("No hay reservas disponibles");
                    emptyText.setTextSize(16);
                    emptyText.setPadding(20, 20, 20, 20);
                    containerReservas.addView(emptyText);
                }
            });
            
        } catch (org.json.JSONException e) {
            android.util.Log.e("Inicio_VistaReservas", "❌ Error al parsear JSON: " + e.getMessage(), e);
            requireActivity().runOnUiThread(() -> 
                Toast.makeText(getContext(), "Error al procesar las reservas", Toast.LENGTH_SHORT).show()
            );
        }
    }
    
    private void createReserveCard(org.json.JSONObject reserve, LinearLayout container, View rootView) {
        try {
            // Obtener datos de la reserva
            String fullName = reserve.optString("full_name", "Sin nombre");
            int idPackage = reserve.optInt("id_package", 0);
            String dateReserve = reserve.optString("date_reserve", "");
            String statusForm = reserve.optString("status_form", "");
            int reserveId = reserve.optInt("id", 0);
            
            // Crear una copia del RelativeLayout original
            RelativeLayout templateReserva = rootView.findViewById(R.id.RvReservar);
            if (templateReserva == null) {
                android.util.Log.e("Inicio_VistaReservas", "No se encontró el template RvReservar");
                return;
            }
            
            // Clonar el layout
            RelativeLayout rvReservar = new RelativeLayout(getContext());
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                (int) android.util.TypedValue.applyDimension(
                    android.util.TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics()
                ));
            params.setMargins(0, (int) android.util.TypedValue.applyDimension(
                android.util.TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()), 0, 0);
            rvReservar.setLayoutParams(params);
            rvReservar.setBackgroundResource(R.drawable.fd_contorno_azu_fd_blancol);
            
            // Crear los elementos del layout
            ImageView iconView = new ImageView(getContext());
            int iconId = View.generateViewId();
            iconView.setId(iconId);
            RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(
                (int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, 70, getResources().getDisplayMetrics()),
                (int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, 70, getResources().getDisplayMetrics())
            );
            iconParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            iconParams.addRule(RelativeLayout.CENTER_VERTICAL);
            iconView.setLayoutParams(iconParams);
            iconView.setImageResource(R.drawable.ic_contact);
            iconView.setPadding(
                (int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, 14, getResources().getDisplayMetrics()),
                0,
                (int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, 14, getResources().getDisplayMetrics()),
                0
            );
            rvReservar.addView(iconView);
            
            // LinearLayout para los textos
            LinearLayout textLayout = new LinearLayout(getContext());
            textLayout.setOrientation(LinearLayout.VERTICAL);
            RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            textParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            textParams.addRule(RelativeLayout.END_OF, iconId);
            textParams.setMarginStart((int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()));
            textParams.topMargin = (int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
            textLayout.setLayoutParams(textParams);
            textLayout.setId(View.generateViewId());
            
            // TextView Nombre del Paquete
            TextView nombrePaquete = new TextView(getContext());
            nombrePaquete.setText("Paquete ID: " + idPackage);
            nombrePaquete.setTextColor(getResources().getColor(R.color.black, null));
            nombrePaquete.setTextSize(20);
            nombrePaquete.setTypeface(null, android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams nombreParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            nombreParams.topMargin = (int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
            nombreParams.bottomMargin = (int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
            nombrePaquete.setLayoutParams(nombreParams);
            textLayout.addView(nombrePaquete);
            
            // TextView Nombre del Usuario
            TextView nombreUsuario = new TextView(getContext());
            nombreUsuario.setText(fullName);
            nombreUsuario.setTextSize(20);
            nombreUsuario.setTypeface(null, android.graphics.Typeface.BOLD);
            nombreUsuario.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            textLayout.addView(nombreUsuario);
            
            // TextView Fecha/Lugar
            TextView lugarEncuentro = new TextView(getContext());
            lugarEncuentro.setText("Fecha: " + dateReserve);
            lugarEncuentro.setTextSize(20);
            lugarEncuentro.setTypeface(null, android.graphics.Typeface.BOLD);
            lugarEncuentro.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            textLayout.addView(lugarEncuentro);
            
            rvReservar.addView(textLayout);
            
            // TextView Estado
            TextView estadoText = new TextView(getContext());
            estadoText.setText(statusForm.equals("pending") ? "Pendiente" : statusForm);
            estadoText.setTextColor(getResources().getColor(R.color.black, null));
            estadoText.setTextSize(20);
            estadoText.setTypeface(null, android.graphics.Typeface.BOLD);
            RelativeLayout.LayoutParams estadoParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            estadoParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            estadoParams.addRule(RelativeLayout.CENTER_VERTICAL);
            estadoParams.setMarginEnd((int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics()));
            estadoText.setLayoutParams(estadoParams);
            rvReservar.addView(estadoText);
            
            // Configurar click para navegar al detalle
            rvReservar.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putInt("reserve_id", reserveId);
                Navigation.findNavController(rootView)
                    .navigate(R.id.action_navigation_inicio_VistaReservas_to_reservationDetail, bundle);
            });
            
            // Agregar al contenedor
            container.addView(rvReservar);
            
        } catch (Exception e) {
            android.util.Log.e("Inicio_VistaReservas", "Error al crear tarjeta de reserva: " + e.getMessage(), e);
        }
    }
    
    private LinearLayout findLinearLayoutContainer(ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout ll = (LinearLayout) child;
                // Verificar si contiene RvReservar
                if (ll.findViewById(R.id.RvReservar) != null) {
                    return ll;
                }
            } else if (child instanceof ViewGroup) {
                LinearLayout found = findLinearLayoutContainer((ViewGroup) child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}