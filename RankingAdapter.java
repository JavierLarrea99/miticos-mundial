package com.example.miticosmundial;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import java.util.Map;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.VH> {

    private final List<Map<String, Object>> ranking;

    public RankingAdapter(List<Map<String, Object>> ranking) {
        this.ranking = ranking;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ranking, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Map<String, Object> u = ranking.get(position);

        holder.posicion.setText((position + 1) + "º");
        holder.nombre.setText(String.valueOf(u.get("nombre")));

        Object pts = u.get("puntos");
        int puntos = (pts instanceof Number) ? ((Number) pts).intValue() : 0;
        holder.puntos.setText(puntos + " pts");

        Object url = u.get("fotoUrl");
        if (url != null && !url.toString().isEmpty() && !"null".equals(url.toString())) {
            Glide.with(holder.itemView).load(url.toString()).circleCrop()
                    .placeholder(R.mipmap.ic_launcher).into(holder.foto);
        } else {
            holder.foto.setImageResource(R.mipmap.ic_launcher);
        }

        Object campeon = u.get("campeon");
        if (campeon != null && !campeon.toString().isEmpty()) {
            holder.campeon.setVisibility(View.VISIBLE);
            PartidoAdapter.cargarEscudo(holder.campeon, campeon.toString());
        } else {
            holder.campeon.setVisibility(View.GONE);
        }

        // ---- Colores del podio + último ----
        int total = getItemCount();
        int colorBorde;
        int colorPos; // color del número de posición
        if (position == 0) {
            colorBorde = 0xFFE0A82E; colorPos = 0xFFE0A82E;   // oro
        } else if (position == 1) {
            colorBorde = 0xFFB0B0B0; colorPos = 0xFFB0B0B0;   // plata
        } else if (position == 2) {
            colorBorde = 0xFFB87333; colorPos = 0xFFB87333;   // bronce
        } else if (position == total - 1 && total > 3) {
            colorBorde = 0xFFE05A5A; colorPos = 0xFFE05A5A;   // último: rojo
        } else {
            colorBorde = 0xFF2A2A2A; colorPos = 0xFFE0A82E;   // resto: como estaban
        }
        com.google.android.material.card.MaterialCardView card =
                (com.google.android.material.card.MaterialCardView) holder.itemView;
        card.setStrokeColor(colorBorde);
        card.setStrokeWidth(Math.round(2 * holder.itemView.getResources().getDisplayMetrics().density));
        holder.posicion.setTextColor(colorPos);
    }

    @Override
    public int getItemCount() {
        return ranking == null ? 0 : ranking.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView posicion, nombre, puntos;
        ImageView foto, campeon;
        VH(@NonNull View itemView) {
            super(itemView);
            posicion = itemView.findViewById(R.id.textoPosicion);
            nombre = itemView.findViewById(R.id.textoNombre);
            puntos = itemView.findViewById(R.id.textoPuntos);
            foto = itemView.findViewById(R.id.imagenFoto);
            campeon = itemView.findViewById(R.id.imagenCampeon);
        }
    }
}