package com.example.miticosmundial;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import java.util.HashMap;

public class ResultadosActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultados);

        findViewById(R.id.botonVolver).setOnClickListener(v -> finish());

        RecyclerView recycler = findViewById(R.id.recyclerResultados);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        ProgressBar progreso = findViewById(R.id.progreso);
        progreso.setVisibility(View.VISIBLE);

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
            ParseQuery<ParseObject> q = ParseQuery.getQuery("Partido");
            q.whereEqualTo("estado", "finalizado");
            q.orderByDescending("fechaHora");
            q.setLimit(1000);
            q.findInBackground((partidos, e2) -> {
                progreso.setVisibility(View.GONE);
                if (e2 == null) recycler.setAdapter(new PartidoAdapter(partidos, mapa));
                else Toast.makeText(this, "Error: " + e2.getMessage(), Toast.LENGTH_LONG).show();
            });
        });
    }
}