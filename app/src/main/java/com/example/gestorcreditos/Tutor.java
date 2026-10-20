package com.example.gestorcreditos;

public class Tutor {
    private int id;
    private String clave;
    private String nombre;
    private String telefono;
    private String correo;

    // Constructor para CREAR (POST) - sin id
    public Tutor(String clave, String nombre, String telefono, String correo) {
        this.clave = clave;
        this.nombre = nombre;
        this.telefono = telefono;
        this.correo = correo;
    }

    // Getters para LEER (GET)
    public int getId() { return id; }
    public String getClave() { return clave; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
}