package com.example.miticosmundial;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.parse.ParseObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.VH> {

    private final List<ParseObject> partidos;
    private final Set<String> enEdicion = new HashSet<>();

    public AdminAdapter(List<ParseObject> partidos) {
        this.partidos = partidos;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_partido, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ParseObject partido = partidos.get(position);
        String id = partido.getObjectId();
        String local = partido.getString("equipoLocal");
        String visit = partido.getString("equipoVisitante");

        holder.nombreLocal.setText(local);
        holder.nombreVisitante.setText(visit);
        PartidoAdapter.cargarEscudo(holder.escudoLocal, local);
        PartidoAdapter.cargarEscudo(holder.escudoVisitante, visit);

        // En admin no hace falta "ver pronósticos"
        View bp = holder.itemView.findViewById(R.id.botonPronosticos);
        if (bp != null) bp.setVisibility(View.GONE);

        Date fecha = partido.getDate("fechaHora");
        if (fecha != null) {
            SimpleDateFormat fmt = new SimpleDateFormat("EEE d MMM · HH:mm", new Locale("es", "ES"));
            fmt.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
            holder.fecha.setText(fmt.format(fecha));
        } else {
            holder.fecha.setText("");
        }

        holder.estado.setVisibility(View.GONE);
        holder.zonaPrediccion.setVisibility(View.VISIBLE);

        boolean tieneResultado = partido.get("golesLocal") != null && partido.get("golesVisitante") != null;
        boolean editable = !tieneResultado || enEdicion.contains(id);

        holder.golesLocal.setText(partido.get("golesLocal") != null ? String.valueOf(partido.getInt("golesLocal")) : "");
        holder.golesVisitante.setText(partido.get("golesVisitante") != null ? String.valueOf(partido.getInt("golesVisitante")) : "");

        holder.golesLocal.setEnabled(editable);
        holder.golesVisitante.setEnabled(editable);
        holder.guardar.setText(editable ? "Guardar resultado" : "✏ Modificar");

        holder.guardar.setOnClickListener(v -> {
            if (!editable) {
                enEdicion.add(id);
                notifyItemChanged(holder.getAdapterPosition());
                return;
            }
            String sl = holder.golesLocal.getText().toString().trim();
            String sv = holder.golesVisitante.getText().toString().trim();
            if (sl.isEmpty() || sv.isEmpty()) {
                Toast.makeText(v.getContext(), "Pon los dos goles", Toast.LENGTH_SHORT).show();
                return;
            }
            int gl = Integer.parseInt(sl);
            int gv = Integer.parseInt(sv);

            new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                    .setTitle("¿Confirmar resultado?")
                    .setMessage(local + "   " + gl + " - " + gv + "   " + visit)
                    .setPositiveButton("Sí, es correcto", (d, w) -> {
                        partido.put("golesLocal", gl);       // solo para que la fila lo muestre
                        partido.put("golesVisitante", gv);
                        java.util.HashMap<String, Object> params = new java.util.HashMap<>();
                        params.put("partidoId", partido.getObjectId());
                        params.put("golesLocal", gl);
                        params.put("golesVisitante", gv);
                        com.parse.ParseCloud.callFunctionInBackground("guardarResultado", params,
                                (Object r, com.parse.ParseException e) -> {
                                    if (e == null) {
                                        enEdicion.remove(id);
                                        notifyItemChanged(holder.getAdapterPosition());
                                        Toast.makeText(v.getContext(), "¡Resultado guardado!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(v.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    @Override public int getItemCount() { return partidos.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView escudoLocal, escudoVisitante;
        TextView nombreLocal, nombreVisitante, fecha, estado;
        LinearLayout zonaPrediccion;
        EditText golesLocal, golesVisitante;
        Button guardar;
        VH(@NonNull View itemView) {
            super(itemView);
            escudoLocal = itemView.findViewById(R.id.escudoLocal);
            escudoVisitante = itemView.findViewById(R.id.escudoVisitante);
            nombreLocal = itemView.findViewById(R.id.nombreLocal);
            nombreVisitante = itemView.findViewById(R.id.nombreVisitante);
            fecha = itemView.findViewById(R.id.textoFecha);
            estado = itemView.findViewById(R.id.textoEstado);
            zonaPrediccion = itemView.findViewById(R.id.zonaPrediccion);
            golesLocal = itemView.findViewById(R.id.golesLocal);
            golesVisitante = itemView.findViewById(R.id.golesVisitante);
            guardar = itemView.findViewById(R.id.botonGuardar);
        }
    }
}