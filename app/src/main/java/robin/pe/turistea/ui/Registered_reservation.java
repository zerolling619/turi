package robin.pe.turistea.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import robin.pe.turistea.R;

public class Registered_reservation extends Fragment {

    private NavController navController;

    public Registered_reservation() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_registered_reservation, container, false);

        // 2. Obtener el botón
        Button btnRegisterReservation = view.findViewById(R.id.BtnSeeContract);

        // 3. Evento click → navegar al fragment_reservation
        btnRegisterReservation.setOnClickListener(v -> {
            navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_navigation_registered_reservation_to_navigation_signing_process);
        });

        Button btnInicio = view.findViewById(R.id.BtnInicio);
        btnInicio.setOnClickListener(v -> {
            navController = Navigation.findNavController(view);
            navController.navigate(R.id.navigation_inicio);
        });


        TextView tvMensaje = view.findViewById(R.id.TvGenerated);

        tvMensaje.setText("Estamos generando el contrato\n" +
                "porfavor validaremos los documentos y te\n" +
                "entregaremos un contrato de afiliación con el\n" +
                "conductor.");

        return view;
    }
}
