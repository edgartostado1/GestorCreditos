package com.example.gestorcreditos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProgramasActivity extends AppCompatActivity {

    private ListView listView;
    private FloatingActionButton fabAgregar;
    private List<ProgramaEducativo> listaActual = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_programas);

        listView = findViewById(R.id.listViewProgramas);
        fabAgregar = findViewById(R.id.fabAgregar);

        cargarDatos();

        // AGREGAR: ventana para capturar nombre de la carrera y créditos
        fabAgregar.setOnClickListener(v -> mostrarDialogAgregar());

        // SELECCIONAR: tocar (o dejar presionado) un item abre el diálogo con botón Borrar
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position < listaActual.size()) {
                mostrarDialogEliminar(listaActual.get(position));
            }
        });
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            if (position < listaActual.size()) {
                mostrarDialogEliminar(listaActual.get(position));
            }
            return true;
        });
    }

    // ---------- CREAR ----------
    private void mostrarDialogAgregar() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_agregar_programa, null);
        TextInputEditText etNombre = dialogView.findViewById(R.id.etNombrePrograma);
        TextInputEditText etCreditos = dialogView.findViewById(R.id.etCreditos);

        new AlertDialog.Builder(this)
                .setTitle("Nuevo programa educativo")
                .setView(dialogView)
                .setPositiveButton("Agregar", (dialog, which) -> {
                    String nombre = etNombre.getText().toString().trim();
                    String creditosTxt = etCreditos.getText().toString().trim();

                    if (nombre.isEmpty()) {
                        Toast.makeText(this, "Escribe el nombre de la carrera",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int creditos;
                    try {
                        creditos = Integer.parseInt(creditosTxt);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Créditos no válidos",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ProgramaEducativo nuevo = new ProgramaEducativo(nombre, creditos);
                    Call<ResponseBody> call =
                            RetrofitClient.getInstance().getApi().crearPrograma(nuevo);

                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(ProgramasActivity.this, "Carrera agregada",
                                        Toast.LENGTH_SHORT).show();
                                cargarDatos(); // Recargar la lista
                            } else {
                                Toast.makeText(ProgramasActivity.this,
                                        "Error al guardar (código " + response.code() + ")",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(ProgramasActivity.this,
                                    "Error de red: " + t.getMessage(),
                                    Toast.LENGTH_LONG).show();
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
                    Call<ResponseBody> call =
                            RetrofitClient.getInstance().getApi().eliminarPrograma(programa.getId());

                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(ProgramasActivity.this, "Eliminado",
                                        Toast.LENGTH_SHORT).show();
                                cargarDatos(); // Recargar tras eliminar
                            } else {
                                Toast.makeText(ProgramasActivity.this,
                                        "Error al borrar (código " + response.code() + ")",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(ProgramasActivity.this,
                                    "Error de red: " + t.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ---------- LEER ----------
    private void cargarDatos() {
        Call<List<ProgramaEducativo>> call =
                RetrofitClient.getInstance().getApi().getProgramas();

        call.enqueue(new Callback<List<ProgramaEducativo>>() {
            @Override
            public void onResponse(Call<List<ProgramaEducativo>> call,
                                   Response<List<ProgramaEducativo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaActual = response.body();

                    if (listaActual.isEmpty()) {
                        Toast.makeText(ProgramasActivity.this,
                                "No hay programas todavía", Toast.LENGTH_SHORT).show();
                    }

                    // Convertir los objetos a Strings para el ListView simple
                    ArrayList<String> nombres = new ArrayList<>();
                    for (ProgramaEducativo p : listaActual) {
                        nombres.add(p.getNombre() + " (" + p.getTotal_creditos_requeridos() + " créditos)");
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            ProgramasActivity.this,
                            android.R.layout.simple_list_item_1,
                            nombres
                    );
                    listView.setAdapter(adapter);
                } else {
                    Toast.makeText(ProgramasActivity.this,
                            "Error al cargar (código " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ProgramaEducativo>> call, Throwable t) {
                Toast.makeText(ProgramasActivity.this,
                        "Error de red: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
