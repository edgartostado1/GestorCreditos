package com.example.gestorcreditos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "GestorCreditosLocal.db";
    private static final int DATABASE_VERSION = 2;

    // Tabla de Usuarios
    public static final String TABLE_USUARIOS = "usuarios";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_MATRICULA = "matricula";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_NOMBRE = "nombre";
    // Estado de sincronización: 0 = Pendiente, 1 = Sincronizado
    public static final String COLUMN_SINCRONIZADO = "sincronizado";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_USUARIOS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_MATRICULA + " TEXT, "
                + COLUMN_PASSWORD + " TEXT, "
                + COLUMN_NOMBRE + " TEXT, "
                + COLUMN_SINCRONIZADO + " INTEGER DEFAULT 0)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIOS);
        onCreate(db);
    }

    // --- OPERACIONES CRUD LOCALES ---

    // 1. CREAR / INSERTAR USUARIO LOCALMENTE
    public boolean insertarUsuario(String matricula, String password, String nombre, int sincronizado) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MATRICULA, matricula);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_NOMBRE, nombre);
        values.put(COLUMN_SINCRONIZADO, sincronizado);

        long result = db.insert(TABLE_USUARIOS, null, values);
        return result != -1;
    }

    // 2. LEER / OBTENER TODOS LOS USUARIOS
    public List<Usuario> obtenerTodosUsuarios() {
        List<Usuario> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USUARIOS, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String matricula = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MATRICULA));
                String password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE));
                int sincronizado = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SINCRONIZADO));

                lista.add(new Usuario(id, matricula, password, nombre, sincronizado));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    // 3. OBTENER USUARIOS PENDIENTES DE SINCRONIZAR (sincronizado = 0)
    public List<Usuario> obtenerUsuariosPendientes() {
        List<Usuario> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USUARIOS + " WHERE " + COLUMN_SINCRONIZADO + " = 0", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String matricula = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MATRICULA));
                String password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE));

                lista.add(new Usuario(id, matricula, password, nombre, 0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    // 4. ACTUALIZAR ESTADO DE SINCRONIZACIÓN A 1
    public void marcarComoSincronizado(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SINCRONIZADO, 1);
        db.update(TABLE_USUARIOS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // 5. ELIMINAR USUARIO LOCAL
    public boolean eliminarUsuario(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_USUARIOS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)}) > 0;
    }
}