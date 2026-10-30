package com.example.gestorcreditos;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    // Tiempo de duración del Splash (2000 milisegundos = 2 segundos)
    private static final int SPLASH_DURATION = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        // Usar un Handler para retrasar el cambio de actividad
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Crear el Intent para ir a la pantalla de Login (MainActivity)
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            // Finalizar SplashActivity para que el usuario no pueda regresar a ella con el botón "Atrás"
            finish();
        }, SPLASH_DURATION);
    }
}
