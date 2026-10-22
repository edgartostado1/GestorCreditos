package com.example.gestorcreditos;

public class Tutor {
    private int id;           // ID local (SQLite) o ID servidor
    private String clave;
    private String nombre;
    private String telefono;
    private String correo;
    private int sincronizado; // 0 = Pendiente, 1 = Sincronizado

    // Constructor para CREAR (POST) - sin id, sin sincronizado
    public Tutor(String clave, String nombre, String telefono, String correo) {
        this.clave = clave;
        this.nombre = nombre;
        this.telefono = telefono;
        this.correo = correo;
        this.sincronizado = 0; // Por defecto pendiente
    }

    // Constructor para BD LOCAL (con id y sincronizado)
    public Tutor(int id, String clave, String nombre, String telefono, String correo, int sincronizado) {
        this.id = id;
        this.clave = clave;
        this.nombre = nombre;
        this.telefono = telefono;
        this.correo = correo;
        this.sincronizado = sincronizado;
    }

    // Getters
    public int getId() { return id; }
    public String getClave() { return clave; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
    public int getSincronizado() { return sincronizado; }

    // Setters (necesarios para BD local y sync)
    public void setId(int id) { this.id = id; }
    public void setSincronizado(int sincronizado) { this.sincronizado = sincronizado; }
}