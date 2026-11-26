package com.example.gestorcreditos;

public class ActividadTipo {
    private int id;
    private String nombre_tipo;
    private String descripcion;

    public ActividadTipo(String nombre_tipo, String descripcion) {
        this.nombre_tipo = nombre_tipo;
        this.descripcion = descripcion;
    }

    public int getId() { return id; }
    public String getNombre_tipo() { return nombre_tipo; }
    public String getDescripcion() { return descripcion; }

    @Override
    public String toString() { return nombre_tipo; }
}
