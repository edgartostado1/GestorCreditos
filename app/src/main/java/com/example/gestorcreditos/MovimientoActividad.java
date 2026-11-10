package com.example.gestorcreditos;

public class MovimientoActividad {
    private String matricula;
    private int id_subtipo;
    private String fecha;
    private int horas;
    private String descripcion;

    public MovimientoActividad(String matricula, int id_subtipo, String fecha, int horas, String descripcion) {
        this.matricula = matricula;
        this.id_subtipo = id_subtipo;
        this.fecha = fecha;
        this.horas = horas;
        this.descripcion = descripcion;
    }

    public String getMatricula() { return matricula; }
    public int getId_subtipo() { return id_subtipo; }
    public String getFecha() { return fecha; }
    public int getHoras() { return horas; }
    public String getDescripcion() { return descripcion; }
}
