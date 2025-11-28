package robin.pe.turistea.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import robin.pe.turistea.R;
import robin.pe.turistea.models.RouteItemDetail;


public class RouteDetailsDialog extends DialogFragment {

    private static final String ARG_ROUTES = "routes";
    private static final String ARG_TITLE = "title";
    private static final String ARG_DRIVER_NAME = "driver_name";
    private static final String ARG_DRIVER_PLATE = "driver_plate";
    private static final String ARG_DRIVER_CAR = "driver_car";

    private List<RouteItemDetail> routesList;
    private String modalTitle;
    private String driverName;
    private String driverPlate;
    private String driverCar;

    public static RouteDetailsDialog newInstance(ArrayList<RouteItemDetail> routes, String title, String driverName, String driverPlate, String driverCar) {
        RouteDetailsDialog dialog = new RouteDetailsDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ROUTES, routes);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DRIVER_NAME, driverName);
        args.putString(ARG_DRIVER_PLATE, driverPlate);
        args.putString(ARG_DRIVER_CAR, driverCar);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        
        if (getArguments() != null) {
            routesList = (List<RouteItemDetail>) getArguments().getSerializable(ARG_ROUTES);
            modalTitle = getArguments().getString(ARG_TITLE, "Detalles de Rutas");
            driverName = getArguments().getString(ARG_DRIVER_NAME, "-");
            driverPlate = getArguments().getString(ARG_DRIVER_PLATE, "-");
            driverCar = getArguments().getString(ARG_DRIVER_CAR, "-");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.modal_route_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView imgBackModal = view.findViewById(R.id.imgBackModal);
        TextView tvModalTitle = view.findViewById(R.id.tvModalTitle);
        TextView tvDriverName = view.findViewById(R.id.tvDriverName);
        TextView tvDriverPlate = view.findViewById(R.id.tvDriverPlate);
        TextView tvDriverCar = view.findViewById(R.id.tvDriverCar);
        RecyclerView recyclerViewRoutes = view.findViewById(R.id.recyclerViewRoutes);
        Button btnCerrarModal = view.findViewById(R.id.btnCerrarModal);
        RoutePathView routeView = view.findViewById(R.id.routeView);

        // Configurar el título del modal
        if (modalTitle != null) {
            tvModalTitle.setText(modalTitle);
        }
        if (tvDriverName != null) {
            tvDriverName.setText("Conductor: " + driverName);
        }
        if (tvDriverPlate != null) {
            tvDriverPlate.setText("Placa: " + driverPlate);
        }
        if (tvDriverCar != null) {
            tvDriverCar.setText("Auto: " + driverCar);
        }

        // Parsear el route_json y pasar los puntos al custom view
        if (routesList != null && routeView != null) {
            // Convertir RouteItemDetail a RoutePoint (solo los campos necesarios)
            List<RoutePoint> points = new ArrayList<>();
            for (RouteItemDetail r : routesList) {
                points.add(new RoutePoint(r.getId(), r.getIndex(), r.getTitle(), r.getDescription(), r.getBgImage()));
            }
            routeView.setPoints(points);
        }

        // Configurar el RecyclerView
        recyclerViewRoutes.setLayoutManager(new LinearLayoutManager(getContext()));
        RouteDetailsAdapter adapter = new RouteDetailsAdapter(routesList);
        recyclerViewRoutes.setAdapter(adapter);

        // Botón de cerrar
        imgBackModal.setOnClickListener(v -> dismiss());
        btnCerrarModal.setOnClickListener(v -> dismiss());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }
}
