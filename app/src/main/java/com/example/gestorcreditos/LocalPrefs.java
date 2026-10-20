package com.example.gestorcreditos;

import android.content.Context;
import android.content.SharedPreferences;

// Guardado LOCAL del usuario (SharedPreferences) para sincronizar despues
public class LocalPrefs {

    private static final String ARCHIVO = "gestor_creditos_local";

    public static void guardarUsuario(Context ctx, String matricula, String password, String nombre) {
        ctx.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
                .edit()
                .putString("matricula", matricula)
                .putString("password", password)
                .putString("nombre", nombre)
                .apply();
    }

    public static String getMatricula(Context ctx) {
        return ctx.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).getString("matricula", null);
    }

    public static String getPassword(Context ctx) {
        return ctx.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).getString("password", null);
    }

    public static String getNombre(Context ctx) {
        return ctx.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).getString("nombre", null);
    }

    public static boolean hayUsuarioLocal(Context ctx) {
        return getMatricula(ctx) != null;
    }
}
