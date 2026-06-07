package com.example.miticosmundial;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.parse.ParseFile;
import com.parse.ParseUser;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imagen;
    private TextView nombre;
    private ActivityResultLauncher<String> selectorFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imagen = findViewById(R.id.imagenPerfil);
        nombre = findViewById(R.id.textoNombre);
        TextView campeon = findViewById(R.id.textoCampeon);
        TextView puntos = findViewById(R.id.textoPuntos);
        Button logout = findViewById(R.id.botonLogout);
        Button botonCambiarNombre = findViewById(R.id.botonCambiarNombre);
        Button botonCambiarFoto = findViewById(R.id.botonCambiarFoto);

        // Botón Volver (si lo tienes en el layout)
        if (findViewById(R.id.botonVolver) != null) {
            findViewById(R.id.botonVolver).setOnClickListener(v -> finish());
        }

        logout.setOnClickListener(v -> {
            ParseUser.logOut();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });

        // Selector de foto
        selectorFoto = registerForActivityResult(
                new ActivityResultContracts.GetContent(), uri -> {
                    if (uri != null) guardarFoto(uri);
                });

        botonCambiarFoto.setOnClickListener(v -> selectorFoto.launch("image/*"));
        botonCambiarNombre.setOnClickListener(v -> dialogoCambiarNombre());

        cargarDatos(campeon, puntos);
    }

    private void cargarDatos(TextView campeon, TextView puntos) {
        ParseUser user = ParseUser.getCurrentUser();
        if (user == null) return;
        user.<ParseUser>fetchInBackground((u, e) -> {
            if (e != null) return;
            nombre.setText(u.getString("nombre"));
            String camp = u.getString("prediccionCampeon");
            campeon.setText("Tu campeón: " + (camp != null ? camp : "-"));
            puntos.setText("⭐ Puntos: " + u.getInt("puntosTotales"));
            ParseFile foto = u.getParseFile("foto");
            if (foto != null) {
                Glide.with(this).load(foto.getUrl()).circleCrop()
                        .placeholder(R.mipmap.ic_launcher).into(imagen);
            } else {
                imagen.setImageResource(R.mipmap.ic_launcher);
            }
        });
    }

    private void dialogoCambiarNombre() {
        ParseUser user = ParseUser.getCurrentUser();
        if (user == null) return;

        EditText input = new EditText(this);
        input.setText(user.getString("nombre"));
        input.setTextColor(0xFFFFFFFF);
        input.setPadding(48, 32, 48, 32);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Cambiar nombre")
                .setView(input)
                .setPositiveButton("Guardar", (d, w) -> {
                    String nuevo = input.getText().toString().trim();
                    if (nuevo.isEmpty()) {
                        Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    user.put("nombre", nuevo);
                    user.saveInBackground(e -> {
                        if (e == null) {
                            nombre.setText(nuevo);
                            Toast.makeText(this, "¡Nombre actualizado!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardarFoto(Uri uri) {
        ParseUser user = ParseUser.getCurrentUser();
        if (user == null) return;
        try {
            byte[] bytes = comprimirFoto(uri);
            if (bytes == null) {
                Toast.makeText(this, "No se pudo procesar la foto", Toast.LENGTH_SHORT).show();
                return;
            }
            user.put("foto", new ParseFile("perfil.jpg", bytes));
            Toast.makeText(this, "Guardando foto…", Toast.LENGTH_SHORT).show();
            user.saveInBackground(e -> {
                if (e == null) {
                    Glide.with(this).load(uri).circleCrop().into(imagen);
                    Toast.makeText(this, "¡Foto actualizada!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception ex) {
            Toast.makeText(this, "No se pudo procesar la foto", Toast.LENGTH_SHORT).show();
        }
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