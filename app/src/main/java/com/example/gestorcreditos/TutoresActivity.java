package com.example.gestorcreditos;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TutoresActivity extends AppCompatActivity {

    private EditText etClave, etNombre, etTelefono, etCorreo;
    private Button btnGuardar;
    private ListView lvTutores;
    private List<Tutor> listaTutores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutores);

        // Toolbar con botón "Up" (atrás)
        Toolbar toolbar = findViewById(R.id.toolbarTutores);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        // Referencias a vistas
        etClave = findViewById(R.id.etClaveTutor);
        etNombre = findViewById(R.id.etNombreTutor);
        etTelefono = findViewById(R.id.etTelefonoTutor);
        etCorreo = findViewById(R.id.etCorreoTutor);
        btnGuardar = findViewById(R.id.btnGuardarTutor);
        lvTutores = findViewById(R.id.lvTutores);

        // Cargar lista inicial
        cargarTutores();

        // Botón guardar
        btnGuardar.setOnClickListener(v -> validarYEnviar());

        // Eliminar con clic largo en item de la lista
        lvTutores.setOnItemLongClickListener((parent, view, position, id) -> {
            if (position < listaTutores.size()) {
                Tutor t = listaTutores.get(position);
                eliminarTutor(t.getId());
            }
            return true;
        });
    }

    // Botón "Up" en toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // ===== VALIDACIONES Y ENVÍO =====
    private void validarYEnviar() {
        String clave = etClave.getText().toString().trim();
        String nombre = etNombre.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();

        // 1. Validación de campos vacíos
        if (clave.isEmpty() || nombre.isEmpty() || telefono.isEmpty() || correo.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Validación de formato de correo electrónico
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreo.setError("Correo electrónico inválido");
            etCorreo.requestFocus();
            return;
        }

        // 3. Validación de longitud de teléfono (mínimo 10 dígitos)
        if (telefono.length() < 10) {
            etTelefono.setError("El teléfono debe tener al menos 10 dígitos");
            etTelefono.requestFocus();
            return;
        }

        // Si pasa validaciones, enviar con Retrofit
        Tutor nuevoTutor = new Tutor(clave, nombre, telefono, correo);
        Call<ResponseBody> call = RetrofitClient.getInstance().getApi().crearTutor(nuevoTutor);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(TutoresActivity.this, "Tutor guardado exitosamente", Toast.LENGTH_SHORT).show();
                    limpiarFormulario();
                    cargarTutores();
                } else {
                    Toast.makeText(TutoresActivity.this, "Error del servidor: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(TutoresActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ===== CARGAR LISTA (GET) =====
    private void cargarTutores() {
        Call<List<Tutor>> call = RetrofitClient.getInstance().getApi().getTutores();

        call.enqueue(new Callback<List<Tutor>>() {
            @Override
            public void onResponse(Call<List<Tutor>> call, Response<List<Tutor>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaTutores = response.body();

                    ArrayList<String> items = new ArrayList<>();
                    for (Tutor t : listaTutores) {
                        items.add(t.getClave() + " - " + t.getNombre() + " (" + t.getCorreo() + ")");
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            TutoresActivity.this,
                            android.R.layout.simple_list_item_1,
                            items
                    );
                    lvTutores.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Tutor>> call, Throwable t) {
                Toast.makeText(TutoresActivity.this, "Error al cargar tutores: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ===== ELIMINAR (DELETE) =====
    private void eliminarTutor(int id) {
        Call<ResponseBody> call = RetrofitClient.getInstance().getApi().eliminarTutor(id);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(TutoresActivity.this, "Tutor eliminado", Toast.LENGTH_SHORT).show();
                    cargarTutores();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(TutoresActivity.this, "Error al eliminar: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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