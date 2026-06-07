package com.example.miticosmundial;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class AdminActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        RecyclerView recycler = findViewById(R.id.recyclerAdmin);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        ParseQuery<ParseObject> q = ParseQuery.getQuery("Partido");
        q.orderByAscending("fechaHora");
        q.setLimit(1000);
        q.findInBackground((partidos, e) -> {
            if (e == null) recycler.setAdapter(new AdminAdapter(partidos));
            else Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
        findViewById(R.id.botonVolver).setOnClickListener(v -> finish());
    }
}