package com.example.gestorcreditos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistroActividadesActivity extends AppCompatActivity {

    private Spinner spinnerTipo, spinnerSubtipo;
    private EditText etFecha, etHoras, etDescripcion;
    private Button btnRegistrar;

    private List<ActividadTipo> listaTiposGlobal;
    private List<ActividadSubtipo> listaSubtiposGlobal;
    private List<ActividadSubtipo> listaSubtiposFiltrados;

    // La matrícula se recibe desde DashboardActivity
    private String matriculaAlumno;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_actividades);

        spinnerTipo = findViewById(R.id.spinnerTipo);
        spinnerSubtipo = findViewById(R.id.spinnerSubtipo);
        etFecha = findViewById(R.id.etFecha);
        etHoras = findViewById(R.id.etHoras);
        etDescripcion = findViewById(R.id.etDescripcion);
        btnRegistrar = findViewById(R.id.btnRegistrar);

        listaSubtiposFiltrados = new ArrayList<>();

        // Recuperar matrícula del intent (enviada por DashboardActivity)
        matriculaAlumno = getIntent().getStringExtra("MATRICULA_ALUMNO");
        if (matriculaAlumno == null) matriculaAlumno = "12345";

        // Formatear fecha automáticamente como DD/MM/AA
        etFecha.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String digits = s.toString().replaceAll("[^0-9]", "");
                if (digits.length() > 6) digits = digits.substring(0, 6);

                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < digits.length(); i++) {
                    if (i == 2 || i == 4) {
                        formatted.append('/');
                    }
                    formatted.append(digits.charAt(i));
                }

                s.replace(0, s.length(), formatted.toString());
                isFormatting = false;
            }
        });

        cargarTiposYSubtipos();

        // 1. Lógica en Cascada: al seleccionar un Tipo, filtra los Subtipos
        spinnerTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (listaTiposGlobal != null && !listaTiposGlobal.isEmpty()) {
                    int idTipoSeleccionado = listaTiposGlobal.get(position).getId();
                    filtrarSubtipos(idTipoSeleccionado);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // 2. Evento del botón Registrar
        btnRegistrar.setOnClickListener(v -> validarFormularioYRegistrar());
    }

    private void cargarTiposYSubtipos() {
        // Cargar Tipos
        RetrofitClient.getInstance().getApi().getTipos().enqueue(new Callback<List<ActividadTipo>>() {
            @Override
            public void onResponse(Call<List<ActividadTipo>> call, Response<List<ActividadTipo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaTiposGlobal = response.body();
                    ArrayAdapter<ActividadTipo> adapter = new ArrayAdapter<>(
                        RegistroActividadesActivity.this,
                        android.R.layout.simple_spinner_dropdown_item,
                        listaTiposGlobal
                    );
                    spinnerTipo.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<ActividadTipo>> call, Throwable t) {
            }
        });

        // Cargar Subtipos globalmente (para luego filtrarlos localmente)
        RetrofitClient.getInstance().getApi().getSubtipos().enqueue(new Callback<List<ActividadSubtipo>>() {
            @Override
            public void onResponse(Call<List<ActividadSubtipo>> call, Response<List<ActividadSubtipo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaSubtiposGlobal = response.body();
                    // Si ya hay un tipo seleccionado, refiltrar para mostrar subtipos
                    if (listaTiposGlobal != null && !listaTiposGlobal.isEmpty()) {
                        int pos = spinnerTipo.getSelectedItemPosition();
                        if (pos >= 0 && pos < listaTiposGlobal.size()) {
                            filtrarSubtipos(listaTiposGlobal.get(pos).getId());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ActividadSubtipo>> call, Throwable t) {
            }
        });
    }

    private void filtrarSubtipos(int idTipoPadre) {
        if (listaSubtiposGlobal == null) return;
        listaSubtiposFiltrados.clear();
        ArrayList<String> nombresMostrar = new ArrayList<>();
        for (ActividadSubtipo sub : listaSubtiposGlobal) {
            if (sub.getId_tipo() == idTipoPadre) {
                listaSubtiposFiltrados.add(sub);
                nombresMostrar.add(sub.getNombre_subtipo() + " (" + sub.getCreditos() + " hrs)");
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, nombresMostrar);
        spinnerSubtipo.setAdapter(adapter);
    }

    private void validarFormularioYRegistrar() {
        if (spinnerSubtipo.getSelectedItem() == null || listaSubtiposFiltrados.isEmpty()) {
            Toast.makeText(this, "Debe seleccionar un subtipo de actividad válido", Toast.LENGTH_SHORT).show();
            return;
        }
        String fecha = etFecha.getText().toString().trim();
        String horasStr = etHoras.getText().toString().trim();
        String desc = etDescripcion.getText().toString().trim();

        if (fecha.length() != 8 || fecha.isEmpty() || horasStr.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Debe llenar todos los campos (fecha: DD/MM/AA)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Recuperar el subtipo exacto seleccionado
        int posicionSeleccionada = spinnerSubtipo.getSelectedItemPosition();
        ActividadSubtipo subtipoElegido = listaSubtiposFiltrados.get(posicionSeleccionada);

        int horasIngresadas = Integer.parseInt(horasStr);
        int horasBaseSubtipo = subtipoElegido.getCreditos();

        // VALIDACIÓN DE REGLA DE NEGOCIO
        if (horasIngresadas != horasBaseSubtipo) {
            etHoras.setError("Error: Debe capturar exactamente " + horasBaseSubtipo + " horas.");
            etHoras.requestFocus();
            Toast.makeText(this, "Las horas ingresadas no coinciden con las horas oficiales de la actividad.", Toast.LENGTH_LONG).show();
            return;
        }

        // Enviar a la Base de Datos con Retrofit
        String fechaMySQL = convertirFechaAMySQL(fecha);
        MovimientoActividad mov = new MovimientoActividad(matriculaAlumno, subtipoElegido.getId(), fechaMySQL, horasIngresadas, desc);
        RetrofitClient.getInstance().getApi().registrarMovimiento(mov).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegistroActividadesActivity.this, "Actividad registrada con éxito", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(RegistroActividadesActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Convierte DD/MM/AA → YYYY-MM-DD (formato MySQL)
    private String convertirFechaAMySQL(String fecha) {
        String[] parts = fecha.split("/");
        if (parts.length != 3) return fecha;
        String dd = parts[0];
        String mm = parts[1];
        String yy = parts[2];
        int year = Integer.parseInt(yy);
        if (year < 100) year += 2000;
        return String.format("%04d-%02d-%02d", year, Integer.parseInt(mm), Integer.parseInt(dd));
    }
}
