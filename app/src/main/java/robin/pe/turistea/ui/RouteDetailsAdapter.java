package robin.pe.turistea.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import robin.pe.turistea.R;
import robin.pe.turistea.models.RouteItemDetail;

public class RouteDetailsAdapter extends RecyclerView.Adapter<RouteDetailsAdapter.RouteViewHolder> {

    private List<RouteItemDetail> routeList;

    public RouteDetailsAdapter(List<RouteItemDetail> routeList) {
        this.routeList = routeList;
    }

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_route_detail, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        RouteItemDetail route = routeList.get(position);
        holder.tvRouteTitle.setText(route.getTitle());
        holder.tvRouteDescription.setText(route.getDescription());
    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }

    public static class RouteViewHolder extends RecyclerView.ViewHolder {
        TextView tvRouteTitle;
        TextView tvRouteDescription;

        public RouteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRouteTitle = itemView.findViewById(R.id.tvRouteTitle);
            tvRouteDescription = itemView.findViewById(R.id.tvRouteDescription);
        }
    }
}
