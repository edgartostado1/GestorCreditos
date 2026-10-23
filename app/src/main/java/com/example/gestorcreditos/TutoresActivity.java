package com.example.gestorcreditos;

import android.os.Bundle;
import android.util.Patterns;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TutoresActivity extends AppCompatActivity {

    private TextInputLayout tilClave, tilNombre, tilTelefono, tilCorreo;
    private TextInputEditText etClave, etNombre, etTelefono, etCorreo;
    private MaterialButton btnGuardarLocal, btnSincronizar;
    private RecyclerView rvTutores;
    private MaterialCardView cardFormulario;

    private DatabaseHelper dbHelper;
    private List<Tutor> listaTutores;

    // URL centralizada en Config.java
    private static final String URL_SYNC = Config.URL_BASE + "crud_tutores.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutores);

        Toolbar toolbar = findViewById(R.id.toolbarTutores);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }

        dbHelper = new DatabaseHelper(this);

        // Formulario
        tilClave = findViewById(R.id.tilClaveTutor);
        tilNombre = findViewById(R.id.tilNombreTutor);
        tilTelefono = findViewById(R.id.tilTelefonoTutor);
        tilCorreo = findViewById(R.id.tilCorreoTutor);
        etClave = findViewById(R.id.etClaveTutor);
        etNombre = findViewById(R.id.etNombreTutor);
        etTelefono = findViewById(R.id.etTelefonoTutor);
        etCorreo = findViewById(R.id.etCorreoTutor);

        btnGuardarLocal = findViewById(R.id.btnGuardarTutor);
        btnSincronizar = findViewById(R.id.btnSincronizarTutores);
        cardFormulario = findViewById(R.id.cardFormulario);

        rvTutores = findViewById(R.id.rvTutores);
        rvTutores.setAdapter(new TutoresAdapter());
        rvTutores.setHasFixedSize(true);

        // Cargar lista inicial desde SQLite
        cargarTutoresLocales();

        // Guardar Localmente en SQLite
        btnGuardarLocal.setOnClickListener(v -> {
            String clave = etClave.getText().toString().trim();
            String nombre = etNombre.getText().toString().trim();
            String telefono = etTelefono.getText().toString().trim();
            String correo = etCorreo.getText().toString().trim();

            boolean valido = true;
            if (clave.isEmpty()) {
                tilClave.setError("Ingresa la clave");
                valido = false;
            } else {
                tilClave.setError(null);
            }
            if (nombre.isEmpty()) {
                tilNombre.setError("Ingresa el nombre");
                valido = false;
            } else {
                tilNombre.setError(null);
            }
            if (telefono.isEmpty()) {
                tilTelefono.setError("Ingresa el teléfono");
                valido = false;
            } else {
                tilTelefono.setError(null);
            }
            if (correo.isEmpty()) {
                tilCorreo.setError("Ingresa el correo");
                valido = false;
            } else {
                tilCorreo.setError(null);
            }

            // Validación de correo
            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                tilCorreo.setError("Correo electrónico inválido");
                etCorreo.requestFocus();
                valido = false;
            }

            // Validación de teléfono
            if (telefono.length() < 10) {
                tilTelefono.setError("El teléfono debe tener al menos 10 dígitos");
                etTelefono.requestFocus();
                valido = false;
            }

            if (!valido) return;

            // Insertar en SQLite local (sincronizado = 0 = Pendiente)
            boolean insertado = dbHelper.insertarTutor(clave, nombre, telefono, correo, 0);
            if (insertado) {
                Toast.makeText(this, "Guardado localmente en SQLite", Toast.LENGTH_SHORT).show();
                limpiarFormulario();
                cargarTutoresLocales();

                // Intento automático de sincronización si hay red
                if (NetworkUtils.hayConexionInternet(this)) {
                    sincronizarPendientes();
                }
            } else {
                Toast.makeText(this, "Error al guardar localmente", Toast.LENGTH_SHORT).show();
            }
        });

        // Botón Manual de Sincronización
        btnSincronizar.setOnClickListener(v -> {
            if (NetworkUtils.hayConexionInternet(this)) {
                sincronizarPendientes();
            } else {
                Toast.makeText(this, "Sin conexión a Internet. Se sincronizará más tarde.", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void cargarTutoresLocales() {
        listaTutores = dbHelper.obtenerTodosTutores();
        rvTutores.getAdapter().notifyDataSetChanged();
    }

    private void sincronizarPendientes() {
        List<Tutor> pendientes = dbHelper.obtenerTutoresPendientes();

        if (pendientes.isEmpty()) {
            Toast.makeText(this, "No hay tutores pendientes por sincronizar", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        for (Tutor t : pendientes) {
            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("clave", t.getClave());
                jsonBody.put("nombre", t.getNombre());
                jsonBody.put("telefono", t.getTelefono());
                jsonBody.put("correo", t.getCorreo());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, URL_SYNC, jsonBody,
                    response -> {
                        try {
                            if (response.getBoolean("success")) {
                                int localId = dbHelper.obtenerIdTutorPorClave(t.getClave());
                                if (localId != -1) {
                                    dbHelper.marcarTutorComoSincronizado(localId);
                                }
                                cargarTutoresLocales();
                                Toast.makeText(this, "Sincronizado: " + t.getNombre(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Error servidor: " + response.optString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        String msg = "Error de red";
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            try {
                                String err = new String(error.networkResponse.data);
                                JSONObject je = new JSONObject(err);
                                msg = je.optString("message", err);
                            } catch (Exception ignored) {}
                        }
                        Toast.makeText(this, "Error: " + msg, Toast.LENGTH_SHORT).show();
                    });

            queue.add(jsonRequest);
        }
    }

    // ===== ADAPTER PARA RECYCLERVIEW =====
    private class TutoresAdapter extends RecyclerView.Adapter<TutoresAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_tutor, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Tutor t = listaTutores.get(position);
            holder.tvClaveNombre.setText(t.getClave() + " - " + t.getNombre());
            holder.tvTelefonoCorreo.setText(t.getTelefono() + " • " + t.getCorreo());

            // Estado de sincronización
            if (t.getSincronizado() == 1) {
                holder.tvEstadoSync.setText("✓ Sincronizado");
                holder.tvEstadoSync.setTextColor(getColor(R.color.md_sys_color_primary));
            } else {
                holder.tvEstadoSync.setText("⏳ Pendiente");
                holder.tvEstadoSync.setTextColor(getColor(R.color.md_sys_color_secondary));
            }

            holder.itemView.setOnClickListener(v -> {
                // Click en la tarjeta = mostrar opciones
                if (t.getSincronizado() == 1) {
                    mostrarDialogEliminarConServer(t);
                } else {
                    mostrarDialogEliminar(t);
                }
            });

            holder.ivEliminar.setOnClickListener(v -> {
                if (t.getSincronizado() == 1) {
                    mostrarDialogEliminarConServer(t);
                } else {
                    mostrarDialogEliminar(t);
                }
            });
        }

        @Override
        public int getItemCount() {
            return listaTutores.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvClaveNombre, tvTelefonoCorreo, tvEstadoSync;
            ImageView ivEliminar;

            ViewHolder(View view) {
                super(view);
                tvClaveNombre = view.findViewById(R.id.tvClaveNombre);
                tvTelefonoCorreo = view.findViewById(R.id.tvTelefonoCorreo);
                tvEstadoSync = view.findViewById(R.id.tvEstadoSync);
                ivEliminar = view.findViewById(R.id.ivEliminar);
            }
        }
    }

    private void mostrarDialogEliminar(Tutor tutor) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar tutor")
                .setMessage("¿Borrar \"" + tutor.getClave() + " - " + tutor.getNombre() + "\"?")
                .setPositiveButton("Borrar", (dialog, which) -> {
                    dbHelper.eliminarTutorLocal(tutor.getId());
                    Toast.makeText(this, "Tutor eliminado de SQLite", Toast.LENGTH_SHORT).show();
                    cargarTutoresLocales();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogEliminarConServer(Tutor tutor) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar tutor")
                .setMessage("¿Borrar \"" + tutor.getClave() + " - " + tutor.getNombre() + "\"?\n\nTambién se eliminará del servidor.")
                .setPositiveButton("Borrar todo", (dialog, which) -> {
                    eliminarTutorServer(tutor.getId());
                    dbHelper.eliminarTutorLocal(tutor.getId());
                    Toast.makeText(this, "Tutor eliminado (local + servidor)", Toast.LENGTH_SHORT).show();
                    cargarTutoresLocales();
                })
                .setNeutralButton("Solo local", (dialog, which) -> {
                    dbHelper.eliminarTutorLocal(tutor.getId());
                    Toast.makeText(this, "Eliminado solo de SQLite", Toast.LENGTH_SHORT).show();
                    cargarTutoresLocales();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarTutorServer(int id) {
        Call<ResponseBody> call = RetrofitClient.getInstance().getApi().eliminarTutor(id);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(TutoresActivity.this, "Error al eliminar del servidor: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(TutoresActivity.this, "Error de red al eliminar: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void limpiarFormulario() {
        etClave.setText("");
        etNombre.setText("");
        etTelefono.setText("");
        etCorreo.setText("");
        tilClave.setError(null);
        tilNombre.setError(null);
        tilTelefono.setError(null);
        tilCorreo.setError(null);
    }
}