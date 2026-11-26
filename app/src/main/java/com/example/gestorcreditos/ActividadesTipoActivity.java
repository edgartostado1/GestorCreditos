package com.example.gestorcreditos;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActividadesTipoActivity extends AppCompatActivity {

    private EditText etNombre, etDesc;
    private Button btnGuardar;
    private ListView listView;
    private List<ActividadTipo> listaTipos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividades_tipo);
        etNombre = findViewById(R.id.etNombreTipo);
        etDesc = findViewById(R.id.etDescTipo);
        btnGuardar = findViewById(R.id.btnGuardarTipo);
        listView = findViewById(R.id.lvTipos);

        cargarTipos();

        btnGuardar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            if (nombre.isEmpty()) {
                Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
                return;
            }
            ActividadTipo nuevo = new ActividadTipo(nombre, desc);
            RetrofitClient.getInstance().getApi().crearTipo(nuevo).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(ActividadesTipoActivity.this, "Tipo registrado", Toast.LENGTH_SHORT).show();
                        etNombre.setText("");
                        etDesc.setText("");
                        cargarTipos();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(ActividadesTipoActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void cargarTipos() {
        RetrofitClient.getInstance().getApi().getTipos().enqueue(new Callback<List<ActividadTipo>>() {
            @Override
            public void onResponse(Call<List<ActividadTipo>> call, Response<List<ActividadTipo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaTipos = response.body();
                    ArrayList<String> nombres = new ArrayList<>();
                    for (ActividadTipo t : listaTipos) {
                        nombres.add(t.getNombre_tipo() + " - " + t.getDescripcion());
                    }
                    listView.setAdapter(new ArrayAdapter<>(ActividadesTipoActivity.this, android.R.layout.simple_list_item_1, nombres));
                }
            }

            @Override
            public void onFailure(Call<List<ActividadTipo>> call, Throwable t) {
            }
        });
    }
}
