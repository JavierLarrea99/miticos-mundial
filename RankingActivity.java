package com.example.miticosmundial;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.parse.ParseCloud;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RankingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        RecyclerView recycler = findViewById(R.id.recyclerRanking);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        ParseCloud.<List<Map<String, Object>>>callFunctionInBackground(
                "getRanking", new HashMap<>(), (lista, e) -> {
                    if (e == null) {
                        recycler.setAdapter(new RankingAdapter(lista));
                    } else {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
        findViewById(R.id.botonVolver).setOnClickListener(v -> finish());
    }
}