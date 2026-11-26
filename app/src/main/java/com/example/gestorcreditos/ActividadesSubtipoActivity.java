package com.example.gestorcreditos;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActividadesSubtipoActivity extends AppCompatActivity {

    private Spinner spinnerTipos;
    private EditText etNombreSubtipo, etCreditos;
    private Button btnGuardar;
    private ListView lvSubtipos;
    private List<ActividadTipo> listaTipos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividades_subtipo);
        spinnerTipos = findViewById(R.id.spinnerTipos);
        etNombreSubtipo = findViewById(R.id.etNombreSubtipo);
        etCreditos = findViewById(R.id.etCreditos);
        btnGuardar = findViewById(R.id.btnGuardarSubtipo);
        lvSubtipos = findViewById(R.id.lvSubtipos);

        cargarSpinnerTipos();
        cargarSubtipos();

        btnGuardar.setOnClickListener(v -> {
            ActividadTipo tipoSeleccionado = (ActividadTipo) spinnerTipos.getSelectedItem();
            if (tipoSeleccionado == null) {
                Toast.makeText(this, "Debe registrar y seleccionar un Tipo primero", Toast.LENGTH_SHORT).show();
                return;
            }
            int idTipo = tipoSeleccionado.getId();
            String nombreSub = etNombreSubtipo.getText().toString().trim();
            String credStr = etCreditos.getText().toString().trim();
            if (nombreSub.isEmpty() || credStr.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }
            int creditos = Integer.parseInt(credStr);
            ActividadSubtipo nuevoSub = new ActividadSubtipo(idTipo, nombreSub, creditos);
            RetrofitClient.getInstance().getApi().crearSubtipo(nuevoSub).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(ActividadesSubtipoActivity.this, "Subtipo registrado con éxito", Toast.LENGTH_SHORT).show();
                        etNombreSubtipo.setText("");
                        etCreditos.setText("");
                        cargarSubtipos();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(ActividadesSubtipoActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void cargarSpinnerTipos() {
        RetrofitClient.getInstance().getApi().getTipos().enqueue(new Callback<List<ActividadTipo>>() {
            @Override
            public void onResponse(Call<List<ActividadTipo>> call, Response<List<ActividadTipo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaTipos = response.body();
                    ArrayAdapter<ActividadTipo> adapter = new ArrayAdapter<>(
                        ActividadesSubtipoActivity.this,
                        android.R.layout.simple_spinner_dropdown_item,
                        listaTipos
                    );
                    spinnerTipos.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<ActividadTipo>> call, Throwable t) {
                Toast.makeText(ActividadesSubtipoActivity.this, "Error al cargar tipos en el Spinner", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarSubtipos() {
        RetrofitClient.getInstance().getApi().getSubtipos().enqueue(new Callback<List<ActividadSubtipo>>() {
            @Override
            public void onResponse(Call<List<ActividadSubtipo>> call, Response<List<ActividadSubtipo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ActividadSubtipo> subtipos = response.body();
                    ArrayList<String> nombres = new ArrayList<>();
                    for (ActividadSubtipo s : subtipos) {
                        nombres.add(s.getNombre_subtipo() + " (" + s.getCreditos() + " créditos)");
                    }
                    lvSubtipos.setAdapter(new ArrayAdapter<>(ActividadesSubtipoActivity.this, android.R.layout.simple_list_item_1, nombres));
                }
            }

            @Override
            public void onFailure(Call<List<ActividadSubtipo>> call, Throwable t) {
            }
        });
    }
}
