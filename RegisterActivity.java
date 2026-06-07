package com.example.miticosmundial;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.parse.ParseFile;
import com.parse.ParseUser;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class RegisterActivity extends AppCompatActivity {

    private Uri fotoUri = null;
    private ActivityResultLauncher<String> selectorFoto;

    private final String[] EQUIPOS = {
            "Argentina","Brasil","Ecuador","Uruguay","Paraguay","Colombia",
            "Estados Unidos","México","Canadá","Haití","Panamá","Curazao",
            "Inglaterra","Francia","Croacia","Noruega","Portugal","Alemania",
            "Países Bajos","España","Escocia","Bélgica","Austria","Suiza",
            "Suecia","Turquía","República Checa","Bosnia y Herzegovina",
            "Japón","Irán","Jordania","Uzbekistán","Corea del Sur","Australia",
            "Catar","Arabia Saudita","Irak",
            "Marruecos","Túnez","Argelia","Egipto","Ghana","Cabo Verde",
            "Senegal","Costa de Marfil","Sudáfrica","RD Congo","Nueva Zelanda"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText campoNombre = findViewById(R.id.campoNombre);
        EditText campoEmail = findViewById(R.id.campoEmail);
        EditText campoPassword = findViewById(R.id.campoPassword);
        Spinner spinnerCampeon = findViewById(R.id.spinnerCampeon);
        Button botonCrear = findViewById(R.id.botonCrear);
        Button botonFoto = findViewById(R.id.botonFoto);
        ImageView imagenPerfil = findViewById(R.id.imagenPerfil);

        // Desplegable de equipos
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner, EQUIPOS);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerCampeon.setAdapter(adapter);

        // Selector de fotos clásico (compatible con MIUI/Xiaomi)
        selectorFoto = registerForActivityResult(
                new ActivityResultContracts.GetContent(), uri -> {
                    if (uri != null) {
                        fotoUri = uri;
                        imagenPerfil.setImageURI(uri);
                    }
                });

        botonFoto.setOnClickListener(v -> selectorFoto.launch("image/*"));

        botonCrear.setOnClickListener(v -> {
            String nombre = campoNombre.getText().toString().trim();
            String email = campoEmail.getText().toString().trim();
            String password = campoPassword.getText().toString();
            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            ParseUser user = new ParseUser();
            user.setUsername(email);
            user.setEmail(email);
            user.setPassword(password);
            user.put("nombre", nombre);
            user.put("prediccionCampeon", spinnerCampeon.getSelectedItem().toString());
            user.put("puntosTotales", 0);

            botonCrear.setEnabled(false); // evita doble pulsación

            // 1) Primero registramos al usuario SIN la foto
            user.signUpInBackground(e -> {
                if (e != null) {
                    botonCrear.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }
                // 2) Usuario ya creado. Si eligió foto, la subimos ahora.
                if (fotoUri != null) {
                    try {
                        byte[] bytes = comprimirFoto(fotoUri);
                        if (bytes != null) {
                            ParseFile archivo = new ParseFile("perfil.jpg", bytes);
                            user.put("foto", archivo);
                            user.saveInBackground(e2 -> {
                                // Entramos al menú aunque la foto fallara (la cuenta ya está creada)
                                irAlMenu();
                            });
                            return;
                        }
                    } catch (Exception ex) {
                        Toast.makeText(this, "No se pudo procesar la foto, cuenta creada sin ella", Toast.LENGTH_SHORT).show();
                    }
                }
                // Sin foto (o si falló al procesarla): directo al menú
                irAlMenu();
            });
        });
    }

    private void irAlMenu() {
        startActivity(new Intent(this, MainActivity.class));
        finishAffinity();
    }

    private byte[] comprimirFoto(Uri uri) throws Exception {
        InputStream is = getContentResolver().openInputStream(uri);
        Bitmap original = BitmapFactory.decodeStream(is);
        if (is != null) is.close();
        if (original == null) return null;

        int maxLado = 500;
        int ancho = original.getWidth();
        int alto = original.getHeight();
        float escala = Math.min(1f, (float) maxLado / Math.max(ancho, alto));
        Bitmap pequena = Bitmap.createScaledBitmap(
                original,
                Math.max(1, Math.round(ancho * escala)),
                Math.max(1, Math.round(alto * escala)),
                true);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        pequena.compress(Bitmap.CompressFormat.JPEG, 80, out);
        return out.toByteArray();
    }
}