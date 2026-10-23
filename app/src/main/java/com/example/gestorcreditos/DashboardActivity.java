package com.example.gestorcreditos;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class DashboardActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private RecyclerView rvAccionesRapidas;
    private TextView tvDatoPrincipal, tvDatoPrincipalLabel;

    // Acciones rápidas para el grid
    private static final class AccionRapida {
        final int iconRes;
        final String titulo;
        final String subtitulo;
        final Class<?> destino;

        AccionRapida(int iconRes, String titulo, String subtitulo, Class<?> destino) {
            this.iconRes = iconRes;
            this.titulo = titulo;
            this.subtitulo = subtitulo;
            this.destino = destino;
        }
    }

    private final AccionRapida[] ACCIONES = {
            new AccionRapida(R.drawable.ic_programas, "Programas", "Gestionar programas", ProgramasActivity.class),
            new AccionRapida(R.drawable.ic_tutores, "Tutores", "Gestionar tutores", TutoresActivity.class),
            new AccionRapida(R.drawable.ic_usuarios, "Usuarios", "Gestionar usuarios", UsuariosActivity.class),
            new AccionRapida(R.drawable.ic_dashboard, "Créditos", "Ver detalle", null) // null = solo info
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        // Vistas
        bottomNavigation = findViewById(R.id.bottomNavigation);
        rvAccionesRapidas = findViewById(R.id.rvAccionesRapidas);
        tvDatoPrincipal = findViewById(R.id.tvDatoPrincipal);
        tvDatoPrincipalLabel = findViewById(R.id.tvDatoPrincipalLabel);

        // Configurar RecyclerView de acciones rápidas
        rvAccionesRapidas.setAdapter(new AccionesAdapter());
        rvAccionesRapidas.setHasFixedSize(true);

        // BottomNavigationView
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                // Ya estamos aquí
                return true;
            } else if (id == R.id.nav_programas) {
                startActivity(new Intent(this, ProgramasActivity.class));
                return true;
            } else if (id == R.id.nav_tutores) {
                startActivity(new Intent(this, TutoresActivity.class));
                return true;
            } else if (id == R.id.nav_usuarios) {
                startActivity(new Intent(this, UsuariosActivity.class));
                return true;
            }
            return false;
        });

        // Seleccionar el item "Inicio" por defecto
        bottomNavigation.setSelectedItemId(R.id.nav_dashboard);

        // Cargar dato principal (simulado - luego conectar con BD real)
        cargarDatoPrincipal();
    }

    private void cargarDatoPrincipal() {
        // TODO: Conectar con BD real para obtener créditos reales
        // Por ahora datos de prueba
        tvDatoPrincipal.setText("120");
        tvDatoPrincipalLabel.setText("CRÉDITOS DISPONIBLES");
    }

    // ===== ADAPTER PARA ACCIONES RÁPIDAS =====
    private class AccionesAdapter extends RecyclerView.Adapter<AccionesAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_accion_rapida, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AccionRapida accion = ACCIONES[position];
            holder.ivIcono.setImageResource(accion.iconRes);
            holder.tvTitulo.setText(accion.titulo);
            holder.tvSubtitulo.setText(accion.subtitulo);

            holder.cardView.setOnClickListener(v -> {
                if (accion.destino != null) {
                    startActivity(new Intent(DashboardActivity.this, accion.destino));
                } else {
                    Toast.makeText(DashboardActivity.this, "Próximamente: Detalle de créditos", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return ACCIONES.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView cardView;
            ImageView ivIcono;
            TextView tvTitulo, tvSubtitulo;

            ViewHolder(View view) {
                super(view);
                cardView = view.findViewById(R.id.cardView);
                ivIcono = view.findViewById(R.id.ivIcono);
                tvTitulo = view.findViewById(R.id.tvTitulo);
                tvSubtitulo = view.findViewById(R.id.tvSubtitulo);
            }
        }
    }
}