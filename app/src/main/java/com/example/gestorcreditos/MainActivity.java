package com.example.gestorcreditos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.button.MaterialButton;

// Importaciones de Volley
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextInputLayout tilMatricula, tilPassword;
    private TextInputEditText etUsuario, etPassword;
    private MaterialButton btnLogin;

    // URL centralizada en Config.java
    private static final String URL_LOGIN = Config.URL_LOGIN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarLogin);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        tilMatricula = findViewById(R.id.tilMatricula);
        tilPassword = findViewById(R.id.tilPassword);
        etUsuario = findViewById(R.id.etUsuario);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usuario = etUsuario.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                // Validación visual con TextInputLayout
                boolean valido = true;
                if (usuario.isEmpty()) {
                    tilMatricula.setError("Ingresa tu matrícula");
                    valido = false;
                } else {
                    tilMatricula.setError(null);
                }
                if (password.isEmpty()) {
                    tilPassword.setError("Ingresa tu contraseña");
                    valido = false;
                } else {
                    tilPassword.setError(null);
                }

                if (valido) {
                    validarLoginServidor(usuario, password);
                }
            }
        });
    }

    private void validarLoginServidor(final String matricula, final String password) {
        // Instanciar la cola de peticiones de Volley
        RequestQueue queue = Volley.newRequestQueue(this);

        // Crear una petición tipo POST
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_LOGIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Convertir el texto recibido (PHP) en objeto JSON
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");

                            if (success) {
                                // Obtener el nombre de la BD para enviarlo a la otra pantalla
                                String nombreAlumno = jsonObject.getString("nombre");

                                Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                                intent.putExtra("MATRICULA_ALUMNO", matricula);
                                intent.putExtra("NOMBRE_ALUMNO", nombreAlumno);
                                startActivity(intent);
                                finish();
                            } else {
                                String errorMsg = jsonObject.getString("message");
                                Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Error procesando datos", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Error de red: Verifica tu IP y XAMPP", Toast.LENGTH_LONG).show();
                    }
                }) {
            // Este método envía los datos simulando un formulario HTML
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("matricula", matricula);
                params.put("password", password);
                return params;
            }
        };

        // Agregar la petición a la cola para que se ejecute
        queue.add(stringRequest);
    }
}
