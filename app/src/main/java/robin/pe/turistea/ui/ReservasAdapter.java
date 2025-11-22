package robin.pe.turistea.ui;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import robin.pe.turistea.R;

public class ReservasAdapter extends RecyclerView.Adapter<ReservasAdapter.ReservaViewHolder> {

    private List<JSONObject> reservas;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(JSONObject item);
    }

    public ReservasAdapter(List<JSONObject> reservas, OnItemClickListener listener) {
        this.reservas = reservas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReservaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reserva, parent, false);
        return new ReservaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        JSONObject reserva = reservas.get(position);
        holder.bind(reserva, listener);
    }

    @Override
    public int getItemCount() {
        return reservas.size();
    }

    static class ReservaViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPackageName;
        private TextView tvUserName;
        private TextView tvMeetingPoint;
        private TextView tvStatus;

        public ReservaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPackageName = itemView.findViewById(R.id.tvItemPackageName);
            tvUserName = itemView.findViewById(R.id.tvItemUserName);
            tvMeetingPoint = itemView.findViewById(R.id.tvItemMeetingPoint);
            tvStatus = itemView.findViewById(R.id.tvItemStatus);
        }

        public void bind(final JSONObject item, final OnItemClickListener listener) {
            Log.d("ReservasAdapter", "Mostrando datos para: " + item.toString());

            try {
                // **CORRECCIÓN FINAL**: Usar las claves correctas del JSON real.

                // 1. Obtener el nombre del paquete desde el objeto anidado "package"
                String packageName = "Paquete no encontrado";
                if (item.has("package") && !item.isNull("package")) {
                    JSONObject packageInfo = item.getJSONObject("package");
                    packageName = packageInfo.optString("title", "Título no disponible");
                }
                tvPackageName.setText(packageName);

                // 2. Obtener el nombre completo del usuario
                tvUserName.setText("Reservado por: " + item.optString("full_name", "Usuario no disponible"));
                
                // 3. El punto de encuentro no está claro en el JSON, se usa un valor por defecto.
                //    Si estuviera en el JSON, se cambiaría aquí.
                tvMeetingPoint.setText("Encuentro: No especificado");
                
                // 4. Obtener el estado del formulario
                String status = item.optString("status_form", "desconocido");
                tvStatus.setText("ESTADO: " + status.toUpperCase());

                itemView.setOnClickListener(v -> listener.onItemClick(item));

            } catch (JSONException e) {
                Log.e("ReservasAdapter", "Error al parsear el item de reserva", e);
                e.printStackTrace();
            }
        }
    }
}