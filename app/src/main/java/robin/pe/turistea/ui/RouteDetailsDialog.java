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

    private List<RouteItemDetail> routesList;
    private String modalTitle;

    public static RouteDetailsDialog newInstance(ArrayList<RouteItemDetail> routes, String title) {
        RouteDetailsDialog dialog = new RouteDetailsDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ROUTES, routes);
        args.putString(ARG_TITLE, title);
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
        RecyclerView recyclerViewRoutes = view.findViewById(R.id.recyclerViewRoutes);
        Button btnCerrarModal = view.findViewById(R.id.btnCerrarModal);

        // Configurar el título del modal
        if (modalTitle != null) {
            tvModalTitle.setText(modalTitle);
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
