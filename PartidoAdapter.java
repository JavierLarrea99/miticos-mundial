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
import com.parse.ParseUser;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public class PartidoAdapter extends RecyclerView.Adapter<PartidoAdapter.VH> {

    private final List<ParseObject> partidos;
    private final HashMap<String, ParseObject> predicciones;
    private final Set<String> enEdicion = new HashSet<>();

    private static final Map<String, String> CODIGOS = new HashMap<>();
    static {
        CODIGOS.put("México", "mx"); CODIGOS.put("Sudáfrica", "za"); CODIGOS.put("Corea del Sur", "kr"); CODIGOS.put("Chequia", "cz");
        CODIGOS.put("Canadá", "ca"); CODIGOS.put("Suiza", "ch"); CODIGOS.put("Qatar", "qa"); CODIGOS.put("Bosnia", "ba");
        CODIGOS.put("Brasil", "br"); CODIGOS.put("Marruecos", "ma"); CODIGOS.put("Haití", "ht"); CODIGOS.put("Escocia", "gb-sct");
        CODIGOS.put("Estados Unidos", "us"); CODIGOS.put("Paraguay", "py"); CODIGOS.put("Australia", "au"); CODIGOS.put("Turquía", "tr");
        CODIGOS.put("Alemania", "de"); CODIGOS.put("Curazao", "cw"); CODIGOS.put("Costa de Marfil", "ci"); CODIGOS.put("Ecuador", "ec");
        CODIGOS.put("Países Bajos", "nl"); CODIGOS.put("Japón", "jp"); CODIGOS.put("Túnez", "tn"); CODIGOS.put("Suecia", "se");
        CODIGOS.put("Bélgica", "be"); CODIGOS.put("Egipto", "eg"); CODIGOS.put("Irán", "ir"); CODIGOS.put("Nueva Zelanda", "nz");
        CODIGOS.put("España", "es"); CODIGOS.put("Cabo Verde", "cv"); CODIGOS.put("Arabia Saudita", "sa"); CODIGOS.put("Uruguay", "uy");
        CODIGOS.put("Francia", "fr"); CODIGOS.put("Senegal", "sn"); CODIGOS.put("Noruega", "no"); CODIGOS.put("Iraq", "iq");
        CODIGOS.put("Argentina", "ar"); CODIGOS.put("Argelia", "dz"); CODIGOS.put("Austria", "at"); CODIGOS.put("Jordania", "jo");
        CODIGOS.put("Portugal", "pt"); CODIGOS.put("Colombia", "co"); CODIGOS.put("Uzbekistán", "uz"); CODIGOS.put("RD Congo", "cd");
        CODIGOS.put("Inglaterra", "gb-eng"); CODIGOS.put("Croacia", "hr"); CODIGOS.put("Ghana", "gh"); CODIGOS.put("Panamá", "pa");
    }

    private static String normalizar(String s) {
        String n = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD).replaceAll("\\p{Mn}", "");
        n = n.toLowerCase().replaceAll("[^a-z0-9]+", "_");
        return n.replaceAll("^_+|_+$", "");
    }

    public static void cargarEscudo(ImageView destino, String equipo) {
        android.content.Context ctx = destino.getContext();
        int resId = ctx.getResources().getIdentifier(normalizar(equipo), "drawable", ctx.getPackageName());
        String code = CODIGOS.get(equipo);
        String urlBandera = (code != null) ? "https://flagcdn.com/w160/" + code + ".png" : null;

        com.bumptech.glide.RequestManager glide = com.bumptech.glide.Glide.with(ctx);
        if (resId != 0) {
            if (urlBandera != null) glide.load(resId).error(glide.load(urlBandera)).into(destino);
            else glide.load(resId).into(destino);
        } else if (urlBandera != null) {
            glide.load(urlBandera).into(destino);
        } else {
            destino.setImageDrawable(null);
        }
    }

    public PartidoAdapter(List<ParseObject> partidos, HashMap<String, ParseObject> predicciones) {
        this.partidos = partidos;
        this.predicciones = predicciones;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_partido, parent, false);
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
        cargarEscudo(holder.escudoLocal, local);
        cargarEscudo(holder.escudoVisitante, visit);

        String fase = partido.getString("fase");
        if (fase != null && !fase.isEmpty()) {
            holder.textoFase.setText(fase.toUpperCase());
            holder.textoFase.setVisibility(View.VISIBLE);
        } else {
            holder.textoFase.setVisibility(View.GONE);
        }

        Date fecha = partido.getDate("fechaHora");
        if (fecha != null) {
            SimpleDateFormat fmt = new SimpleDateFormat("EEE d MMM · HH:mm", new Locale("es", "ES"));
            fmt.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
            holder.fecha.setText(fmt.format(fecha));
        } else {
            holder.fecha.setText("");
        }

        ParseObject pred = predicciones.get(id);
        boolean cerrado = fecha != null && fecha.before(new Date());

        holder.botonPronosticos.setOnClickListener(v -> {
            Date f = partido.getDate("fechaHora");
            boolean empezado = f != null && f.before(new Date());
            if (!empezado) {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(v.getContext())
                        .setTitle("🔒 Aún no")
                        .setMessage("Podrás ver los pronósticos de los demás cuando empiece el partido.")
                        .setPositiveButton("Vale", null).show();
                return;
            }
            java.util.HashMap<String, Object> params = new java.util.HashMap<>();
            params.put("partidoId", partido.getObjectId());
            com.parse.ParseCloud.<java.util.List<String>>callFunctionInBackground("getPronosticos", params, (lista, e) -> {
                if (e != null) {
                    Toast.makeText(v.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }
                String html = (lista == null || lista.isEmpty())
                        ? "Nadie ha pronosticado este partido."
                        : android.text.TextUtils.join("<br><br>", lista);
                View vista = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_pronosticos, null);
                ((TextView) vista.findViewById(R.id.contenidoPronosticos))
                        .setText(android.text.Html.fromHtml(html, android.text.Html.FROM_HTML_MODE_LEGACY));
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(v.getContext())
                        .setView(vista)
                        .setPositiveButton("Cerrar", null)
                        .show();
            });
        });

        if (cerrado) {
            holder.zonaPrediccion.setVisibility(View.GONE);
            holder.estado.setVisibility(View.VISIBLE);

            com.google.android.material.card.MaterialCardView card =
                    (com.google.android.material.card.MaterialCardView) holder.itemView;
            card.setStrokeWidth(Math.round(3 * holder.itemView.getResources().getDisplayMetrics().density));

            boolean hayResultado = partido.get("golesLocal") != null && partido.get("golesVisitante") != null;

            if (!hayResultado) {
                card.setStrokeColor(0xFF2A2A2A);
                String h = "<font color='#B0B0B0'>Aún sin resultado</font>";
                if (pred != null) h += "<br><font color='#B0B0B0'>Tu pronóstico: "
                        + pred.getInt("predLocal") + " - " + pred.getInt("predVisitante") + "</font>";
                holder.estado.setText(android.text.Html.fromHtml(h, android.text.Html.FROM_HTML_MODE_LEGACY));
            } else {
                int rgl = partido.getInt("golesLocal");
                int rgv = partido.getInt("golesVisitante");
                String marcador = rgl + " - " + rgv;
                int borde; String colorHex; String titulo;

                if (pred == null) {
                    borde = 0xFF777777; colorHex = "#9E9E9E"; titulo = "— No pronosticaste  -1";
                } else {
                    int pl = pred.getInt("predLocal");
                    int pv = pred.getInt("predVisitante");
                    Integer pts = (pred.get("puntos") != null) ? pred.getInt("puntos") : null;
                    String ptsTxt = (pts != null) ? ("  " + (pts >= 0 ? "+" : "") + pts + " pts") : "";
                    if (pl == rgl && pv == rgv) {
                        borde = 0xFFE0A82E; colorHex = "#E0A82E"; titulo = "Marcador exacto" + ptsTxt;
                    } else {
                        int resReal = rgl > rgv ? 1 : (rgl < rgv ? 2 : 0);
                        int resPred = pl > pv ? 1 : (pl < pv ? 2 : 0);
                        if (resReal == resPred) {
                            borde = 0xFF4CAF50; colorHex = "#4CAF50"; titulo = "✓ Acertaste el resultado" + ptsTxt;
                        } else {
                            borde = 0xFFE05A5A; colorHex = "#E05A5A"; titulo = "✗ Fallaste" + ptsTxt;
                        }
                    }
                }
                card.setStrokeColor(borde);

                String h = "<b><font color='" + colorHex + "'>" + titulo + "</font></b>";
                h += "<br><font color='#FFFFFF'>Resultado <b>" + marcador + "</b></font>";
                if (pred != null) {
                    h += "<font color='#B0B0B0'>  ·  Tu " + pred.getInt("predLocal") + " - " + pred.getInt("predVisitante") + "</font>";
                }
                holder.estado.setText(android.text.Html.fromHtml(h, android.text.Html.FROM_HTML_MODE_LEGACY));
            }

        } else {
            holder.zonaPrediccion.setVisibility(View.VISIBLE);
            holder.estado.setVisibility(View.GONE);
            com.google.android.material.card.MaterialCardView card =
                    (com.google.android.material.card.MaterialCardView) holder.itemView;
            card.setStrokeColor(0xFF2A2A2A);
            card.setStrokeWidth(Math.round(1 * holder.itemView.getResources().getDisplayMetrics().density));

            boolean tienePrediccion = pred != null;
            boolean editable = !tienePrediccion || enEdicion.contains(id);

            if (pred != null) {
                holder.golesLocal.setText(String.valueOf(pred.getInt("predLocal")));
                holder.golesVisitante.setText(String.valueOf(pred.getInt("predVisitante")));
            } else {
                holder.golesLocal.setText("");
                holder.golesVisitante.setText("");
            }

            holder.golesLocal.setEnabled(editable);
            holder.golesVisitante.setEnabled(editable);
            holder.guardar.setText(editable ? "Guardar" : "✏ Modificar");

            holder.guardar.setOnClickListener(v -> {
                if (!editable) {
                    enEdicion.add(id);
                    notifyItemChanged(holder.getAdapterPosition());
                    return;
                }
                String sl = holder.golesLocal.getText().toString().trim();
                String sv = holder.golesVisitante.getText().toString().trim();
                if (sl.isEmpty() || sv.isEmpty()) {
                    Toast.makeText(v.getContext(), "Pon los dos marcadores", Toast.LENGTH_SHORT).show();
                    return;
                }
                int gl = Integer.parseInt(sl);
                int gv = Integer.parseInt(sv);

                ParseObject p = predicciones.get(id);
                if (p == null) {
                    p = new ParseObject("Prediccion");
                    p.put("usuario", ParseUser.getCurrentUser());
                    p.put("partido", partido);
                }
                p.put("predLocal", gl);
                p.put("predVisitante", gv);

                ParseObject pFinal = p;
                p.saveInBackground(e -> {
                    if (e == null) {
                        predicciones.put(id, pFinal);
                        enEdicion.remove(id);
                        notifyItemChanged(holder.getAdapterPosition());
                        Toast.makeText(v.getContext(), "¡Pronóstico guardado!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(v.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            });
        }
    }

    @Override public int getItemCount() { return partidos.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView escudoLocal, escudoVisitante;
        TextView nombreLocal, nombreVisitante, fecha, estado, textoFase;
        LinearLayout zonaPrediccion;
        EditText golesLocal, golesVisitante;
        Button guardar, botonPronosticos;
        VH(@NonNull View itemView) {
            super(itemView);
            escudoLocal = itemView.findViewById(R.id.escudoLocal);
            escudoVisitante = itemView.findViewById(R.id.escudoVisitante);
            nombreLocal = itemView.findViewById(R.id.nombreLocal);
            nombreVisitante = itemView.findViewById(R.id.nombreVisitante);
            fecha = itemView.findViewById(R.id.textoFecha);
            estado = itemView.findViewById(R.id.textoEstado);
            textoFase = itemView.findViewById(R.id.textoFase);
            zonaPrediccion = itemView.findViewById(R.id.zonaPrediccion);
            golesLocal = itemView.findViewById(R.id.golesLocal);
            golesVisitante = itemView.findViewById(R.id.golesVisitante);
            guardar = itemView.findViewById(R.id.botonGuardar);
            botonPronosticos = itemView.findViewById(R.id.botonPronosticos);
        }
    }
}