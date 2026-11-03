package com.example.gestorcreditos;

public class ActividadSubtipo {
    private int id;
    private int id_tipo;
    private String nombre_subtipo;
    private int creditos;
    private String nombre_tipo;

    public ActividadSubtipo(int id_tipo, String nombre_subtipo, int creditos) {
        this.id_tipo = id_tipo;
        this.nombre_subtipo = nombre_subtipo;
        this.creditos = creditos;
    }

    public int getId() { return id; }
    public int getId_tipo() { return id_tipo; }
    public String getNombre_subtipo() { return nombre_subtipo; }
    public int getCreditos() { return creditos; }
    public String getNombre_tipo() { return nombre_tipo; }
}
