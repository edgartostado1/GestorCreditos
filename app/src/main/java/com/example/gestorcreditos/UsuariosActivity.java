package com.example.gestorcreditos;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsuariosActivity extends AppCompatActivity {

    private EditText etMatricula, etPassword, etNombre;
    private Button btnGuardarLocal, btnSincronizar;
    private ListView lvUsuarios;

    private DatabaseHelper dbHelper;
    private List<Usuario> listaUsuarios;

    // URL centralizada en Config.java
    private static final String URL_SYNC = Config.URL_SINCRONIZAR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuarios);

        Toolbar toolbar = findViewById(R.id.toolbarUsuarios);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        dbHelper = new DatabaseHelper(this);

        etMatricula = findViewById(R.id.etMatriculaUser);
        etPassword = findViewById(R.id.etPasswordUser);
        etNombre = findViewById(R.id.etNombreUser);
        btnGuardarLocal = findViewById(R.id.btnGuardarLocal);
        btnSincronizar = findViewById(R.id.btnSincronizar);
        lvUsuarios = findViewById(R.id.lvUsuarios);

        // Cargar lista inicial desde SQLite
        cargarUsuariosLocales();

        // Guardar Localmente en SQLite
        btnGuardarLocal.setOnClickListener(v -> {
            String mat = etMatricula.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            String nom = etNombre.getText().toString().trim();

            if (mat.isEmpty() || pass.isEmpty() || nom.isEmpty()) {
                Toast.makeText(this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

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

        // Eliminar con Clic Largo
        lvUsuarios.setOnItemLongClickListener((parent, view, position, id) -> {
            Usuario u = listaUsuarios.get(position);
            dbHelper.eliminarUsuario(u.getId());
            Toast.makeText(this, "Usuario eliminado de SQLite", Toast.LENGTH_SHORT).show();
            cargarUsuariosLocales();
            return true;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void cargarUsuariosLocales() {
        listaUsuarios = dbHelper.obtenerTodosUsuarios();
        ArrayList<String> visualizacion = new ArrayList<>();

        for (Usuario u : listaUsuarios) {
            String estado = (u.getSincronizado() == 1) ? " ✓" : " ⏳";
            visualizacion.add(u.getMatricula() + " - " + u.getNombre() + estado);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, visualizacion);
        lvUsuarios.setAdapter(adapter);
    }

    private void sincronizarPendientes() {
        List<Usuario> pendientes = dbHelper.obtenerUsuariosPendientes();

        if (pendientes.isEmpty()) {
            Toast.makeText(this, "No hay registros pendientes por sincronizar", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        for (Usuario u : pendientes) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_SYNC,
                    response -> {
                        try {
                            JSONObject json = new JSONObject(response);
                            if (json.getBoolean("success")) {
                                dbHelper.marcarComoSincronizado(u.getId());
                                cargarUsuariosLocales();
                                Toast.makeText(this, "Sincronizado: " + u.getNombre(), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Toast.makeText(this, "Error al conectar con el servidor PHP", Toast.LENGTH_SHORT).show()) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("matricula", u.getMatricula());
                    params.put("password", u.getPassword());
                    params.put("nombre", u.getNombre());
                    return params;
                }
            };

            queue.add(stringRequest);
        }
    }

    private void limpiarCampos() {
        etMatricula.setText("");
        etPassword.setText("");
        etNombre.setText("");
    }
}