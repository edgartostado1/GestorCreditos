package com.example.gestorcreditos;

public class MovimientoActividad {
    private String matricula;
    private int id_subtipo;
    private String fecha;
    private int horas;
    private String descripcion;
    private String foto;      // Base64 o URL
    private String ubicacion; // Lat, Lng

    public MovimientoActividad(String matricula, int id_subtipo, String fecha, int horas, String descripcion, String foto, String ubicacion) {
        this.matricula = matricula;
        this.id_subtipo = id_subtipo;
        this.fecha = fecha;
        this.horas = horas;
        this.descripcion = descripcion;
        this.foto = foto;
        this.ubicacion = ubicacion;
    }

    public String getMatricula() { return matricula; }
    public int getId_subtipo() { return id_subtipo; }
    public String getFecha() { return fecha; }
    public int getHoras() { return horas; }
    public String getDescripcion() { return descripcion; }
    public String getFoto() { return foto; }
    public String getUbicacion() { return ubicacion; }
}
