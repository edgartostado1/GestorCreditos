package com.example.gestorcreditos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProgramasActivity extends AppCompatActivity {

    private RecyclerView rvProgramas;
    private FloatingActionButton fabAgregar;
    private List<ProgramaEducativo> listaActual = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_programas);

        // Toolbar con flecha de regreso
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }

        rvProgramas = findViewById(R.id.rvProgramas);
        fabAgregar = findViewById(R.id.fabAgregar);

        rvProgramas.setAdapter(new ProgramasAdapter());
        rvProgramas.setHasFixedSize(true);

        cargarDatos();

        fabAgregar.setOnClickListener(v -> mostrarDialogAgregar());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // ---------- CREAR ----------
    private void mostrarDialogAgregar() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_agregar_programa, null);

        TextInputLayout tilNombre = dialogView.findViewById(R.id.tilNombrePrograma);
        TextInputLayout tilCreditos = dialogView.findViewById(R.id.tilCreditos);
        TextInputEditText etNombre = dialogView.findViewById(R.id.etNombrePrograma);
        TextInputEditText etCreditos = dialogView.findViewById(R.id.etCreditos);

        new AlertDialog.Builder(this)
                .setTitle("Nuevo programa educativo")
                .setView(dialogView)
                .setPositiveButton("Agregar", (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();
                    String creditosTxt = etCreditos.getText().toString().trim();

                    boolean valido = true;
                    if (nombre.isEmpty()) {
                        tilNombre.setError("Escribe el nombre de la carrera");
                        valido = false;
                    } else {
                        tilNombre.setError(null);
                    }

                    int creditos;
                    try {
                        creditos = Integer.parseInt(etCreditos.getText().toString().trim());
                    } catch (NumberFormatException e) {
                        tilCreditos.setError("Créditos no válidos");
                        valido = false;
                    }

                    if (!valido) return;

                    ProgramaEducativo nuevo = new ProgramaEducativo(nombre, creditos);
                    Call<ResponseBody> call = RetrofitClient.getInstance().getApi().crearPrograma(nuevo);

                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(ProgramasActivity.this, "Carrera agregada", Toast.LENGTH_SHORT).show();
                                cargarDatos();
                            } else {
                                Toast.makeText(ProgramasActivity.this,
                                        "Error al guardar (código " + response.code() + ")", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(ProgramasActivity.this,
                                    "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ---------- ELIMINAR ----------
    private void mostrarDialogEliminar(ProgramaEducativo programa) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar programa")
                .setMessage("¿Borrar \"" + programa.getNombre() + "\"?")
                .setPositiveButton("Borrar", (dialog, which) -> {
                    Call<ResponseBody> call = RetrofitClient.getInstance().getApi().eliminarPrograma(programa.getId());

                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(ProgramasActivity.this, "Eliminado", Toast.LENGTH_SHORT).show();
                                cargarDatos();
                            } else {
                                Toast.makeText(ProgramasActivity.this,
                                        "Error al borrar (código " + response.code() + ")", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(ProgramasActivity.this,
                                    "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ---------- LEER ----------
    private void cargarDatos() {
        Call<List<ProgramaEducativo>> call = RetrofitClient.getInstance().getApi().getProgramas();

        call.enqueue(new Callback<List<ProgramaEducativo>>() {
            @Override
            public void onResponse(Call<List<ProgramaEducativo>> call, Response<List<ProgramaEducativo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaActual = response.body();

                    if (listaActual.isEmpty()) {
                        Toast.makeText(ProgramasActivity.this, "No hay programas todavía", Toast.LENGTH_SHORT).show();
                    }

                    rvProgramas.getAdapter().notifyDataSetChanged();
                } else {
                    Toast.makeText(ProgramasActivity.this, "Error al cargar (código " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ProgramaEducativo>> call, Throwable t) {
                Toast.makeText(ProgramasActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ===== ADAPTER PARA RECYCLERVIEW =====
    private class ProgramasAdapter extends RecyclerView.Adapter<ProgramasAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_programa, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ProgramaEducativo p = listaActual.get(position);
            holder.tvNombre.setText(p.getNombre());
            holder.tvCreditos.setText(p.getTotal_creditos_requeridos() + " créditos");

            holder.itemView.setOnClickListener(v -> {
                // Click en la tarjeta = editar (opcional, por ahora solo eliminar)
                mostrarDialogEliminar(p);
            });

            holder.ivEliminar.setOnClickListener(v -> mostrarDialogEliminar(p));
        }

        @Override
        public int getItemCount() {
            return listaActual.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNombre, tvCreditos;
            ImageView ivEliminar;

            ViewHolder(View view) {
                super(view);
                tvNombre = view.findViewById(R.id.tvNombre);
                tvCreditos = view.findViewById(R.id.tvCreditos);
                ivEliminar = view.findViewById(R.id.ivEliminar);
            }
        }
    }
}