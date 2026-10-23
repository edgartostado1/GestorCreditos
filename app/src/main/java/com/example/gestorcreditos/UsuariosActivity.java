package com.example.gestorcreditos;

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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UsuariosActivity extends AppCompatActivity {

    private TextInputLayout tilMatricula, tilPassword, tilNombre;
    private com.google.android.material.textfield.TextInputEditText etMatricula, etPassword, etNombre;
    private com.google.android.material.button.MaterialButton btnGuardarLocal, btnSincronizar;
    private androidx.recyclerview.widget.RecyclerView rvUsuarios;

    private DatabaseHelper dbHelper;
    private List<Usuario> listaUsuarios;

    // URL centralizada en Config.java
    private static final String URL_SYNC = Config.URL_SINCRONIZAR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuarios);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarUsuarios);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }

        dbHelper = new DatabaseHelper(this);

        tilMatricula = findViewById(R.id.tilMatriculaUser);
        tilPassword = findViewById(R.id.tilPasswordUser);
        tilNombre = findViewById(R.id.tilNombreUser);
        etMatricula = findViewById(R.id.etMatriculaUser);
        etPassword = findViewById(R.id.etPasswordUser);
        etNombre = findViewById(R.id.etNombreUser);
        btnGuardarLocal = findViewById(R.id.btnGuardarLocal);
        btnSincronizar = findViewById(R.id.btnSincronizar);

        rvUsuarios = findViewById(R.id.rvUsuarios);
        rvUsuarios.setAdapter(new UsuariosAdapter());
        rvUsuarios.setHasFixedSize(true);

        // Cargar lista inicial desde SQLite
        cargarUsuariosLocales();

        // Guardar Localmente en SQLite
        btnGuardarLocal.setOnClickListener(v -> {
            String mat = etMatricula.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            String nom = etNombre.getText().toString().trim();

            boolean valido = true;
            if (mat.isEmpty()) {
                tilMatricula.setError("Ingresa la matrícula");
                valido = false;
            } else {
                tilMatricula.setError(null);
            }
            if (pass.isEmpty()) {
                tilPassword.setError("Ingresa la contraseña");
                valido = false;
            } else {
                tilPassword.setError(null);
            }
            if (nom.isEmpty()) {
                tilNombre.setError("Ingresa el nombre");
                valido = false;
            } else {
                tilNombre.setError(null);
            }

            if (!valido) return;

            boolean insertado = dbHelper.insertarUsuario(mat, pass, nom, 0); // 0 = Pendiente
            if (insertado) {
                Toast.makeText(this, "Guardado localmente en SQLite", Toast.LENGTH_SHORT).show();
                limpiarCampos();
                cargarUsuariosLocales();

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

    private void cargarUsuariosLocales() {
        listaUsuarios = dbHelper.obtenerTodosUsuarios();
        rvUsuarios.getAdapter().notifyDataSetChanged();
    }

    private void sincronizarPendientes() {
        List<Usuario> pendientes = dbHelper.obtenerUsuariosPendientes();

        if (pendientes.isEmpty()) {
            Toast.makeText(this, "No hay registros pendientes por sincronizar", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        for (Usuario u : pendientes) {
            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("matricula", u.getMatricula());
                jsonBody.put("password", u.getPassword());
                jsonBody.put("nombre", u.getNombre());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // JsonObjectRequest envía JSON (Content-Type: application/json)
            com.android.volley.toolbox.JsonObjectRequest jsonRequest = new com.android.volley.toolbox.JsonObjectRequest(Request.Method.POST, URL_SYNC, jsonBody,
                    response -> {
                        try {
                            if (response.getBoolean("success")) {
                                dbHelper.marcarComoSincronizado(u.getId());
                                cargarUsuariosLocales();
                                Toast.makeText(this, "Sincronizado: " + u.getNombre(), Toast.LENGTH_SHORT).show();
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
    private class UsuariosAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<UsuariosAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_usuario, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Usuario u = listaUsuarios.get(position);
            holder.tvMatriculaNombre.setText(u.getMatricula() + " - " + u.getNombre());

            if (u.getSincronizado() == 1) {
                holder.tvEstadoSync.setText("✓ Sincronizado");
                holder.tvEstadoSync.setTextColor(getColor(R.color.md_sys_color_primary));
            } else {
                holder.tvEstadoSync.setText("⏳ Pendiente");
                holder.tvEstadoSync.setTextColor(getColor(R.color.md_sys_color_secondary));
            }

            holder.ivEliminar.setOnClickListener(v -> {
                dbHelper.eliminarUsuario(u.getId());
                Toast.makeText(UsuariosActivity.this, "Usuario eliminado de SQLite", Toast.LENGTH_SHORT).show();
                cargarUsuariosLocales();
            });
        }

        @Override
        public int getItemCount() {
            return listaUsuarios.size();
        }

        class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            TextView tvMatriculaNombre, tvEstadoSync;
            ImageView ivEliminar;

            ViewHolder(View view) {
                super(view);
                tvMatriculaNombre = view.findViewById(R.id.tvMatriculaNombre);
                tvEstadoSync = view.findViewById(R.id.tvEstadoSync);
                ivEliminar = view.findViewById(R.id.ivEliminar);
            }
        }
    }

    private void limpiarCampos() {
        etMatricula.setText("");
        etPassword.setText("");
        etNombre.setText("");
        tilMatricula.setError(null);
        tilPassword.setError(null);
        tilNombre.setError(null);
    }
}