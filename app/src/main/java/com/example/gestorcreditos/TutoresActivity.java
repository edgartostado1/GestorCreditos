package com.example.gestorcreditos;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TutoresActivity extends AppCompatActivity {

    private EditText etClave, etNombre, etTelefono, etCorreo;
    private Button btnGuardarLocal, btnSincronizar;
    private ListView lvTutores;

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
            }
        }

        dbHelper = new DatabaseHelper(this);

        etClave = findViewById(R.id.etClaveTutor);
        etNombre = findViewById(R.id.etNombreTutor);
        etTelefono = findViewById(R.id.etTelefonoTutor);
        etCorreo = findViewById(R.id.etCorreoTutor);
        btnGuardarLocal = findViewById(R.id.btnGuardarTutor);
        btnSincronizar = findViewById(R.id.btnSincronizarTutores);
        lvTutores = findViewById(R.id.lvTutores);

        // Cargar lista inicial desde SQLite
        cargarTutoresLocales();

        // Guardar Localmente en SQLite
        btnGuardarLocal.setOnClickListener(v -> {
            String clave = etClave.getText().toString().trim();
            String nombre = etNombre.getText().toString().trim();
            String telefono = etTelefono.getText().toString().trim();
            String correo = etCorreo.getText().toString().trim();

            if (clave.isEmpty() || nombre.isEmpty() || telefono.isEmpty() || correo.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validación de correo
            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                etCorreo.setError("Correo electrónico inválido");
                etCorreo.requestFocus();
                return;
            }

            // Validación de teléfono
            if (telefono.length() < 10) {
                etTelefono.setError("El teléfono debe tener al menos 10 dígitos");
                etTelefono.requestFocus();
                return;
            }

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

        // Eliminar con Clic Largo (solo local)
        lvTutores.setOnItemLongClickListener((parent, view, position, id) -> {
            if (position < listaTutores.size()) {
                Tutor t = listaTutores.get(position);
                // Si ya está sincronizado, preguntar si eliminar también del servidor
                if (t.getSincronizado() == 1) {
                    mostrarDialogEliminarConServer(t);
                } else {
                    // Solo local
                    dbHelper.eliminarTutorLocal(t.getId());
                    Toast.makeText(this, "Tutor eliminado de SQLite", Toast.LENGTH_SHORT).show();
                    cargarTutoresLocales();
                }
            }
            return true;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void cargarTutoresLocales() {
        listaTutores = dbHelper.obtenerTodosTutores();
        ArrayList<String> visualizacion = new ArrayList<>();

        for (Tutor t : listaTutores) {
            String estado = (t.getSincronizado() == 1) ? " ✓" : " ⏳";
            visualizacion.add(t.getClave() + " - " + t.getNombre() + " (" + t.getCorreo() + ")" + estado);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, visualizacion);
        lvTutores.setAdapter(adapter);
    }

    private void sincronizarPendientes() {
        List<Tutor> pendientes = dbHelper.obtenerTutoresPendientes();

        if (pendientes.isEmpty()) {
            Toast.makeText(this, "No hay tutores pendientes por sincronizar", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        for (Tutor t : pendientes) {
            // JSON para enviar al servidor (crud_tutores.php espera JSON)
            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("clave", t.getClave());
                jsonBody.put("nombre", t.getNombre());
                jsonBody.put("telefono", t.getTelefono());
                jsonBody.put("correo", t.getCorreo());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // JsonObjectRequest envía JSON (Content-Type: application/json)
            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, URL_SYNC, jsonBody,
                    response -> {
                        try {
                            if (response.getBoolean("success")) {
                                // Marcar como sincronizado en local
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

    private void mostrarDialogEliminar(Tutor tutor) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar tutor")
                .setMessage("¿Borrar \"" + tutor.getClave() + " - " + tutor.getNombre() + "\"?")
                .setPositiveButton("Borrar", (dialog, which) -> {
                    // Si está sincronizado, también eliminar del servidor
                    if (tutor.getSincronizado() == 1) {
                        eliminarTutorServer(tutor.getId());
                    } else {
                        // Solo local
                        dbHelper.eliminarTutorLocal(tutor.getId());
                        Toast.makeText(this, "Tutor eliminado de SQLite", Toast.LENGTH_SHORT).show();
                        cargarTutoresLocales();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogEliminarConServer(Tutor tutor) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar tutor")
                .setMessage("¿Borrar \"" + tutor.getClave() + " - " + tutor.getNombre() + "\"?\n\nTambién se eliminará del servidor.")
                .setPositiveButton("Borrar todo", (dialog, which) -> {
                    // Eliminar del servidor
                    eliminarTutorServer(tutor.getId());
                    // Eliminar local
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
    }
}