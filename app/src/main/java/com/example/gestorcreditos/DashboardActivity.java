package com.example.gestorcreditos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        // Toolbar: sin ella el menú no aparece (el tema es NoActionBar)
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvBienvenida = findViewById(R.id.tvBienvenida);
        String nombre = getIntent().getStringExtra("NOMBRE_ALUMNO");
        if (nombre != null) {
            tvBienvenida.setText("Bienvenido, " + nombre);
        }

        // Botón directo para abrir los programas
        Button btnProgramas = findViewById(R.id.btnProgramas);
        btnProgramas.setOnClickListener(v ->
                startActivity(new Intent(this, ProgramasActivity.class)));
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_programas) {
            startActivity(new Intent(this, ProgramasActivity.class));
            return true;
        } else if (id == R.id.menu_usuarios) {
            startActivity(new Intent(this, UsuariosActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}