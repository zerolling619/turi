package robin.pe.turistea.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.navigation.Navigation;
import robin.pe.turistea.R;

public class Inicio_VistaReservas extends Fragment {

    private ImageView icMenuLateral;

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

        // Navegar al detalle de reserva al tocar el contenedor
        RelativeLayout rvReservar = view.findViewById(R.id.RvReservar);
        if (rvReservar != null) {
            rvReservar.setOnClickListener(v ->
                    Navigation.findNavController(view)
                            .navigate(R.id.action_navigation_inicio_VistaReservas_to_reservationDetail)
            );
        }
    }
}