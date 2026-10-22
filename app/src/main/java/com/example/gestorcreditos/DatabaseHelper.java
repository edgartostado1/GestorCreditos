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
    private static final int DATABASE_VERSION = 3;  // Subida de versión para nueva tabla

    // Tabla de Usuarios
    public static final String TABLE_USUARIOS = "usuarios";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_MATRICULA = "matricula";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_NOMBRE = "nombre";
    // Estado de sincronización: 0 = Pendiente, 1 = Sincronizado
    public static final String COLUMN_SINCRONIZADO = "sincronizado";

    // Tabla de Tutores
    public static final String TABLE_TUTORES = "tutores";
    public static final String COLUMN_TUTOR_ID = "id";
    public static final String COLUMN_TUTOR_CLAVE = "clave";
    public static final String COLUMN_TUTOR_NOMBRE = "nombre";
    public static final String COLUMN_TUTOR_TELEFONO = "telefono";
    public static final String COLUMN_TUTOR_CORREO = "correo";
    public static final String COLUMN_TUTOR_SINCRONIZADO = "sincronizado";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tabla usuarios
        String CREATE_USUARIOS = "CREATE TABLE " + TABLE_USUARIOS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_MATRICULA + " TEXT, "
                + COLUMN_PASSWORD + " TEXT, "
                + COLUMN_NOMBRE + " TEXT, "
                + COLUMN_SINCRONIZADO + " INTEGER DEFAULT 0)";
        db.execSQL(CREATE_USUARIOS);

        // Tabla tutores
        String CREATE_TUTORES = "CREATE TABLE " + TABLE_TUTORES + " ("
                + COLUMN_TUTOR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TUTOR_CLAVE + " TEXT, "
                + COLUMN_TUTOR_NOMBRE + " TEXT, "
                + COLUMN_TUTOR_TELEFONO + " TEXT, "
                + COLUMN_TUTOR_CORREO + " TEXT, "
                + COLUMN_TUTOR_SINCRONIZADO + " INTEGER DEFAULT 0)";
        db.execSQL(CREATE_TUTORES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TUTORES);
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

    // ===== TUTORES (OFFLINE-FIRST) =====

    // 1. INSERTAR TUTOR LOCALMENTE
    public boolean insertarTutor(String clave, String nombre, String telefono, String correo, int sincronizado) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TUTOR_CLAVE, clave);
        values.put(COLUMN_TUTOR_NOMBRE, nombre);
        values.put(COLUMN_TUTOR_TELEFONO, telefono);
        values.put(COLUMN_TUTOR_CORREO, correo);
        values.put(COLUMN_TUTOR_SINCRONIZADO, sincronizado);

        long result = db.insert(TABLE_TUTORES, null, values);
        return result != -1;
    }

    // 2. OBTENER TODOS LOS TUTORES LOCALES
    public List<Tutor> obtenerTodosTutores() {
        List<Tutor> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TUTORES, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TUTOR_ID));
                String clave = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TUTOR_CLAVE));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TUTOR_NOMBRE));
                String telefono = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TUTOR_TELEFONO));
                String correo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TUTOR_CORREO));
                int sincronizado = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TUTOR_SINCRONIZADO));

                Tutor tutor = new Tutor(id, clave, nombre, telefono, correo, sincronizado);
                lista.add(tutor);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    // 3. OBTENER TUTORES PENDIENTES DE SINCRONIZAR
    public List<Tutor> obtenerTutoresPendientes() {
        List<Tutor> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TUTORES + " WHERE " + COLUMN_TUTOR_SINCRONIZADO + " = 0", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TUTOR_ID));
                String clave = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TUTOR_CLAVE));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TUTOR_NOMBRE));
                String telefono = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TUTOR_TELEFONO));
                String correo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TUTOR_CORREO));

                Tutor tutor = new Tutor(id, clave, nombre, telefono, correo, 0);
                lista.add(tutor);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    // 4. MARCAR TUTOR COMO SINCRONIZADO
    public void marcarTutorComoSincronizado(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TUTOR_SINCRONIZADO, 1);
        db.update(TABLE_TUTORES, values, COLUMN_TUTOR_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // 5. ELIMINAR TUTOR LOCAL
    public boolean eliminarTutorLocal(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_TUTORES, COLUMN_TUTOR_ID + " = ?", new String[]{String.valueOf(id)}) > 0;
    }

    // 6. OBTENER ID LOCAL DE TUTOR POR CLAVE (para marcar sincronizado tras sync)
    public int obtenerIdTutorPorClave(String clave) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_TUTOR_ID + " FROM " + TABLE_TUTORES + " WHERE " + COLUMN_TUTOR_CLAVE + " = ?", new String[]{clave});
        int id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TUTOR_ID));
        }
        cursor.close();
        return id;
    }
}