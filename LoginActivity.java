package com.example.miticosmundial;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Si ya hay sesión guardada, entrar directo
        if (ParseUser.getCurrentUser() != null) {
            irAlMenu();
            return;
        }

        setContentView(R.layout.activity_login);

        EditText campoEmail = findViewById(R.id.campoEmail);
        EditText campoPassword = findViewById(R.id.campoPassword);
        Button botonEntrar = findViewById(R.id.botonEntrar);
        TextView textoRegistro = findViewById(R.id.textoRegistro);

        botonEntrar.setOnClickListener(v -> {
            String email = campoEmail.getText().toString().trim();
            String password = campoPassword.getText().toString();
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Rellena email y contraseña", Toast.LENGTH_SHORT).show();
                return;
            }
            ParseUser.logInInBackground(email, password, (user, e) -> {
                if (e == null) {
                    irAlMenu();
                } else {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        textoRegistro.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void irAlMenu() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}