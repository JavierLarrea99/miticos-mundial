package com.example.miticosmundial;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button botonPerfil = findViewById(R.id.botonPerfil);
        botonPerfil.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        Button botonRanking = findViewById(R.id.botonRanking);
        Button botonAdmin = findViewById(R.id.botonAdmin);
        botonAdmin.setOnClickListener(v -> startActivity(new Intent(this, AdminActivity.class)));
        comprobarAdmin(botonAdmin);
        findViewById(R.id.botonResultados).setOnClickListener(v ->
                startActivity(new Intent(this, ResultadosActivity.class)));
        RecyclerView recycler = findViewById(R.id.recyclerPartidos);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        findViewById(R.id.botonActualizar).setOnClickListener(v -> {
            Toast.makeText(this, "Actualizando…", Toast.LENGTH_SHORT).show();
            recargar();
        });

        botonRanking.setOnClickListener(v ->
                startActivity(new Intent(this, RankingActivity.class)));

        // Enlazar este móvil con el usuario (para las push dirigidas)
        ParseUser usuarioActual = ParseUser.getCurrentUser();
        if (usuarioActual != null) {
            com.parse.ParseInstallation inst = com.parse.ParseInstallation.getCurrentInstallation();
            inst.put("user", usuarioActual);
            inst.saveInBackground();
        }

        pedirPermisoNotificaciones();
        mostrarPuntosGanados();
        recargar();
    }

    // ---------- Permiso de notificaciones (Android 13+) ----------
    private void pedirPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
        }
    }

    private void mostrarPuntosGanados() {
        ParseUser user = ParseUser.getCurrentUser();
        if (user == null) return;
        user.<ParseUser>fetchInBackground((actualizado, e) -> {
            if (e != null) return;
            int actuales = actualizado.getInt("puntosTotales");
            SharedPreferences prefs = getSharedPreferences("ajustes", MODE_PRIVATE);
            if (!prefs.contains("ultimosPuntos")) {
                prefs.edit().putInt("ultimosPuntos", actuales).apply();
                return; // primera vez, no mostramos nada
            }
            int previos = prefs.getInt("ultimosPuntos", actuales);
            int delta = actuales - previos;
            prefs.edit().putInt("ultimosPuntos", actuales).apply();
            if (delta > 0) {
                new AlertDialog.Builder(this)
                        .setTitle("🎉 ¡Has sumado puntos!")
                        .setMessage("Has ganado " + delta + " puntos desde la última vez.\n\nTotal: " + actuales + " pts")
                        .setPositiveButton("¡Bien!", null)
                        .show();
            }
        });
    }

    private void comprobarAdmin(Button botonAdmin) {
        ParseUser user = ParseUser.getCurrentUser();
        if (user == null) return;
        user.<ParseUser>fetchInBackground((u, e) -> {
            if (e == null && u.getBoolean("esAdmin")) {
                botonAdmin.setVisibility(android.view.View.VISIBLE);
            }
        });
    }

    private void recargar() {
        RecyclerView recycler = findViewById(R.id.recyclerPartidos);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        ProgressBar progreso = findViewById(R.id.progreso);
        progreso.setVisibility(android.view.View.VISIBLE);
        TextView textoVacio = findViewById(R.id.textoVacio);

        ParseQuery<ParseObject> qPred = ParseQuery.getQuery("Prediccion");
        qPred.whereEqualTo("usuario", ParseUser.getCurrentUser());
        qPred.findInBackground((preds, e1) -> {
            HashMap<String, ParseObject> mapa = new HashMap<>();
            if (e1 == null) {
                for (ParseObject pr : preds) {
                    ParseObject part = pr.getParseObject("partido");
                    if (part != null) mapa.put(part.getObjectId(), pr);
                }
            }

            // Ventana: desde hace 4 horas (para que un partido EN JUEGO siga saliendo) hasta +36h
            java.util.Date inicioHoy = new java.util.Date(System.currentTimeMillis() - 4L * 60 * 60 * 1000); // hace 4 horas
            java.util.Date inicioManana = new java.util.Date(System.currentTimeMillis() + 36L * 60 * 60 * 1000); // +36 horas

            ParseQuery<ParseObject> qPart = ParseQuery.getQuery("Partido");
            qPart.whereGreaterThanOrEqualTo("fechaHora", inicioHoy);
            qPart.whereLessThan("fechaHora", inicioManana);
            qPart.whereNotEqualTo("estado", "finalizado");
            qPart.orderByAscending("fechaHora");
            qPart.setLimit(1000);
            qPart.findInBackground((partidos, e2) -> {
                progreso.setVisibility(android.view.View.GONE);
                if (e2 == null) {
                    recycler.setAdapter(new PartidoAdapter(partidos, mapa));
                    textoVacio.setVisibility(partidos.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                } else {
                    Toast.makeText(this, "Error: " + e2.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}